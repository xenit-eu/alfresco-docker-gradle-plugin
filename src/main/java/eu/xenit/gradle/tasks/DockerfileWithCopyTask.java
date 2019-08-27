package eu.xenit.gradle.tasks;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;

public class DockerfileWithCopyTask extends Dockerfile {

    private abstract class DockerCopyAction {

        abstract public CopySpec getCopySpec();

        public CopyFileInstruction getCopyInstruction() {
            return new CopyFileInstruction(getTemporaryDirectory(), destinationInImage);
        }

        abstract public boolean isEmpty();

        protected final String destinationInImage;
        protected final int copyActionIndex;

        DockerCopyAction(String destinationInImage) {
            this.destinationInImage = destinationInImage;
            copyActionIndex = ++copyActionCounter;
            getInputs().property("dockerCopyAction." + copyActionIndex + ".dest", destinationInImage);
        }

        public String getTemporaryDirectory() {
            return "copyAction" + copyActionIndex;
        }
    }

    private class DockerCopyCollectionAction extends DockerCopyAction {

        private final FileCollection files;

        private DockerCopyCollectionAction(FileCollection files, String destinationInImage) {
            super(destinationInImage);
            this.files = files;
            getInputs().files(files)
                    .withPropertyName("dockerCopyAction." + copyActionIndex + ".files");
        }

        @Override
        public CopySpec getCopySpec() {
            return getProject().copySpec(copySpec -> {
                copySpec.from(files);
                copySpec.into(getTemporaryDirectory());
            });
        }

        @Override
        public boolean isEmpty() {
            return files.isEmpty();
        }

    }

    private class DockerCopyFileAction extends DockerCopyAction {

        private final File file;

        private DockerCopyFileAction(File file, String destinationInImage) {
            super(destinationInImage);
            this.file = file;
            getInputs().file(file)
                    .withPropertyName("dockerCopyAction." + copyActionIndex + ".file");
        }


        @Override
        public CopySpec getCopySpec() {
            return getProject().copySpec(copySpec -> {
                copySpec.from(file);
                copySpec.into(getTemporaryDirectory());
            });
        }

        @Override
        public CopyFileInstruction getCopyInstruction() {
            return new CopyFileInstruction(getTemporaryDirectory() + "/" + file.getName(),
                    destinationInImage);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private int copyActionCounter = 0;

    private final List<DockerCopyAction> copyActions = new LinkedList<>();

    public void copyFile(File file, String destinationInImage) {
        copyActions.add(new DockerCopyFileAction(file, destinationInImage));
    }

    public void copyFile(FileCollection files, String destinationInImage) {
        copyActions.add(new DockerCopyCollectionAction(files, destinationInImage));
    }

    @TaskAction
    @Override
    public void create() {
        for (DockerCopyAction copyAction : copyActions) {
            if (!copyAction.isEmpty()) {
                getProject().delete(getDestFile().getParentFile().toPath().resolve(copyAction.getTemporaryDirectory())
                        .toFile());
                getProject().copy(copySpec -> {
                    copySpec.with(copyAction.getCopySpec());
                    copySpec.into(getDestFile().getParentFile());
                });
                getInstructions().add(copyAction.getCopyInstruction());
            }
        }

        super.create();
    }

}
