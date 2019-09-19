package eu.xenit.gradle.tasks;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import java.io.File;
import java.nio.file.Path;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;

public class DockerfileWithCopyTask extends Dockerfile {
    private int copyFileCounter = 0;

    private final CopySpec copyFileCopySpec;

    private String createCopyFileStagingDirectory() {
        copyFileCounter++;
        return "copyFile/"+copyFileCounter;
    }

    public void copyFile(File file, String destinationInImage) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(file);
        });
        getInstructions().add(new CopyFileInstruction(stagingDirectory+"/"+file.getName(), destinationInImage));
        getInputs().files(file).withPropertyName("copyFile."+copyFileCounter);
    }

    public void copyFile(FileCollection files, String destinationInImage) {
        String stagingDirectory = createCopyFileStagingDirectory();
        copyFileCopySpec.into(stagingDirectory, copySpec -> {
            copySpec.from(files);
        });
        getInstructions().add(new CopyFileInstruction(stagingDirectory, destinationInImage));
        getInputs().files(files).withPropertyName("copyFile."+copyFileCounter);
    }

    public DockerfileWithCopyTask() {
        super();
        copyFileCopySpec = getProject().copySpec();
    }

    @TaskAction
    @Override
    public void create() {
        Path dockerfileDirectory = getDestFile().getParentFile().toPath();
        Path copyFileDirectory = dockerfileDirectory.resolve("copyFile");
        getProject().delete(copyFileDirectory);
        getProject().copy(copySpec -> {
            copySpec.with(copyFileCopySpec);
            copySpec.into(dockerfileDirectory);
        });

        super.create();
    }

}
