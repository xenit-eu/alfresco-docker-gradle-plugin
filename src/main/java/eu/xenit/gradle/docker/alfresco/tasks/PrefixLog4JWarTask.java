package eu.xenit.gradle.docker.alfresco.tasks;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class PrefixLog4JWarTask extends AbstractWarEnrichmentTask {

    private static final String LOG4J_ROOT_LOGGER = "log4j.rootLogger";
    private static final String LOG4J_CONVERSION_PATTERN = "log4j.appender.Console.layout.ConversionPattern";
    private final Property<String> log4JProperties = getProject().getObjects().property(String.class)
            .convention("/WEB-INF/classes/log4j.properties");

    private final Property<String> prefix = getProject().getObjects().property(String.class);

    @Input
    public Property<String> getPrefix() {
        return prefix;
    }

    @Input
    public Property<String> getLog4JProperties() {
        return log4JProperties;
    }

    @TaskAction
    public void prefixLog4J() throws IOException {
        File outputWar = getOutputWar().get().getAsFile();
        FileUtils.copyFile(getInputWar().getAsFile().get(), outputWar);
        Util.withWar(outputWar, war -> {
            TFile propertiesFile = new TFile(war.getAbsolutePath() + getLog4JProperties().get());

            if (propertiesFile.exists()) {
                Properties log4jProperties = new Properties();
                try (InputStream propertiesFileInput = new TFileInputStream(propertiesFile)) {
                    log4jProperties.load(propertiesFileInput);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                prefixProperties(log4jProperties);

                try (OutputStream propertiesFileOutput = new TFileOutputStream(propertiesFile)) {
                    log4jProperties.store(propertiesFileOutput, null);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

            }
        });
    }

    private void prefixProperties(Properties log4jProperties) {
        if (log4jProperties.containsKey(LOG4J_ROOT_LOGGER)) {
            String rootLogger = log4jProperties.getProperty(LOG4J_ROOT_LOGGER);
            log4jProperties.setProperty(LOG4J_ROOT_LOGGER, rootLogger.replace(", File", ""));
        }
        if (log4jProperties.containsKey(LOG4J_CONVERSION_PATTERN)) {
            String conversionPattern = log4jProperties.getProperty(LOG4J_CONVERSION_PATTERN);
            log4jProperties.setProperty(LOG4J_CONVERSION_PATTERN, "[" + getPrefix().get() + "] " + conversionPattern);
        }
    }


}
