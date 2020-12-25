package alphaComplex.core.gameplay;

import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.core.networking.ParanoiaServer;
import alphaComplex.core.networking.ServerListener;
import daiv.networking.ParanoiaSocket;
import daiv.networking.SocketListener;
import daiv.networking.command.ParanoiaCommand;
import daiv.networking.command.acpf.request.DefineRequest;
import daiv.networking.command.acpf.request.LobbyRequest;
import daiv.networking.command.general.DisconnectRequest;
import daiv.networking.command.general.Ping;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParanoiaLobby implements
    ServerListener,
    SocketListener
{

    private final ParanoiaLogger logger = LoggerFactory.getLogger();

    private final ParanoiaServer server;
    private ParanoiaLobbyListener lobby;
    private String password;
    private final List<ParanoiaPlayer> players = new ArrayList<>();

    public ParanoiaLobby() {
        server = new ParanoiaServer();
        server.addListener(this);
    }

    public void startServer(int port, String password) {
        this.password = password;
        players.clear();
        lobby.updateServer(password, String.valueOf(port));
        lobby.updateConnections(server.getSockets());
        lobby.updatePlayers(players);
        server.start(port);
    }

    public boolean isOpen() {
        return server != null && server.isRunning();
    }

    public void close() {
        server.stop();
    }

    @Override
    public void receiveConnection(ParanoiaSocket socket) {
        int sockets = server.getSockets();
        //Refresh connections
        lobby.updateConnections(sockets);
        //Verify connection:
        socket.addListener(this);
        ParanoiaPlayer player = new ParanoiaPlayer(socket, sockets, this);
        players.add(player);
        lobby.updatePlayers(players);
    }

    public boolean authorize(String inputPassword) {
        return inputPassword.equals(password);
    }

    @Override
    public boolean checkName(String name) {
        return players.stream().map(ParanoiaPlayer::getPlayerName).noneMatch(n -> n.equals(name));
    }

    @Override
    public boolean hasPassword() {
        return !password.isEmpty();
    }

    @Override
    public void kickPlayer(String uuid) {
        Optional<ParanoiaPlayer> optional = players.stream().filter(p -> p.getUUID().equals(uuid)).findAny();
        if(optional.isPresent()) {
            ParanoiaPlayer player = optional.get();
            player.disconnect("Player has been kicked");
            kickPlayer(player);
        }
    }

    @Override
    public void kickPlayer(ParanoiaPlayer player) {
        players.remove(player);
        lobby.updatePlayers(players);
    }

    @Override
    public void readInput(byte[] message) {
        try {
            ParanoiaCommand parsedCommand = ParanoiaCommand.parseCommand(message);
            String host = parsedCommand.getHost();
            if(parsedCommand.getType() != ParanoiaCommand.CommandType.PING)
                logger.info("Socket [" + host + "] sent a " + parsedCommand.getType() + " command");
            //Get player
            ParanoiaPlayer player = players.stream().filter(p ->
                p.getHost().equals(host)).findFirst().orElse(null);
            if(player == null) return;
            //parse command
            switch (parsedCommand.getType()) {
                case PING:
                    Ping.create(parsedCommand, player.getLatencyMeter()).execute();
                    break;
                case LOBBY:
                    LobbyRequest.create(parsedCommand, player).execute();
                    break;
                case DISCONNECT:
                    DisconnectRequest.create(parsedCommand, player).execute();
                    break;
                case ACPF:
                    DefineRequest.create(parsedCommand, player).execute();
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            logger.error("An error happened during reading");
            logger.exception(e);
        } catch (ClassNotFoundException e) {
            logger.error("Invalid operation");
            logger.exception(e);
        }
    }

    @Override
    public void readError(Throwable throwable) {
        if(!throwable.getClass().equals(EOFException.class))
            logger.warning("An error happened during reading: " + throwable.getLocalizedMessage());
    }

    @Override
    public void fireTerminated(String message) {
        logger.info("A socket has been terminated");
        server.clean();
        lobby.updateConnections(server.getSockets());
    }

    public void addListener(ParanoiaLobbyListener listener) {
        lobby = listener;
    }
}
