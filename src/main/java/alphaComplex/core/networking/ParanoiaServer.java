package alphaComplex.core.networking;

import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import paranoia.services.technical.networking.ParanoiaSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the Server side. The server uses REST API to communicate with the clients.
 * The main unit of communication is the socket. The server verifies every packet it receives, and
 * drops all the invalid packets.
 */
public class ParanoiaServer {

    /**
     * The time defined to keep a socket connection alive without messages in milliseconds.
     */
    public static final int PROTOCOL_TIMEOUT = 10000;

    /**
     * Default logger class
     */
    private final ParanoiaLogger logger = LoggerFactory.getLogger();

    /**
     * The server socket. It handles the different connections.
     */
    private ServerSocket server;

    private final List<ServerListener> listeners = new ArrayList<>();

    /**
     * The connected sockets. Not all socket can be elevated to client!
     */
    private final List<ParanoiaSocket> paranoiaSockets = new ArrayList<>();

    private Thread listeningThread;

    private boolean running = false;

    /**
     * Starts the server with a port number
     * @param port the port the server is running on
     */
    public void start(int port) {
        if(running) return;
        logger.info("Starting paranoia server on port " + port);
        running = true;
        try {
            server = new ServerSocket(port);
            listeningThread = new Thread(this::acceptSockets);
            listeningThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            logger.exception(e);
            logger.error("Could not start server!");
        }

    }

    private void acceptSockets() {
        while(running) {
            try {
                Socket clientSocket = server.accept();
                addSocket(clientSocket);
                logger.info("Client accepted with IP = " + clientSocket.getLocalAddress());
            } catch (SocketException socket) {
                if(server.isClosed())
                    logger.warning("Server socket has been closed");
                else logger.exception(socket);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("An error happened during listening!");
                running = false;
            }
        }
    }

    private synchronized void addSocket(Socket socket) throws IOException {
        ParanoiaSocket pSocket = new ParanoiaSocket(socket);
        paranoiaSockets.add(pSocket);
        paranoiaSockets.removeIf(s -> !s.isOpen());
        if(pSocket.isOpen())
            listeners.forEach(l -> l.receiveConnection(pSocket));
    }

    public int getSockets() {
        return paranoiaSockets.size();
    }

    public void addListener(ServerListener listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("Unused")
    public void removeListener(ServerListener listener) {
        listeners.remove(listener);
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
        if(listeningThread != null){
            try {
                server.close();
                listeningThread.join();
                paranoiaSockets.clear();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
