package alphaComplex.visuals;

import alphaComplex.core.networking.ParanoiaServer;
import alphaComplex.core.networking.ServerListener;
import alphaComplex.core.networking.ServerProperty;
import paranoia.services.plc.AssetManager;
import paranoia.visuals.custom.ParanoiaButton;
import paranoia.visuals.messages.ParanoiaMessage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static paranoia.services.plc.LayoutManager.panelOf;

public class ServerFrame extends JFrame implements ServerListener {

    private final JLabel lbStatus = new JLabel();
    private final JLabel lbHost = new JLabel();
    private final JLabel lbPort = new JLabel();
    private final JLabel lbPlayers = new JLabel();
    private final ParanoiaServer server;
    private final JLabel lbPassword = new JLabel();
    private final JButton btnOpen = new ParanoiaButton("START SERVER");
    private final JButton btnStart = new JButton("START GAME");
    private final TroubleShooterList playerList;

    public ServerFrame() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(720,480));
        setTitle("Alpha Complex Simulator 5000");

        server = new ParanoiaServer();
        playerList = server.createTroubleShooterList();
        server.addListener(this);

        JMenuBar menubar = new JMenuBar();
        menubar.add(createMenu());
        setJMenuBar(menubar);

        getContentPane().setLayout(new BorderLayout());
        add(createOperationPanel(), BorderLayout.EAST);
        add(new JScrollPane(playerList), BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!server.isOpen() || ParanoiaMessage.confirm(
                    "You are shutting down the Alpha Complex. Continue?",
                    ServerFrame.this
                )){
                    server.close();
                    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    super.windowClosing(e);
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    public JPanel createOperationPanel() {
        Font generalfont = AssetManager.getFont(18);

        lbHost.setFont(generalfont);
        lbHost.setText("Address: localhost");

        lbStatus.setFont(AssetManager.getBoldFont(20));
        serverPropertyChanged(ServerProperty.STATUS);

        lbPort.setFont(generalfont);
        serverPropertyChanged(ServerProperty.PORT);

        lbPlayers.setFont(generalfont);
        serverPropertyChanged(ServerProperty.PLAYERS);

        lbPassword.setFont(AssetManager.getFont(13, true, true, false));
        serverPropertyChanged(ServerProperty.PASSWORD);

        btnOpen.setFont(generalfont);
        btnOpen.addActionListener( e -> {
            if (server.isOpen()) {
                if (ParanoiaMessage.confirm("You are shutting down the Alpha Complex. Continue?", ServerFrame.this)) {
                    server.close();
                }
            } else {
                startServerOperation();
            }
            boolean open = server.isOpen();
                btnStart.setEnabled(open);
                btnOpen.setBackground(open ? new Color(213, 75, 75) : AssetManager.defaultButtonBackground);
                btnOpen.setText(open ? "STOP SERVER" : "START SERVER");
        });

        btnStart.setFont(generalfont);
        btnStart.setEnabled(false);

        Dimension size = new Dimension(200,10);
        btnStart.setMaximumSize(size);
        btnOpen.setMaximumSize(size);

        return panelOf(new Component[]{
            lbHost,
            lbPort,
            lbPlayers,
            lbPassword,
            lbStatus,
            Box.createVerticalGlue(),
            btnOpen,
            btnStart
        }, BoxLayout.PAGE_AXIS);
    }


    public JMenu createMenu() {
        JMenu menu = new JMenu("Settings");
        JMenuItem miLogs = new JMenuItem("Logs");
        miLogs.addActionListener( e -> new LoggerWindow().setVisible(true));
        menu.add(miLogs);
        return menu;
    }

    @Override
    public void serverPropertyChanged(ServerProperty property) {
        switch (property) {
            case STATUS:
                lbStatus.setText(server.isOpen() ? "ONLINE" : "OFFLINE");
                lbStatus.setForeground(server.isOpen() ? new Color(98, 160, 16) : new Color(170, 30, 30));
                break;
            case PASSWORD:
                lbPassword.setText("Password: " + server.getPassword());
                break;
            case PLAYERS:
                lbPlayers.setText("Players: " + server.getPlayers());
                playerList.refreshComponents();
                break;
            case PORT:
                lbPort.setText("Port: " + server.getPort());
                break;
        }
    }

    private void startServerOperation() {
        String port = JOptionPane.showInputDialog("Input server port:");
        if(port == null || port.isEmpty()) return;

        String password = JOptionPane.showInputDialog("Input server password (optional):");
        try {
            server.start(Integer.parseInt(port), password);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
