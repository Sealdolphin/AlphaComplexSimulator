package alphaComplex.visuals;

import alphaComplex.core.PlayerListener;
import paranoia.services.plc.AssetManager;
import paranoia.visuals.custom.ParanoiaImage;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

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

    public PlayerPanel (PlayerListener listener) {
        setLayout(new GridBagLayout());
        this.listener = listener;

        Font generalFont = AssetManager.getFont(20);
        lbName.setFont(generalFont);
        lbUUID.setFont(AssetManager.getFont(15));
        lbStatus.setFont(AssetManager.getFont(20, true, true, false));
        lbPlayerID.setFont(generalFont);
        profile.setPreferredSize(new Dimension(100,100));

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

        JButton btn = new JButton("X");
        btn.addActionListener(l -> listener.disconnect());
        add(btn, createGrid().at(1,3).get());
    }

    public void updateVisuals(String name, String uuid, boolean status, int id, BufferedImage image) {
        lbName.setText(name);
        lbUUID.setText(uuid);
        lbStatus.setText(status ? "ONLINE" : "OFFLINE");
        lbStatus.setForeground(status ? new Color(98, 160, 16) : new Color(170, 30, 30));
        lbPlayerID.setText("#" + id + ":");
        profile.changeImage(image);
        revalidate();
    }
}
