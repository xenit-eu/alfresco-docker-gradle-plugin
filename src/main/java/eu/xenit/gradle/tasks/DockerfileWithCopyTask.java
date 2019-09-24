package eu.xenit.gradle.tasks;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import java.nio.file.Path;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

public class DockerfileWithCopyTask extends Dockerfile {

    private int copyFileCounter = 0;

    private final CopySpec copyFileCopySpec;

    private String createCopyFileStagingDirectory() {
        copyFileCounter++;
        return "copyFile/" + copyFileCounter;
    }

    /**
     * smartCopy copies a file from anywhere into a docker image.
     * <p>
     * It works similar to {@link #copyFile(String, String)}, except smartCopy is able to directly copy a file from anywhere into the docker image.
     * Copying to the build context is not necessary, because smartCopy handles this automatically.
     *
     * @param file               The file to copy, as evaluated by {@link org.gradle.api.Project#file(Object)}
     * @param destinationInImage Destination file or directory inside the docker image
     */
    public void smartCopy(String file, String destinationInImage) {
        smartCopy(getProject().file(file), destinationInImage);
    }

    /**
     * smartCopy copies a file from anywhere into a docker image.
     *
     * @param file               The file to copy
     * @param destinationInImage Destination file or directory inside the docker image
     * @see #smartCopy(String, String)
     */
    public void smartCopy(java.io.File file, String destinationInImage) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(file);
        });
        copyFile(stagingDirectory + "/" + file.getName(), destinationInImage);
        getInputs().files(file).withPropertyName("copyFile." + copyFileCounter);
    }

    /**
     * smartCopy copies files from anywhere into a docker image.
     *
     * @param files               A collection of files to copy
     * @param destinationInImage Destination directory inside the docker image where all files will be copied to
     * @see #smartCopy(String, String)
     */
    public void smartCopy(FileCollection files, String destinationInImage) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(files);
        });
        copyFile(stagingDirectory, destinationInImage);
        getInputs().files(files).withPropertyName("copyFile." + copyFileCounter);
    }

    public DockerfileWithCopyTask() {
        super();
        copyFileCopySpec = getProject().copySpec();
    }

    @TaskAction
    @Override
    public void create() {
        Provider<Path> dockerfileDirectory = getDestFile().map(f -> f.getAsFile().getParentFile().toPath());
        Provider<Path> copyFileDirectory = dockerfileDirectory.map(p -> p.resolve("copyFile"));
        getProject().delete(copyFileDirectory);
        getProject().copy(copySpec -> {
            copySpec.with(copyFileCopySpec);
            copySpec.into(dockerfileDirectory);
        });

        super.create();
    }

}