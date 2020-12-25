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

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static alphaComplex.core.networking.ParanoiaServer.PROTOCOL_TIMEOUT;

public class ParanoiaPlayer implements
    DisconnectRequest.ParanoiaDisconnectListener,
    LobbyRequest.ParanoiaAuthValidatior,
    DefineRequest.DefineListener {

    private final ParanoiaSocket connection;

    private String name = "???";
    private Clone clone;
    private final String uuid = UUID.randomUUID().toString();
    private final int id;

    private Timer timeout = new Timer(true);
    private Instant ping = Instant.now();

    private PlayerStatus status = PlayerStatus.INIT;
    private ACPFStatus create_status = ACPFStatus.DEFINE;
    private PlayerListener playerView;
    private final ServerListener lobby;

    private final Ping.ParanoiaPingListener latencyMeter;
    private final Object start = new Object();

    private final static ParanoiaLogger logger = LoggerFactory.getLogger();

    public ParanoiaPlayer(ParanoiaSocket socket, int id, ServerListener lobby) {
        connection = socket;
        this.id = id;
        this.lobby = lobby;
        PlayerLatencyMeter latencyMeter = new PlayerLatencyMeter();
        new Thread(latencyMeter).start();
        this.latencyMeter = latencyMeter;
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
            connection.sendMessage(new LobbyResponse(valid, true)
                .toNetworkMessage(connection.getAddress()));
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
            connection.sendMessage(
                new LobbyResponse(valid, hasPass)
                    .toNetworkMessage(connection.getAddress())
            );
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
        connection.sendMessage(new DisconnectRequest(message).toNetworkMessage(connection.getAddress()));
        connection.destroy();
        logger.info("Player " + name + " has been disconnected. Reason: " + message);
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

    public Ping.ParanoiaPingListener getLatencyMeter() {
        return latencyMeter;
    }

    public PlayerPanel createPanel() {
        PlayerPanel panel = new PlayerPanel(name, uuid, id, lobby);
        this.playerView = panel;
        synchronized (start) {
            logger.info("Player [" + id + "]: created player view");
            start.notify();
        }
        return panel;
    }

    private class PlayerLatencyMeter implements Ping.ParanoiaPingListener, Runnable {
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
            if(connection.isOpen())
                connection.sendMessage(new Ping(ping).toNetworkMessage(connection.getAddress()));
        }

        @Override
        public void run() {
            pong(ping);
        }
    }
}
