package com.csakitheone.wholesomeware.experiment

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class IcecastPlayer(context: Context) {
    private val player = ExoPlayer.Builder(context).build()

    fun playStream(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.release()
    }
}