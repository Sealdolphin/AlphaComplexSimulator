package alphaComplex.core;

import alphaComplex.core.networking.PlayerStatus;
import paranoia.services.hpdmc.ParanoiaController;
import paranoia.visuals.panels.ChatPanel;

public interface PlayerListener extends ParanoiaController {

    ChatPanel getChatPanel();

    void changeStatus(PlayerStatus status);

}
