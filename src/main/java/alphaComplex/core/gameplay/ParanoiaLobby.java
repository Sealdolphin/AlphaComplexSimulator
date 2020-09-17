package alphaComplex.core.gameplay;

import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.core.networking.ParanoiaServer;
import alphaComplex.core.networking.ServerListener;
import paranoia.services.technical.networking.ParanoiaSocket;
import paranoia.services.technical.networking.SocketListener;

import java.util.ArrayList;
import java.util.List;

public class ParanoiaLobby implements ServerListener, SocketListener {

    private final ParanoiaLogger logger = LoggerFactory.getLogger();

    private final ParanoiaServer server;
    private ParanoiaLobbyListener frame;
    private String password;
    private final List<ParanoiaPlayer> players = new ArrayList<>();

    public ParanoiaLobby() {
        server = new ParanoiaServer();
        server.addListener(this);
    }

    public void startServer(int port, String password) {
        this.password = password;
        frame.updateServer(password, String.valueOf(port));
        frame.updateConnections(server.getSockets());
        server.start(port);
    }

    public boolean isOpen() {
        return server != null && server.isRunning();
    }

    public void close() {
        server.stop();
    }

    @Override
    public void receiveConnection(ParanoiaSocket socket) {
        //Refresh connections
        frame.updateConnections(server.getSockets());
        //Verify connection:
        socket.addListener(this);
        //Wait for response (timeout)
        new Thread(() -> verifySocket(socket)).start();
    }

    @Override
    public void readInput(String host, String message) {
        logger.info("Socket [" + host + "] says: " + message);
    }

    @Override
    public void fireTerminated() {
        logger.info("A socket has been terminated");
        server.clean();
        frame.updateConnections(server.getSockets());
    }

    private synchronized void verifySocket(ParanoiaSocket socket) {
        socket.sendMessage("Hello");
        try {
            wait(ParanoiaServer.PROTOCOL_TIMEOUT);
        } catch (InterruptedException e) {
            //Create player
            e.printStackTrace();
            return;
        }
        socket.destroy();
        server.clean();
        frame.updateConnections(server.getSockets());
    }

    public void addListener(ParanoiaLobbyListener listener) {
        frame = listener;
    }
}
