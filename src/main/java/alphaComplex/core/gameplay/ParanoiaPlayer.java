package alphaComplex.core.gameplay;

import alphaComplex.core.PlayerListener;
import alphaComplex.core.networking.PlayerStatus;
import paranoia.services.technical.command.PingCommand;
import paranoia.services.technical.networking.ParanoiaSocket;

import java.util.ArrayList;
import java.util.List;

import static alphaComplex.core.networking.ParanoiaServer.PROTOCOL_TIMEOUT;

public class ParanoiaPlayer {

    private final ParanoiaSocket connection;
    private PlayerStatus status = PlayerStatus.INVALID;
    private final Object verifyLock = new Object();
    private final List<PlayerListener> listeners = new ArrayList<>();

    public ParanoiaPlayer(ParanoiaSocket socket) {
        connection = socket;
        //Verify connection?
//        synchronized (verifyLock) {
//            while (status.equals(PlayerStatus.INVALID)) {
                socket.sendMessage(new PingCommand().toNetworkMessage(socket.getAddress()));
//                try {
//                    wait(PROTOCOL_TIMEOUT);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    socket.destroy();
//                    Thread.currentThread().interrupt();
//                }
//            }
//            notifyAll();
//            if(status.equals(PlayerStatus.INVALID))
//                socket.destroy();
//        }
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

}
