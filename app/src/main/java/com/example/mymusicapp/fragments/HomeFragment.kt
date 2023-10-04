package com.example.mymusicapp.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.mymusicapp.R
import com.example.mymusicapp.Service.MusicService
import com.example.mymusicapp.adapter.MusicAdapter
import com.example.mymusicapp.databinding.FragmentHomeBinding
import com.example.mymusicapp.db.entity.MusicEntity
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog


class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private lateinit var list: ArrayList<MusicEntity>
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: MusicAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        if (!hasPermission()) {
            requestPermission()
        } else {
            list = scanDeviceForMp3Files() as ArrayList<MusicEntity>

            adapter = MusicAdapter(list) { music, position ->
                val intent = Intent(requireContext(), MusicService::class.java)
                intent.putExtra("key", "STOP")
                ContextCompat.startForegroundService(requireContext(), intent)
                val bundle = Bundle()
                bundle.putSerializable("music", music)
                bundle.putInt("index", position)
                findNavController().navigate(R.id.musicFragment, bundle)
            }
            binding.rv.adapter = adapter
        }

        return binding.root
    }


    private fun hasPermission() =
        EasyPermissions.hasPermissions(
            requireContext(), Manifest.permission.READ_MEDIA_AUDIO
        )

    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            this, "Qo'shiqlarni o'qishga ruxsat berishingiz kerak!",
            1,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            list = scanDeviceForMp3Files() as ArrayList<MusicEntity>
            adapter = MusicAdapter(list) { music, position ->
                val bundle = Bundle()
                bundle.putSerializable("music", music)
                bundle.putInt("index", position)
                findNavController().navigate(R.id.musicFragment, bundle)
            }
            binding.rv.adapter = adapter
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (hasPermission()) {

        } else {
            Toast.makeText(requireContext(), "Not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        list = scanDeviceForMp3Files() as ArrayList<MusicEntity>
        adapter = MusicAdapter(list) { music, position ->
            findNavController().navigate(R.id.musicFragment)
        }
        binding.rv.adapter = adapter
        return super.shouldShowRequestPermissionRationale(permission)
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