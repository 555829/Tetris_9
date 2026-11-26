package Network;

import game.ui.MainFrame;
import game.ui.outgame.MultiLobbyPanel;
import game.ui.outgame.MultiRoomPanel;
import game.ui.outgame.popup.AuthPopup;
import game.ui.outgame.popup.ShopPopup;
import game.ui.multigame.MultiGameScreen;

import java.util.Arrays;
import game.UserProfileState;

// NetworkConnection에 문자열을 보내고, 받은 문자열을 해독하는 클래스.
// 다른 클래스에서 request~() 메소드를 호출하는 것으로 서버에 송신할 수 있음.
// 해독은 decode() 메소드로 한 번에 처리. 여기서 다른 클래스의 메소드를 호출.
public class NetworkHandler
{

    private static NetworkHandler instance;

    public static NetworkHandler getInstance()
    { return instance; }

    private final NetworkConnection network;
    private final MainFrame mainFrame;
    private final AuthPopup authPopup;
    private final ShopPopup shopPopup;
    private final MultiLobbyPanel multiLobbyPanel;
    private final MultiRoomPanel multiRoomPanel;
    private final MultiGameScreen multiGameScreen;

    public NetworkHandler(
            NetworkConnection network,
            MainFrame mainFrame,
            AuthPopup authPopup,
            ShopPopup shopPopup,
            MultiLobbyPanel multiLobbyPanel,
            MultiRoomPanel multiRoomPanel,
            MultiGameScreen multiGameScreen)
    {
        this.network = network;
        this.mainFrame = mainFrame;
        this.authPopup = authPopup;
        this.shopPopup = shopPopup;
        this.multiLobbyPanel = multiLobbyPanel;
        this.multiRoomPanel = multiRoomPanel;
        this.multiGameScreen = multiGameScreen;
        instance = this;
    }


    // ==================== Request: Account ====================

    public void requestLogin(String id, String pw)
    { network.addSend("REQ_LOGIN|" + id + "|" + pw); }

    public void requestUserInfo()
    { network.addSend("REQ_USER_INFO"); }

    public void requestRegister(String id, String pw, String nick)
    { network.addSend("REQ_REGISTER|" + id + "|" + pw + "|" + nick); }

    public void requestChangeNick(String nick)
    { network.addSend("REQ_CHANGE_NICK|" + nick); }

    public void requestProfilePic(String base64Image)
    { network.addSend("REQ_PIC_PROFILE|" + base64Image); }


    // ==================== Request: Room ====================

    public void requestRoomList()
    { network.addSend("REQ_ROOM_LIST"); }

    public void requestCreateRoom(String title, String mode)
    { network.addSend("REQ_CREATE_ROOM|" + title + "|" + mode); }

    public void requestJoinRoom(int roomId)
    { network.addSend("REQ_JOIN_ROOM|" + roomId); }

    public void requestLeaveRoom()
    { network.addSend("REQ_LEAVE_ROOM"); }

    public void requestChangeSlotState(int slotIndex, String type)
    { network.addSend("REQ_SLOT_CHANGE|" + slotIndex + "|" + type); }

    public void requestToggleReady()
    { network.addSend("REQ_TOGGLE_READY"); }

    public void requestStartGame()
    { network.addSend("REQ_START_GAME"); }

    public void requestAddBot(int slot)
    { network.addSend("REQ_ADD_BOT|" + slot); }

    public void requestRemoveBot(int slot)
    { network.addSend("REQ_REMOVE_BOT|" + slot); }

    public void requestSetSlotLocked(int slot, boolean locked)
    { network.addSend("REQ_SET_SLOT_LOCKED|" + slot + "|" + locked); }

    public void requestChangeRoomTitle(String title)
    { network.addSend("REQ_CHANGE_TITLE|" + title); }

    public void requestChangeRoomSettings(String mode)
    { network.addSend("REQ_CHANGE_SETTINGS|" + mode); }


    // ==================== Request: Chat ====================

    public void requestChat(String scope, String msg)
    { network.addSend("REQ_CHAT|" + scope + "|" + msg); }


    // ==================== Request: Game ====================

    public void requestBoardSync(int slot, String boardData)
    { network.addSend("REQ_BOARD_SYNC|" + slot + "|" + boardData); }

    public void requestPlayerStats(int slot, int score, int lines)
    { network.addSend("REQ_PLAYER_STATS|" + slot + "|" + score + "|" + lines); }

    public void requestGameOver(int slot, int score, int lines)
    { network.addSend("REQ_GAME_OVER|" + slot + "|" + score + "|" + lines); }

    public void requestMatchWin(int slot, int score, int lines)
    { network.addSend("REQ_MATCH_WIN|" + slot + "|" + score + "|" + lines); }

    public void requestTimeResult(int slot, int score, int lines)
    { network.addSend("REQ_TIME_RESULT|" + slot + "|" + score + "|" + lines); }


    // ==================== Request: Card ====================

    public void requestBuyCard(String buy)
    { network.addSend("REQ_BUY_CARD|" + buy); }

    public void requestEquipCard(String equip)
    { network.addSend("REQ_EQUIP_CARD|" + equip); }


    // ==================== Debug ====================

    public void sendRaw(String message)
    { network.addSend(message); }


    // ==================== Decode ====================

    public void decode(String received)
    {
        if (received == null || received.isEmpty())
            return;

        String[] packets = received.split("\n");

        for (String packet : packets)
        {
            if (packet.isBlank())
                continue;

            String[] tokens = packet.split("\\|", -1);

            try
            {
                switch (tokens[0])
                {
                    // Account
                    case "RES_LOGIN"       -> decodeLogin(tokens);
                    case "RES_USER_INFO"   -> decodeUserInfo(tokens);
                    case "RES_REGISTER"    -> decodeRegister(tokens);
                    case "RES_CHANGE_NICK" -> decodeChangeNick(tokens);
                    case "RES_PIC_PROFILE" -> decodeProfilePic(tokens);

                    // Room
                    case "RES_ROOM_LIST"    -> decodeRoomList(tokens);
                    case "RES_CREATE_ROOM"  -> decodeCreateRoom(tokens);
                    case "RES_JOIN_ROOM"    -> decodeJoinRoom(tokens);
                    case "RES_ROOM_INFO"    -> decodeRoomInfo(tokens);
                    case "RES_READY_TOGGLE" -> decodeReadyToggle(tokens);
                    case "RES_START_GAME"   -> decodeStartGame();
                    case "RES_ROOM_CLOSED"  -> decodeRoomClosed();

                    // Chat
                    case "RES_CHAT" -> decodeChat(packet);

                    // Game
                    case "RES_BOARD_SYNC"   -> decodeBoardSync(tokens);
                    case "RES_PLAYER_STATS" -> decodePlayerStats(tokens);
                    case "RES_GAME_OVER"    -> decodeGameOver(tokens);
                    case "RES_MATCH_RESULT" -> decodeMatchResult(packet);

                    // Card
                    case "RES_BUY_CARD"   -> decodeBuyCard(tokens);
                    case "RES_EQUIP_CARD" -> decodeEquipCard(tokens);

                    default -> System.err.println(
                            "Unknown packet: " + tokens[0]);
                }
            }
            catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
            {
                System.err.println("Packet processing failed: " + tokens[0]);
                e.printStackTrace();
            }
        }
    }


    // ==================== Decode: Account ====================

    // RES_LOGIN|isSuccess|nickname|failReason
    private void decodeLogin(String[] t)
    { mainFrame.onLogin(t[1], t[2], t[3]); }

    private void decodeUserInfo(String[] t)
    { mainFrame.onUserInfo(UserProfileState.fromUserInfoPacket(t)); }

    // RES_REGISTER|isSuccess|failReason
    private void decodeRegister(String[] t)
    { authPopup.onRegister(t[1], t[2]); }

    // RES_CHANGE_NICK|isSuccess|nickname|failReason
    private void decodeChangeNick(String[] t)
    { mainFrame.onChangeNick(t[1], t[2], t[3]); }

    // RES_PIC_PROFILE|isSuccess|base64Image|failReason
    private void decodeProfilePic(String[] t)
    { mainFrame.onProfilePic(t[1], t[2], t[3]); }


    // ==================== Decode: Room ====================

    private void decodeRoomList(String[] t)
    { multiLobbyPanel.updateRoomList(Arrays.copyOfRange(t, 1, t.length)); }

    // RES_CREATE_ROOM|isSuccess|roomId|failReason
    private void decodeCreateRoom(String[] t)
    {
        multiRoomPanel.onCreateRoom(
                t[1], Integer.parseInt(t[2]), t[3]);
    }

    // RES_JOIN_ROOM|isSuccess|roomId|slotIndex|failReason
    private void decodeJoinRoom(String[] t)
    {
        multiRoomPanel.onJoinRoom(
                t[1], Integer.parseInt(t[2]),
                Integer.parseInt(t[3]), t[4]);
    }

    private void decodeRoomInfo(String[] t)
    { multiRoomPanel.updateRoomInfo(t); }

    private void decodeReadyToggle(String[] t)
    {
        multiRoomPanel.onReadyToggle(
                Integer.parseInt(t[1]), Boolean.parseBoolean(t[2]));
    }

    private void decodeStartGame()
    { multiRoomPanel.onStartGame(); }

    private void decodeRoomClosed()
    { mainFrame.onRoomClosed(); }


    // ==================== Decode: Chat ====================
    private void decodeChat(String packet)
    {
        String[] t = packet.split("\\|", 3);
        mainFrame.onChat(t[1], t[2]);
    }

    // ==================== Decode: Game ====================

    private void decodeBoardSync(String[] t)
    { multiGameScreen.onUpdateRemoteBoard(Integer.parseInt(t[1]), t[2]); }

    private void decodePlayerStats(String[] t)
    {
        multiGameScreen.onUpdatePlayerStats(
                Integer.parseInt(t[1]),
                Integer.parseInt(t[2]),
                Integer.parseInt(t[3]));
    }

    private void decodeGameOver(String[] t)
    { multiGameScreen.onGameOver(Integer.parseInt(t[1])); }

    private void decodeMatchResult(String packet)
    { multiGameScreen.onMatchResult(packet); }


    // ==================== Decode: Card ====================

    private void decodeBuyCard(String[] t)
    { shopPopup.onBuyCard(t[1]); }

    private void decodeEquipCard(String[] t)
    { shopPopup.onEquipCard(t[1]); }
}