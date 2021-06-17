package com.bu.selfstudy.data.network

import com.bu.selfstudy.tool.log
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class WordPageHandler: DefaultHandler() {

    private var nodeName = ""
    private lateinit var test: StringBuilder

    override fun startDocument() {
        test = StringBuilder()
    }

    override fun endDocument() {
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {

        if(localName=="span") {
            "----------------------------------------".log()
            uri?.log()
            localName?.log()
            qName?.log()
            attributes?.log()
            "----------------------------------------".log()
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
    }
}