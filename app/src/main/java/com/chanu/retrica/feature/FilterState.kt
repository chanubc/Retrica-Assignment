package com.chanu.retrica.feature

sealed interface FilterState {
    data class Brightness(val progress: Float) : FilterState

    data object GrayScale : FilterState

    data object Default : FilterState
}
