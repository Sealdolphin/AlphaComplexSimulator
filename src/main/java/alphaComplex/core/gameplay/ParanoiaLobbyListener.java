package alphaComplex.core.gameplay;

import java.util.List;

public interface ParanoiaLobbyListener {

    void updateConnections(int connections);

    void updateServer(String password, String port);

    void updatePlayers(List<ParanoiaPlayer> players);

}
