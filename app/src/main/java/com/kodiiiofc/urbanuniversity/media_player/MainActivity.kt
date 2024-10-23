package com.kodiiiofc.urbanuniversity.media_player

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.kodiiiofc.urbanuniversity.media_player.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private var songList = mutableListOf(
        R.raw.nerevar_rising,
        R.raw.dragonborn,
        R.raw.blessing_of_vivec,
        R.raw.over_the_next_hill
    )
    private var isMusicPlaying = false
    private var currentSongId = 0
    private val mmr = MediaMetadataRetriever()
    private var song = songList[currentSongId]
    private var volumeLevel = 0.25f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressSlider.setLabelFormatter {
            "${(it / 1000 / 60).toInt()} : ${(it / 1000 % 60).toInt()}"
        }
        playSound()
        updateDataSong(currentSongId)
    }

    private fun updateDataSong(songId: Int) {
        song = songList[currentSongId]
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(song))
            .appendPath(resources.getResourceTypeName(song))
            .appendPath(resources.getResourceEntryName(song))
            .build()

        mmr.setDataSource(this@MainActivity, uri)
        val pictureBytes = mmr.embeddedPicture
        val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)

        if (pictureBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size)
            binding.coverIv.setImageBitmap(bitmap)
        }

        binding.titleTv.text = "$artist - $title"

    }

    private fun playSound() {
        binding.fabPlayPause.setOnClickListener {
            if (!isMusicPlaying) {
                play(song)
            } else {
                pause()
            }
        }
        binding.fabStop.setOnClickListener {
            stop()
        }

        binding.fabPrev.setOnClickListener {
            song = changeSongId(--currentSongId)
            if (isMusicPlaying) {
                play(song)
            } else setSong(song)
        }

        binding.fabNext.setOnClickListener {
            song = changeSongId(++currentSongId)
            if (isMusicPlaying) {
                play(song)
            } else setSong(song)
        }

        binding.progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) mediaPlayer?.seekTo(value.toInt())
        }

        binding.volumeSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.setVolume((progress.toFloat() / 100), (progress.toFloat() / 100))
                    volumeLevel = progress.toFloat() / 100
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        )
    }

    private fun stop() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer = null
            binding.fabPlayPause.setImageResource(R.drawable.ic_play)
            isMusicPlaying = false
        }
    }

    private fun pause() {
        if (mediaPlayer != null) {
            binding.fabPlayPause.setImageResource(R.drawable.ic_play)
            mediaPlayer?.pause()
            isMusicPlaying = false
        }
    }

    private fun setSong(song: Int) {
        updateDataSong(song)
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, song)
            mediaPlayer?.setVolume(volumeLevel, volumeLevel)
            binding.progressSlider.valueTo = mediaPlayer!!.duration.toFloat()
        }
    }

    private fun play(song: Int) {
        setSong(song)
        binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
        initSlider()
        mediaPlayer?.start()
        mediaPlayer?.setVolume(volumeLevel, volumeLevel)
        isMusicPlaying = true
    }

    private fun changeSongId(songId: Int): Int {
        currentSongId = if (songId < 0) {
            songId + songList.size
        } else if (songId >= songList.size) {
            songId % songList.size
        } else songId
        mediaPlayer?.reset()
        mediaPlayer = null
        return songList[currentSongId]
    }

    private fun initSlider() {
        binding.progressSlider.valueTo = mediaPlayer!!.duration.toFloat()
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.progressSlider.value = mediaPlayer!!.currentPosition.toFloat()
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    binding.progressSlider.value = 0f
                }
            }
        }, 0)
    }

}