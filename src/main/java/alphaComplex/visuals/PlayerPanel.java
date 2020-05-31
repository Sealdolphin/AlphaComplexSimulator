package alphaComplex.visuals;

import alphaComplex.core.PlayerListener;
import paranoia.Paranoia;
import paranoia.core.ParanoiaPlayer;
import paranoia.services.hpdmc.ParanoiaController;
import paranoia.services.plc.AssetManager;
import paranoia.visuals.custom.ParanoiaButton;
import paranoia.visuals.custom.ParanoiaImage;
import paranoia.visuals.messages.ParanoiaMessage;
import paranoia.visuals.panels.ChatPanel;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.REMAINDER;
import static paranoia.services.plc.LayoutManager.createGrid;

public class PlayerPanel extends JPanel {

    private final JLabel lbName = new JLabel();
    private final JLabel lbClone = new JLabel();
    private final JLabel lbStatus = new JLabel();
    private final JLabel lbUUID = new JLabel();
    private final JLabel lbPlayerID = new JLabel();
    private final ParanoiaImage profile = new ParanoiaImage(null, true);
    private final PlayerListener listener;
    private final ParanoiaButton btnChat;

    public PlayerPanel (PlayerListener listener) {
        setLayout(new GridBagLayout());
        this.listener = listener;

        Font generalFont = AssetManager.getFont(20);
        lbName.setFont(generalFont);
        lbUUID.setFont(AssetManager.getFont(15));
        lbStatus.setFont(AssetManager.getFont(20, true, true, false));
        lbPlayerID.setFont(generalFont);
        profile.setPreferredSize(new Dimension(100,100));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    createPopupMenu().show(PlayerPanel.this, e.getX(), e.getY());
                }
            }
        });

        BufferedImage chatIcon;
        try {
            chatIcon = ImageIO.read(new File(Paranoia.getParanoiaResource("ui/btnChat.png")));
        } catch (IOException e) {
            e.printStackTrace();
            chatIcon = null;
        }
        btnChat = new ParanoiaButton(chatIcon);

        btnChat.addActionListener(e -> openChatWindow(listener));

        setUpComponents();
    }

    public void setUpComponents() {
        add(new JLabel("UUID:"), createGrid().at(0,0).anchor(LINE_START).get());
        add(lbUUID, createGrid().at(1,0, REMAINDER, 1).anchor(CENTER).fill(HORIZONTAL).get());

        add(profile, createGrid().at(0, 1, 1, 3).anchor(LINE_START).get());
        add(lbPlayerID, createGrid().at(1, 1).get());
        add(lbName, createGrid().at(2, 1).get());
        add(new JLabel("Clone:"), createGrid().at(3, 1).anchor(LINE_START).get());
        add(lbClone, createGrid().at(4,1).get());
        add(new JLabel("Status:"), createGrid().at(1,2).anchor(LINE_START).get());
        add(lbStatus, createGrid().at(2,2).get());

        add(btnChat, createGrid().at(1,3).get());
    }

    public void updateVisuals(String name, String uuid, boolean status, int id, BufferedImage image) {
        lbName.setText(name);
        lbUUID.setText(uuid);
        lbStatus.setText(status ? "ONLINE" : "OFFLINE");
        lbStatus.setForeground(status ? new Color(98, 160, 16) : new Color(170, 30, 30));
        lbPlayerID.setText("#" + id);
        profile.changeImage(image);
        revalidate();
    }

    private void copyUUID() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
            new StringSelection(lbUUID.getText()), null
        );
        ParanoiaMessage.info("UUID was copied to the clipboard");
    }


    private void openChatWindow(PlayerListener player) {
        JFrame chatFrame = new JFrame();
        chatFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        chatFrame.setLayout(new BorderLayout());
        chatFrame.add(player.getChatPanel(), BorderLayout.CENTER);
        chatFrame.setTitle("Feed of Player " + lbPlayerID.getText());
        chatFrame.pack();
        chatFrame.setVisible(true);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miCopy = new JMenuItem("Copy UUID");
        JMenuItem miKick = new JMenuItem("Kick Player");

        miKick.setEnabled(lbStatus.getText().equals("ONLINE"));

        miCopy.addActionListener( e -> copyUUID());
        miKick.addActionListener( e -> listener.disconnect() );

        popup.add(new JLabel("Actions with Player " + lbPlayerID.getText()));
        popup.addSeparator();
        popup.add(miCopy);
        popup.add(miKick);
        return popup;
    }
}
