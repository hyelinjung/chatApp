package org.example;


import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    private SocketServer socketServer;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    String nickname;
    String input;
    String chatUID;

    public Server(SocketServer socketServer, Socket socket) throws IOException {
        this.socketServer = socketServer;
        this.socket = socket;
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        start();
        System.out.println("서버객체 생성");
    }
    /*처음 입장할 때 key- status
    * 전달받은 데이터 key- data */
    void start(){
        socketServer.threadPool.execute(()->{
                try {
                    while ((input = reader.readLine()) != null) {

                        JSONObject jsonObject = new JSONObject(input);
                        String status = jsonObject.getString("status"); //처음인지 아닌지

                        nickname = jsonObject.getString("nickname");
                        String uid = jsonObject.getString("uid");//수정 ->보낸이 표시 하기위한(클라이언트에서 판단하기 위한)
                        chatUID = jsonObject.getString("chatUID");//수정 -> 같은 채팅방 인원한테만 보내기 위한

                        switch (status) {
                            case "incoming":
                                //처음 들어옴
                                socketServer.list.add(this);
                                socketServer.annotation_connect(this);
                                socketServer.sendAll(this, "들어옴", nickname, uid, "","");
                                break;
                            case "second":
                                String data = jsonObject.getString("data");
                                String time_string = jsonObject.getString("time_string"); //메세지 보낸시간 HH:MM
                                String time = jsonObject.getString("send_time"); //메세지 보낸시간 yyyy-MM-dd 형식
                                socketServer.sendAll(this, data, nickname, uid, time,time_string);
                                break;
                        }
                    }
                }catch (SocketException e) {
                    System.out.println(e + ":소켓이 갑자기 닫힘");

                }catch (EOFException e) {
                    System.out.println(e + ":스트림의 끝에 다달음");
                }catch (IOException e) {
                    System.out.println(e);
                }finally {
                    socketServer.sendAll(this,"나감",nickname,"","","");
                    socketServer.list.remove(this);
                    System.out.println("리스트길이:"+socketServer.list.size());
                    }
        });
    }

    void send(String json){
        System.out.println("다시전달 호스트 네임:"+socket.getInetAddress().getHostName());
        System.out.println("데이터 다시 전달");
        writer.println(json);
    }
    void quit(){
        try {
            socket.close();
        }catch (IOException e){
            System.out.println("클라이언트 소켓 종료 오류"+e.getMessage());
        }
    }

}
