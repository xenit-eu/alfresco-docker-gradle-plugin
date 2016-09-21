package eu.xenit.gradle.tasks;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

import java.io.File;
import java.util.function.Consumer;

class Util {

    static void withWar(File warFile, Consumer<TFile> closure) {
        try {
            TConfig config = TConfig.get();
            config.setArchiveDetector(new TArchiveDetector("war|amp", new JarDriver(IOPoolLocator.SINGLETON)));
            TFile archive = new TFile(warFile);
            closure.accept(archive);
        } finally {
            try {
                TVFS.umount();
            } catch (FsSyncException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
