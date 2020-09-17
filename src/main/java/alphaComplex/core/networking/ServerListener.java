package alphaComplex.core.networking;

import paranoia.services.technical.networking.ParanoiaSocket;

public interface ServerListener {

    void receiveConnection(ParanoiaSocket socket);

}
