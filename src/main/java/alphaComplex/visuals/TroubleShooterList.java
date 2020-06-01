package alphaComplex.visuals;

import alphaComplex.core.networking.TroubleShooterClient;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.List;

public class TroubleShooterList extends JPanel {

    private final List<TroubleShooterClient> model;

    public TroubleShooterList(List<TroubleShooterClient> model) {
        this.model = model;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        refreshComponents();
    }

    public void refreshComponents() {
        removeAll();
        if(model == null) return;
        for (TroubleShooterClient troubleShooterClient : model) {
            addComponentToLayout(troubleShooterClient.getVisuals());
        }
        updateUI();
    }

    private void addComponentToLayout(JComponent component) {
        component.setAlignmentX(CENTER_ALIGNMENT);
        component.setMaximumSize(new Dimension(
            Short.MAX_VALUE, component.getPreferredSize().height
        ));
        add(component);
        add(Box.createVerticalStrut(5));
    }
}
