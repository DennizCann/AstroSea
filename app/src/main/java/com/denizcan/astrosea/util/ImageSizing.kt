package com.denizcan.astrosea.util

/** Decode close to the target before scaling, rather than allocating the full asset. */
fun bitmapSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    require(maxDimension > 0)
    var sample = 1
    while (maxOf(width, height) / (sample * 2L) >= maxDimension) sample *= 2
    return sample
}

fun boundedImageSize(width: Int, height: Int, maxDimension: Int): Pair<Int, Int> {
    require(width > 0 && height > 0 && maxDimension > 0)
    val scale = minOf(1.0, maxDimension.toDouble() / maxOf(width, height))
    return maxOf(1, (width * scale).toInt()) to maxOf(1, (height * scale).toInt())
}
