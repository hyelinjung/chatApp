package com.example.fire.userUI

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.fire.data.ChatUserData
import com.example.fire.databinding.ActivityChatnewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
/*회원가입*/
class ChatnewActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityChatnewBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        database = Firebase.database.reference
        var id = binding.idInput.text
        var pwd = binding.passwordInput.text
        binding.newButton.setOnClickListener {
            new(id.toString(),pwd.toString())
        }
    }
/*닉네임은 중복이 불가하므로 회원가입하기 전단계에서 데이터베이스에 존재하는지 확인 먼저 해야한다.*/
    private fun new(email:String, password:String){
        try {
            Log.d("아이디", email)
            Log.d("비번", password)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(ContentValues.TAG, "createUserWithEmail:success")
                        val user = Firebase.auth.currentUser
                        val uid = user!!.uid
                        println("user!! $user")
                        val data = ChatUserData(email,password,binding.nicknameInput.text.toString())
                        database.child("user").child(uid).setValue(data)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this,
                            "회원가입 실패!!",
                            Toast.LENGTH_LONG,
                        ).show()

                    }
                }
        }catch (e:Exception){
            e.message?.let { Log.d("에러발생", it) }
        }

    }
}