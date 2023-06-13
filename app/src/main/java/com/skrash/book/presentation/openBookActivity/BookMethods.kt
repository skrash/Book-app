package com.skrash.book.presentation.openBookActivity

interface BookMethods {

    // книга любого формата должна поддерживать данные методы

    fun setPage(itemPage: Int) // установка текущей страницы просмотра во вью модели

    fun jumpTo(page: Int) // переход по указанной странице или смещению

    fun finish(page: Int) // запись данных в бд ( текущей страницы просмотра )

    fun addBookmark(page: Int) // добавление закладки

    fun deleteBookmark(page: Int) // удаление закладки

}