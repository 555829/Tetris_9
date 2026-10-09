package game;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class NetworkManager {
    private static final NetworkManager instance = new NetworkManager();
    public static NetworkManager getInstance() { return instance; }
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    public boolean isConnected = false;
    private Consumer<String> packetListener;
    private NetworkManager() {}
    //서버 주소
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 5000;

    //==================================================================================================================
    // 서버 연결
    //==================================================================================================================
    public boolean connect(String host, int port) {
        if (isConnected) {disconnect();} //연결된 소켓 있을시 정리

        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT); //서버측 아이피랑 포트 쓰는곳 "20.196.201.100" 포트: 5000 (서버 내려서접속안됨)
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            isConnected = true;
            Thread listenThread = new Thread(this::listen); //패킷 수신 , 메인스레드종료시자동종료
            listenThread.setDaemon(true);
            listenThread.start();
            return true;
        } catch (IOException e) {
            System.err.println("서버 연결실패 " + e.getMessage());
            isConnected = false;
            return false;
        }
    }

    //==================================================================================================================
    // 수동 서버연결해제
    //==================================================================================================================
    public void disconnect() {
        isConnected = false;
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("소켓 해제실패 " + e.getMessage());
        }
    }

    //==================================================================================================================
    //서버 패킷 수신
    //==================================================================================================================
    private void listen() {
        try {
            String line;
            while (isConnected && (line = reader.readLine()) != null) {
                String finalLine = line;
                SwingUtilities.invokeLater(() -> {
                    if (packetListener != null) packetListener.accept(finalLine);
                });
            }
        } catch (IOException e) {
            System.err.println("패킷수신 오류 " + e.getMessage());
            if (isConnected) { System.err.println("서버 연결해제 오류" + e.getMessage());}
        } finally {
            disconnect();
        }
    }

    //==================================================================================================================
    //서버 패킷 송신
    //==================================================================================================================
    public void send(String packet) {
        if (writer != null && isConnected) {
            writer.println(packet);
        }
    }

    //==================================================================================================================
    // 통신 패킷 전송 (|로 구분하여 패킷을 사용할수 있습니다. 전송받은 패킷을 쪼개서 사용해야함)
    //==================================================================================================================
    public void requestLogin(String id, String pw) { send("REQ_LOGIN|" + id + "|" + pw); }
    public void requestRegister(String id, String pw, String nick) { send("REQ_REGISTER|" + id + "|" + pw + "|" + nick); }
    public void requestRoomList() { send("REQ_ROOM_LIST"); }
    public void createRoom(String title, String mode) { send("REQ_CREATE_ROOM|" + title + "|" + mode); }
    public void joinRoom(int roomId) { send("REQ_JOIN_ROOM|" + roomId); }
    public void leaveRoom() { send("REQ_LEAVE_ROOM"); }
    public void changeSlotState(int slotIndex, String type) { send("REQ_SLOT_CHANGE|" + slotIndex + "|" + type); }
    public void toggleReady() { send("REQ_READY_TOGGLE"); }
    public void startGame() { send("REQ_START_GAME"); }
    public void sendChat(String scope, String msg) { send("REQ_CHAT|" + scope + "|" + msg); }
    public void sendBoardSync(String boardData) { send("REQ_BOARD_SYNC|" + boardData); }
}