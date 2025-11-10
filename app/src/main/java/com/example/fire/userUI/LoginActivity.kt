package com.example.fire.userUI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.fire.Chat_list_Activity
import com.example.fire.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var id: String
    private lateinit var pwd: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.loginButton.setOnClickListener {
            //로그인인
            id = binding.idInput.text.toString()
            pwd = binding.passwordInput.text.toString()
            login(id,pwd)
        }
        binding.newButton.setOnClickListener {
            //회원가입
            val intent = Intent(this, ChatnewActivity::class.java)
            startActivity(intent)
        }
        binding.toolbar.setOnClickListener {
                onBackPressedDispatcher
        }
    }


    private fun login(email:String, password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("성공", "signInWithEmail:success")
                    /*채팅 리스트 화면으로 전환*/
                    val intent = Intent(this, Chat_list_Activity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("실패", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        this,
                        "로그인 실패",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }


}