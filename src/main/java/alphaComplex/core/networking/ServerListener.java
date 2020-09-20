package alphaComplex.core.networking;

import daiv.networking.ParanoiaSocket;

public interface ServerListener {

    void receiveConnection(ParanoiaSocket socket);

}
