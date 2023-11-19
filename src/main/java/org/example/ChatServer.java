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
    private static int readyUserCnt = 0;
    private static int playUserCnt = 0;
    private static ArrayList<String> nameArr = new ArrayList<>();
    private static int currentTimeInSeconds = 0;

    //타이머 변수
    private static int mm;
    private static int ss;
    private static int ms;
    private static int t = 0;
    private static String currentTime;
    private static Thread p_display;


    public static void main(String[] args) {
        try {
            // 서버 소캣 생성 및 포트번호 설정
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                // 클라이언트로의 연결을 기다림
                Socket clientSocket = serverSocket.accept();
                readyUserCnt++;
                System.out.println(readyUserCnt);

                Scanner scanner = new Scanner(clientSocket.getInputStream());

                if (scanner.hasNextLine()) {
                    String username = scanner.nextLine().substring(5);

                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                    clientMap.put(writer, username);
                    // 준비여부 확인
                    if (nameArr.contains(username)) {
                        playUserCnt++;
                        System.out.println(playUserCnt);
                    } else {
                        nameArr.add(username);
                    }

                    // 게임시작과 준비에 따라 문구 다르게 출력
                    if (playUserCnt == 0) {
                        broadcastMessage(username + "님이 입장했습니다.");

                    } else {
                        broadcastMessage(username + "님 준비완료.");
                    }

                    if (nameArr.size() == playUserCnt) {
                        for (int i = 3; i >= 1; i--) {
                            try {
                                Thread.sleep(1000); //1초 대기
                                broadcastMessage(i + "");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        broadcastMessage("게임시작");
                    }


                    // 각 클라이언트를 처리할 핸들러 스레드 시작
                    Thread t = new Thread(new ClientHandler(clientSocket, writer, username));
                    t.start();
                    if (nameArr.size() == playUserCnt) {
                        TimerRuning();
                    }
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

    public static void broadcastClear() {
        for (PrintWriter writer : clientMap.keySet()) {
            writer.println("clear");
            System.out.println("야야야1111");
            writer.flush();
        }
    }
    public static void broadcastTime(PrintWriter writer, String currentTime) {
            writer.println("Time : " + currentTime);
            writer.flush();;
    }
    public static void TimerRuning() {
        p_display = new Thread(() -> {
            while (p_display == Thread.currentThread()) {
                mm = t % (1000 * 60) / 100 / 60;
                ss = t % (1000 * 60) / 100 % 60;

                try {
                    Thread.sleep(10);
                    t++;
                    currentTime = String.format("%02d : %02d", mm, ss);

                    for (PrintWriter writer : clientMap.keySet()) {
                        broadcastTime(writer, currentTime);
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        p_display.start();
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

                writer.println("userCnt:" + readyUserCnt);
                writer.flush();

                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    if (message.startsWith("draw:")) {
                        processDrawingMessage(message);
                    } else if(message.equals("clear")) {
                        processClearMessage(message);
                    }else {
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

        private void processClearMessage(String message){
            broadcastClear();
        }
    }
}