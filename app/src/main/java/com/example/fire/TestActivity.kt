package com.example.fire

import Post
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.fire.data.ChatUserData
import com.example.fire.databinding.ActivityTestBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TestActivity : AppCompatActivity() {
    private lateinit var binding:ActivityTestBinding
    val database = Firebase.database.reference
    val auth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bt.setOnClickListener {
            writeNewPost("수정","수정username","수정title","수정body")
        }
    }

    private fun writeNewPost(userId: String, username: String, title: String, body: String) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
       /* val key = database.child("posts").push().key
        if (key == null) {
            Log.w("확인", "Couldn't get push key for posts")
            return
        }*/

        val post = Post(userId, username, title, body)
        val postValues = post.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/posts/-O6hRunpnOJ63JREjfoq" to postValues
           /* "/user-posts/$key" to postValues,*/
        )

       // database.updateChildren(childUpdates)

            database.child("user").child(auth.currentUser?.uid!!).child("nick").get().addOnSuccessListener {
                val nick  = it.value
                Log.d("nick",nick.toString())
            }.addOnFailureListener {
                Log.d("유저불러오기오류",it.toString())
            }
         }

    }
