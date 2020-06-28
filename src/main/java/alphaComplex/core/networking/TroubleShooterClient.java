package alphaComplex.core.networking;

import alphaComplex.core.PlayerListener;
import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.visuals.PlayerPanel;
import org.json.JSONException;
import paranoia.core.SecurityClearance;
import paranoia.core.cpu.DiceRoll;
import paranoia.services.hpdmc.ParanoiaController;
import paranoia.services.technical.CommandParser;
import paranoia.services.technical.HelperThread;
import paranoia.services.technical.command.ACPFCommand;
import paranoia.services.technical.command.DiceCommand;
import paranoia.services.technical.command.DisconnectCommand;
import paranoia.services.technical.command.HelloCommand;
import paranoia.services.technical.command.ParanoiaCommand;
import paranoia.services.technical.command.PingCommand;
import paranoia.visuals.panels.ChatPanel;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

public class TroubleShooterClient implements
    ACPFCommand.ParanoiaACPFListener,
    DisconnectCommand.ParanoiaDisconnectListener,
    DiceCommand.ParanoiaDiceResultListener,
    HelloCommand.ParanoiaInfoListener,
    PingCommand.ParanoiaPingListener,
    ParanoiaController,
    PlayerListener {

    private final ParanoiaLogger logger = LoggerFactory.getLogger();

    private final ParanoiaServer parent;
    private final Socket coreTechLink;
    private final CommandParser parser;
    private BufferedWriter coreTechFeed;
    private BufferedReader clientFeed;
    private boolean connected;
    private boolean pong = false;
    private PlayerStatus status = PlayerStatus.AUTHENTICATING;
    private final int id;
    private final UUID uuid = UUID.randomUUID();
    private final PlayerPanel visuals = new PlayerPanel(this);
    private final Object readingLock = new Object();
    private final Object responseLock = new Object();
    private final ChatPanel chatPanel = new ChatPanel(() -> "Computer", this);

    //In-game attributes
    private BufferedImage image;
    private String cloneName;
    private String playerName;
    private String gender;
    private SecurityClearance clearance = SecurityClearance.INFRARED;
    private DiceRoll lastRoll;

    public TroubleShooterClient(Socket link, int id, ParanoiaServer server) throws IOException {
        this.id = id;
        this.coreTechLink = link;
        this.parent = server;
        this.parser = new CommandParser();
        getIOStream();
        //Set listeners
        parser.setAcpfListener(this);
        parser.setPingListener(this);
        parser.setDisconnectListener(this);
        parser.setDiceListener(this);
        parser.setInfoListener(this);
        parser.setChatListener(chatPanel);
        fireDataChanged();
    }

    private void getIOStream() throws IOException {
        coreTechFeed = new BufferedWriter(new OutputStreamWriter(coreTechLink.getOutputStream()));
        clientFeed = new BufferedReader(new InputStreamReader(coreTechLink.getInputStream()));
        startReaderThread();
    }

    public String getInfo() {
        String address = String.valueOf(coreTechLink.getInetAddress().getHostAddress());
        return "Clone [ID: " + id + ", " + address + "]";
    }

    public void sendMessage(String message) {
        if(coreTechLink == null || coreTechFeed == null) return;
        if(coreTechLink.isConnected() && !coreTechLink.isClosed())
        try {
            logger.info("Sending message to Player(" + id + ")");
            logger.debug("MSG = " + message);
            coreTechFeed.write(message);
            coreTechFeed.newLine();
            coreTechFeed.flush();
        } catch (IOException e) {
            logger.exception(e);
        }
    }

    private void startReaderThread() {
        connected = true;
        Thread readerThread = new Thread(this::readMessage);
        readerThread.start();
    }

    public boolean isConnected() {
        return connected;
    }

    private synchronized void readMessage() {
        while (connected) {
            try {
                HelperThread<String> reading =
                    new HelperThread<>(v -> doRead(), readingLock);
                reading.start();
                synchronized (readingLock) { readingLock.wait(); }
                String line = reading.getValue();
                if (line == null) {
                    disconnect();
                    break;
                }
                logger.info(getInfo() + ": " + line);
                parser.parse(line);
            } catch (InterruptedException e) {
                logger.exception(e);
                disconnect();
                break;
            } catch (JSONException parse) {
                logger.exception(parse);
                disconnect();
            }
        }
    }

    private String doRead() {
        try {
            return clientFeed.readLine();
        } catch (IOException e) {
            logger.exception(e);
            return null;
        }
    }

    private void fireDataChanged() {
        visuals.updateVisuals(playerName, cloneName, clearance , uuid.toString(), status.name(), id, image, lastRoll);
    }

    public PlayerPanel getVisuals() {
        return visuals;
    }

    @Override
    public boolean sendCommand(ParanoiaCommand command) {
        sendMessage(command.toJsonObject().toString());
        return connected;
    }

    @Override
    public void changeStatus(PlayerStatus status) {
        this.status = status;
        fireDataChanged();
    }

    @Override
    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public void disconnect() {
        if(!connected) return;
        try {
            sendCommand(new DisconnectCommand(null));
            synchronized (readingLock) { readingLock.notify(); }
            coreTechLink.close();
        } catch (IOException e) {
            logger.exception(e);
        }
        connected = false;
        logger.info(getInfo() + ": Disconnected");
        status = PlayerStatus.OFFLINE;
        fireDataChanged();
        parent.updatePlayerNumber();
    }

    @Override
    public void updateProfile(String name, String gender, String[] personality, BufferedImage image) {
        this.cloneName = name;
        this.gender = gender;
        this.image = image;
        this.clearance = SecurityClearance.RED;
        fireDataChanged();
    }

    @Override
    public void getResult(int success, boolean computer) {
        status = PlayerStatus.IDLE;
        lastRoll = new DiceRoll(success, computer);
        fireDataChanged();
    }

    @Override
    public void sayHello(String player, String password, boolean hasPassword) {
        playerName = player;
        if(parent.authenticate(id, password)) {
            status = PlayerStatus.IDLE;
            synchronized (responseLock) {
                responseLock.notify();
            }
            fireDataChanged();
        }
    }

    public void sendAuthRequest(String password) throws InterruptedException {
        sendCommand(new HelloCommand(
            null, null,
            !password.isEmpty(), null)
        );
        synchronized (responseLock) {
            responseLock.wait(ParanoiaServer.PROTOCOL_TIMEOUT);
        }
        if(!status.equals(PlayerStatus.IDLE)){
            parent.deletePlayer(id);
        }
    }

    public boolean ping() throws InterruptedException {
        pong = false;
        sendCommand(new PingCommand());
        synchronized (responseLock) {
            responseLock.wait(ParanoiaServer.PROTOCOL_TIMEOUT);
        }
        return pong;
    }

    @Override
    public void pong() {
        synchronized (responseLock) {
            pong = true;
            responseLock.notify();
        }
    }
}
