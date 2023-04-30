package com.skrash.book.FormatBook

import com.skrash.book.FormatBook.FB2Parser.FictionBook
import com.skrash.book.FormatBook.FB2Parser.P
import java.io.File

class FB2(private val file: File) {

    private var _fb2: FictionBook? = null
    val fb2: FictionBook?
        get() = _fb2

    private val _listText = mutableListOf<String>()
    val listText: List<String>
        get() = _listText

    init {
        _fb2 = FictionBook(file)
        for (n in _fb2!!.body!!.sections) {
            // эпиграф
            if (n.title != null) {
                for (r in n.title.paragraphs) {
                    _listText.add(BOLD_TEXT + r.text + BOLD_TEXT)
                }
            }

            if (n.image != null) {
                _listText.add(IMAGE_TAG + n?.image?.value?.substring(1) + IMAGE_TAG)
            }
            // перебор всех абзацев
            for (i in n.elements) {
                if (i is P) {
                    if (i.images != null) {
                        for (z in i.images) {
                            _listText.add(IMAGE_TAG + z.value.substring(1) + IMAGE_TAG)
                        }
                    }
                    if (i.strong != null) {
                        _listText.add(BOLD_TEXT + i.text + BOLD_TEXT)
                    } else {
                        _listText.add(i.text)
                    }
                }
            }
        }
    }

    companion object {
        const val IMAGE_TAG = ":image:"
        const val BOLD_TEXT = ":bold:"
    }
}