package alphaComplex;

import alphaComplex.core.logging.LoggerFactory;
import alphaComplex.core.logging.ParanoiaLogger;
import alphaComplex.visuals.ServerFrame;

import javax.swing.event.ListDataEvent;

public class AlphaComplex {

    public static void main(String[] args) {

        if(args.length > 0) {
            ParanoiaLogger.debugMode = args[0].equals("-v");
        }

        ParanoiaLogger logger = LoggerFactory.getLogger();
        logger.info("Hello Computer!");
        new ServerFrame().setVisible(true);
    }

}
