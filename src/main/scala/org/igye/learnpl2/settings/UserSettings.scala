package org.igye.learnpl2.settings

import javax.xml.bind.annotation.{XmlElement, XmlRootElement}

@XmlRootElement
class UserSettings {
    @XmlElement
    var directoryWithTexts: String = _
    @XmlElement
    var urlForTranslation: String = _
}
