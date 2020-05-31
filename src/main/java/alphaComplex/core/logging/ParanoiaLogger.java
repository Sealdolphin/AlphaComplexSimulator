package alphaComplex.core.logging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParanoiaLogger {

    public interface LogListener {
        void updateLogs(String log);
    }

    public static boolean debugMode = false;
    private final List<LogListener> listeners = new ArrayList<>();

    ParanoiaLogger() {}

    private enum LogLevel {
        INFO,
        DEBUG,
        WARNING,
        STACKTRACE,
        ERROR
    }

    public void addLogListener(LogListener listener) {
        listeners.add(listener);
    }

    private final List<String> logMessages = new ArrayList<>();

    public void info(String message) {
        createLog(LogLevel.INFO, message);
    }

    public void error(String message) {
        createLog(LogLevel.ERROR, message);
    }

    public void warning(String message) {
        createLog(LogLevel.WARNING, message);
    }

    public void debug(String message) {
        if(debugMode)
            createLog(LogLevel.DEBUG, message);
    }

    public void exception(Throwable e) {
        createLog(LogLevel.ERROR, e.getLocalizedMessage());
        String stack = Arrays
            .stream(e.getStackTrace())
            .map(StackTraceElement::toString)
            .collect(Collectors.joining("\n"));
        createLog(LogLevel.STACKTRACE, stack);
    }

    private void createLog(LogLevel level, String message) {
        LocalDateTime now = LocalDateTime.now();
        String log = "[" + now.toString() + "] [" + level + "]: " + message;
        logMessages.add(log);
        System.out.println(log);
        listeners.forEach(l -> l.updateLogs(log));
    }

    public String[] requestHistory() {
        return logMessages.toArray(new String[0]);
    }

}
