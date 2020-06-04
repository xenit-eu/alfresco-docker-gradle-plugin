package eu.xenit.gradle.docker.tasks;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import java.nio.file.Path;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
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
        smartCopy(getProject().provider(() -> file), destinationInImage);
    }

    /**
     * smartCopy copies a file from anywhere into a docker image.
     *
     * @param file               The file to copy
     * @param destinationInImage Destination file or directory inside the docker image
     * @see #smartCopy(String, String)
     */
    public void smartCopy(Provider<java.io.File> file, String destinationInImage) {
        smartCopy(file, getProject().provider(() -> destinationInImage));
    }

    /**
     * smartCopy copies a file from anywhere into a docker image.
     *
     * @param file               The file to copy
     * @param destinationInImage Destination file or directory inside the docker image
     * @see #smartCopy(String, String)
     */
    public void smartCopy(Provider<java.io.File> file, Provider<String> destinationInImage) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(file);
            // Patch up the copySpec when the file to copy is a directory
            // The behavior of a copyspec is different when using from() on a file or on a directory
            // In case of a file, it is copied to the directory specified with into()
            // In case of a directory, its contents are copied to the directory specified with into()
            // To patch up this difference, we re-introduce the directory name in the path for every copied file
            copySpec.eachFile(fileCopyDetails -> {
                if (file.get().isDirectory()) {
                    String path = fileCopyDetails.getPath();
                    String filePath = path.substring(stagingDirectory.length());
                    fileCopyDetails.setPath(stagingDirectory+"/"+file.get().getName()+filePath);
                }
            });
        });
        copyFile(destinationInImage.flatMap(d -> file.map(f -> new CopyFile(stagingDirectory + "/"+f.getName(), d))));
        getInputs().files(file).withPropertyName("copyFile." + copyFileCounter);
    }
    /**
     * smartCopy copies files from anywhere into a docker image.
     *
     * @param files              A collection of files to copy
     * @param destinationInImage Destination directory inside the docker image where all files will be copied to
     * @see #smartCopy(String, String)
     */
    public void smartCopy(FileCollection files, String destinationInImage) {
        smartCopy(files, getProject().provider(() -> destinationInImage));
    }

    /**
     * smartCopy copies files from anywhere into a docker image.
     *
     * @param files              A collection of files to copy
     * @param destinationInImage Destination directory inside the docker image where all files will be copied to
     * @see #smartCopy(String, String)
     */
    public void smartCopy(FileCollection files, Provider<String> destinationInImage) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(files);
        });
        copyFile(destinationInImage.map(d -> new CopyFile(stagingDirectory, d)));
        getInputs().files(files).withPropertyName("copyFile." + copyFileCounter);
    }

    public DockerfileWithCopyTask() {
        super();
        copyFileCopySpec = getProject().copySpec();
    }

    @TaskAction
    @Override
    public void create() {
        // copyFile base directory
        Provider<Directory> copyFileDirectory = getDestDir().map(d -> d.dir("copyFile"));
        getProject().delete(copyFileDirectory);
        // Ensure empty directory exists
        getProject().copy(copySpec -> {
            copySpec.with(copyFileCopySpec);
            copySpec.into(getDestDir());
        });

        // Create non-existing directories, so the COPY command in the Dockerfile does not throw an error in case empty
        // FileCollections are added with smartCopy
        for(int i = 1; i <= copyFileCounter; i++) {
            java.io.File copyFile = copyFileDirectory.get().dir(Integer.toString(i)).getAsFile();
            if(!copyFile.exists()) {
                if(!copyFile.mkdir()) {
                    throw new UncheckedIOException("Cannot create folder "+copyFile);
                }
            }

        }

        super.create();
    }

}
