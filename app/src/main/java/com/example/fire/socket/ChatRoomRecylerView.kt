package com.example.fire.socket

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fire.R
import com.example.fire.data.ChatRoomSendData
import com.example.fire.databinding.ChatRecBinding
import com.example.fire.databinding.ChatSenderBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.collections.ArrayList

class ChatRoomRecylerView(private val list:ArrayList<ChatRoomSendData>,private val context: Context):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val send =1
    private val rece =2


    class SenderViewHolder(private val binding: ChatSenderBinding):RecyclerView.ViewHolder(binding.root){
        val sendMsg = binding.senderView
        val sender_date = binding.senderDateTextView // 메세지 보낸시간 -날짜 구분(yyyy-MM-dd)형태
        val send_time = binding.sentMessageTime //메세지 보낸시간
        val send_status = binding.sentMessageStatus //읽음 상태
        val root = binding.root

    }
    class ReceViewHolder(private val binding: ChatRecBinding):RecyclerView.ViewHolder(binding.root){
        val recMsg = binding.receivedMessageText
        val rec_date = binding.recDateTextView //메세지 보낸시간 -날짜 구분(yyyy-MM-dd)형태
        val rec_time = binding.recMessageTime//메세지 보낸시간
        val rec_status = binding.recMessageStatus
        val nickname = binding.chettingNickname
        val root = binding.root
    }

    override fun getItemViewType(position: Int): Int {
        val data = list[position]
        FirebaseAuth.getInstance().currentUser?.uid?.let { Log.d("사용자", it) }
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(data.sender)){
            Log.d("send","보내는쪽")
            return send
        }else{
            Log.d("rece","받는쪽")
            return rece
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


       return if (viewType ==1){
            val binding = ChatSenderBinding.inflate(LayoutInflater.from(context),parent,false)
            SenderViewHolder(binding)

        }else{
            val binding = ChatRecBinding.inflate(LayoutInflater.from(context),parent,false)
            ReceViewHolder(binding)

        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        val previousMsg:String? = if(position>0)list[position-1].time else null
        if (previousMsg==null || !previousMsg.equals(data.time)){
            if (holder.javaClass == SenderViewHolder::class.java){
                holder as SenderViewHolder
                holder.sendMsg.text = data.message
                holder.send_time.text = data.hour
                holder.sender_date.visibility = View.VISIBLE
                holder.sender_date.text = data.time
                if (data.connect==1){
                    holder.send_status.text = "읽음"
                    holder.send_status.setTextColor(R.color.black)
                }
                Log.d("send","1")
            }else{
                Log.d("받는쪽에서 시간 변경","!")
                holder as ReceViewHolder
                holder.nickname.text =data.nickname
                holder.recMsg.text = data.message
                holder.rec_time.text = data.hour
                holder.rec_date.visibility = View.VISIBLE
                holder.rec_date.text = data.time
                if (data.connect==1){
                    holder.rec_status.text = "읽음"
                    holder.rec_status.setTextColor(R.color.black)
                }
                Log.d("rece","2")
            }
        }else{
            if (holder.javaClass == SenderViewHolder::class.java){
                holder as SenderViewHolder
                holder.sendMsg.text = data.message
                holder.send_time.text = data.hour
                holder.sender_date.visibility = View.GONE
                if (data.connect==1){
                    holder.send_status.text = "읽음"
                    holder.send_status.setTextColor(R.color.black)
                }
                Log.d("send","1")
            }else{
                holder as ReceViewHolder
                holder.nickname.text =data.nickname
                holder.recMsg.text = data.message
                holder.rec_time.text = data.hour
                holder.rec_date.visibility = View.GONE
                if (data.connect==1){
                    holder.rec_status.text = "읽음"
                    holder.rec_status.setTextColor(R.color.black)
                }
                Log.d("rece","2")
            }
        }

    }

    override fun getItemCount(): Int {
       return list.size
    }


}