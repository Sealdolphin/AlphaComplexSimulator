package alphaComplex.core.logging;

public abstract class LoggerFactory {

    private static final ParanoiaLogger logger = new ParanoiaLogger();

    public static ParanoiaLogger getLogger() {
        return logger;
    }

}
