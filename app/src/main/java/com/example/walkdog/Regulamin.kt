package com.example.walkdog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.walkdog.databinding.ActivityRegulaminBinding

class Regulamin : AppCompatActivity() {
    lateinit var binding : ActivityRegulaminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegulaminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAkceptacja.setOnClickListener {
            onBackPressed()
        }
    }
}