import javax.swing.JPanel;

//package alphaComplex.visuals;
//
//import alphaComplex.core.PlayerListener;
//import paranoia.Paranoia;
//import paranoia.core.SecurityClearance;
//import paranoia.core.cpu.DiceRoll;
//import paranoia.core.cpu.Skill;
//import paranoia.core.cpu.Stat;
//import paranoia.services.plc.AssetManager;
//import paranoia.services.technical.command.DisconnectCommand;
//import paranoia.services.technical.command.RollCommand;
//import paranoia.visuals.custom.ParanoiaButton;
//import paranoia.visuals.custom.ParanoiaImage;
//import paranoia.visuals.messages.ParanoiaMessage;
//
//import javax.imageio.ImageIO;
//import javax.swing.BorderFactory;
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JMenuItem;
//import javax.swing.JPanel;
//import javax.swing.JPopupMenu;
//import javax.swing.SwingConstants;
//import javax.swing.WindowConstants;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.GridBagLayout;
//import java.awt.Toolkit;
//import java.awt.datatransfer.StringSelection;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.Collections;
//
//import static alphaComplex.core.networking.PlayerStatus.IDLE;
//import static alphaComplex.core.networking.PlayerStatus.OFFLINE;
//import static alphaComplex.core.networking.PlayerStatus.ROLLING;
//import static java.awt.GridBagConstraints.CENTER;
//import static java.awt.GridBagConstraints.HORIZONTAL;
//import static java.awt.GridBagConstraints.LINE_START;
//import static java.awt.GridBagConstraints.RELATIVE;
//import static java.awt.GridBagConstraints.REMAINDER;
//import static paranoia.services.plc.LayoutManager.createGrid;
//import static paranoia.services.plc.LayoutManager.panelOf;
//
public class PlayerPanel extends JPanel {
//
//    private final JLabel lbName = new JLabel();
//    private final JLabel lbClone = new JLabel();
//    private final JLabel lbStatus = new JLabel();
//    private final JLabel lbUUID = new JLabel("", SwingConstants.CENTER);
//    private final JLabel lbPlayerID = new JLabel();
//    private final JLabel rollStatus = new JLabel("Hasn't rolled yet", SwingConstants.CENTER);
//    private final ParanoiaImage profile = new ParanoiaImage(null, true);
//    private final PlayerListener listener;
//    private final ParanoiaButton btnChat;
//    private final ParanoiaButton btnRoll;
//
//    public PlayerPanel (PlayerListener listener) {
//        setLayout(new GridBagLayout());
//        this.listener = listener;
//
//        Font generalFont = AssetManager.getFont(18);
//        lbName.setFont(generalFont);
//        lbClone.setFont(generalFont);
//        rollStatus.setFont(AssetManager.getBoldFont(12));
//        lbUUID.setFont(AssetManager.getFont(15));
//        lbStatus.setFont(AssetManager.getFont(20, true, true, false));
//        lbPlayerID.setFont(generalFont);
//        profile.setPreferredSize(new Dimension(100,100));
//
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if(e.getButton() == MouseEvent.BUTTON3) {
//                    createPopupMenu().show(PlayerPanel.this, e.getX(), e.getY());
//                }
//            }
//        });
//
//        BufferedImage chatIcon;
//        BufferedImage rollIcon;
//        try {
//            chatIcon = ImageIO.read(new File(Paranoia.getParanoiaResource("ui/btnChat.png")));
//            rollIcon = ImageIO.read(new File(Paranoia.getParanoiaResource("ui/btnRoll.png")));
//        } catch (IOException e) {
//            e.printStackTrace();
//            chatIcon = new BufferedImage(0,0,0);
//            rollIcon = new BufferedImage(0,0,0);
//        }
//        btnChat = new ParanoiaButton(chatIcon, 25);
//        btnChat.addActionListener(e -> openChatWindow(listener));
//        btnChat.setPreferredSize(new Dimension(25, 25));
//
//        btnRoll = new ParanoiaButton(rollIcon, 25);
//        btnRoll.addActionListener(e -> {
////            listener.sendCommand(new RollCommand(
////                Stat.VIOLENCE, Skill.ATHLETICS, true, true,
////                Collections.emptyMap(), Collections.emptyMap(), null
////            ));
//            listener.statusChanged(ROLLING);
//        });
//        btnRoll.setPreferredSize(new Dimension(25, 25));
//
//        setUpComponents();
//        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
//    }
//
//    public void setUpComponents() {
//        add(lbUUID, createGrid().at(0,0, REMAINDER, 1).anchor(CENTER).fill(HORIZONTAL).get());
//        add(profile, createGrid().at(0, 1, 1, 3).anchor(LINE_START).get());
//
//        add(lbPlayerID, createGrid().at(1, 1).get());
//        add(lbName, createGrid().at(2, 1).get());
//        add(new JLabel("as"), createGrid().at(3, 1).anchor(LINE_START).get());
//        add(lbClone, createGrid().at(4,1).get());
//        add(new JLabel("Status:"), createGrid().at(1,2).anchor(LINE_START).get());
//        add(lbStatus, createGrid().at(2,2).get());
//
//        add(createBtnPanel(), createGrid().at(1, 3, REMAINDER, 1).anchor(CENTER).fill(HORIZONTAL).get());
//        add(rollStatus, createGrid().at(0,RELATIVE, REMAINDER, 1).anchor(CENTER).fill(HORIZONTAL).get());
//    }
//
//    public void updateVisuals(
//        String name, String clone,
//        SecurityClearance clearance,
//        String uuid, String status,
//        int id, BufferedImage image,
//        DiceRoll lastRoll
//    ) {
//        lbName.setText(name);
//        lbClone.setText(clone);
//        lbUUID.setText(uuid);
//        lbStatus.setText(status);
//        lbStatus.setForeground(status.equals("OFFLINE") ? new Color(170, 30, 30) : new Color(98, 160, 16));
//        lbPlayerID.setText("#" + id);
//        profile.changeImage(image);
//            if(lastRoll != null)
//        rollStatus.setText("Last Roll: " + lastRoll.getSuccess() + " success " + (lastRoll.isComputer() ? " + Computer" : ""));
//
//        btnRoll.setEnabled(status.equals(IDLE.name()));
//        btnChat.setEnabled(!status.equals(OFFLINE.name()));
//
//        revalidate();
//    }
//
//    private JPanel createBtnPanel() {
//        return panelOf(new Component[]{
//            btnChat,
//            Box.createHorizontalStrut(5),
//            btnRoll
//        }, BoxLayout.LINE_AXIS);
//    }
//
//    private void copyUUID() {
//        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
//            new StringSelection(lbUUID.getText()), null
//        );
//        ParanoiaMessage.info("UUID was copied to the clipboard");
//    }
//
//    private void openChatWindow(PlayerListener player) {
//        JFrame chatFrame = new JFrame();
//        chatFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        chatFrame.setLayout(new BorderLayout());
////        chatFrame.add(player.getChatPanel(), BorderLayout.CENTER);
//        chatFrame.setTitle("Feed of Player " + lbPlayerID.getText());
//        chatFrame.pack();
//        chatFrame.setVisible(true);
//    }
//
//    private JPopupMenu createPopupMenu() {
//        JPopupMenu popup = new JPopupMenu();
//        JMenuItem miCopy = new JMenuItem("Copy UUID");
//        JMenuItem miKick = new JMenuItem("Kick Player");
//
//        miKick.setEnabled(!lbStatus.getText().equals("OFFLINE"));
//
//        miCopy.addActionListener( e -> copyUUID());
////        miKick.addActionListener( e -> listener.sendCommand(new DisconnectCommand(null)) );
//
//        popup.add(new JLabel("Actions with Player " + lbPlayerID.getText()));
//        popup.addSeparator();
//        popup.add(miCopy);
//        popup.add(miKick);
//        return popup;
//    }
}
