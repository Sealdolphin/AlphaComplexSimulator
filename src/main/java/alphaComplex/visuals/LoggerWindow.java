package alphaComplex.visuals;

import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import paranoia.services.plc.AssetManager;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class LoggerWindow extends JFrame implements ParanoiaLogger.LogListener {

    private final ParanoiaLogger logger;
    private final JTextArea logArea = new JTextArea();
    private boolean autoUpdate = true;

    public LoggerWindow() {
        this.logger = LoggerFactory.getLogger();
        logger.addLogListener(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Paranoia server logs");
        setMinimumSize(new Dimension(300,300));

        createLogPanel();

        pack();
        setLocationRelativeTo(null);
    }

    private void createLogPanel() {
        getContentPane().setLayout(new BorderLayout());
        logArea.setFont(AssetManager.getBoldFont(15));
        logArea.setEditable(false);
        logArea.setOpaque(false);

        JButton btnScrollDown = new JButton("Stop following");
        btnScrollDown.addActionListener( e -> {
            autoUpdate = !autoUpdate;
            btnScrollDown.setText(
                autoUpdate ? "Stop following" : "Follow"
            );
        });

        add(new JScrollPane(
            logArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        ), BorderLayout.CENTER);
        add(btnScrollDown, BorderLayout.SOUTH);

        for (String log : logger.requestHistory()) {
            updateLogs(log);
        }

    }


    @Override
    public void updateLogs(String log) {
        logArea.append(log + "\n");
        if(autoUpdate)
            logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
