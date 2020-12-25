package alphaComplex.core.gameplay;

import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.core.networking.ServerListener;
import alphaComplex.core.networking.state.ACPFStatus;
import alphaComplex.core.networking.state.PlayerStatus;
import alphaComplex.visuals.PlayerPanel;
import daiv.networking.ParanoiaSocket;
import daiv.networking.command.ParanoiaCommand;
import daiv.networking.command.acpf.request.DefineRequest;
import daiv.networking.command.acpf.request.LobbyRequest;
import daiv.networking.command.acpf.response.LobbyResponse;
import daiv.networking.command.general.DisconnectRequest;
import daiv.networking.command.general.Ping;
import daiv.networking.command.general.PlayerBroadcast;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static alphaComplex.core.networking.ParanoiaServer.PROTOCOL_TIMEOUT;

public class ParanoiaPlayer implements
    Ping.ParanoiaPingListener,
    DisconnectRequest.ParanoiaDisconnectListener,
    LobbyRequest.ParanoiaAuthValidatior,
    DefineRequest.DefineListener {

    private final ParanoiaSocket connection;

    private String name = "???";
    private Clone clone = Clone.createDummy();
    private final String uuid = UUID.randomUUID().toString();
    private final int id;

    private Timer timeout = new Timer(true);
    private Instant ping = Instant.now();

    private PlayerStatus status = PlayerStatus.INIT;
    private ACPFStatus create_status = ACPFStatus.DEFINE;
    private PlayerListener playerView;
    private final ServerListener lobby;

    private final Object start = new Object();

    private final static ParanoiaLogger logger = LoggerFactory.getLogger();

    public ParanoiaPlayer(ParanoiaSocket socket, int id, ServerListener lobby) {
        connection = socket;
        this.id = id;
        this.lobby = lobby;
        pong(ping);
    }

    /**
     * Validates socket connection
     */
    @Override
    public void pong(Instant pong) {
        timeout.cancel();
        timeout = new Timer(true);
        timeout.schedule(new TimerTask() {
            @Override
            public void run() {
                disconnect("Connection was timed out");
            }
        }, PROTOCOL_TIMEOUT);
        ping = Instant.now();
        long latency = ping.toEpochMilli() - pong.toEpochMilli();
        if(playerView != null)
            playerView.updateLatency(latency);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ping = Instant.now();
        if(connection.isOpen()) sendCommand(new Ping(ping));
    }

    @Override
    public void authenticate(String name, String inputPassword) {
        while (playerView == null) {
            try {
                synchronized (start) {
                    start.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (status.equals(PlayerStatus.AUTH)) {
            boolean valid = lobby.authorize(inputPassword);
            sendCommand(new LobbyResponse(valid, true));
            if(valid) {
                status = PlayerStatus.VALID;
            }
        } else {
            boolean hasPass = lobby.hasPassword();
            boolean valid = (lobby.checkName(name) || !name.isEmpty()) && !hasPass;
            if(valid) {
                this.name = name;
                playerView.updateName(name);
                status = PlayerStatus.VALID;
            }
            if(hasPass) {
                status = PlayerStatus.AUTH;
            }
            sendCommand(new LobbyResponse(valid, hasPass));
        }
        playerView.updateStatus(status);
    }

    /**
     * Transition from any state to OFFLINE
     */
    @Override
    public void disconnect(String message) {
        if(!connection.isOpen()) return;
        timeout.cancel();
        if(status.equals(PlayerStatus.INIT)) lobby.kickPlayer(this);
        status = PlayerStatus.OFFLINE;
        playerView.updateStatus(status);
        sendCommand(new DisconnectRequest(message));
        connection.destroy();
        logger.info("Player " + name + " has been disconnected. Reason: " + message);
    }

    @Override
    public void defineCharacter(String name, String sector, String gender, String[] personality, byte[] profile) {
        clone = new Clone(name, sector, gender, personality, ParanoiaCommand.imageFromBytes(profile));
        playerView.updateClone(clone);
    }

    public void sendCommand(ParanoiaCommand command) {
        connection.sendMessage(command.toNetworkMessage(connection.getAddress(), uuid));
    }

    public String getHost() {
        return connection.getAddress();
    }

    public String getPlayerName() {
        return name;
    }

    public String getUUID() {
        return uuid;
    }

    public int getID() {
        return id;
    }

    public PlayerPanel createPanel() {
        PlayerPanel panel = new PlayerPanel(name, uuid, id, lobby);
        this.playerView = panel;
        updateView();
        synchronized (start) {
            logger.info("Player [" + id + "]: created player view");
            start.notify();
        }
        return panel;
    }

    private void updateView() {
        playerView.updateClone(clone);
        playerView.updateStatus(status);
    }

    public PlayerBroadcast.PlayerData broadcastSelf() {
        return new PlayerBroadcast.PlayerData(name, uuid, id, ParanoiaCommand.parseImage(clone.getPicture()));
    }
}
