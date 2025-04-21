package com.chanu.retrica.filter

import android.graphics.ColorMatrix

object ColorMatrixFactory {
    private val sharedMatrix = ColorMatrix()

    fun toGrayScaleFilter(): ColorMatrix {
        sharedMatrix.reset()
        sharedMatrix.setSaturation(0f)
        return sharedMatrix
    }

    fun toBrightnessFilter(value: Float): ColorMatrix {
        sharedMatrix.reset()
        sharedMatrix.set(
            floatArrayOf(
                1f, 0f, 0f, 0f, value,
                0f, 1f, 0f, 0f, value,
                0f, 0f, 1f, 0f, value,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        return sharedMatrix
    }

    fun toDefaultFilter(): ColorMatrix {
        sharedMatrix.reset()
        return sharedMatrix
    }

    fun toCustomFilter(matrix: FloatArray) {
        sharedMatrix.reset()
        sharedMatrix.set(matrix)
    }
}
