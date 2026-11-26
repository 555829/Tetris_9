package Network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

// 서버와의 통신 담당 클래스
// 한 Tick이 끝날 때 sendServer()로 서버에 문자열을 보내고,
// 한 Tick이 시작할 때 receiveServer()로 서버에서 문자열을 받음.
// 문자열의 생성과 해독은 NetworkHandler의 담당
public class NetworkConnection
{
    private final StringBuilder sendBuffer = new StringBuilder();
    private Session session;

    public void connect(String host, int port)
    {
        disconnect();

        session = new Session();
        session.connect(host, port);
    }

    public void disconnect()
    {
        if (session != null)
            session.close();

        session = null;
        sendBuffer.setLength(0);
    }

    public boolean isConnecting()
    { return session != null && session.isConnecting(); }

    public boolean isClosed()
    { return session != null && session.isClosed(); }
    public boolean isConnected()
    {
        return session != null && session.isConnected();
    }

    public void addSend(String message)
    {
        if (isConnected())
            sendBuffer.append(message).append('\n');
    }

    public void sendServer()
    {
        if (session == null || sendBuffer.length() == 0)
            return;

        session.addSend(sendBuffer.toString());
        sendBuffer.setLength(0);
    }

    public String receiveServer()
    {
        if (session == null)
            return "";

        return session.getReceived();
    }


    private static class Session
    {
        private final Socket socket = new Socket();

        private final StringBuilder receiveBuffer = new StringBuilder();
        private final StringBuilder sendBuffer = new StringBuilder();

        private boolean connected;
        private boolean closed;

        public void connect(String host, int port)
        {
            Thread thread = new Thread(() -> receiveLoop(host, port));
            thread.setDaemon(true);
            thread.start();
        }
        public synchronized boolean isConnecting()
        { return !connected && !closed; }
        public synchronized boolean isConnected()
        { return connected && !closed; }
        public synchronized boolean isClosed()
        { return closed; }

        private void receiveLoop(String host, int port)
        {
            try
            {
                socket.connect(new InetSocketAddress(host, port), 5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8));

                synchronized (this)
                {
                    if (closed)
                        return;

                    connected = true;
                }

                Thread sender = new Thread(this::sendLoop);
                sender.setDaemon(true);
                sender.start();

                String message;

                while ((message = reader.readLine()) != null)
                    addReceived(message);
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
            finally
            {
                close();
            }
        }

        private void sendLoop()
        {
            try
            {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream(),
                                StandardCharsets.UTF_8));

                String message;

                while ((message = getSend()) != null)
                {
                    writer.write(message);
                    writer.flush();
                }
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                close();
            }
        }


        // Receive

        private synchronized void addReceived(String message)
        {
            if (!closed)
                receiveBuffer.append(message).append('\n');
        }

        public synchronized String getReceived()
        {
            String message = receiveBuffer.toString();
            receiveBuffer.setLength(0);

            return message;
        }


        // Send

        public synchronized void addSend(String message)
        {
            if (closed)
                return;

            sendBuffer.append(message);
            notifyAll();
        }

        private synchronized String getSend()
                throws InterruptedException
        {
            while (sendBuffer.length() == 0 && !closed)
                wait();

            if (closed)
                return null;

            String message = sendBuffer.toString();
            sendBuffer.setLength(0);

            return message;
        }


        // Close

        public void close()
        {
            synchronized (this)
            {
                if (closed)
                    return;

                closed = true;
                connected = false;
                sendBuffer.setLength(0);

                notifyAll();
            }

            try{ socket.close(); }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
        }
    }
}