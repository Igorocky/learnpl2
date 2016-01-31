package org.igye.learnpl2.settings

import javax.xml.bind.annotation.{XmlElement, XmlRootElement}

@XmlRootElement
class UserSettings {
    @XmlElement
    var directoryWithTexts: String = _
    @XmlElement
    var urlForTranslation: String = _
    @XmlElement
    var probabilityPercent: Int = 10
    @XmlElement
    var randomOrderOfSentences: Boolean = false
    @XmlElement
    var skipReadingStage: Boolean = false
    @XmlElement
    var autoRepeat: Int = 0

    def validateAndCorrect(): Unit = {
        if (probabilityPercent < 0 || probabilityPercent > 100) {
            probabilityPercent = 10
        }
        if (autoRepeat < 0) {
            autoRepeat = 0
        }
    }
}
