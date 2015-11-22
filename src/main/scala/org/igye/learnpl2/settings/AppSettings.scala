package org.igye.learnpl2.settings

import javax.xml.bind.annotation.{XmlElement, XmlRootElement}

@XmlRootElement
class AppSettings {
    @XmlElement
    var userSettingsFilePath: String = ""
}

