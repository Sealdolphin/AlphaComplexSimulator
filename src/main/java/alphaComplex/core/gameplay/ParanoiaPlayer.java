package alphaComplex.core.gameplay;

import paranoia.services.technical.networking.ParanoiaSocket;

public class ParanoiaPlayer {

    private final ParanoiaSocket connection;

    public ParanoiaPlayer(ParanoiaSocket socket) {
        connection = socket;
    }

    public void sendMessage(String message) {
        connection.sendMessage(message);
    }

}
