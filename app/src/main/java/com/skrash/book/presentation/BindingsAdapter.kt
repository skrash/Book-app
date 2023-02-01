package com.skrash.book.presentation

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.skrash.book.R

@BindingAdapter("errorInputTitle")
fun bindErrorInputTitle(textInputLayout: TextInputLayout, isError: Boolean) {
    val message = if (isError) {
        textInputLayout.context.getString(R.string.error_input_title)
    } else {
        null
    }
    textInputLayout.error = message
}

@BindingAdapter("errorInputAuthor")
fun bindErrorInputAuthor(textInputLayout: TextInputLayout, isError: Boolean) {
    val message = if (isError) {
        textInputLayout.context.getString(R.string.error_input_author)
    } else {
        null
    }
    textInputLayout.error = message
}

@BindingAdapter("errorInputDescription")
fun bindErrorInputDescription(textInputLayout: TextInputLayout, isError: Boolean) {
    val message = if (isError) {
        textInputLayout.context.getString(R.string.error_input_description)
    } else {
        null
    }
    textInputLayout.error = message
}

@BindingAdapter("errorInputGenres")
fun bindErrorInputGenres(textInputLayout: TextInputLayout, isError: Boolean) {
    val message = if (isError) {
        textInputLayout.context.getString(R.string.error_input_genres)
    } else {
        null
    }
    textInputLayout.error = message
}

@BindingAdapter("errorInputTags")
fun bindErrorInputTags(textInputLayout: TextInputLayout, isError: Boolean) {
    val message = if (isError) {
        textInputLayout.context.getString(R.string.error_input_tags)
    } else {
        null
    }
    textInputLayout.error = message
}

@BindingAdapter("errorInputPath")
fun bindErrorInputPath(textInputLayout: TextInputLayout, isError: Boolean) {
    val message = if (isError) {
        textInputLayout.context.getString(R.string.error_input_path)
    } else {
        null
    }
    textInputLayout.error = message
}