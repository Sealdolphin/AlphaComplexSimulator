package alphaComplex.core.networking;

import alphaComplex.core.PlayerListener;
import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.visuals.PlayerPanel;
import paranoia.services.technical.CommandParser;
import paranoia.services.technical.HelperThread;
import paranoia.services.technical.command.ACPFCommand;
import paranoia.services.technical.command.DisconnectCommand;

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
    PlayerListener {

    private final ParanoiaLogger logger = LoggerFactory.getLogger();

    private Socket coreTechLink;
    private final CommandParser parser;
    private BufferedWriter coreTechFeed;
    private BufferedReader clientFeed;
    private Thread readerThread;
    private boolean connected;
    private final int id;
    private final UUID uuid = UUID.randomUUID();
    private final PlayerPanel visuals = new PlayerPanel(this);
    private final Object readingLock = new Object();

    //In-game attributes
    private BufferedImage image;
    private String name;
    private String gender;

    public TroubleShooterClient(Socket link, int id) throws IOException {
        this.id = id;
        this.coreTechLink = link;
        this.parser = new CommandParser();
        getIOStream();
        //Set listeners
        parser.setAcpfListener(this);
        parser.setDisconnectListener(this);
        visuals.updateVisuals(name, uuid.toString(), connected, id, image);
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
        readerThread = new Thread(this::readMessage);
        readerThread.start();
    }

    public void disconnect() {
        if(!connected) return;
        connected = false;
        try {
            sendMessage(new DisconnectCommand(null).toJsonObject().toString());
            synchronized (readingLock) { readingLock.notify(); }
            coreTechLink.close();
        } catch (IOException e) {
            logger.exception(e);
        }
        logger.info(getInfo() + ": Disconnected");
        visuals.updateVisuals(name, uuid.toString(), connected, id, image);
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
            }
        }
    }

    private String doRead() {
        try {
            return clientFeed.readLine();
        } catch (IOException e) {
            if(connected) logger.exception(e);
            return null;
        }
    }

    public PlayerPanel getVisuals() {
        return visuals;
    }

    @Override
    public void updateProfile(String name, String gender, String[] personality, BufferedImage image) {
        this.name = name;
        this.gender = gender;
        this.image = image;
        visuals.updateVisuals(name, uuid.toString(), connected, id, image);
    }
}
