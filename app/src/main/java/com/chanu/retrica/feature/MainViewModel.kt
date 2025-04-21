package com.chanu.retrica.feature

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _filterState = MutableStateFlow<FilterState>(FilterState.Default)
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    fun onClickGrayFilter() {
        _filterState.update { FilterState.Default }
        _filterState.update { FilterState.GrayScale }
    }

    fun onClickDefaultFilter() {
        _filterState.update { FilterState.Default }
    }

    fun onSlideBrightnessFilter(progress: Int) {
        _filterState.update { FilterState.Brightness(progress - 255f) }
    }
}
