package com.example.fire.chatRoom

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fire.Chat_list_Activity
import com.example.fire.R
import com.example.fire.SearchFragment
import com.example.fire.callback.GetChatRoomCallbackInterface
import com.example.fire.data.ChatRoomData
import com.example.fire.data.ChatRoomSendData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
/*채팅방 프래그먼트*/
class ChatRoomListFragment:Fragment() {
    private  var list = ArrayList<ChatRoomData>() // 파이어베이스에서 데이터 가져올때 담을
    private  var list_ChatRoomSendData = ArrayList<ChatRoomSendData>() // 마지막 채팅시간 확인하기 위해
    private  var database= Firebase.database.reference
    private lateinit var adapter: ChatListRecylerview
    var count=0
    var lastReadTime =""
    var unReadCount=0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chatroom_list_fragment,container,false)
    }

    //리사이클러 뷰에 데이터 어뎁터 하기 -> 파이어베이스 에서 데이터를 가져오면 노티파이 해야함
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ChatListRecylerview(list,requireContext())
        val recycler = view.findViewById<RecyclerView>(R.id.chatRoom_recyclerView)
        recycler.adapter = adapter
        recycler.layoutManager =LinearLayoutManager(requireContext())

        firstGetDB(object : GetChatRoomCallbackInterface {
            override fun firstCallBack(data: List<ChatRoomData>) {
                Log.d("첫 콜백","success")
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
                getSearchList(v.text.toString())
                true
            }else{
                Log.d("이벤트 실패","!")
                false
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }






    fun getSearchList(name:String){
        Log.d("getSearchList",name)
        val bundle=Bundle()
        val newfragment = SearchFragment()
        bundle.putString("input",name)
        newfragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.content_frame,newfragment).addToBackStack(null).commit()

    }

    fun firstGetDB(callback:GetChatRoomCallbackInterface){
        database.child("chat").child("room").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("firstGetDB","success")
                list.clear()

                for (preshot in snapshot.children){
                    //Log.d("확인",preshot.toString())
                    val receive = preshot.getValue(ChatRoomData::class.java)!!
                    receive.key = preshot.key.toString()// 해당 채팅방 으로 입장할때 인텐트에 담아서 보낼 채팅방 pk
                    list.add(receive)
                }
                callback.firstCallBack(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("디비 리스트 불러옴 오류",error.message)
            }

        })
    }

    fun secondGetDB(data:ChatRoomData){
        database.child("chat").child("chatMassage").child(data.key).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("secondGetDB","success")
                list_ChatRoomSendData.clear()
                count = 0
                unReadCount = 0
                if (snapshot.children.any()) {
                    for (chat_send in snapshot.children) {
                        //Log.d("변경발생-snapshot",chat_send.toString())
                        count++
                        val chat_msg_data = chat_send.getValue(ChatRoomSendData::class.java)
                        if (chat_msg_data?.connect == 0) unReadCount++
                        list_ChatRoomSendData.add(chat_msg_data!!)
                        //Log.d("unReadCount", unReadCount.toString())
                       // Log.d("count", count.toString())
                        //Log.d("list_ChatRoomSendData크기",list_ChatRoomSendData.size.toString())
                    }
                    lastReadTime =
                        list_ChatRoomSendData[count - 1].hour //마지막 채팅 시간
                    Log.d("lastReadTime", lastReadTime)
                }

                data.unReadCount = unReadCount
                data.lastReadTime = lastReadTime
                Log.d("notifyDataSetChanged", "success")
                adapter.notifyDataSetChanged() //list<ChatRoomData>의 전부를 노티파이
            }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("디비 리스트 불러옴 오류",error.message)
                }

            })
    }

    override fun onPause() {
        super.onPause()
        Log.d("리스너 삭제","!")
        database.child("chat").child("room").removeEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("firstGetDB", "success")
                list.clear()

                for (preshot in snapshot.children) {
                    val receive = preshot.getValue(ChatRoomData::class.java)!!
                    receive.key = preshot.key.toString()// 해당 채팅방 으로 입장할때 인텐트에 담아서 보낼 채팅방 pk
                    list.add(receive)
                }
                object : GetChatRoomCallbackInterface {
                    override fun firstCallBack(data: List<ChatRoomData>) {
                        Log.d("첫 콜백", "success")
                        for (chat: ChatRoomData in data) {
                            database.child("chat").child("chatMassage").child(chat.key).removeEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    Log.d("secondGetDB","success")
                                    list_ChatRoomSendData.clear()
                                    count = 0
                                    unReadCount = 0
                                    if (snapshot.children.any()) {
                                        for (chat_send in snapshot.children) {
                                            count++
                                            val chat_msg_data = chat_send.getValue(ChatRoomSendData::class.java)
                                            if (chat_msg_data?.connect == 0) unReadCount++
                                            list_ChatRoomSendData.add(chat_msg_data!!)
                                        }
                                        lastReadTime =
                                            list_ChatRoomSendData[count - list_ChatRoomSendData.size].hour //마지막 채팅 시간
                                        Log.d("lastReadTime", lastReadTime)
                                    }
                                    chat.unReadCount = unReadCount
                                    chat.lastReadTime = lastReadTime
                                    adapter.notifyDataSetChanged() //list<ChatRoomData>의 전부를 노티파이
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("디비 리스트 불러옴 오류",error.message)
                                }

                            })
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("디비 리스트 불러옴 오류",error.message)
            }

        })
    }

}
