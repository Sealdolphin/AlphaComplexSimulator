package alphaComplex.core.gameplay;

import alphaComplex.core.networking.ParanoiaServer;
import alphaComplex.core.networking.ServerListener;
import paranoia.services.technical.networking.ParanoiaSocket;

public class ParanoiaLobby implements ServerListener {

    private final ParanoiaServer server;
    private ParanoiaLobbyListener frame;
    private String password;

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
        //Verify connection
    }

    public void addListener(ParanoiaLobbyListener listener) {
        frame = listener;
    }
}
