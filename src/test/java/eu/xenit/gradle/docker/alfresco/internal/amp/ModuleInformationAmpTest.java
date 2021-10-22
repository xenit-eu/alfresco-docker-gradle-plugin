package eu.xenit.gradle.docker.alfresco.internal.amp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ModuleInformationAmpTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void createFromAmp() throws IOException {
        File ampFile = temporaryFolder.newFile("test-amp.amp");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(ampFile))) {
            ZipEntry modulePropertiesZipEntry = new ZipEntry("module.properties");
            zipOutputStream.putNextEntry(modulePropertiesZipEntry);
            Properties moduleProperties = new Properties();

            moduleProperties.put("module.id", "eu.xenit.test123");
            moduleProperties.put("module.version", "1.3.4");
            moduleProperties.put("module.title", "TEST AMP");
            moduleProperties.put("module.description", "A test AMP");

            ByteArrayOutputStream modulePropertiesStream = new ByteArrayOutputStream();
            moduleProperties.store(modulePropertiesStream, "");
            zipOutputStream.write(modulePropertiesStream.toByteArray());
            zipOutputStream.closeEntry();
        }
        ModuleInformation moduleInformation = new ModuleInformationAmp(ampFile);

        assertEquals("eu.xenit.test123", moduleInformation.getId());
        assertEquals("1.3.4", moduleInformation.getVersion());
    }

    @Test
    public void createFromJar() throws IOException {

        File jarFile = temporaryFolder.newFile("test-jar.jar");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(jarFile))) {
            // Empty
        }

        ModuleInformation moduleInformation = new ModuleInformationAmp(jarFile);

        InvalidModuleException exception = assertThrows(InvalidModuleException.class, () -> {
            moduleInformation.getId();
        });

        assertTrue("Has the JAR file name", exception.getMessage().contains("test-jar.jar"));
        assertTrue("Has the parent exception message", exception.getMessage().contains("NoSuchFileException: /module.properties"));
    }

    @Test
    public void createFromInvalidAmp() throws IOException {

        File jarFile = temporaryFolder.newFile("test-amp.amp");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(jarFile))) {
            zipOutputStream.putNextEntry(new ZipEntry("module.properties"));
            zipOutputStream.write("module.id=test-amp\n".getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }

        ModuleInformation moduleInformation = new ModuleInformationAmp(jarFile);

        InvalidModuleException exception = assertThrows(InvalidModuleException.class, () -> {
            moduleInformation.getId();
        });

        assertTrue("Has the AMP file name", exception.getMessage().contains("test-amp.amp"));
        assertTrue("Has the error message", exception.getMessage().contains("The following module properties need to be defined"));
    }

}
