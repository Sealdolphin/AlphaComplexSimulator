package alphaComplex.core.gameplay;

import alphaComplex.core.networking.state.PlayerStatus;

public interface PlayerListener {

    void updateLatency(long latency);

    void updateName(String name);

    void updateStatus(PlayerStatus status);

}
