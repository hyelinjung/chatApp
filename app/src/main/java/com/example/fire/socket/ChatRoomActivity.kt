package com.example.fire.socket

import android.annotation.SuppressLint
import android.os.*
import android.text.Editable
import android.text.TextWatcher

import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.fire.R

import com.example.fire.data.ChatRoomSendData
import com.example.fire.data.ChatUserData
import com.example.fire.databinding.ActivityChatRoomBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


import org.json.JSONObject
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.min


/*통신을 위해 소켓 생성 후 주고 받는 데이터 ui에 적용
* 기록을 위해 데이터 서버로 보낼때 디비에 저장*/
class ChatRoomActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityChatRoomBinding.inflate(layoutInflater)
    }
    private lateinit var list:ArrayList<ChatRoomSendData>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ChatRoomRecylerView

    private lateinit var thread: HandlerThread
    private lateinit var mServiceHandler: ServiceHandler /*스레드의 핸들러*/
    private lateinit var socket: Socket
    private lateinit var networkReader: BufferedReader
    private lateinit var networkWriter: BufferedWriter

    var MSG_CONNECT=1
    var MSG_STOP=2
    var MSG_CLIENT_STOP=3
    var MSG_SERVER_STOP=4
    var MSG_START=5
    var MSG_ERROR=999
    private lateinit var client : TCPClient
    private lateinit var nm:String //닉네임
    private lateinit var input:Editable
    private var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var connectOrNot = 0 //나 말고 다른 유저가 들어왔는지 확인하는 변수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        thread = HandlerThread("HandlerThread")
        thread.start()
        mServiceHandler = ServiceHandler(thread.looper)
        auth = Firebase.auth
        database = Firebase.database.reference
        list = ArrayList()
        getlist()//대화 기록 불러오기
        getNm()//들어온 사용자 닉네임
        binding.contactName.text=intent.getStringExtra("title")
        binding.messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
               Log.d("입력전","!")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("입력중..","!")
            }


            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()){
                        binding.sendButton.isClickable=true
                        //binding.chatSub.setBackgroundColor(resources.getColor(R.color.yellow, null))

                    }else{
                        binding.sendButton.isClickable=false
                        //binding.chatSub.setBackgroundColor(resources.getColor(R.color.gray, null))
                    }
                }

            }
        })

        binding.sendButton.setOnClickListener {
            /*데이터 전송*/
           input = binding.messageInput.text
            sendMessage(input.toString(),intent.getStringExtra("key")!!)//클릭한 채팅방의 uid
            input.clear()
        }
        adapter = ChatRoomRecylerView(list,this)
        binding.chatListRecycle.adapter = adapter
        binding.chatListRecycle.layoutManager = LinearLayoutManager(this)

        binding.backButton.setOnClickListener {
            Log.d("뒤로가기","!")
            onBackPressed()
        }


    } //create 끝



    override fun onStop() {
        super.onStop()
        SystemClock.sleep(100)
        if (socket !=null && !socket.isClosed){
            Log.d("onstop","success")
            mServiceHandler.sendEmptyMessage(MSG_CLIENT_STOP)
        }

    }




    private fun sendMessage(input:String,roomUID:String){
        val time:String = dateFormat.format(Date(System.currentTimeMillis())) //메세지를 보낸 시간 저장
        val userUid = auth.currentUser?.uid
        // 현재 시간 정보를 LocalDateTime을 통해 가져오기
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        Log.d("현재시간:",now.toString())
        var hour = now.hour
        val minute = now.minute
        val dayAndNight = if (hour < 12) "AM" else "PM"


        hour = when {
            hour == 0 -> 12         // 0시 → 12 AM
            hour > 12 -> hour - 12  // 13~23시 → 1~11 PM
            else -> hour            // 1~12시 그대로
        }
        val minuteAsString = if (minute < 10) "0$minute" else "$minute"
        Log.d("$hour:",minute.toString())
        val time_string = "$hour:$minuteAsString $dayAndNight"
        var json:JSONObject = JSONObject()
        json.put("status","second")
        json.put("nickname",nm)
        json.put("data",input)
        json.put("uid",userUid!!)
        json.put("chatUID",roomUID)
        json.put("send_time",time)
        json.put("time_string",time_string)
        var send = json.toString()
        val msg = mServiceHandler.obtainMessage()
        msg.what = MSG_START
        msg.obj =send
        mServiceHandler.sendMessage(msg)/*보낸 데이터 ui 표시해야함-> 워커 스레드에서*/

    }

    private fun getlist(){
        val roomUID =intent.getStringExtra("key")
        database.child("chat").child("chatMassage").child(roomUID!!).get().addOnSuccessListener {
            for (preshot in it.children) {
                val data = preshot.getValue(ChatRoomSendData::class.java)
                list.add(data!!)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun start(nm:String,chatRoom:String,userUid:String){
            client = TCPClient("172.30.1.24", 8002, nm, chatRoom, userUid!!)
            client.start()



    }

    private fun getNm(){
        /*닉네임 가져오기*/
        Thread{
            if (connectOrNot==1) onDestroy() //최대 2명만 가능
            val currentUser = auth.currentUser?.uid
            database.child("user").child(currentUser!!).get().addOnSuccessListener {
                Log.i("성공", "!")
                val data = it.getValue(ChatUserData::class.java)
                nm = data!!.nick
                start(nm,intent.getStringExtra("key")!!,currentUser)
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
                nm=""
            }
        }.start()


    }

    /*채팅방에 들어가면 해당 채팅방에서만 소통이 가능해야한다.-전송한 데이터 다른 채팅방에도 전송하면 안됨
   *데이터를 저장할 key 값에 해당 채팅방의 pk필요 */
    inner class TCPClient@Throws(RuntimeException::class) constructor( ip:String, port:Int, name:String,chatRoom:String,user:String):Thread(){
        val socketAddress: SocketAddress
        private val connect_timeout=3000
        var nm:String
        var chatUID :String
        var sender:String
        var loop =true
        init {
            Log.d("소켓 초기화", "!")
            socketAddress = InetSocketAddress(ip,port)
            nm = name
            chatUID = chatRoom /*채팅방 pk*/
            sender = user /*연결자 pk*/
        }

        override fun run() {
            super.run()
            Log.d("소켓실행", "!")
            var serverToClient:String
            try {

                socket = Socket()
                socket.setSoLinger(true,connect_timeout)
                socket.connect(socketAddress,connect_timeout)
                /*스트림 초기화*/
                networkReader = BufferedReader(InputStreamReader(socket.getInputStream()))
                networkWriter = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                val json: JSONObject = JSONObject()
                json.put("status","incoming")
                json.put("nickname",nm)
                json.put("chatUID",chatUID)/*서버 수정-> 완료!*/
                json.put("uid",sender)
                val send = json.toString()
                Log.d("보낼json",send)
                /*데이터를 보낼때는 다른 스레드를 사용해서 -이유 정리 필요*/
                val mMsg = mServiceHandler.obtainMessage()
                mMsg.what=MSG_CONNECT
                mMsg.obj=send
                mServiceHandler.sendMessage(mMsg)

                while (loop){
                    serverToClient=networkReader.readLine()
                    if (serverToClient==null) break
                    val inputASjson = JSONObject(serverToClient)
                    val inputMsg = inputASjson.optString("data","no value")
                    val sender = inputASjson.optString("sender","no value")
                    val toMain = mMainHandler.obtainMessage()
                    val nick = inputASjson.optString("nickname","no value")
                    val time_string = inputASjson.optString("time_string","no value")//HH:MM
                    val annotate_guest = inputASjson.optInt("annotate_guest",0)  //서버에서 넘어온 연결상태
                    //val time:String = inputASjson.get("time_date") as String
                    if (inputMsg=="들어옴"){
                        connectOrNot=1
                        updateDBThread(chatUID,connectOrNot)
                        //토스트 띄움
                        toMain.what = MSG_CONNECT
                        toMain.obj =nick+inputMsg
                        mMainHandler.sendMessage(toMain)
                    }else if(inputMsg=="나감") { //소통하던 상대 나감
                        connectOrNot=0
                        toMain.what = MSG_CONNECT
                        toMain.obj = nick + inputMsg
                        mMainHandler.sendMessage(toMain)
                    }else if(annotate_guest==1){ //내가 두 번째로 들어오는 조건에서는 토스트 띄울 필요없어서 나눔
                        Log.d("사람이랑 대화중,,,", "!")
                        Log.d("들어온 데이터", inputASjson.toString())
                        connectOrNot=1
                        //디비 업데이트 -> 현재 회원 uid의 값과 같은 데이터들의 컨넥션 1로 체인지
                        updateDBThread(chatUID,connectOrNot)
                    }else{
                        connectOrNot=1
                        toMain.what = MSG_START
                        toMain.obj =ChatRoomSendData(inputMsg,sender,null,connectOrNot,nick,time_string)
                        Log.d("들어온 데이터", inputMsg)
                        mMainHandler.sendMessage(toMain)
                       // Log.d("들어온 데이터 시간", time)
                    }
                    //mMainHandler.sendMessage(toMain)
                   // Log.d("들어온 닉네임", nick)
                }
            }catch (e: SocketException) {

                Log.d("연결오류", "서버와 연결이 끊겼습니다!")
                socket.close()
                mMainHandler.sendEmptyMessage(MSG_SERVER_STOP)
            }catch (e: EOFException ){
                Log.d("EOFException", e.toString())
            }catch (e:IOException ){
                Log.d("IOException", e.toString())
            }catch (e:Exception){
                Log.d("연결오류",e.message!!)
                socket.close()
                val msg = mMainHandler.obtainMessage()
                msg.what=MSG_ERROR
                msg.obj="오류가 발생했습니다"
                mMainHandler.sendMessage(msg)
            }finally {
                loop=false
                connectOrNot=0
            }
        }
        /*run 함수 종료*/
        fun quit(){
            try {
                Log.d("quit ","success")
                socket.close()
                loop=false
                val msg = mMainHandler.obtainMessage()
                msg.what=MSG_CLIENT_STOP
                msg.obj="클라이언트 서버 종료"
                mMainHandler.sendMessage(msg)
            }catch (e:Exception){
                Log.d("클라이언트 종료 오류",e.message!!)
            }
        }
    }

    inner class ServiceHandler(looper: Looper): Handler(looper){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
           val mMsg = mMainHandler.obtainMessage()
            when(msg.what){
                MSG_START->{
                    try {
                        val d = msg.obj as String
                        networkWriter.write(d)
                        networkWriter.newLine()
                        networkWriter.flush()
                        /*디비에 데이터 저장*/
                        val j:JSONObject= JSONObject(d)
                        val a=j.get("data")as String
                        val send=j.get("uid")as String
                        val room=j.get("chatUID")as String
                        val time=j.get("send_time")as String //메세지 보낸시간 yyyy-MM-dd 형식
                        val nickname=j.get("nickname")as String
                        val time_string=j.get("time_string")as String
                        val ob = ChatRoomSendData(a,send,time,connectOrNot,nickname,time_string)

                        database.child("chat").child("chatMassage").child(room).push().setValue(ob)
                        mMsg.what=MSG_START //보내는 메세지 ui에 표시하기 위해
                        mMsg.obj=ob
                        mMainHandler.sendMessage(mMsg)
                    }catch (e:Exception){
                    mMsg.what=MSG_ERROR
                        mMsg.obj ="전송시 에러 발생"
                        e.let { it -> Log.d("메세지 전송에 오류가 발생함!",it.message!!)}
                        mMainHandler.sendMessage(mMsg)
                    }

                }
                MSG_CONNECT->{
                    try {
                        networkWriter.write(msg.obj as String)
                        networkWriter.newLine()
                        networkWriter.flush()

                    }catch (e:Exception){
                       mMsg.what=MSG_ERROR
                        mMsg.obj ="처음 전송시 에러 발생"
                        e.let { it -> Log.d("메세지 전송에 오류가 발생함!",it.message!!)}
                        mMainHandler.sendMessage(mMsg)
                    }

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
                    m=msg.obj as String
                }
                MSG_CLIENT_STOP->{
                    m=msg.obj as String
                }
                MSG_SERVER_STOP->{
                    m="서버 연결이 끊겼습니다!"
                }
                MSG_START->{
                   val ob = msg.obj as ChatRoomSendData
                    list.add(ob)
                    adapter.notifyDataSetChanged()
                }
                else->{
                    m=msg.obj as String
                }
            }
            if (m!=null){
                Toast.makeText(this@ChatRoomActivity,m.toString(),Toast.LENGTH_LONG).show()
            }

        }
    }

    fun updateDBThread(chatUid:String,read:Int){
        Thread{
            Log.d("디비업데이트","!")
            database.child("chat").child("chatMassage").child(chatUid).get().addOnSuccessListener {
                list.clear()
                for (preshot in it.children) {
                    val key = preshot.key //채팅의 uid
                    val data = preshot.getValue(ChatRoomSendData::class.java)
                    if (data?.connect ==0){
                        data.connect=1
                        //디비 업데이트
                        val m = data.toMap() //업데이트할 내용
                        val updateKey = "/chat/chatMassage/$chatUid/$key" //위치
                        val update = hashMapOf<String,Any>(updateKey to m) //map 객체 생성
                        database.updateChildren(update) //디비반영
                    }
                    list.add(data!!)
                }
                adapter.notifyDataSetChanged()
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
        }.start()
    }
}