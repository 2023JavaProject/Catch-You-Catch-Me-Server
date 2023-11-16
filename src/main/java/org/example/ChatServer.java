package org.example;

import java.awt.*;
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
            // 서버 소캣 생성 및 포트번호 설정
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                // 클라이언트로의 연결을 기다림
                Socket clientSocket = serverSocket.accept();

                Scanner scanner = new Scanner(clientSocket.getInputStream());

                if (scanner.hasNextLine()) {
                    String username = scanner.nextLine();

                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                    clientMap.put(writer, username);
                    broadcastMessage(username + "님이 입장했습니다.");

                    // 각 클라이언트를 처리할 핸들러 스레드 시작
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
    public static void broadcastDrawing(int x1, int y1, int x2, int y2, Color color, int penSize) {
        String message = String.format("draw:%d,%d,%d,%d,%d,%d", x1, y1, x2, y2, color.getRGB(), penSize);
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
<<<<<<< HEAD
                    if (message.startsWith("draw:")) {
                        processDrawingMessage(message);
                    } else {
=======
                    // 공백 입력 허용 x
                    if (!message.equals("")) {
>>>>>>> 1267117931f6b513c49010a33f48280223fbfd62
                        broadcastMessage(username, message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 나간 유저를 맵에서 제거, 안내문구 출력
                clientMap.remove(writer);
                broadcastMessage(username + "님이 나가셨습니다.");
            }
        }
        private void processDrawingMessage(String message) {
            // Format: "draw:x1,y1,x2,y2,color,penSize"
            String[] parts = message.substring(5).split(",");
            int x1 = Integer.parseInt(parts[0]);
            int y1 = Integer.parseInt(parts[1]);
            int x2 = Integer.parseInt(parts[2]);
            int y2 = Integer.parseInt(parts[3]);
            Color color = new Color(Integer.parseInt(parts[4]));
            int penSize = Integer.parseInt(parts[5]);
            broadcastDrawing(x1, y1, x2, y2, color, penSize);
        }
    }
}