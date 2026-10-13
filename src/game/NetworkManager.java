package game;

import javax.swing.*;
import game.profile.ProfileImageCodec;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

//======================================================================================================================
// 네트워크 담당하는 부분은 여기에 싹 다 몰아넣었습니다.
//======================================================================================================================
public class NetworkManager {
    private static final NetworkManager instance = new NetworkManager();
    public static NetworkManager getInstance() { return instance; }

    private static final boolean DEBUG = false;

    private volatile Socket socket;
    private volatile BufferedReader reader;
    private volatile PrintWriter writer;
    public volatile boolean isConnected = false;

    private volatile UserProfileState profile = UserProfileState.empty();

    private final CopyOnWriteArrayList<Consumer<String>> packetListeners = new CopyOnWriteArrayList<>();

    private NetworkManager() {}

    public void addPacketListener(Consumer<String> listener) {
        if (listener != null) packetListeners.addIfAbsent(listener);
    }

    public void removePacketListener(Consumer<String> listener) {
        if (listener != null) {
            packetListeners.remove(listener);
        }
    }

    public String getMyNickname() { return profile.getNickname(); }
    public void setMyNickname(String nickname) { profile = profile.withNickname(nickname == null ? "" : nickname); }
    public int getMoney() { return profile.getMoney(); }
    public int getPlayCount() { return profile.getPlayCount(); }
    public String getCreatedAt() { return profile.getCreatedAt(); }
    public String getEquippedSkin() { return profile.getEquippedSkin(); }
    public boolean hasWhiteCard() { return profile.hasWhiteCard(); }
    public boolean hasSilverCard() { return profile.hasSilverCard(); }
    public boolean hasGoldCard() { return profile.hasGoldCard(); }
    public boolean hasPlatinumCard() { return profile.hasPlatinumCard(); }

    public ImageIcon getProfilePic() {
        byte[] image = profile.getProfileImage();
        if (image != null && image.length > 0) {
            return new ImageIcon(image);
        }
        return null;
    }

    public static final String SERVER_HOST = configuredServerHost();
    public static final int SERVER_PORT = configuredServerPort();

    private static String configuredServerHost() {
        String host = System.getProperty("tetris.server.host");
        if (host == null || host.trim().isEmpty()) host = System.getenv("TETRIS_SERVER_HOST");
        return host == null || host.trim().isEmpty() ? "20.196.201.100" : host.trim();
    }

    private static int configuredServerPort() {
        String portValue = System.getProperty("tetris.server.port");
        if (portValue == null || portValue.trim().isEmpty()) portValue = System.getenv("TETRIS_SERVER_PORT");
        if (portValue == null || portValue.trim().isEmpty()) return 5000;
        try {
            int port = Integer.parseInt(portValue.trim());
            return port >= 1 && port <= 65535 ? port : 5000;
        } catch (NumberFormatException ignored) {
            return 5000;
        }
    }

    //서버 연결
    public synchronized boolean connect(String host, int port) {
        if (isConnected) { disconnect(); }
        log("서버 연결 시도중... (" + host + ":" + port + ")");
        Socket newSocket = new Socket();
        try {
            newSocket.connect(new InetSocketAddress(host, port), 5000);

            BufferedReader newReader = new BufferedReader(new InputStreamReader(newSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter newWriter = new PrintWriter(new OutputStreamWriter(newSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            socket = newSocket;
            reader = newReader;
            writer = newWriter;
            isConnected = true;
            log("서버 연결 성공!");

            Thread listenThread = new Thread(() -> listen(newReader, newSocket), "tetris-network-listener");
            listenThread.setDaemon(true);
            // 화면을 멈추지 않도록 수신은 별도 스레드에서 계속 읽습니다.
            listenThread.start();
            return true;
        } catch (IOException e) {
            logError("서버 연결 실패: " + e.getMessage());
            try { newSocket.close(); } catch (IOException ignored) {}
            isConnected = false;
            return false;
        }
    }

    //서버 닫기
    public synchronized void disconnect() {
        if (isConnected) {
            log("서버 연결 해제 진행중...");
        }
        isConnected = false;
        profile = UserProfileState.empty();
        Socket oldSocket = socket;
        BufferedReader oldReader = reader;
        PrintWriter oldWriter = writer;
        socket = null;
        reader = null;
        writer = null;
        try {
            if (oldSocket != null && !oldSocket.isClosed()) oldSocket.close();
        } catch (IOException e) { logError("소켓 해제 실패: " + e.getMessage()); }
        if (oldWriter != null) oldWriter.close();
        if (oldReader != null) {
            try { oldReader.close(); }
            catch (IOException e) { logError("입력 스트림 해제 실패: " + e.getMessage()); }
        }
        log("소켓 자원 해제 완료");
    }

    /** 연결 세대가 바뀐 뒤 도착한 이전 소켓 응답은 무시하며 현재 연결만 읽습니다. */
    // 이해는 했는데 저보고 짜라고 했으면 못 짯겟네요 이런건
    private void listen(BufferedReader listenReader, Socket listenSocket) {
        try {
            String line;
            while (isConnected && socket == listenSocket && (line = listenReader.readLine()) != null) {
                String finalLine = line;
                log("[RECV] " + finalLine);
                processInternalPacket(finalLine);

                // Swing 컴포넌트를 건드리는 리스너는 이벤트 디스패치 스레드에서 실행합니다.
                SwingUtilities.invokeLater(() -> {
                    for (Consumer<String> listener : packetListeners) {
                        try {
                            listener.accept(finalLine);
                        } catch (Exception e) {
                            logError("리스너 처리 중 오류: " + e.getMessage());
                        }
                    }
                });
            }
        } catch (IOException e) {
            if (isConnected && socket == listenSocket) {
                logError("패킷 수신 오류 (소켓 끊김): " + e.getMessage());
            }
        } finally {
            if (socket == listenSocket) {
                disconnect();
            } else {
                try { listenReader.close(); } catch (IOException ignored) {}
            }
        }
    }

    //데이터 값 불러오는것보다 ui 먼저 띄우는거 막는 코드
    private void processInternalPacket(String packet) {
        if (packet == null || packet.trim().isEmpty()) return;

        String[] tokens = packet.split("\\|", -1);
        String cmd = tokens[0];

        try {
            if (tokens.length >= 3 && "RES_LOGIN".equals(cmd) && "SUCCESS".equals(tokens[1])) {
                setMyNickname(tokens[2]);
                log("로그인 성공! (닉네임: " + getMyNickname() + ")");
                requestUserInfo();
            } else if (tokens.length >= 3 && "RES_CHANGE_NICK".equals(cmd)
                    && "SUCCESS".equals(tokens[1])) {
                setMyNickname(tokens[2]);
            } else if (tokens.length >= 3 && "RES_PIC_PROFILE".equals(cmd)
                    && "SUCCESS".equals(tokens[1])) {
                try {
                    byte[] image = ProfileImageCodec.decodeFromServer(tokens[2]);
                    profile = profile.withProfileImage(image);
                } catch (IllegalArgumentException | IOException e) {
                    logError("프로필 이미지 응답을 읽을 수 없습니다.");
                }
            } else if ("RES_USER_INFO".equals(cmd)) {
                UserProfileState updatedProfile = UserProfileState.fromUserInfoPacket(tokens);
                if (updatedProfile != null) profile = updatedProfile;
            }
        } catch (Exception e) {
            logError("패킷 파싱 중 오류 발생 (" + cmd + "): " + e.getMessage());
        }
    }

    //패킷 전송하기
    public synchronized void send(String packet) {
        if (writer != null && isConnected) {
            log("[SEND] " + safePacketForLog(packet));
            writer.println(packet);
            if (writer.checkError()) {
                logError("패킷 전송 실패: 연결을 종료합니다.");
                disconnect();
            }
        } else {
            logError("패킷 전송 실패 (서버 미연결 상태): " + safePacketForLog(packet));
        }
    }

    // 패킷 보내는 메서드들
    public void requestLogin(String id, String pw) { send("REQ_LOGIN|" + id + "|" + pw); }
    public void requestUserInfo() { send("REQ_USER_INFO"); }
    public void requestRegister(String id, String pw, String nick) { send("REQ_REGISTER|" + id + "|" + pw + "|" + nick); }
    public void requestLogout() { disconnect(); }
    public void requestChangeNick(String nick) { send("REQ_CHANGE_NICK|" + nick ); }
    public void requestProfilePic(String base64Image) { send("REQ_PIC_PROFILE|" + base64Image); }
    public void requestRoomList() { send("REQ_ROOM_LIST"); }
    public void createRoom(String title, String mode) { send("REQ_CREATE_ROOM|" + title + "|" + mode); }
    public void joinRoom(int roomId) { send("REQ_JOIN_ROOM|" + roomId); }
    public void leaveRoom() { send("REQ_LEAVE_ROOM"); }
    public void changeSlotState(int slotIndex, String type) { send("REQ_SLOT_CHANGE|" + slotIndex + "|" + type); }
    public void toggleReady() { send("REQ_TOGGLE_READY"); }
    public void startGame() { send("REQ_START_GAME"); }
    public void sendChat(String scope, String msg) { send("REQ_CHAT|" + scope + "|" + msg); }
    public void sendBoardSync(int slot, String boardData) { send("REQ_BOARD_SYNC|" + slot + "|" + boardData); }
    public void addBot(int slot) { send("REQ_ADD_BOT|" + slot); }
    public void removeBot(int slot) { send("REQ_REMOVE_BOT|" + slot); }
    public void setSlotLocked(int slot, boolean locked) { send("REQ_SET_SLOT_LOCKED|" + slot + "|" + locked); }
    public void changeRoomTitle(String title) { send("REQ_CHANGE_TITLE|" + title); }
    public void changeRoomSettings(String mode) { send("REQ_CHANGE_SETTINGS|" + mode); }
    public void reportPlayerStats(int slot, int score, int lines) {
        send("REQ_PLAYER_STATS|" + slot + "|" + score + "|" + lines);
    }
    public void reportGameOver(int slot, int score, int lines) {
        send("REQ_GAME_OVER|" + slot + "|" + score + "|" + lines);
    }
    public void sendAttackLines(int slot, int lines) { send("REQ_ATTACK_LINES|" + slot + "|" + lines); }
    public void reportMatchWin(int slot, int score, int lines) {
        send("REQ_MATCH_WIN|" + slot + "|" + score + "|" + lines);
    }
    public void reportTimeResult(int slot, int score, int lines) {
        send("REQ_TIME_RESULT|" + slot + "|" + score + "|" + lines);
    }
    public void requestBuyCard(String buy) { send("REQ_BUY_CARD|" + buy); }
    public void requestEquipCard(String equip) { send("REQ_EQUIP_CARD|" + equip); }

    private static String safePacketForLog(String packet) {
        if (packet == null) return "[NULL PACKET]";
        int separator = packet.indexOf('|');
        String command = separator < 0 ? packet : packet.substring(0, separator);
        if ("REQ_LOGIN".equals(command) || "REQ_REGISTER".equals(command)
                || "REQ_PIC_PROFILE".equals(command)) {
            return command + "|[REDACTED]";
        }
        return packet;
    }

    private void log(String msg) {
        if (DEBUG) System.out.println("[NetworkManager] " + msg);
    }
    private void logError(String msg) {
        System.err.println("[NetworkManager ERROR] " + msg);
    }
}