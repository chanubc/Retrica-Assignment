package com.chanu.retrica.feature

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _filterState = MutableStateFlow<FilterState>(FilterState.Default)
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _progress = MutableStateFlow(PROGRESS_DEFAULT)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    fun onClickGrayFilter() {
        _progress.update { PROGRESS_DEFAULT }
        _filterState.update { FilterState.GrayScale }
    }

    fun onClickDefaultFilter() {
        _progress.update { PROGRESS_DEFAULT }
        _filterState.update { FilterState.Default }
    }

    fun onSlideBrightnessFilter(progress: Int) {
        _progress.update { progress }
        _filterState.update { FilterState.Brightness(progress - 255f) }
    }

    companion object {
        private const val PROGRESS_DEFAULT = 255
    }
}
