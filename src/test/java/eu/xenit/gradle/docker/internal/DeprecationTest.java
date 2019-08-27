package eu.xenit.gradle.docker.internal;

import eu.xenit.gradle.docker.internal.Deprecation.Warning;
import org.gradle.StartParameter;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.logging.configuration.WarningMode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DeprecationTest {

    @Before
    public void setup() {
        Deprecation.LOGGER = Mockito.mock(Logger.class);
    }

    @Test
    public void testDefaultDeprecationWarning() {
        StartParameter startParameter = new StartParameter();
        Deprecation.setStartParameter(startParameter);

        Deprecation.warnDeprecation("BLABLA");
        Mockito.verifyZeroInteractions(Deprecation.LOGGER);

        Deprecation.printSummary();
        Mockito.verify(Deprecation.LOGGER).warn(Mockito.contains("Deprecated features were used in this build"));
    }

    @Test
    public void testDeprecationWarningAll() {
        StartParameter startParameter = new StartParameter();
        startParameter.setWarningMode(WarningMode.All);
        Deprecation.setStartParameter(startParameter);

        Deprecation.warnDeprecation("BLABLA");
        Mockito.verify(Deprecation.LOGGER).warn(Mockito.contains("BLABLA\t(Run with --stacktrace"));

        Deprecation.printSummary();
        Mockito.verifyZeroInteractions(Deprecation.LOGGER);
    }

    @Test
    public void testDeprecationWarningStacktrace() {
        StartParameter startParameter = new StartParameter();
        startParameter.setWarningMode(WarningMode.All);
        startParameter.setShowStacktrace(ShowStacktrace.ALWAYS);
        Deprecation.setStartParameter(startParameter);

        Deprecation.warnDeprecation("BLABLA");
        Mockito.verify(Deprecation.LOGGER).warn(Mockito.eq("BLABLA"), Mockito.any(Deprecation.Warning.class));

        Deprecation.printSummary();
        Mockito.verifyZeroInteractions(Deprecation.LOGGER);
    }

    @Test(expected = Warning.class)
    public void testDeprecationErrorStacktrace() {
        StartParameter startParameter = new StartParameter();
        startParameter.setWarningMode(WarningMode.Fail);
        Deprecation.setStartParameter(startParameter);

        Deprecation.warnDeprecation("BLABLA");
    }

    @Test
    public void testDeprecatedReplacedBy() {
        StartParameter startParameter = new StartParameter();
        startParameter.setWarningMode(WarningMode.All);
        Deprecation.setStartParameter(startParameter);

        Deprecation.warnDeprecatedReplacedBy("def");
        Mockito.verify(Deprecation.LOGGER).warn(Mockito
                .contains(getClass().getCanonicalName().toString() + "#testDeprecatedReplacedBy is deprecated"));

        Deprecation.printSummary();
        Mockito.verifyZeroInteractions(Deprecation.LOGGER);
    }
}
