package alphaComplex.visuals;

import alphaComplex.core.gameplay.ParanoiaLobby;
import alphaComplex.core.gameplay.ParanoiaLobbyListener;
import alphaComplex.core.gameplay.ParanoiaPlayer;
import daiv.networking.command.ParanoiaCommand;
import daiv.ui.AssetManager;
import daiv.ui.custom.ParanoiaMessage;
import daiv.ui.visuals.ParanoiaButton;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import java.util.List;

import static daiv.ui.LayoutManager.panelOf;

public class ServerFrame extends JFrame implements ParanoiaLobbyListener {

    private final JLabel lbStatus = new JLabel();
    private final JLabel lbHost = new JLabel();
    private final JLabel lbPort = new JLabel();
    private final JLabel lbPlayers = new JLabel();
    private final ParanoiaLobby lobby = new ParanoiaLobby();
    private final JLabel lbPassword = new JLabel();
    private final JButton btnOpen = new ParanoiaButton("START SERVER");
    private final JButton btnStart = new ParanoiaButton("START GAME");
    private JScrollPane playerList = new JScrollPane(new JPanel());

    public ServerFrame() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(720,480));
        setTitle("Alpha Complex Simulator 5000");
        lobby.addListener(this);

        JMenuBar menubar = new JMenuBar();
        menubar.add(createMenu());
        setJMenuBar(menubar);

        getContentPane().setLayout(new BorderLayout());
        add(createOperationPanel(), BorderLayout.EAST);
        add(playerList, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!lobby.isOpen() || ParanoiaMessage.confirm(
                    "You are shutting down the Alpha Complex. Continue?",
                    ServerFrame.this
                )){
                    lobby.close();
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
        lbPort.setFont(generalfont);
        lbPlayers.setFont(generalfont);
        lbPassword.setFont(AssetManager.getFont(13, true, true, false));

        btnOpen.setFont(generalfont);
        btnOpen.addActionListener( e -> {
            if (lobby.isOpen()) {
                if (ParanoiaMessage.confirm("You are shutting down the Alpha Complex. Continue?", ServerFrame.this)) {
                    lobby.close();
                }
            } else {
                startServerOperation();
            }
            boolean open = lobby.isOpen();
                btnStart.setEnabled(open);
                btnOpen.setBackground(open ? new Color(213, 75, 75) : AssetManager.defaultButtonBackground);
                btnOpen.setText(open ? "STOP SERVER" : "START SERVER");
        });

        btnStart.setFont(generalfont);
        btnStart.setEnabled(false);
        btnStart.addActionListener( e -> lobby.startGame());

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
        miLogs.addActionListener( e -> new LoggerWindow(ServerFrame.this).setVisible(true));
        menu.add(miLogs);
        return menu;
    }

    private void startServerOperation() {
        String port = JOptionPane.showInputDialog("Input server port:");
        if(port == null || port.isEmpty()) return;
        String password = JOptionPane.showInputDialog("Input server password (optional):");
        lobby.startServer(Integer.parseInt(port), password);
    }

    @Override
    public void updateConnections(int connections) {
        lbPlayers.setText("Connections: " + connections);
    }

    @Override
    public void updatePlayers(List<ParanoiaPlayer> players) {
        remove(playerList);
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.PAGE_AXIS));
        players.forEach(player -> playerPanel.add(player.createPanel()));
        playerList = new JScrollPane(playerPanel);
        add(playerList, BorderLayout.CENTER);
        revalidate();
    }

    @Override
    public void updateServer(String password, String port) {
        lbPassword.setText("Password: " + password + " ");
        lbPort.setText("Port: " + port);
    }
}
