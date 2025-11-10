package org.example;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*소켓을 accept 해서 스트림을 생성하는 스레드*/
public class SocketServer extends Thread {
    private ServerSocket serverSocket;

     List<Server> list = Collections.synchronizedList(new ArrayList<>());
     ExecutorService threadPool = Executors.newFixedThreadPool(5);
    SocketServer(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true){
            try {
                Socket socket = serverSocket.accept();
                System.out.println("소켓 생성");
                Server server = new Server(this,socket);
            } catch (IOException e) {
                System.out.println("소캣서버 오류!!");
                throw new RuntimeException(e);
            }

        }

    }
    void quit(){
        try {
            serverSocket.close();
            threadPool.shutdown();
            list.stream().forEach(i ->i.quit());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    void sendAll(Server server,String data, String nickname,String uid,String time,String time_string){//수정
        System.out.println("sendAll함수");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        jsonObject.put("nickname",nickname);
        jsonObject.put("sender",uid);//수정
        jsonObject.put("time_date",time);
        jsonObject.put("time_string",time_string);
       String json = jsonObject.toString();
        for (Server s : list){
            if (server.chatUID.equals(s.chatUID)&& s != server){
                s.send(json);
            }

        }

    }
    void annotation_connect(Server server){
        System.out.println("연결확인!");
        JSONObject jsonObject = new JSONObject();
        for (Server s : list){
            if (server.chatUID.equals(s.chatUID)){ //자기 자신이 아닌 스레드객체를 비교
                System.out.println("동접자가 있다!!");
                jsonObject.put("annotate_guest",1);
                server.send(jsonObject.toString());
                s.send(jsonObject.toString());
            }
        }
    }
}
