package com.example.mymusicapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.mymusicapp.R
import com.example.mymusicapp.Service.MusicService
import com.example.mymusicapp.Service.MusicService.Companion.myMusicPlayer
import com.example.mymusicapp.databinding.FragmentMusicBinding
import com.example.mymusicapp.db.entity.MusicEntity


class MusicFragment : Fragment()  {

    private lateinit var binding: FragmentMusicBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var list: ArrayList<MusicEntity>
    private lateinit var handler: Handler
    private var index: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMusicBinding.inflate(inflater)
        val music = arguments?.getSerializable("music") as MusicEntity
        index = arguments?.getInt("index", -1)!!
        list = scanDeviceForMp3Files() as ArrayList<MusicEntity>

        val intent = Intent(requireContext(), MusicService::class.java)
        intent.putExtra("path", music.aPath)
//        intent.putExtra("key","START")
        //     binding.seekbar.progress = 50
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(requireContext(), intent)
        } else {
            ContextCompat.getSystemService(requireContext(), MusicService::class.java)
        }


        //MusicDatabase.getDatabase(requireContext()).musicDao().listMusics() as ArrayList<MusicEntity>
        binding.musicCount.text = "${(index + 1)} / ${list.size}"
        playing(music)
        binding.apply {

            btnPlay.setOnClickListener {
                if (myMusicPlayer?.isPlaying == true) {
                    //   mediaPlayer?.pause()
//                    startForegroundService(
//                        requireContext(),
//                        Intent(requireContext(), MusicService::class.java).putExtra("key", "PAUSE")
//                    )
                    myMusicPlayer?.pause()
                    binding.btnPlay.setImageResource(R.drawable.btn_play)
                } else {
                    //  mediaPlayer?.start()
//                    startForegroundService(
//                        requireContext(),
//                        Intent(requireContext(), MusicService::class.java).putExtra("key", "START")
//                    )
                    myMusicPlayer?.start()
                    binding.btnPlay.setImageResource(R.drawable.btn_pause)
                }
            }

            btnBack.setOnClickListener {
                //mediaPlayer?.stop()
                myMusicPlayer?.reset()

                val intent1 = Intent(requireContext(), MusicService::class.java)
                if (index == 0) {
                    playing(list[list.size - 1])
                    intent1.putExtra("path", list[list.size - 1].aPath)
                    index = list.size - 1
                } else {
                    playing(list[index - 1])
                    intent1.putExtra("path", list[index - 1].aPath)
                    index -= 1
                }
                ContextCompat.startForegroundService(requireContext(), intent1)
                binding.musicCount.text = "${(index + 1)} / ${list.size}"
            }

            btnNext.setOnClickListener {
                //  mediaPlayer?.stop()
                myMusicPlayer?.reset()
                val intent1 = Intent(requireContext(), MusicService::class.java)
                index = if (index == list.size - 1) {
                    playing(list[0])
                    intent1.putExtra("path", list[0].aPath)
                    0
                } else {
                    playing(list[index + 1])
                    intent1.putExtra("path", list[index + 1].aPath)
                    index + 1
                }
                ContextCompat.startForegroundService(requireContext(), intent1)
                binding.musicCount.text = "${(index + 1)} / ${list.size}"
            }

            btnBack10.setOnClickListener {
                // mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.minus(5000) ?: 0)
                myMusicPlayer?.seekTo(myMusicPlayer?.currentPosition?.minus(5000) ?: 0)

            }

            btnNext10.setOnClickListener {
                //  mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.plus(5000) ?: 0)
                myMusicPlayer?.seekTo(myMusicPlayer?.currentPosition?.plus(5000) ?: 0)
            }
            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p2) {
                        //      mediaPlayer?.seekTo(p1)
                        myMusicPlayer?.seekTo(p1)
                        p0?.progress = p1
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) = Unit
            })
        }
        binding.listImg.setOnClickListener {
            findNavController().popBackStack()
        }


        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
        return binding.root
    }


    private val runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {

            val currentPosition = myMusicPlayer?.currentPosition
            Log.d("position", "run: $currentPosition")
            if (currentPosition != null) {
                binding.seekbar.progress = currentPosition

                if ((currentPosition % 60000) / 1000 > 9 && currentPosition / 60000 > 9) {
                    binding.musicTime.text =
                        "${currentPosition / 60000}:${(currentPosition % 60000) / 1000}"
                }
                if (currentPosition / 60000 < 10) {
                    binding.musicTime.text =
                        "0${currentPosition / 60000}:${(currentPosition % 60000) / 1000}"
                }
                if ((currentPosition % 60000) / 1000 < 10) {
                    binding.musicTime.text =
                        "${currentPosition / 60000}:0${(currentPosition % 60000) / 1000}"
                }
                if ((currentPosition % 60000) / 1000 < 10 && currentPosition / 60000 < 10) {
                    binding.musicTime.text =
                        "0${currentPosition / 60000}:0${(currentPosition % 60000) / 1000}"
                }
            }
            handler.postDelayed(this, 1000)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun playing(music: MusicEntity) {
//        mediaPlayer = MediaPlayer()
//        mediaPlayer?.setDataSource(requireContext(), Uri.parse(music.aPath))
//        mediaPlayer?.setOnPreparedListener(this)
//        mediaPlayer?.prepareAsync()

        binding.musicName.text = music.aName
        binding.musicArtist.text = music.aArtist
        val duration = music.duration
        if ((duration % 60000) / 1000 > 9 && duration / 60000 > 9) {
            binding.musicMaxTime.text = "/ ${duration / 60000}:${(duration % 60000) / 1000}"
        } else if (duration / 60000 < 10) {
            binding.musicMaxTime.text = "/ 0${duration / 60000}:${(duration % 60000) / 1000}"
        } else if ((duration % 60000) / 1000 < 10) {
            binding.musicMaxTime.text = "/ ${duration / 60000}:0${(duration % 60000) / 1000}"
        } else if ((duration % 60000) / 1000 < 10 && duration / 60000 < 10) {
            binding.musicMaxTime.text = "/ 0${duration / 60000}:0${(duration % 60000) / 1000}"
        }
        if (mediaPlayer?.isPlaying == true) {
            binding.btnPlay.setImageResource(R.drawable.btn_play)
        } else {
            binding.btnPlay.setImageResource(R.drawable.btn_pause)
        }
        binding.musicCount.text = "${(index + 1)} / ${list.size}"


        binding.musicTime.text = "00:00"
        binding.seekbar.max = music.duration.toInt()
        //myMusicPlayer?.duration!!
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runnable, 1000)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        //mediaPlayer?.stop()
        //mediaPlayer?.release()


    }


    fun scanDeviceForMp3Files(): List<MusicEntity> {

        // val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM
        )
        val mp3Files: MutableList<MusicEntity> = ArrayList()
        var cursor: Cursor? = null
        try {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    //   val title: String = cursor.getString(0)
                    val artist: String = cursor.getString(1)
                    val path: String = cursor.getString(2)
                    val displayName: String = cursor.getString(3)
                    val songDuration: Long = cursor.getLong(4)
                    val album: String = cursor.getString(5)
                    cursor.moveToNext()
//                   if (path.endsWith(".mp3")) {
                    val music = MusicEntity(
                        aPath = path,
                        aArtist = artist,
                        aName = displayName,
                        aAlbum = album,
                        duration = songDuration
                    )
                    mp3Files.add(music)
                    //      MusicDatabase.getDatabase(requireContext()).musicDao().addMusic(music)
                    //  }
                }
            }

        } catch (e: Exception) {
            Log.e("TAG", e.toString())
        } finally {
            cursor?.close()
        }
        return mp3Files
    }


}