package com.example.fire


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.example.fire.databinding.ActivityMainBinding
import com.example.fire.userUI.ChatnewActivity
import com.example.fire.userUI.LoginActivity

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            //로그인인
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            }
            binding.newButton.setOnClickListener {
                //회원가입
                val intent = Intent(this, ChatnewActivity::class.java)
                startActivity(intent)
            }
        }






}