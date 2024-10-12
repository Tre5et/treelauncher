package dev.treset.treelauncher.style

/**
 * Copied from https://github.com/adamglin0/compose-shadow
 * @author adamglin0
 */

import androidx.compose.ui.graphics.NativePaint

internal actual fun NativePaint.setMaskFilter(blurRadius: Float) {
    this.maskFilter = org.jetbrains.skia.MaskFilter.makeBlur(org.jetbrains.skia.FilterBlurMode.NORMAL, blurRadius / 2, true)
}