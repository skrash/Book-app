package com.skrash.book.FormatBook

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader

class FB2(private val file: File) {

    private val dom = mutableMapOf<String, String>()

    init {
//        parseFormat()
    }

    fun parseFormat(){
        val bytes = file.readBytes()
        val text = String(bytes, Charsets.UTF_8)
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()

        parser.setInput(StringReader(text))
        var eventType = parser.eventType
        var tagName = ""
        while (eventType != XmlPullParser.END_DOCUMENT){
            if(eventType == XmlPullParser.START_TAG) {
                tagName = parser.name
            } else if(eventType == XmlPullParser.TEXT) {
                dom[tagName] = parser.text
            }
            eventType = parser.next();
        }
        dom.forEach {
            Log.d("FB2", "tag: ${it.key} value: ${it.value}")
        }
        Log.d("FB2", "lengh: ${dom.size}")
    }
}