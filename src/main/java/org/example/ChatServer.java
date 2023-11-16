package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {
    private static final int PORT = 8090;
    private static Map<PrintWriter, String> clientMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                Scanner scanner = new Scanner(clientSocket.getInputStream());
                if (scanner.hasNextLine()) {
                    String username = scanner.nextLine();
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                    clientMap.put(writer, username);
                    broadcastMessage(username+"님이 입장했습니다.");

                    Thread t = new Thread(new ClientHandler(clientSocket, writer, username));
                    t.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 채팅
    public static void broadcastMessage(String sender, String message) {
        for (PrintWriter writer : clientMap.keySet()) {
            writer.println(sender + ": " + message);
            writer.flush();
        }
    }

    // 출입 안내 문구
    public static void broadcastMessage(String message) {
        for (PrintWriter writer : clientMap.keySet()) {
            writer.println(message);
            writer.flush();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private String username;

        public ClientHandler(Socket socket, PrintWriter writer, String username) {
            this.clientSocket = socket;
            this.writer = writer;
            this.username = username;
        }
        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(clientSocket.getInputStream());

                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    if ( !message.equals("")) {
                        broadcastMessage(username, message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 나간 유저를 맵에서 제거, 안내문구 출력
                clientMap.remove(writer);
                broadcastMessage(username+"님이 나가셨습니다.");
            }
        }
    }
}