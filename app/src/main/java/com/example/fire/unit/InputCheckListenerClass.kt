package com.example.fire.unit

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.fire.R

class InputCheckListenerClass(val edit:EditText,val button: Button,val flug:Int) {

         var edit2:EditText = edit
        var data_1 = 0
        var data_2:Int
        var db_pwd:String=""

    constructor(edit2: EditText,edit:EditText, button: Button, flug:Int):this(edit,button,flug){
        this.edit2 = edit2
        data_2 =0
        button.isEnabled = edit.text.isNotEmpty()
        Log.d("보조data_2",data_2.toString())
        Log.d("보조data_1",data_1.toString())

    }
    //삭제나 채팅창에 입장할때 - 비번 비교가 필요할 때
    constructor(edit: EditText,db_pwd:String, button: Button, flug:Int):this(edit,button,flug){
        data_2 =1
        button.isEnabled = edit.text.isNotEmpty()
        Log.d("db_pwd",db_pwd)
    }


    init {
        button.isEnabled = edit.text.isNotEmpty()
        data_2 =1
        Log.d("주data_2",data_2.toString())
        Log.d("주data_1",data_1.toString())
    }


    /*타이틀 or 비번*/
    fun check_one(){
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("입력전","!")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("입력중","!")
            }

            override fun afterTextChanged(s: Editable?) {
              when(flug){
                  1->{
                      check_title(s)
                  }
                  2->{
                      check_pwd(s)
                  }
                  3,4->{
                      check_db_pwd(s)
                  }

              }
            }

        })
    }
    /*타이틀-비밀채팅*/
    fun check_two(){
        edit2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("입력전2","!")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("입력중2","!")
            }

            override fun afterTextChanged(s: Editable?) {
                        check_title(s)
            }

        })
    }

    //일반 채팅방 디아로그 텍스트 조건
    fun check_title(s: Editable?){
        if (s != null) {
            if (s.isNotEmpty()){
                data_1 =1
                Log.d("data_2",data_2.toString())
                if (data_1 * data_2 == 0){
                    button.isEnabled = false
                }else{
                    button.isEnabled = true
                }
            }else{
                data_1 =0
                button.isEnabled = false
            }
        }
    }

    fun check_pwd(s: Editable?) {
        if (s != null) {
            if (data_2 != 1) {//비밀 채팅이면
                if (s.isNotEmpty() && s.length > 3) {
                    data_2 = 1
                    Log.d("data_2",data_2.toString())
                    Log.d("data_1",data_1.toString())
                    if (data_1 * data_2 == 0) {
                        button.isEnabled = false

                    } else {
                        button.isEnabled = true

                    }

                } else {
                    data_2 = 0
                    button.isEnabled = false

                }
            }else{
                if (s.isNotEmpty() && s.length > 3) {
                    data_1 = 1
                    Log.d("data_2tt",data_2.toString())
                    Log.d("data_1tt",data_1.toString())
                    if (data_1 * data_2 == 0) {
                        button.isEnabled = false

                    } else {
                        button.isEnabled = true

                    }

                } else {
                    data_1 = 0
                    button.isEnabled = false

                }

            }
        }
    }


    fun check_db_pwd(s: Editable?){
        if (s != null) {
            if (s.toString() == db_pwd){
                button.isEnabled = true
            }else{
                button.isEnabled =false
            }
        }
    }

}