package eu.xenit.gradle.docker.internal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.gradle.StartParameter;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.logging.configuration.WarningMode;

public class Deprecation {

    private static class Warning extends RuntimeException {

        public Warning(String message) {
            super(message);
        }
    }

    private static WarningMode warningMode = WarningMode.Summary;
    private static ShowStacktrace printStacktrace = ShowStacktrace.INTERNAL_EXCEPTIONS;
    private static List<Warning> warnings = new LinkedList<>();

    private static final Logger LOGGER = Logging.getLogger(Deprecation.class);

    private Deprecation() {
    }

    public static void setStartParameter(StartParameter startParameter) {
        warningMode = startParameter.getWarningMode();
        printStacktrace = startParameter.getShowStacktrace();
    }

    private static void printWarning(Warning warning) {
        if (printStacktrace == ShowStacktrace.ALWAYS || printStacktrace == ShowStacktrace.ALWAYS_FULL) {
            LOGGER.warn(warning.getMessage(), warning);
        } else {
            LOGGER.warn(warning.getMessage()
                    + "\t(Run with --stacktrace to get the full stack trace of this deprecation warning.)");
        }
    }

    public static void warnDeprecatedReplacedBy(String newMethod) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String oldMethod = stackTraceElements[1].getMethodName();
        String oldClass = stackTraceElements[1].getClassName();
        createWarning(
                oldClass + "#" + oldMethod + " is deprecated and will be removed in the next version. Use " + newMethod
                        + " instead.", 1);
    }

    public static void warnDeprecatedReplaced(String method, String newMethod) {
        createWarning(method + " is deprecated and will be removed in the next version. Use " + newMethod + " instead.",
                1);
    }

    public static void warnDeprecation(String message) {
        createWarning(message, 1);
    }

    private static void createWarning(String message, int stripTraces) {
        try {
            throw new Warning(message);
        } catch (Warning warning) {
            StackTraceElement[] stackTraceElements = warning.getStackTrace();
            warning.setStackTrace(
                    Arrays.copyOfRange(stackTraceElements, stripTraces + 1, stackTraceElements.length - stripTraces));
            if (warningMode == WarningMode.All) {
                printWarning(warning);
            }
            warnings.add(warning);
            if (warningMode.name().equals("Fail")) {
                throw warning;
            }
        }
    }

    public static void printSummary() {
        if (warningMode == WarningMode.Summary && !warnings.isEmpty()) {
            LOGGER.warn(
                    "Deprecated features were used in this build, making it incompatible with alfresco-docker-plugin 5.0.\n"+
                            "Use --warning-mode all to show individual deprecation warnings.");
        }
    }
}
