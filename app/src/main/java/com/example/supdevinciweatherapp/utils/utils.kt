package com.example.supdevinciweatherapp.utils

import android.content.Context
import android.widget.Toast

class utils {
    fun showToast(text: String, context: Context) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}