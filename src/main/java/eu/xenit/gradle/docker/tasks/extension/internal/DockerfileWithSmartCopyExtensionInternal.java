package eu.xenit.gradle.docker.tasks.extension.internal;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.tasks.extension.DockerfileWithSmartCopyExtension;
import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface DockerfileWithSmartCopyExtensionInternal extends DockerfileWithSmartCopyExtension {

    void smartCopy(Provider<File> files, Provider<String> destinationInImage,
            @Nullable Transformer<FileCollection, File> transformer);

    static DockerfileWithSmartCopyExtensionInternal get(Dockerfile dockerfile) {
        if (dockerfile instanceof DockerfileWithSmartCopyExtensionInternal) {
            return (DockerfileWithSmartCopyExtensionInternal) dockerfile;
        } else {
            return dockerfile.getConvention().getPlugin(DockerfileWithSmartCopyExtensionInternal.class);
        }
    }
}
