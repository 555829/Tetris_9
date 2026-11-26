package game;

import Network.NetworkConnection;
import Network.NetworkHandler;
import game.ui.MainFrame;

import javax.swing.Timer;

public class MainManager
{
    private final NetworkConnection networkConnection;
    private final NetworkHandler networkHandler;
    private final MainFrame mainFrame;

    private final Timer timer;

    public MainManager()
    {
        networkConnection = new NetworkConnection();

        mainFrame = new MainFrame(networkConnection);

        networkHandler = new NetworkHandler(
                networkConnection,
                mainFrame,
                mainFrame.getAuthPopup(),
                mainFrame.getShopPopup(),
                mainFrame.getMultiLobbyPanel(),
                mainFrame.getMultiRoomPanel(),
                mainFrame.getMultiGameScreen());

        timer = new Timer(1000 / 60, e -> tick());

        mainFrame.setVisible(true);
        timer.start();
    }

    private void tick()
    {
        networkHandler.decode(networkConnection.receiveServer());

        mainFrame.tick();

        networkConnection.sendServer();
    }
}