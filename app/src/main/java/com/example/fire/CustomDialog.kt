package com.example.fire

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.fire.chatRoom.ChatRoomListFragment
import com.example.fire.data.ChatRoomData
import com.example.fire.databinding.ChatDialogBinding
import com.example.fire.databinding.ChatDialogDeletePublicBinding

import com.example.fire.databinding.ChatDialogPublicBinding
import com.example.fire.socket.ChatRoomActivity
import com.example.fire.unit.InputCheckListenerClass
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/*상황에 따른 커스텀 디아로그 생성 클래스*/
class CustomDialog(private val context: Chat_list_Activity, val flug:Int,val list:ChatRoomData=ChatRoomData()) :Dialog(context){
    /*기본 채팅 생성 디아로그*/
   private val dialog_public by lazy {
       ChatDialogPublicBinding.inflate(layoutInflater)
   }
    /*시크릿 채팅 생성*/
    private val dialog_private by lazy {
        ChatDialogBinding.inflate(layoutInflater)
    }
    private val dialog_delete_public by lazy {
        ChatDialogDeletePublicBinding.inflate(layoutInflater)
    }

    private val database = Firebase.database.reference
    val pk:String=list.key
    val db_pwd:String=list.pwd
    val chatRoomTitle =list.title


    /*1.기본
    * 2.비밀방 생성
    * 3삭제
    * 4.비밀방 입장*/

    private lateinit var title:EditText
    private lateinit var pwd:EditText
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when(flug){ //디비에 저장 or삭제만 하고 ui는 메인 액티비티에서
          1->{
              setContentView(dialog_public.root)
              title =dialog_public.publicDialogTitle
              InputCheckListenerClass(title,dialog_public.okButton,flug).check_one()
              dialog_public.cancelButton.setOnClickListener {
                  Log.d("일반","취소")
                  dismiss()
              }
              dialog_public.okButton.setOnClickListener {
                  //파이어베이스 디비에 저장 -> 리스트 프래그먼트 클래스에서 디비 불러오는 함수에서 자동으로 노티파이(해당 프래그먼트에 작성)
                  Log.d("일반","오케")
                  saveDB(ChatRoomData(title.text.toString()))
                  dismiss()
              }
          }
            2->{
                setContentView(dialog_private.root)
                title=dialog_private.privateDialogTitle
                pwd = dialog_private.privateDialogPwd
               val inputCheckListenerClass =  InputCheckListenerClass(title,pwd,dialog_private.okButton,flug)
                inputCheckListenerClass.check_one()
                inputCheckListenerClass.check_two()
                dialog_private.cancelButton.setOnClickListener {
                    Log.d("비밀","취소")
                    dismiss()
                }

                dialog_private.okButton.setOnClickListener {
                    Log.d("비밀","오케")
                        //채팅방 생성
                        val data = ChatRoomData(title.text.toString(),pwd.text.toString())
                       saveDB(data)
                    dismiss()
                }
            }
            3->{//넘어온 room 의 pk를 가지고 비번 비교-> 비번을 알맞게 입력해야 삭제가능
                setContentView(dialog_public.root)
                dialog_public.dialogTitle.text = "채팅방 삭제"
                dialog_public.publicDialogTitle.hint = "비밀번호를 입력하세요"
                pwd = dialog_public.publicDialogTitle
                InputCheckListenerClass(pwd,db_pwd,dialog_private.okButton,flug)
                dialog_public.cancelButton.setOnClickListener {
                    Log.d("삭제","취소")
                    dismiss()
                }
                dialog_public.okButton.setOnClickListener {
                    Log.d("삭제","오케")
                    database.child("chat").child("room").child(pk).removeValue()
                    dismiss()
                }
            }
            4->{
                //넘어온 room 의 pk를 가지고 비번 비교
                setContentView(dialog_public.root)
                dialog_public.dialogTitle.text = "채팅방 입장"
                dialog_public.publicDialogTitle.hint = "비밀번호를 입력하세요"
                pwd = dialog_public.publicDialogTitle
                InputCheckListenerClass(pwd,db_pwd,dialog_private.okButton,flug).check_one()
                dialog_public.cancelButton.setOnClickListener {
                    Log.d("입장4","취소")
                    dismiss()
                }
                dialog_public.okButton.setOnClickListener {
                    Log.d("입장4","오케")
                    val intent = Intent(context, ChatRoomActivity::class.java)
                    intent.putExtra("key",pk)
                    intent.putExtra("title",list.title)
                    ContextCompat.startActivity(context,intent,null)
                    dismiss()
                }

            }
            else->{
                setContentView(dialog_delete_public.root)
                dialog_delete_public.deleteDialogPwd.text = "$chatRoomTitle 삭제하시겠습니까?"
                dialog_delete_public.cancelButton.setOnClickListener {
                    Log.d("삭제 기본","취소")
                    dismiss()
                }
                dialog_delete_public.okButton.setOnClickListener {
                    Log.d("삭제 기본","오케")
                    database.child("chat").child("room").child(pk).removeValue()
                    dismiss()
                }
            }


        }
    }

    fun saveDB(data:ChatRoomData){
        database.child("chat").child("room").push().setValue(data)
        // 1. 빈화면일 때 리스트 프래그먼트를 불러옴 2.이미 리스트 프래그먼트 화면일 때 프래그먼트 어텝터에 노티파이함? 아님 클래스에 프래그먼트 불러옴?
        if (context.getCurrentFragment()==0){
            context.getFragment(ChatRoomListFragment())
        }
    }
}
