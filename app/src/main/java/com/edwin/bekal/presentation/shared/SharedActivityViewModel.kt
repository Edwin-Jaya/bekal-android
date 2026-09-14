package com.edwin.bekal.presentation.shared

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner


@Composable
inline fun <reified VM : ViewModel> sharedActivityViewModel(): VM {
    val owner = checkNotNull(LocalActivity.current as? ViewModelStoreOwner) {
        "Composable harus berjalan di dalam ComponentActivity."
    }
    return hiltViewModel(owner)
}