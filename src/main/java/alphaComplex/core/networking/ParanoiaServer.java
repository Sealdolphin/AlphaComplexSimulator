package alphaComplex.core.networking;

import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.visuals.TroubleShooterList;
import paranoia.services.technical.HelperThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ParanoiaServer {

    public static final int PROTOCOL_TIMEOUT = 10000;

    private ServerSocket server;
    private final List<TroubleShooterClient> troubleShooters = new ArrayList<>();
    private final List<ServerListener> listeners = new ArrayList<>();

    private boolean opened = false;
    private String password;
    private Thread thConnection = createConnectionThread();
    private final Object acceptLock = new Object();

    private final ParanoiaLogger logger = LoggerFactory.getLogger();

    public void start(int port, String password) throws IOException {
        if(opened) throw new IOException("Server is already running");
        this.password = password;
        server = new ServerSocket(port);
        opened = true;
        listeners.forEach(l -> l.serverPropertyChanged(ServerProperty.STATUS));
        listeners.forEach(l -> l.serverPropertyChanged(ServerProperty.PORT));
        listeners.forEach(l -> l.serverPropertyChanged(ServerProperty.PASSWORD));
        logger.info("Server started on port " + getPort() + " password: " + password);
        logger.info("Server is listening for connections");
        thConnection.start();
    }

    public void addListener(ServerListener listener) {
        listeners.add(listener);
    }

    private Socket createLink() {
        try {
            return server.accept();
        } catch (IOException e) {
            if(opened) logger.exception(e);
            return null;
        }
    }

    public boolean isOpen() {
        return opened;
    }

    public int getPort() {
        if(server == null) return -1;
        return server.getLocalPort();
    }

    public int getPlayers() {
        return (int) troubleShooters.stream().filter(TroubleShooterClient::isConnected).count();
    }

    public String getPassword() {
        return password;
    }

    private Thread createConnectionThread() {
        return new Thread(this::listenToConnections);
    }

    private synchronized void listenToConnections() {
        while (opened) {
            try {
                HelperThread<Socket> socketCreator = new HelperThread<>(v -> createLink(), acceptLock);
                socketCreator.start();
                synchronized (acceptLock) { acceptLock.wait(); }
                Socket newClient = socketCreator.getValue();
                if(newClient == null) {
                    logger.warning("Server failed to accept connection");
                    continue;
                }
                int id = troubleShooters.size();
                TroubleShooterClient clone = createTrouleShooterClient(newClient, id);
                troubleShooters.add(clone);
                updatePlayerNumber();
                logger.info("New connection has been established");
                logger.info(clone.getInfo());
                //Send auth request - start timer
                clone.sendAuthRequest(password);
            } catch (IOException | InterruptedException e) {
                logger.exception(e);
            }
        }
    }

    private TroubleShooterClient createTrouleShooterClient(
        Socket newClient, int id
    ) throws IOException, InterruptedException {
        //Initiating Paranoia protocol:
        //Creating socket client
        TroubleShooterClient clone = new TroubleShooterClient(newClient, id, this);
        //Send basic ping command
        if(!clone.ping()){
            throw new IOException("Socket does not follow Paranoia Protocol within timeout");
        }
        //Awaiting response
        return clone;
    }

    public void close() {
        if(!opened) return;
        logger.info("Closing server");
        try {
            opened = false;
            troubleShooters.forEach(TroubleShooterClient::disconnect);
            troubleShooters.clear();
            synchronized (acceptLock) { acceptLock.notify(); }
            thConnection.join();
            thConnection = createConnectionThread();
            server.close();
        } catch (IOException | InterruptedException e) {
            logger.exception(e);
        }
        logger.info("Server closed");
        listeners.forEach(l -> l.serverPropertyChanged(ServerProperty.STATUS));
        updatePlayerNumber();
    }

    public boolean authenticate(int id, String pass) {
        if(password != null && !password.isEmpty()){
            try {
                if(pass == null || !pass.equals(password)){
                    deletePlayer(id);
                    return false;
                }
            } catch (Exception e) {
                logger.exception(e);
                deletePlayer(id);
                return false;
            }
        }
        return true;
    }

    public void deletePlayer(int id) {
        TroubleShooterClient client = troubleShooters.get(id);
        if(client != null) {
            client.disconnect();
            troubleShooters.remove(id);
            updatePlayerNumber();
            logger.info("Player (" + id + ") has been deleted");
        }
    }

    public TroubleShooterList createTroubleShooterList() {
        return new TroubleShooterList(troubleShooters);
    }

    public void updatePlayerNumber() {
        listeners.forEach(l -> l.serverPropertyChanged(ServerProperty.PLAYERS));
    }
}
