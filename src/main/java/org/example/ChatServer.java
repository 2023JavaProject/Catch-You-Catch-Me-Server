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
    private static ArrayList<String> playUserName = new ArrayList<>();
    private static int currentTimeInSeconds = 0;
    static String[] topics = "퇴학,우거지,피고인,핵가족,연장전,포크레인,바이오리듬,삼국시대,시험관아기,풍년,새우젓,프라이드 치킨,열매,소방관,전사자,태양,카레이서,개인기,가로수,사시나무,쥐불놀이,가격표,공중전화,불똥,양반,양팔,잠수,초등학교,철종경기,코너킥,티눈,귓속말,백수,원빈,줄다리기,토양,초음파검사,창조물,창업자,작은북,중고생,손맛,강아지,고양이,사과,로딩중,개발,커피,핫식스,산,제주,부산,교실,학생,충전기,취업,체육복,교복,단추,주석,의자,사표,과장,회식,소화기,교탁,교장,똥,박세연,마우스,섹시,컴퓨터,핸드폰,필기,연필,머리카락,인스타,탕후루,매운탕,마라탕,힐리스,에어팟,버즈,난방,에어컨,이어폰,손소독제,장구,꽹과리,오카리나,피리,제니,지수,리사,로제,카리나,윈터,겨울,낙엽,짱".split(",");
    //타이머 변수
    private static int mm;
    private static int ss;
    private static int ms;
    private static int t = 0;
    private static String currentTime;
    private static Thread p_display;

    private static int rightCnt = 0;


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
                        SendName(username);
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
                        sendRepaint();
                    }


                    // 각 클라이언트를 처리할 핸들러 스레드 시작
                    Thread t = new Thread(new ClientHandler(clientSocket, writer, username));
                    t.start();
                    if (nameArr.size() == playUserCnt) {
                        TimerRuning();
                        setTopic();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void SendName(String username) {
        for (PrintWriter writer : clientMap.keySet()) {
            broadcastName(writer, username);
        }
    }

    private static void sendRepaint(){
        for (PrintWriter writer : clientMap.keySet()) {
            broadcastRepaint(writer);
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

    public static void broadcastName(PrintWriter writer, String username){
        if(!playUserName.contains(username))
            playUserName.add(username);
        writer.println("userName : " + playUserName);
        //writer.flush();
    }


    public static void broadcastClear() {
        for (PrintWriter writer : clientMap.keySet()) {
            writer.println("clear");
            writer.flush();
        }
    }

    public static void broadcastTime(PrintWriter writer, String currentTime) {
        writer.println("Time : " + currentTime);
        writer.flush();;
    }
    public static void broadcastTopic(PrintWriter writer, String topics){
        writer.println("Topic : " + topics);
        writer.flush();
    }

    public static void broadcastRepaint(PrintWriter writer){
        writer.println("repaint");
        writer.flush();
    }

    public static void broadcastRight(PrintWriter writer, int rightCnt){
        writer.println("right : " + rightCnt);
        writer.flush();
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

    public static void setTopic(){
        int rowCount = topics.length;

        double random = Math.random();
        int randomValue = (int) (random * rowCount + 1);
        for (PrintWriter writer : clientMap.keySet()) {
            broadcastTopic(writer, topics[randomValue]);
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

                writer.println("userCnt:" + readyUserCnt);
                writer.flush();

                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    if (message.startsWith("draw:")) {
                        processDrawingMessage(message);
                    } else if(message.equals("clear")){
                        processClearMessage();
                    } else if(message.startsWith("exit")){
                        processExit(message);
                    } else if(message.startsWith("topic")){
                        setTopic();
                    } else if(message.equals("right")){
                        rightCnt++;
                        processRight(rightCnt);

                    }
                    else {
                        broadcastMessage(username, message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 나간 유저를 맵에서 제거, 안내문구 출력
                //clientMap.remove(writer);
                //broadcastMessage(username+"님이 나가셨습니다.");
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

        private void processClearMessage(){
            broadcastClear();
        }

        private void processRight(int rightCnt){
            for (PrintWriter writer : clientMap.keySet()) {
                broadcastRight(writer, rightCnt);
                broadcastRepaint(writer);
            }
        }

        private void processExit(String message){
            String finalMessage = message.substring(8);
            clientMap.remove(writer);
            broadcastMessage(username+"님이 나가셨습니다.");;
            nameArr.removeIf(element -> element.contains(finalMessage));
            readyUserCnt --;
        }
    }
}