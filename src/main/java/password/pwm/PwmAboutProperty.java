/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2017 The PWM Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package password.pwm;

import password.pwm.config.PwmSetting;
import password.pwm.i18n.Display;
import password.pwm.util.LocaleHelper;
import password.pwm.util.db.DatabaseService;
import password.pwm.util.java.FileSystemUtility;
import password.pwm.util.java.StringUtil;
import password.pwm.util.logging.PwmLogger;
import password.pwm.util.secure.PwmRandom;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public enum PwmAboutProperty {

    app_version,
    app_chaiApiVersion,
    app_currentTime,
    app_startTime,
    app_installTime,
    app_currentPublishedVersion,
    app_currentPublishedVersionCheckTime,
    app_siteUrl,
    app_instanceID,
    app_trialMode,
    app_mode_appliance,
    app_mode_docker,
    app_mode_manageHttps,
    app_applicationPath,
    app_environmentFlags,
    app_wordlistSize,
    app_seedlistSize,
    app_sharedHistorySize,
    app_sharedHistoryOldestTime,
    app_emailQueueSize,
    app_emailQueueOldestTime,
    app_smsQueueSize,
    app_smsQueueOldestTime,
    app_syslogQueueSize,
    app_localDbLogSize,
    app_localDbLogOldestTime,
    app_localDbStorageSize,
    app_localDbFreeSpace,
    app_configurationRestartCounter,
    app_secureBlockAlgorithm,
    app_secureHashAlgorithm,
    app_ldapProfileCount,

    build_Time,
    build_Number,
    build_Type,
    build_User,
    build_Revision,
    build_JavaVendor,
    build_JavaVersion,
    build_Version,

    java_memoryFree,
    java_memoryAllocated,
    java_memoryMax,
    java_threadCount,
    java_vmVendor,
    java_vmLocation,
    java_vmVersion,
    java_runtimeVersion,
    java_vmName,
    java_osName,
    java_osVersion,
    java_osArch,
    java_randomAlgorithm,
    java_defaultCharset,
    java_appServerInfo,

    database_driverName,
    database_driverVersion,
    database_databaseProductName,
    database_databaseProductVersion,

    ;

    private static final PwmLogger LOGGER = PwmLogger.forClass(PwmAboutProperty.class);

    public static Map<PwmAboutProperty,String> makeInfoBean(
            final PwmApplication pwmApplication
    ) {
        final Map<PwmAboutProperty,String> aboutMap = new TreeMap<>();

        // about page
        aboutMap.put(app_version,                  PwmConstants.SERVLET_VERSION);
        aboutMap.put(app_currentTime,              dateFormatForInfoBean(new Date()));
        aboutMap.put(app_startTime,                dateFormatForInfoBean(pwmApplication.getStartupTime()));
        aboutMap.put(app_installTime,              dateFormatForInfoBean(pwmApplication.getInstallTime()));
        aboutMap.put(app_siteUrl,                  pwmApplication.getConfig().readSettingAsString(PwmSetting.PWM_SITE_URL));
        aboutMap.put(app_ldapProfileCount,         Integer.toString(pwmApplication.getConfig().getLdapProfiles().size()));
        aboutMap.put(app_instanceID,               pwmApplication.getInstanceID());
        aboutMap.put(app_trialMode,                Boolean.toString(PwmConstants.TRIAL_MODE));
        if (pwmApplication.getPwmEnvironment() != null) {
            aboutMap.put(app_mode_appliance,            Boolean.toString(pwmApplication.getPwmEnvironment().getFlags().contains(PwmEnvironment.ApplicationFlag.Appliance)));
            aboutMap.put(app_mode_docker,               Boolean.toString(pwmApplication.getPwmEnvironment().getFlags().contains(PwmEnvironment.ApplicationFlag.Docker)));
            aboutMap.put(app_mode_manageHttps,          Boolean.toString(pwmApplication.getPwmEnvironment().getFlags().contains(PwmEnvironment.ApplicationFlag.ManageHttps)));
            aboutMap.put(app_applicationPath,           pwmApplication.getPwmEnvironment().getApplicationPath().getAbsolutePath());
            aboutMap.put(app_environmentFlags,          StringUtil.collectionToString(pwmApplication.getPwmEnvironment().getFlags(),","));
        }
        aboutMap.put(app_chaiApiVersion,           PwmConstants.CHAI_API_VERSION);

        if (pwmApplication.getConfig().readSettingAsBoolean(PwmSetting.VERSION_CHECK_ENABLE)) {
            if (pwmApplication.getVersionChecker() != null) {
                aboutMap.put(app_currentPublishedVersion, pwmApplication.getVersionChecker().currentVersion());
                aboutMap.put(app_currentPublishedVersionCheckTime, dateFormatForInfoBean(pwmApplication.getVersionChecker().lastReadTimestamp()));
            }
        }

        aboutMap.put(app_secureBlockAlgorithm,     pwmApplication.getSecureService().getDefaultBlockAlgorithm().getLabel());
        aboutMap.put(app_secureHashAlgorithm,      pwmApplication.getSecureService().getDefaultHashAlgorithm().toString());

        aboutMap.put(app_wordlistSize,             Integer.toString(pwmApplication.getWordlistManager().size()));
        aboutMap.put(app_seedlistSize,             Integer.toString(pwmApplication.getSeedlistManager().size()));
        if (pwmApplication.getSharedHistoryManager() != null) {
            aboutMap.put(app_sharedHistorySize,    Integer.toString(pwmApplication.getSharedHistoryManager().size()));
            aboutMap.put(app_sharedHistoryOldestTime, dateFormatForInfoBean(pwmApplication.getSharedHistoryManager().getOldestEntryTime()));
        }


        if (pwmApplication.getEmailQueue() != null) {
            aboutMap.put(app_emailQueueSize,       Integer.toString(pwmApplication.getEmailQueue().queueSize()));
            if (pwmApplication.getEmailQueue().eldestItem() != null) {
                aboutMap.put(app_emailQueueOldestTime, dateFormatForInfoBean(Date.from(pwmApplication.getEmailQueue().eldestItem())));
            }
        }

        if (pwmApplication.getSmsQueue() != null) {
            aboutMap.put(app_smsQueueSize,         Integer.toString(pwmApplication.getSmsQueue().queueSize()));
            if (pwmApplication.getSmsQueue().eldestItem() != null) {
                aboutMap.put(app_smsQueueOldestTime, dateFormatForInfoBean(Date.from(pwmApplication.getSmsQueue().eldestItem())));
            }
        }

        if (pwmApplication.getAuditManager() != null) {
            aboutMap.put(app_syslogQueueSize,      Integer.toString(pwmApplication.getAuditManager().syslogQueueSize()));
        }

        if (pwmApplication.getLocalDB() != null) {
            aboutMap.put(app_localDbLogSize,       Integer.toString(pwmApplication.getLocalDBLogger().getStoredEventCount()));
            aboutMap.put(app_localDbLogOldestTime, dateFormatForInfoBean(pwmApplication.getLocalDBLogger().getTailDate()));

            aboutMap.put(app_localDbStorageSize,   StringUtil.formatDiskSize(FileSystemUtility.getFileDirectorySize(pwmApplication.getLocalDB().getFileLocation())));
            aboutMap.put(app_localDbFreeSpace,     StringUtil.formatDiskSize(FileSystemUtility.diskSpaceRemaining(pwmApplication.getLocalDB().getFileLocation())));
        }


        { // java info
            final Runtime runtime = Runtime.getRuntime();
            aboutMap.put(java_memoryFree,          Long.toString(runtime.freeMemory()));
            aboutMap.put(java_memoryAllocated,     Long.toString(runtime.totalMemory()));
            aboutMap.put(java_memoryMax,           Long.toString(runtime.maxMemory()));
            aboutMap.put(java_threadCount,         Integer.toString(Thread.activeCount()));

            aboutMap.put(java_vmVendor,            System.getProperty("java.vm.vendor"));

            aboutMap.put(java_runtimeVersion,      System.getProperty("java.runtime.version"));
            aboutMap.put(java_vmVersion,           System.getProperty("java.vm.version"));
            aboutMap.put(java_vmName,              System.getProperty("java.vm.name"));
            aboutMap.put(java_vmLocation,          System.getProperty("java.home"));

            aboutMap.put(java_osName,              System.getProperty("os.name"));
            aboutMap.put(java_osVersion,           System.getProperty("os.version"));
            aboutMap.put(java_osArch,              System.getProperty("os.arch"));

            aboutMap.put(java_randomAlgorithm,     PwmRandom.getInstance().getAlgorithm());
            aboutMap.put(java_defaultCharset,      Charset.defaultCharset().name());
        }

        { // build info
            aboutMap.put(build_Time,               PwmConstants.BUILD_TIME);
            aboutMap.put(build_Number,             PwmConstants.BUILD_NUMBER);
            aboutMap.put(build_Type,               PwmConstants.BUILD_TYPE);
            aboutMap.put(build_User,               PwmConstants.BUILD_USER);
            aboutMap.put(build_Revision,           PwmConstants.BUILD_REVISION);
            aboutMap.put(build_JavaVendor,         PwmConstants.BUILD_JAVA_VENDOR);
            aboutMap.put(build_JavaVersion,        PwmConstants.BUILD_JAVA_VERSION);
            aboutMap.put(build_Version,            PwmConstants.BUILD_VERSION);
        }

        { // database info
            try {
                final DatabaseService databaseService = pwmApplication.getDatabaseService();
                if (databaseService != null) {
                    final Map<PwmAboutProperty,String> debugData = databaseService.getConnectionDebugProperties();
                    aboutMap.putAll(debugData);
                }
            } catch (Throwable t) {
                LOGGER.error("error reading database debug properties");
            }
        }

        if (pwmApplication.getPwmEnvironment().getContextManager() != null
                && pwmApplication.getPwmEnvironment().getContextManager().getServerInfo() != null) {
            aboutMap.put(java_appServerInfo, pwmApplication.getPwmEnvironment().getContextManager().getServerInfo());
        }

        return Collections.unmodifiableMap(aboutMap);
    }

    private static String dateFormatForInfoBean(final Date date) {
        return dateFormatForInfoBean(date == null ? null : date.toInstant());
    }

    private static String dateFormatForInfoBean(final Instant date) {
        if (date != null) {
            return date.toString();
        } else {
            return LocaleHelper.getLocalizedMessage(PwmConstants.DEFAULT_LOCALE, Display.Value_NotApplicable, null);
        }

    }

}
