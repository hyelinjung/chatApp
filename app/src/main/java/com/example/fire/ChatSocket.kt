package com.example.fire

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class ChatSocket {
    private lateinit var thread: HandlerThread
    private lateinit var mServiceHandler:ServiceHandler /*스레드의 핸들러*/
    private lateinit var socket: Socket
    private lateinit var networkReader: BufferedReader
    private lateinit var networkWriter: BufferedWriter
    private  var loop:Boolean = false
    var MSG_CONNECT=1
    var MSG_STOP=2
    var MSG_CLIENT_STOP=3
    var MSG_SERVER_STOP=4
    var MSG_START=5
    var MSG_ERROR=999
    private lateinit var client : TCPClient

    /*채팅방에 들어가면 해당 채팅방에서만 소통이 가능해야한다.-전송한 데이터 다른 채팅방에도 전송하면 안됨
    *데이터를 저장할 key 값에 해당 채팅방의 pk필요 */
    inner class TCPClient@Throws(RuntimeException::class) constructor( ip:String, port:Int, name:String,chatRoom:String):Thread(){
        val socketAddress:SocketAddress
        private val connect_timeout=3000
        var nm:String
        var chatUID :String
        init {
            socketAddress = InetSocketAddress(ip,port)
            nm = name
            chatUID = chatRoom /*채팅방 pk*/
        }

        override fun run() {
            super.run()
            Log.d("소켓실행", "!")
            try {
                loop = true
                socket = Socket()
                socket.setSoLinger(true,connect_timeout)
                socket.connect(socketAddress,connect_timeout)
                /*스트림 초기화*/
                networkReader = BufferedReader(InputStreamReader(socket.getInputStream()))
                networkWriter = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                val json:JSONObject = JSONObject()
                json.put("status","incoming")
                json.put("nickname",nm)
                json.put("pk",chatUID)/*서버 수정*/
                val send = json.toString()
                Log.d("보낼json",send)
                /*데이터를 보낼때는 다른 스레드를 사용해서 -이유 정리 필요*/
                val mMsg = mServiceHandler.obtainMessage()
                mMsg.what=MSG_CONNECT
                mMsg.obj=send
                mServiceHandler.sendMessage(mMsg)

                while (networkReader.readLine()!=null){
                    val serverToClient=networkReader.readLine()
                    val inputASjson =JSONObject(serverToClient)
                    val inputMsg = inputASjson.get("data") as String
                    val nick = inputASjson.get("nickname") as String
                    Log.d("들어온 데이터", inputMsg)
                    Log.d("들어온 닉네임", nick)
                }
            }catch (e:Exception){
                loop=false
                Log.d("연결오류",e.message!!)
            }
        }
        /*run 함수 종료*/
        fun quit(){
            loop=false
            try {
                socket.close()
                val msg = mMainHandler.obtainMessage()
                msg.what=MSG_CLIENT_STOP
                msg.obj="클라이언트 서버 종료"
                mMainHandler.sendMessage(msg)
            }catch (e:Exception){
                Log.d("클라이언트 종료 오류",e.message!!)
            }
        }
    }
    inner class ServiceHandler(looper: Looper):Handler(looper){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val mMsg = mMainHandler.obtainMessage()
            when(msg.what){
                MSG_START->{
                    try {
                        networkWriter.write(msg.obj as String)
                        networkWriter.newLine()
                        networkWriter.flush()
                        mMsg.what=MSG_START
                    }catch (e:Exception){
                        mMsg.what=MSG_ERROR
                        mMsg.obj ="전송시 에러 발생"
                        e.let { it -> Log.d("메세지 전송에 오류가 발생함!",it.message!!)}
                        mMainHandler.sendMessage(mMsg)
                    }
                    mMainHandler.sendMessage(mMsg)
                }
                MSG_CONNECT->{
                    try {
                        networkWriter.write(msg.obj as String)
                        networkWriter.newLine()
                        networkWriter.flush()
                        mMsg.what=MSG_CONNECT
                    }catch (e:Exception){
                        mMsg.what=MSG_ERROR
                        mMsg.obj ="처음 전송시 에러 발생"
                        e.let { it -> Log.d("메세지 전송에 오류가 발생함!",it.message!!)}
                        mMainHandler.sendMessage(mMsg)
                    }
                    mMainHandler.sendMessage(mMsg)
                }
                MSG_STOP,MSG_CLIENT_STOP,MSG_SERVER_STOP ->{
                    client.quit()
                    client.join()
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    private val mMainHandler :Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            var m:String? = null
            when(msg.what){
                MSG_CONNECT->{
                    m="정상적으로 서버에 접속함"
                }
                MSG_CLIENT_STOP->{
                    m=msg.obj as String
                }
                MSG_SERVER_STOP->{
                    m="서버가 연결을 종료함"
                }
                MSG_START->{
                    m="메세지를 전송했습니다"
                }
                else->{
                    m=msg.obj as String
                }
            }
           /* Toast.makeText()*/
        }
    }
}