package eu.xenit.gradle.docker.tasks.extension;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import java.io.File;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface DockerfileWithSmartCopyExtension {

    /**
     * smartCopy copies a file from anywhere into a docker image.
     * <p>
     * It works similar to {@link Dockerfile#copyFile(String, String)}, except smartCopy is able to directly copy a file from anywhere into the docker image.
     * Copying to the build context is not necessary, because smartCopy handles this automatically.
     *
     * @param file               The file to copy, as evaluated by {@link org.gradle.api.Project#file(Object)}
     * @param destinationInImage Destination file or directory inside the docker image
     */
    void smartCopy(String file, String destinationInImage);

    /**
     * smartCopy copies a file from anywhere into a docker image.
     *
     * @param file               The file to copy
     * @param destinationInImage Destination file or directory inside the docker image
     * @see #smartCopy(String, String)
     */
    void smartCopy(java.io.File file, String destinationInImage);

    /**
     * smartCopy copies a file from anywhere into a docker image.
     *
     * @param file               The file to copy
     * @param destinationInImage Destination file or directory inside the docker image
     * @see #smartCopy(String, String)
     */
    void smartCopy(Provider<File> file, String destinationInImage);

    /**
     * smartCopy copies a file from anywhere into a docker image.
     *
     * @param file               The file to copy
     * @param destinationInImage Destination file or directory inside the docker image
     * @see #smartCopy(String, String)
     */
    void smartCopy(Provider<File> file, Provider<String> destinationInImage);

    /**
     * smartCopy copies files from anywhere into a docker image.
     *
     * @param files              A collection of files to copy
     * @param destinationInImage Destination directory inside the docker image where all files will be copied to
     * @see #smartCopy(String, String)
     */
    void smartCopy(FileCollection files, String destinationInImage);

    /**
     * smartCopy copies files from anywhere into a docker image.
     *
     * @param files              A collection of files to copy
     * @param destinationInImage Destination directory inside the docker image where all files will be copied to
     * @see #smartCopy(String, String)
     */
    void smartCopy(FileCollection files, Provider<String> destinationInImage);

    static DockerfileWithSmartCopyExtension get(Dockerfile dockerfile) {
        if (dockerfile instanceof DockerfileWithSmartCopyExtension) {
            return (DockerfileWithSmartCopyExtension) dockerfile;
        } else {
            return dockerfile.getConvention().getPlugin(DockerfileWithSmartCopyExtension.class);
        }
    }
}
