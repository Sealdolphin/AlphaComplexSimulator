package alphaComplex.core;

import alphaComplex.core.networking.PlayerStatus;

public interface PlayerListener {

//    ChatPanel getChatPanel();

    void statusChanged(PlayerStatus status);

}
