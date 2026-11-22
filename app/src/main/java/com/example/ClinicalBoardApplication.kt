package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class ClinicalBoardApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader = createAtlasImageLoader(this)

    companion object {
        const val MEMORY_CACHE_PERCENT = 0.18
        const val DISK_CACHE_MAX_BYTES = 32L * 1024L * 1024L
        const val DISK_CACHE_DIR_NAME = "atlas_image_cache"

        fun createAtlasImageLoader(context: Context): ImageLoader {
            val appContext = context.applicationContext
            return ImageLoader.Builder(appContext)
                .memoryCache {
                    MemoryCache.Builder(appContext)
                        .maxSizePercent(MEMORY_CACHE_PERCENT)
                        .strongReferencesEnabled(true)
                        .weakReferencesEnabled(true)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(appContext.cacheDir.resolve(DISK_CACHE_DIR_NAME))
                        .maxSizeBytes(DISK_CACHE_MAX_BYTES)
                        .build()
                }
                .bitmapConfig(Bitmap.Config.RGB_565)
                .allowHardware(true)
                .respectCacheHeaders(false)
                .crossfade(200)
                .build()
        }
    }
}
