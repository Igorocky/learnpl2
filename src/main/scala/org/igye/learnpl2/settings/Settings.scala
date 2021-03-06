package org.igye.learnpl2.settings

import java.io.File

import org.igye.commonutils.JaxbSupport

object Settings {
    private val appSettingsFile = new File("conf/config.xml")
    private var appSettings: AppSettings = new AppSettings
    private var userSettings: UserSettings = new UserSettings

    def userSettingsFilePath = appSettings.userSettingsFilePath
    def userSettingsFilePath_=(newVal: String) = {
        appSettings.userSettingsFilePath = newVal
    }

    def directoryWithTexts = userSettings.directoryWithTexts
    def directoryWithTexts_=(newVal: String) = {
        userSettings.directoryWithTexts = newVal
    }

    def urlForTranslation = userSettings.urlForTranslation
    def urlForTranslation_=(newVal: String) = {
        userSettings.urlForTranslation = newVal
    }

    def urlForTranslation2 = userSettings.urlForTranslation2
    def urlForTranslation2_=(newVal: String) = {
        userSettings.urlForTranslation2 = newVal
    }

    def openBothTranslations = userSettings.openBothTranslations
    def openBothTranslations_=(newVal: Boolean) = {
        userSettings.openBothTranslations = newVal
    }

    def probabilityPercent = userSettings.probabilityPercent
    def probabilityPercent_=(newVal: Int) = {
        userSettings.probabilityPercent = newVal
    }

    def randomOrderOfSentences = userSettings.randomOrderOfSentences
    def randomOrderOfSentences_=(newVal: Boolean) = {
        userSettings.randomOrderOfSentences = newVal
    }

    def skipReadingStage = userSettings.skipReadingStage
    def skipReadingStage_=(newVal: Boolean) = {
        userSettings.skipReadingStage = newVal
    }

    def autoRepeat = userSettings.autoRepeat
    def autoRepeat_=(newVal: Integer) = {
        userSettings.autoRepeat = newVal
    }

    def loadAppSettings(): Unit = {
        if (appSettingsFile.exists()) {
            appSettings = JaxbSupport.unmarshal[AppSettings](appSettingsFile)
        } else {
            appSettings = new AppSettings
        }
    }

    def loadUserSettings(): Unit = {
        val userSettsFile = new File(userSettingsFilePath)
        if (userSettsFile.exists()) {
            userSettings = JaxbSupport.unmarshal[UserSettings](userSettsFile)
        } else {
            userSettings = new UserSettings
        }
        userSettings.validateAndCorrect()
    }

    def saveAppSettings(): Unit = {
        if (!appSettingsFile.getParentFile.exists()) {
            appSettingsFile.getParentFile.mkdirs()
        }
        JaxbSupport.marshal(appSettings, appSettingsFile)
    }

    def saveUserSettings(): Unit = {
        userSettings.validateAndCorrect()
        val userSettsFile = new File(userSettingsFilePath)
        if (!userSettsFile.getParentFile.exists()) {
            userSettsFile.getParentFile.mkdirs()
        }
        JaxbSupport.marshal(userSettings, userSettsFile)
    }
}