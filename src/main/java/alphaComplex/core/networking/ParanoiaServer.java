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

    private ServerSocket server;
    private final List<TroubleShooterClient> troubleShooters = new ArrayList<>();

    private boolean status = false;
    private String password;
    private final List<ServerListener> listeners = new ArrayList<>();
    private final Thread thConnection = createConnectionThread();
    private final Object acceptLock = new Object();

    private final ParanoiaLogger logger = LoggerFactory.getLogger();

    public void start(int port, String password) throws IOException {
        if(status) throw new IOException("Server is already running");
        this.password = password;
        server = new ServerSocket(port);
        status = true;
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
            if(status) logger.exception(e);
            return null;
        }
    }

    public String getStatus() {
        return status ? "ONLINE" : "OFFLINE";
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
        while (status) {
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
                TroubleShooterClient clone = new TroubleShooterClient(newClient, id);
                troubleShooters.add(clone);
                listeners.forEach(l -> l.serverPropertyChanged(ServerProperty.PLAYERS));
                logger.info("New connection has been established");
                logger.info(clone.getInfo());
            } catch (IOException | InterruptedException e) {
                logger.exception(e);
            }
        }
    }

    public void close() {
        if(!status) return;
        logger.info("Closing server");
        try {
            status = false;
            troubleShooters.forEach(TroubleShooterClient::disconnect);
            synchronized (acceptLock) { acceptLock.notify(); }
            thConnection.join();
            server.close();
        } catch (IOException | InterruptedException e) {
            logger.exception(e);
        }
        logger.info("Server closed");
    }

    public TroubleShooterList createTroubleShooterList() {
        return new TroubleShooterList(troubleShooters);
    }
}
