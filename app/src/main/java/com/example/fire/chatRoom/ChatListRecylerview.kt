package com.example.fire.chatRoom

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fire.Chat_list_Activity
import com.example.fire.CustomDialog
import com.example.fire.data.ChatRoomData
import com.example.fire.databinding.ChatitemBinding
import com.example.fire.socket.ChatRoomActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatListRecylerview(private val list: List<ChatRoomData>, val context:Context):RecyclerView.Adapter<ChatListRecylerview.MyViewHolder>() {

    inner class MyViewHolder(private val binding : ChatitemBinding):RecyclerView.ViewHolder(binding.root){
        val title = binding.chatTitle
        val time = binding.chatItemTime
        val unread = binding.unreadMsg
        val root = binding.root /*채팅방을 클릭할 때*/
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =  ChatitemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        Log.d("이너 클래스 생성","success")
        return MyViewHolder(binding)
    }

    /*넘어온 데이터를 갖고 이미지와 타이틀을 데이터 바인딩 해준다.*/
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = list[position]
        Log.d("리스트확인",data.toString())
        holder.title.text = data.title //채팅방 타이틀

        if ( data.unReadCount==0){
            holder.unread.visibility = View.GONE
        }else{
            Log.d("리unReadCount",data.unReadCount.toString())
            holder.unread.visibility = View.VISIBLE
            holder.unread.text = data.unReadCount.toString()
        }
        holder.time.text = data.lastReadTime
        val pwd = data.pwd//채팅방의 pwd
        val key = data.key //채팅방의 pk
        holder.root.setOnClickListener {
            /*채팅방에 들어가 려면 비밀번호를 입력해야함
            * 디아로그 창에 비밀 번호를 찾을 수 있게 함*/
            Log.d("채팅 입장 클릭","success")
            if (pwd.isEmpty()){
                Log.d("채팅 입장 클릭 비번 없음","success")
                val intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("key",key)
                intent.putExtra("title",data.title)
                ContextCompat.startActivity(context,intent,null)
            }else{
                CustomDialog(context as Chat_list_Activity,4,data).show()
            }

       }
        holder.root.setOnLongClickListener {
            Log.d("채팅 삭제","success")
            if (pwd.isEmpty()){
                Log.d("채팅 삭제 비번 없음","success")
                CustomDialog(context as Chat_list_Activity,6,data).show()
            }else{
                CustomDialog(context as Chat_list_Activity,3,data).show()
            }
            true
        }

    }

    override fun getItemCount(): Int {
       return list.size
    }

}