package alphaComplex.core.gameplay;

import alphaComplex.core.PlayerListener;
import alphaComplex.core.networking.PlayerStatus;
import daiv.networking.ParanoiaSocket;
import daiv.networking.command.AuthResponse;
import daiv.networking.command.ParanoiaCommand;
import daiv.networking.command.PingCommand;

import java.util.ArrayList;
import java.util.List;

import static alphaComplex.core.networking.ParanoiaServer.PROTOCOL_TIMEOUT;

public class ParanoiaPlayer {

    private final ParanoiaSocket connection;

    private String name = "";

    private PlayerStatus status = PlayerStatus.INVALID;

    private final Object verifyLock = new Object();

    private final List<PlayerListener> listeners = new ArrayList<>();

    private AuthResponse.ParanoiaAuthListener authListener;

    public ParanoiaPlayer(ParanoiaSocket socket) {
        connection = socket;
        //Verify connection?
        synchronized (verifyLock) {
            socket.sendMessage(new PingCommand().toNetworkMessage(socket.getAddress()));
            try {
                wait(PROTOCOL_TIMEOUT);
                if(status.equals(PlayerStatus.INVALID))
                    socket.destroy();
            } catch (InterruptedException e) {
                e.printStackTrace();
                socket.destroy();
            }
        }
    }

    public void parseCommand(ParanoiaCommand command) {
        switch (command.getType()) {
            case AUTH:
                changeStatus(PlayerStatus.AUTHENTICATING);
                name = ((AuthResponse) command).getPlayerName();
                AuthResponse.create(command, authListener).execute();
                break;
            case PING:
                changeStatus(PlayerStatus.READY);
                break;
            default:
                break;
        }
    }

    public String getHost() {
        return connection.getAddress();
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    public void setAuthListener(AuthResponse.ParanoiaAuthListener listener) {
        authListener = listener;
    }

    private void changeStatus(PlayerStatus status) {
        this.status = status;
        listeners.forEach(l -> l.statusChanged(status));
    }

}
