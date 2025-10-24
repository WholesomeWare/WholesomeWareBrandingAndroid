package com.csakitheone.wholesomeware.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.csakitheone.wholesomeware.MainActivity
import com.csakitheone.wholesomeware.R

class RadioService : Service() {
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val binder = RadioBinder()
    private var currentStreamUrl: String? = null
    private var currentStreamTitle: String = "Vörösmarty Rádió"

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "radio_playback_channel"
        const val ACTION_PLAY = "com.csakitheone.wholesomeware.ACTION_PLAY"
        const val ACTION_PAUSE = "com.csakitheone.wholesomeware.ACTION_PAUSE"
        const val ACTION_STOP = "com.csakitheone.wholesomeware.ACTION_STOP"
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_STREAM_TITLE = "stream_title"
    }

    inner class RadioBinder : Binder() {
        fun getService(): RadioService = this@RadioService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        exoPlayer = ExoPlayer.Builder(this).build()

        // Create MediaSession for MediaStyle notification
        mediaSession = MediaSession.Builder(this, exoPlayer!!)
            .build()

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra(EXTRA_STREAM_URL)
                val title = intent.getStringExtra(EXTRA_STREAM_TITLE)
                if (url != null) {
                    if (title != null) {
                        currentStreamTitle = title
                    }
                    playStream(url)
                }
            }
            ACTION_PAUSE -> pauseStream()
            ACTION_STOP -> stopStream()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for radio playback"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @OptIn(UnstableApi::class)
    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = exoPlayer?.isPlaying == true

        val playPauseIntent = if (isPlaying) {
            Intent(this, RadioService::class.java).apply { action = ACTION_PAUSE }
        } else {
            Intent(this, RadioService::class.java).apply { action = ACTION_PLAY }
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RadioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentStreamTitle)
            .setContentText(if (isPlaying) "Playing" else "Paused")
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(
                R.drawable.ic_pause,
                "Stop",
                stopPendingIntent
            )
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(mediaSession!!)
                    .setShowActionsInCompactView(0, 1)
            )
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun playStream(url: String) {
        currentStreamUrl = url
        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
        startForeground(NOTIFICATION_ID, createNotification())
    }

    fun pauseStream() {
        exoPlayer?.playWhenReady = false
        updateNotification()
    }

    fun stopStream() {
        exoPlayer?.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true

    fun setStreamTitle(title: String) {
        currentStreamTitle = title
        updateNotification()
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }
}

