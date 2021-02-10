package eu.xenit.gradle.docker.tasks;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.tasks.extension.internal.DockerfileWithSmartCopyExtensionInternal;
import javax.annotation.Nullable;
import org.gradle.api.Transformer;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

public class DockerfileWithCopyTask extends Dockerfile implements DockerfileWithSmartCopyExtensionInternal {

    private int copyFileCounter = 0;

    private final CopySpec copyFileCopySpec;

    private String createCopyFileStagingDirectory() {
        copyFileCounter++;
        return "copyFile/" + copyFileCounter;
    }

    @Override
    public void smartCopy(String file, String destinationInImage) {
        smartCopy(getProject().file(file), destinationInImage);
    }

    @Override
    public void smartCopy(java.io.File file, String destinationInImage) {
        smartCopy(getProject().provider(() -> file), destinationInImage);
    }

    @Override
    public void smartCopy(Provider<java.io.File> file, String destinationInImage) {
        smartCopy(file, getProject().provider(() -> destinationInImage));
    }

    @Override
    public void smartCopy(Provider<java.io.File> file, Provider<String> destinationInImage) {
        smartCopy(file, destinationInImage, null);
    }

    @Override
    public void smartCopy(Provider<java.io.File> file, Provider<String> destinationInImage,
            @Nullable Transformer<FileCollection, java.io.File> transformer) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(transformer == null ? file : file.map(transformer).orElse(getProject().files()));
            // Patch up the copySpec when the file to copy is a directory
            // The behavior of a copyspec is different when using from() on a file or on a directory
            // In case of a file, it is copied to the directory specified with into()
            // In case of a directory, its contents are copied to the directory specified with into()
            // To patch up this difference, we re-introduce the directory name in the path for every copied file
            copySpec.eachFile(fileCopyDetails -> {
                if (file.get().isDirectory() && transformer == null) {
                    String path = fileCopyDetails.getPath();
                    String filePath = path.substring(stagingDirectory.length());
                    fileCopyDetails.setPath(stagingDirectory + "/" + file.get().getName() + filePath);
                }
            });
        });
        if (transformer == null) {
            copyFile(destinationInImage
                    .flatMap(d -> file.map(f -> new CopyFile(stagingDirectory + "/" + f.getName(), d))));
        } else {
            copyFile(destinationInImage.map(d -> new CopyFile(stagingDirectory + "/", d + "/")));
        }
        getInputs().files(file).withPropertyName("copyFile." + copyFileCounter).optional();
    }

    @Override
    public void smartCopy(FileCollection files, String destinationInImage) {
        smartCopy(files, getProject().provider(() -> destinationInImage));
    }

    @Override
    public void smartCopy(FileCollection files, Provider<String> destinationInImage) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(files);
        });
        copyFile(destinationInImage.map(d -> new CopyFile(stagingDirectory + "/", d + "/")));
        getInputs().files(files).withPropertyName("copyFile." + copyFileCounter);
    }


    public DockerfileWithCopyTask() {
        super();
        copyFileCopySpec = getProject().copySpec();
    }

    @TaskAction
    public void copyFilesToStagingDirectory() {
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
        for (int i = 1; i <= copyFileCounter; i++) {
            java.io.File copyFile = copyFileDirectory.get().dir(Integer.toString(i)).getAsFile();
            if (!copyFile.exists() && !copyFile.mkdirs()) {
                throw new UncheckedIOException("Cannot create folder " + copyFile);
            }
        }
    }
}
