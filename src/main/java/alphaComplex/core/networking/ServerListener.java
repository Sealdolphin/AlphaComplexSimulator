package alphaComplex.core.networking;

import alphaComplex.core.gameplay.ParanoiaPlayer;
import daiv.networking.ParanoiaSocket;

public interface ServerListener {

    void receiveConnection(ParanoiaSocket socket);

    boolean authorize(String input);

    boolean checkName(String name);

    boolean hasPassword();

    void kickPlayer(String uuid);

    void kickPlayer(ParanoiaPlayer player);

}
