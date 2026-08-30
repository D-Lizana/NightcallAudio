package com.nightcallaudio.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.nightcallaudio.MainActivity
import com.nightcallaudio.NightcallAudioApplication
import com.nightcallaudio.R
import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.usecase.RestorePlaybackSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

class PlaybackWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val restoredState = runCatching {
                val container = (context.applicationContext as NightcallAudioApplication).container
                val session = container.playbackPersistenceRepository.observe().first() ?: return@runCatching PlaybackState()
                val resolved = RestorePlaybackSessionUseCase()(session, container.musicRepository.getTracks())
                    ?: return@runCatching PlaybackState()
                PlaybackState(
                    queue = resolved.tracks,
                    currentIndex = resolved.currentIndex,
                    positionMs = resolved.positionMs,
                    shuffleEnabled = session.shuffleEnabled,
                    repeatMode = session.repeatMode,
                )
            }.getOrDefault(PlaybackState())
            PlaybackWidgetUpdater.updateAll(context, restoredState)
            pendingResult.finish()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val command = WidgetCommand.fromAction(intent.action) ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as NightcallAudioApplication).container
                val tracks = if (container.playbackRepository.state.value.queue.isEmpty()) {
                    container.musicRepository.getTracks()
                } else emptyList()
                withContext(Dispatchers.Main) {
                    if (tracks.isNotEmpty()) container.playbackRepository.restoreSession(tracks)
                    when (command) {
                        WidgetCommand.PREVIOUS -> container.playbackRepository.skipToPrevious()
                        WidgetCommand.PLAY_PAUSE -> if (container.playbackRepository.state.value.isPlaying) {
                            container.playbackRepository.pause()
                        } else container.playbackRepository.play()
                        WidgetCommand.NEXT -> container.playbackRepository.skipToNext()
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

object PlaybackWidgetUpdater {
    fun updateAll(context: Context, state: PlaybackState) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, PlaybackWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val track = state.currentTrack
        val launchAppIntent = activityIntent(context)
        val views = RemoteViews(context.packageName, R.layout.widget_playback).apply {
            setTextViewText(R.id.widget_title, track?.title ?: "Nightcall")
            setTextViewText(R.id.widget_artist, track?.artist ?: "Sin sesión activa")
            setViewVisibility(R.id.widget_controls, if (track == null) View.GONE else View.VISIBLE)
            setImageViewResource(R.id.widget_play_pause, if (state.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            setOnClickPendingIntent(R.id.widget_content, launchAppIntent)
            setOnClickPendingIntent(R.id.widget_title, launchAppIntent)
            setOnClickPendingIntent(R.id.widget_artist, launchAppIntent)
            setOnClickPendingIntent(R.id.widget_previous, commandIntent(context, WidgetCommand.PREVIOUS))
            setOnClickPendingIntent(R.id.widget_play_pause, commandIntent(context, WidgetCommand.PLAY_PAUSE))
            setOnClickPendingIntent(R.id.widget_next, commandIntent(context, WidgetCommand.NEXT))
        }
        manager.updateAppWidget(ids, views)
    }

    private fun activityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            APP_ACTIVITY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun commandIntent(context: Context, command: WidgetCommand): PendingIntent = PendingIntent.getBroadcast(
        context,
        command.ordinal + 1,
        Intent(context, PlaybackWidgetProvider::class.java).setAction(command.action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private const val APP_ACTIVITY_REQUEST_CODE = 100
}

private enum class WidgetCommand(val action: String) {
    PREVIOUS("com.nightcallaudio.widget.PREVIOUS"),
    PLAY_PAUSE("com.nightcallaudio.widget.PLAY_PAUSE"),
    NEXT("com.nightcallaudio.widget.NEXT");

    companion object {
        fun fromAction(action: String?): WidgetCommand? = entries.firstOrNull { it.action == action }
    }
}
