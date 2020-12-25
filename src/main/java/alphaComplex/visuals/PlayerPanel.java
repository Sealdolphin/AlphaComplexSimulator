package alphaComplex.visuals;

import alphaComplex.core.gameplay.Clone;
import alphaComplex.core.gameplay.PlayerListener;
import alphaComplex.core.networking.ServerListener;
import alphaComplex.core.networking.state.PlayerStatus;
import daiv.ui.AssetManager;
import daiv.ui.custom.ParanoiaMessage;
import daiv.ui.visuals.ParanoiaButton;
import daiv.ui.visuals.ParanoiaImage;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static alphaComplex.core.networking.state.PlayerStatus.OFFLINE;
import static alphaComplex.core.networking.state.PlayerStatus.ONLINE;
import static daiv.Computer.getParanoiaResource;

public class PlayerPanel extends JPanel implements PlayerListener {

    private final GroupLayout layout = new GroupLayout(this);

    private final JLabel lbLatency = new JLabel("1000 ms");
    private final JLabel lbName = new JLabel();
    private final JLabel lbGender = new JLabel("Unknown");
    private final JLabel lbClone = new JLabel("???");
    private final JLabel lbStatus = new JLabel(PlayerStatus.INIT.name());
    private final JLabel lbUUID = new JLabel("", SwingConstants.CENTER);
    private final JLabel lbPlayerID = new JLabel();
    private final JLabel rollStatus = new JLabel("Hasn't rolled yet", SwingConstants.CENTER);
    private final ParanoiaImage profile = new ParanoiaImage(null, true);
    private final ParanoiaButton btnChat;
    private final ParanoiaButton btnRoll;

    public PlayerPanel(String name, String UUID, int id, ServerListener lobby) {
        setLayout(layout);

        Font generalFont = AssetManager.getFont(18);
        lbName.setFont(generalFont);
        lbLatency.setFont(AssetManager.getFont("Courier", 13));
        lbClone.setFont(generalFont);
        rollStatus.setFont(AssetManager.getBoldFont(12));
        lbUUID.setFont(AssetManager.getFont(15));
        lbStatus.setFont(AssetManager.getFont(20, true, true, false));
        lbGender.setFont(AssetManager.getFont(15));
        lbPlayerID.setFont(generalFont);
        profile.setPreferredSize(new Dimension(100,100));

        lbName.setText(name);
        lbUUID.setText(UUID);
        lbPlayerID.setText(Integer.toString(id));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    createPopupMenu(lobby).show(PlayerPanel.this, e.getX(), e.getY());
                }
            }
        });

        BufferedImage chatIcon;
        BufferedImage rollIcon;
        try {
            chatIcon = ImageIO.read(new File(getParanoiaResource("ui/btnChat.png")));
            rollIcon = ImageIO.read(new File(getParanoiaResource("ui/btnRoll.png")));
            BufferedImage mystery = ImageIO.read(new File(getParanoiaResource("ui/cloneMystery.png")));
            profile.changeImage(mystery);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            chatIcon = new BufferedImage(25,25,BufferedImage.TYPE_INT_ARGB);
            rollIcon = new BufferedImage(25,25,BufferedImage.TYPE_INT_ARGB);
        }
        btnChat = new ParanoiaButton(chatIcon, "Chat", 25);
        btnChat.setEnabled(false);
        //btnChat.addActionListener(e -> openChatWindow(listener));
        btnChat.setPreferredSize(new Dimension(25, 25));

        btnRoll = new ParanoiaButton(rollIcon, "Roll", 25);
        btnRoll.setEnabled(false);
//        btnRoll.addActionListener(e -> {
//            listener.sendCommand(new RollCommand(
//                Stat.VIOLENCE, Skill.ATHLETICS, true, true,
//                Collections.emptyMap(), Collections.emptyMap(), null
//            ));
//            listener.statusChanged(ROLLING);
//        });
        btnRoll.setPreferredSize(new Dimension(25, 25));
        setUpComponents();
        setMaximumSize(new Dimension(Short.MAX_VALUE, 150));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public void setUpComponents() {
        JLabel lbAs = new JLabel("as");
        JLabel lbStatusText = new JLabel("Status:");
        JLabel lbGenderText = new JLabel("Gender:");
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(lbLatency)
                        .addComponent(profile, 128, 128, 128)
                )
                .addGap(10)
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(lbUUID)
                        .addGroup(
                            layout.createSequentialGroup()
                                .addComponent(lbName)
                                .addGap(7)
                                .addComponent(lbAs)
                                .addGap(7)
                                .addComponent(lbClone)
                        )
                        .addGroup(
                            layout.createSequentialGroup()
                                .addComponent(lbStatusText)
                                .addGap(5)
                                .addComponent(lbStatus)
                                .addGap(15)
                                .addComponent(lbGenderText)
                                .addGap(5)
                                .addComponent(lbGender)
                        )
                        .addGroup(
                            layout.createSequentialGroup()
                                .addComponent(btnChat)
                                .addComponent(btnRoll)
                        )
                        .addComponent(rollStatus)
                )
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(lbLatency)
                        .addComponent(lbUUID)
                )
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(profile, 128, 128, 128)
                        .addGroup(
                            layout.createSequentialGroup()
                                .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lbName)
                                        .addComponent(lbAs)
                                        .addComponent(lbClone)
                                )
                                .addGap(5)
                                .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lbStatusText)
                                        .addComponent(lbStatus)
                                        .addComponent(lbGenderText)
                                        .addComponent(lbGender)
                                )
                                .addGap(5)
                                .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnChat)
                                        .addComponent(btnRoll)
                                )
                                .addGap(5)
                                .addComponent(rollStatus)
                        )
                )
        );
    }

    public void updateVisuals() {
        String status = lbStatus.getText();
        lbStatus.setForeground(status.equals("OFFLINE") ? new Color(170, 30, 30) : new Color(98, 160, 16));
//        lbPlayerID.setText("#" + id);
//        profile.changeImage(image);
//            if(lastRoll != null)
//        rollStatus.setText("Last Roll: " + lastRoll.getSuccess() + " success " + (lastRoll.isComputer() ? " + Computer" : ""));

        btnRoll.setEnabled(status.equals(ONLINE.name()));
        btnChat.setEnabled(!status.equals(OFFLINE.name()));

        revalidate();
    }

    private void copyUUID() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
            new StringSelection(lbUUID.getText()), null
        );
        ParanoiaMessage.info("UUID was copied to the clipboard");
    }

//    private void openChatWindow() {
//        JFrame chatFrame = new JFrame();
//        chatFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        chatFrame.setLayout(new BorderLayout());
////        chatFrame.add(player.getChatPanel(), BorderLayout.CENTER);
//        chatFrame.setTitle("Feed of Player " + lbPlayerID.getText());
//        chatFrame.pack();
//        chatFrame.setVisible(true);
//    }

    private JPopupMenu createPopupMenu(ServerListener lobby) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miCopy = new JMenuItem("Copy UUID");
        JMenuItem miKick = new JMenuItem("Kick Player");

        miCopy.addActionListener( e -> copyUUID());
        miKick.addActionListener( e -> {
            boolean kick = ParanoiaMessage.confirm("Kicking a player will destroy this connection.\nThe citizen CANNOT reconnect after that. Continue?", this);
            if(kick) { lobby.kickPlayer(lbUUID.getText()); }
        });

        popup.add(new JLabel("Actions with Player " + lbPlayerID.getText()));
        popup.addSeparator();
        popup.add(miCopy);
        popup.add(miKick);
        return popup;
    }

    @Override
    public void updateLatency(long latency) {
        lbLatency.setText(latency + " ms");
    }

    @Override
    public void updateName(String name) {
        lbName.setText(name);
    }

    @Override
    public void updateStatus(PlayerStatus status) {
        lbStatus.setText(status.name());
        updateVisuals();
    }

    @Override
    public void updateClone(Clone clone) {
        lbClone.setText(clone.getName());
        lbGender.setText(clone.getGender());
        profile.changeImage(clone.getPicture());
    }
}
