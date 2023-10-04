package com.example.mymusicapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.mymusicapp.Service.MusicService
import com.example.mymusicapp.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this,MusicService::class.java)
        intent.putExtra("key","STOP")
        startForegroundService(intent)
    }
}