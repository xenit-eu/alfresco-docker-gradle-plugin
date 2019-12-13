package eu.xenit.gradle.tasks;

import de.schlichtherle.truezip.file.TFile;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.commons.io.FileUtils;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class InjectFilesInWarTask extends AbstractInjectFilesInWarTask {
    /**
     * Target location to copy the files to inside the war
     */
    private Property<String> targetDirectory = getProject().getObjects().property(String.class);

    @Input
    public Property<String> getTargetDirectory() {
        return targetDirectory;
    }

    @TaskAction
    public void injectFiles() throws IOException {
        File outputWar = getOutputWar().getAsFile().get();
        FileUtils.copyFile(getInputWar().getAsFile().get(), outputWar);
        Util.withWar(outputWar, war -> {
            TFile warJarFolder = new TFile(war.getAbsolutePath() + getTargetDirectory().get());
            warJarFolder.mkdirs();

            try {
                for (File file : getSourceFiles()) {
                    new TFile(file).cp_rp(new TFile(warJarFolder, file.getName()));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}
