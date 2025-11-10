package com.example.fire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.fire.CustomDialog
import com.example.fire.MainActivity
import com.example.fire.R

import com.google.android.material.button.MaterialButton

class EmptyFragment:Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       return inflater.inflate(R.layout.empty_list_fragment,container,false)
    }

    /*UI 상호작용: UI 요소와의 상호작용은 Fragment의 onViewCreated 메서드 내에서 처리합니다. 예를 들어, 버튼 클릭 시 특정 작업을 수행하도록 설정할 수 있습니다.*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("EmptyFragment", "success")
        val make = view.findViewById<TextView>(R.id.start_chat_button)
        make.setOnClickListener {//클릭 시 알림창 ->채팅방 생성하면 리스트 프래그먼트로 이동
            println("생성버튼 클릭")
            //알림창
            CustomDialog(requireContext() as Chat_list_Activity,2).show()

        }

    }


}