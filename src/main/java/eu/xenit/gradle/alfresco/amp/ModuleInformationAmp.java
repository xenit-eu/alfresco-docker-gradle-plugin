package eu.xenit.gradle.alfresco.amp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.function.Function;
import org.alfresco.repo.module.tool.ModuleDetailsHelper;
import org.alfresco.service.cmr.module.ModuleDetails;

class ModuleInformationAmp extends ModuleInformationFromModuleDetails {

    private final File file;
    private ModuleDetails moduleDetails;

    public ModuleInformationAmp(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    private <T> T withFilesystem(Function<? super FileSystem, T> fsConsumer) {
        URI u = URI.create("jar:" + file.toURI().toString());
        try (FileSystem fs = FileSystems.newFileSystem(u, Collections.emptyMap())) {
            return fsConsumer.apply(fs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> T withModuleProperties(Function<? super InputStream, T> inputStreamConsumer) {
        return withFilesystem(fs -> {
            try (InputStream inputStream = Files.newInputStream(fs.getPath("/module.properties"))) {
                return inputStreamConsumer.apply(inputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public ModuleDetails getModuleDetails() {
        if (moduleDetails == null) {
            moduleDetails = withModuleProperties(is -> {
                try {
                    return ModuleDetailsHelper.createModuleDetailsFromPropertiesStream(is);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        return moduleDetails;
    }

}
