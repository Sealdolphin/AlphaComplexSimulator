package alphaComplex.core.networking;

public class ParanoiaLobby {

    private final ParanoiaServer server;
    private String password;

    public ParanoiaLobby(ServerListener listener) {
        server = new ParanoiaServer();
        server.addListener(listener);
    }

    public void startServer(int port, String password) {
        this.password = password;
        server.start(port);
    }

    public boolean isOpen() {
        return server != null && server.isRunning();
    }

    public void close() {
        server.stop();
    }

}
