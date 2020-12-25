package alphaComplex.core.networking;

import daiv.networking.ParanoiaSocket;

public interface ServerListener {

    void receiveConnection(ParanoiaSocket socket);

    boolean authorize(String input);

    boolean checkName(String name);

    boolean hasPassword();
}
