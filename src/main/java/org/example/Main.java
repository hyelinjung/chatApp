package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = new ServerSocket(8002);
            SocketServer thread = new SocketServer(serverSocket);
            thread.start();
            Scanner scanner = new Scanner(System.in);
            for (;;){
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("q")) break;
            }
            thread.quit();
        } catch (IOException e) {
            System.out.println("서버 메인 오류:"+ e.getMessage());
        }

    }
    }
