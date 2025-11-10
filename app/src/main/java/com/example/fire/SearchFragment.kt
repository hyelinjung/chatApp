package com.example.fire

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fire.callback.GetChatRoomCallbackInterface
import com.example.fire.chatRoom.ChatListRecylerview
import com.example.fire.data.ChatRoomData
import com.example.fire.data.ChatRoomSendData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SearchFragment: Fragment() {
    private  var list = ArrayList<ChatRoomData>() // 파이어베이스에서 데이터 가져올때 담을
    private  var list_ChatRoomSendData = ArrayList<ChatRoomSendData>() // 마지막 채팅시간 확인하기 위해
    private  var database= Firebase.database.reference
    private lateinit var adapter: ChatListRecylerview
    private var count=0
    var lastReadTime =""
    var unReadCount=0
    lateinit var input:String
    lateinit var search:EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chatroom_list_fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChatListRecylerview(list,requireContext())
        val recycler = view.findViewById<RecyclerView>(R.id.chatRoom_search_recyclerView)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())
        input = arguments?.getString("input")?:""
        Log.d("들어온 검색어",input)
        firstGetDB(input,object : GetChatRoomCallbackInterface {
            override fun firstCallBack(data: List<ChatRoomData>) {
                Log.d("첫 검색화면","success")
                for (chat:ChatRoomData in data){
                    secondGetDB(chat)
                }
            }
        })

        val search = view.findViewById<EditText>(R.id.chatRoom_search)
        search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                Log.d("들어온 검색어",v.text.toString())
                firstGetDB(v.text.toString(),object : GetChatRoomCallbackInterface {
                    override fun firstCallBack(data: List<ChatRoomData>) {
                        Log.d("검색 콜백","success")
                        for (chat:ChatRoomData in data){
                            secondGetDB(chat)
                        }
                    }
                })
                true
            }else{
                Log.d("이벤트 실패","!")
                false
            }
        }

    }


    fun firstGetDB(title:String,callback: GetChatRoomCallbackInterface){
        database.child("chat").child("room").get().addOnSuccessListener {
            Log.d("검색1","success")

            list.clear()
            for (preshot in it.children){
                val receive = preshot.getValue(ChatRoomData::class.java)!!
                receive.key = preshot.key.toString()// 해당 채팅방 으로 입장할때 인텐트에 담아서 보낼 채팅방 pk
                if (receive.title == title)  list.add(receive)
            }
            callback.firstCallBack(list)
        }.addOnFailureListener {
            Log.d("디비 리스트 불러옴 오류",it.toString())
        }

    }


    fun secondGetDB(data:ChatRoomData){
        database.child("chat").child("chatMassage").child(data.key).get().addOnSuccessListener {
            Log.d("검색2","success")
            list_ChatRoomSendData.clear()
            count = 0
            unReadCount = 0
            if (it.children.any()) {
                for (chat_send in it.children) {
                    count++
                    val chat_msg_data = chat_send.getValue(ChatRoomSendData::class.java)
                    if (chat_msg_data?.connect == 0) unReadCount++
                    list_ChatRoomSendData.add(chat_msg_data!!)
                }
                lastReadTime =
                    list_ChatRoomSendData[count - 1].hour //마지막 채팅 시간
                Log.d("lastReadTime", lastReadTime)
            }
            data.unReadCount = unReadCount
            data.lastReadTime = lastReadTime
            Log.d("notifyDataSetChanged", "success")
            adapter.notifyDataSetChanged() //list<ChatRoomData>의 전부를 노티파이
        }.addOnFailureListener {
            Log.d("디비 리스트 불러옴 오류",it.toString())
        }

    }

}