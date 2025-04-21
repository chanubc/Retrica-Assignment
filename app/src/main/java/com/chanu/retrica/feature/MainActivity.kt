package com.chanu.retrica.feature

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil3.load
import com.chanu.retrica.R
import com.chanu.retrica.databinding.ActivityMainBinding
import com.chanu.retrica.filter.ColorMatrixFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initView()
        collectState()
    }

    private fun initView() = with(binding) {
        ivMainDisplay.load(R.drawable.img_cherry_blossom)
        btnMainBlack.setOnClickListener { viewModel.onClickGrayFilter() }
        btnMainDefault.setOnClickListener { viewModel.onClickDefaultFilter() }
        initSeekBarChangeListener()
    }

    private fun initSeekBarChangeListener() {
        binding.sbMainLuminosity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.onSlideBrightnessFilter(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun collectState() {
        collectFilterState()
        collectProgressState()
    }

    private fun collectFilterState() {
        viewModel.filterState.flowWithLifecycle(lifecycle).onEach { event ->
            when (event) {
                is FilterState.Brightness -> applyBrightness(event.progress)
                FilterState.GrayScale -> applyGrayScale()
                FilterState.Default -> resetFilter()
            }
        }.launchIn(lifecycleScope)
    }

    private fun collectProgressState() {
        viewModel.progress.flowWithLifecycle(lifecycle).onEach { progress ->
            binding.sbMainLuminosity.progress = progress
        }.launchIn(lifecycleScope)
    }

    private fun applyBrightness(brightness: Float) {
        binding.ivMainDisplay.colorFilter = ColorMatrixColorFilter(ColorMatrixFactory.toBrightnessFilter(brightness))
    }

    private fun applyGrayScale() {
        binding.ivMainDisplay.colorFilter = ColorMatrixColorFilter(ColorMatrixFactory.toGrayScaleFilter())
    }

    private fun resetFilter() {
        binding.ivMainDisplay.colorFilter = ColorMatrixColorFilter(ColorMatrixFactory.toDefaultFilter())
    }

    companion object {
        private const val PROGRESS_DEFAULT = 255
    }
}
