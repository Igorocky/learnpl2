package org.igye.learnpl2.controllers

import java.awt.Desktop
import java.net.URL
import javafx.fxml.FXML
import javafx.scene.control.Tab
import javafx.scene.web.WebView

import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.Initable

@FxmlFile("fxml/BrowserTab.fxml")
class BrowserTabController extends Initable {
    @FXML
    protected var root: Tab = _
    @FXML
    protected var webView: WebView = _

    private var url: String = _

    override def init(): Unit = {
        require(root != null)
        require(webView != null)
    }

    def getTab(word: String) = {
        url = s"https://translate.google.ru/#pl/ru/$word"
        webView.getEngine.load(url)
        root.setText(s"G:$word")
        root
    }

    def openInBrowser(): Unit = {
        Desktop.getDesktop().browse(new URL(url).toURI());
    }
}
