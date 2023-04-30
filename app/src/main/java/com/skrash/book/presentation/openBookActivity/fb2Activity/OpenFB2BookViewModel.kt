package com.skrash.book.presentation.openBookActivity.fb2Activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skrash.book.FormatBook.FB2
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.usecases.MyList.GetMyBookUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class OpenFB2BookViewModel @Inject constructor(
    private val getMyBookUseCase: GetMyBookUseCase
) : ViewModel() {

    private val _bookItem = MutableLiveData<BookItem>()

    private var _fb2 = MutableLiveData<FB2>()
    val fb2: LiveData<FB2>
        get() = _fb2

    fun init(bookItemId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (_bookItem.value == null) {
                val item = getMyBookUseCase.getMyBook(bookItemId)
                _bookItem.postValue(item)
                val file = File(item.path)
                _fb2.postValue(FB2(file))
            }
        }
    }

}