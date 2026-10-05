package com.denizcan.astrosea.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.util.TypedValue
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

private object ResourceBitmaps {
    val decoding = Semaphore(2)
    val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
        // Never recycle evicted bitmaps: a visible composable may still be using them.
    }
}

/** Raster assets are decoded off the UI thread, without drawable density upscaling. */
@Composable
fun rememberResourcePainter(id: Int, maxDimension: Int = 1600): Painter {
    val resources = LocalContext.current.resources
    val resourcePath = remember(id, resources.configuration) {
        TypedValue().also { resources.getValue(id, it, true) }.string.toString()
    }
    if (resourcePath.endsWith(".xml")) return painterResource(id)
    val key = "$resourcePath:$maxDimension"
    val bitmap by produceState(ResourceBitmaps.cache.get(key), key1 = key) {
        value = ResourceBitmaps.cache.get(key)
        value = withContext(Dispatchers.IO) {
            ResourceBitmaps.decoding.withPermit {
                ResourceBitmaps.cache.get(key) ?: run {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true; inScaled = false }
                    BitmapFactory.decodeResource(resources, id, bounds)
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withPermit null
                    val options = BitmapFactory.Options().apply {
                        inScaled = false
                        inSampleSize = bitmapSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
                    }
                    val decoded = BitmapFactory.decodeResource(resources, id, options) ?: return@withPermit null
                    val (width, height) = boundedImageSize(decoded.width, decoded.height, maxDimension)
                    val scaled = Bitmap.createScaledBitmap(decoded, width, height, true)
                    if (scaled !== decoded) decoded.recycle()
                    ResourceBitmaps.cache.put(key, scaled)
                    scaled
                }
            }
        }
    }
    return remember(bitmap, key) { bitmap?.let { BitmapPainter(it.asImageBitmap()) } ?: ColorPainter(Color.Transparent) }
}
