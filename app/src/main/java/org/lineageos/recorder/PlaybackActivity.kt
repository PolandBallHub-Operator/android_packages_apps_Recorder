/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.recorder

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import org.lineageos.recorder.ui.PlaybackWaveformView

class PlaybackActivity : AppCompatActivity(R.layout.activity_playback) {
    private val toolbar by lazy { findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.playbackToolbar) }
    private val titleView by lazy { findViewById<TextView>(R.id.playbackNameTextView) }
    private val dateView by lazy { findViewById<TextView>(R.id.playbackDateTextView) }
    private val timeView by lazy { findViewById<TextView>(R.id.playbackTimeTextView) }
    private val playButton by lazy { findViewById<MaterialButton>(R.id.playbackPlayButton) }
    private val stopButton by lazy { findViewById<MaterialButton>(R.id.playbackStopButton) }
    private val progress by lazy { findViewById<Slider>(R.id.playbackProgress) }
    private val waveform by lazy { findViewById<PlaybackWaveformView>(R.id.playbackWaveformView) }
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var durationMs = 1

    private val progressTicker = object : Runnable {
        override fun run() {
            player?.let { mediaPlayer ->
                if (mediaPlayer.isPlaying) {
                    updateProgress(mediaPlayer.currentPosition)
                    handler.postDelayed(this, 250)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        toolbar.setNavigationOnClickListener { finish() }
        titleView.text = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.playback_title)
        dateView.text = intent.getStringExtra(EXTRA_DATE).orEmpty()
        playButton.setOnClickListener { togglePlayback() }
        stopButton.setOnClickListener { stopPlayback() }
        progress.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                player?.seekTo((value * durationMs).toInt())
                updateProgress((value * durationMs).toInt())
            }
        }
        preparePlayer(intent.getStringExtra(EXTRA_URI))
    }

    private fun preparePlayer(uriString: String?) {
        val uri = uriString?.let(Uri::parse) ?: return
        player = MediaPlayer().apply {
            setDataSource(this@PlaybackActivity, uri)
            setOnPreparedListener {
                durationMs = it.duration.coerceAtLeast(1)
                progress.valueTo = 1f
                updateProgress(0)
                playButton.isEnabled = true
            }
            setOnCompletionListener {
                updateProgress(durationMs)
                playButton.text = getString(R.string.playback_play)
                playButton.setIconResource(R.drawable.ic_play_arrow)
            }
            setOnErrorListener { _, _, _ ->
                playButton.isEnabled = false
                true
            }
            prepareAsync()
        }
        playButton.isEnabled = false
    }

    private fun togglePlayback() {
        val mediaPlayer = player ?: return
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playButton.text = getString(R.string.playback_play)
            playButton.setIconResource(R.drawable.ic_play_arrow)
        } else {
            mediaPlayer.start()
            playButton.text = getString(R.string.playback_pause)
            playButton.setIconResource(R.drawable.ic_pause)
            handler.post(progressTicker)
        }
    }

    private fun stopPlayback() {
        player?.pause()
        player?.seekTo(0)
        updateProgress(0)
        playButton.text = getString(R.string.playback_play)
        playButton.setIconResource(R.drawable.ic_play_arrow)
    }

    private fun updateProgress(positionMs: Int) {
        val fraction = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        progress.value = fraction
        timeView.text = formatTime(positionMs)
        waveform.setProgress(fraction)
    }

    private fun formatTime(ms: Int): String {
        val seconds = (ms / 1000).coerceAtLeast(0)
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URI = "playback_uri"
        const val EXTRA_TITLE = "playback_title"
        const val EXTRA_DATE = "playback_date"
    }
}
