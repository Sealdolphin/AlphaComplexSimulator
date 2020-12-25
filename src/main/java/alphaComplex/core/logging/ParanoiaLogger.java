package alphaComplex.core.logging;

import java.awt.Color;
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
        INFO("#000000"),
        DEBUG("#000000"),
        WARNING("#6D1111"),
        STACKTRACE("#004E5C"),
        ERROR("#9E0000");
        public String color;
        LogLevel(String color) { this.color = color; }
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

    private String trimLog(String log) {
        if(log == null) return "null";
        if(log.length() > 100) {
            List<String> rows = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < log.length(); i++) {
                char c = log.charAt(i);
                if(line.length() > 100 && (c == ' ' || c == ',')) {
                    rows.add(line.toString());
                    if(rows.size() > 5){
                        rows.add("...(showing only the first 5 rows)");
                        break;
                    }
                    line = new StringBuilder();
                } else line.append(c);
            }
            rows.add(line.toString());
            return String.join("\n", rows);
        } else {
            return log;
        }
    }

    private void createLog(LogLevel level, String message) {
        LocalDateTime now = LocalDateTime.now();
        String rawLog = "[" + now.toString() + "] [" + level + "]: " + trimLog(message);
        String log = "<font color=" + level.color + "><b>" + rawLog + "</b></font><br>";
        logMessages.add(log);
        System.out.println(rawLog);
        listeners.forEach(l -> l.updateLogs(log));
    }

    public String[] requestHistory() {
        return logMessages.toArray(new String[0]);
    }

}
