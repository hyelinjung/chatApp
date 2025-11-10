package com.example.fire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.fire.chatRoom.ChatRoomListFragment
import com.example.fire.data.ChatRoomData
import com.example.fire.data.ChatUserData
import com.example.fire.databinding.ActivityChatlistBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/*채팅방 리스트 화면-> 가장 첫 화면 프래그먼트 사용*/
class Chat_list_Activity : AppCompatActivity() {
    private val binding by lazy {
        ActivityChatlistBinding.inflate(layoutInflater)
    }
    private lateinit var drawerLayout: DrawerLayout
    private var last_back_time = 0L //마지막으로 종료를 누른 시간
    private val close_time = 2000 //2초안에 두번 클릭시 종료
    private lateinit var database: DatabaseReference

    private lateinit var dialogCustom :CustomDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val auth = Firebase.auth
        database = Firebase.database.reference
        // 툴바 설정
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)


        // DrawerLayout과 툴바 연동
        drawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        database.child("user").child(auth.currentUser?.uid!!).child("nick").get().addOnSuccessListener {
            val nick  = it.value
            Log.d("nick",nick.toString())
            findViewById<TextView>(R.id.header_text).text = nick.toString()
        }.addOnFailureListener {
            Log.d("유저불러오기오류",it.toString())
        }



        binding.navLogoutLayout.setOnClickListener {
            print("로그아웃")
            Firebase.auth.signOut()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        //맨 처음 프래그먼트-> 채팅방 데이터 베이스가 있으면 리스트 프래그먼트 / 없으면 엠티 프래그먼트
        checkDB()
        //getFragment(ChatRoomListFragment())
        val nav: NavigationView = binding.drawNav

        nav.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_new_message -> {
                    //비밀방 만들기
                    //알림창-> 제목/비번설정-> 채팅방 만들기
                    Log.d("네비클릭","2")
                    dialogCustom = CustomDialog(this,2)
                    dialogCustom.show()
                    true
                }
                R.id.nav_other -> {
                    //채팅방 제목 설정-> 채팅방 만들기
                    Log.d("네비클릭","1")
                    dialogCustom = CustomDialog(this,1)
                    dialogCustom.show()
                    true
                }
                else -> {
                    false
                }
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)// 메뉴 클릭시 원하는 프래그먼트로 전환, 네비게이션 닫힘
            }

        }

    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.content_frame)
            if (currentFragment != null) {/*기본 프래그먼트일때 뒤로가기하면 바로 앱 종료*/
                if (currentFragment.javaClass == EmptyFragment::class.java ||currentFragment.javaClass == ChatRoomListFragment::class.java){
                    var currentTime = System.currentTimeMillis()
                    if (currentTime - last_back_time > close_time) {
                        Toast.makeText(this, "한 번더 클릭시 앱이 종료됩니다!", Toast.LENGTH_LONG).show()
                        last_back_time = currentTime
                    }else if (currentFragment.javaClass == SearchFragment::class.java){
                        getFragment(ChatRoomListFragment())
                    }else{
                        /*앱 종료*/
                      onDestroy()
                        Firebase.auth.signOut()

                    }
                }else{
                    super.onBackPressed()
                }
            }
        }



    }

    fun getFragment(fragment: Fragment){ //프래그먼트 선택
        supportFragmentManager.beginTransaction().replace(R.id.content_frame,fragment)
            .commit()
    }

    fun getCurrentFragment():Int{// 디아로그로 채팅방 만들었을 때 프래그먼트 이동 여부 확인
        val frame = supportFragmentManager.findFragmentById(R.id.content_frame)
        val name = frame?.javaClass?.simpleName
        Log.d("클래스 이름",name!!)
        if (name == "EmptyFragment"){ // 채팅룸 프래그먼트로 이동해야함
            return 0
        }else{
            return 1
        }
    }

    fun checkDB(){ // 데이터 여부에 따른 프래그먼트 선택
        database.child("chat").child("room").get().addOnSuccessListener {
           if (it.exists()) getFragment(ChatRoomListFragment()) else getFragment(EmptyFragment())
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

}