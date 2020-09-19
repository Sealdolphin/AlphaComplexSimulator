package alphaComplex.core.gameplay;

import alphaComplex.core.PlayerListener;
import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.core.networking.ParanoiaServer;
import alphaComplex.core.networking.PlayerStatus;
import alphaComplex.core.networking.ServerListener;
import paranoia.services.technical.command.ParanoiaCommand;
import paranoia.services.technical.networking.ParanoiaSocket;
import paranoia.services.technical.networking.SocketListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParanoiaLobby implements ServerListener, SocketListener, PlayerListener {

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
        new Thread(() -> {
            ParanoiaPlayer player = new ParanoiaPlayer(socket);
            player.addListener(this);
        }).start();
    }

    @Override
    public void readInput(byte[] message) {
        try {
            ParanoiaCommand parsedCommand = ParanoiaCommand.parseCommand(message);
            logger.info("Socket [" + parsedCommand.getHost() + "] sent a " + parsedCommand.getType() + " command");
            //Parse command!
        } catch (IOException e) {
            logger.error("An error happened during reading");
            logger.exception(e);
        } catch (ClassNotFoundException e) {
            logger.error("Invalid operation");
            logger.exception(e);
        }
    }

    @Override
    public void fireTerminated() {
        logger.info("A socket has been terminated");
        server.clean();
        frame.updateConnections(server.getSockets());
    }

    @Override
    public void statusChanged(PlayerStatus status) {
        logger.info("A player status has changed to " + status);
    }

    public void addListener(ParanoiaLobbyListener listener) {
        frame = listener;
    }
}
