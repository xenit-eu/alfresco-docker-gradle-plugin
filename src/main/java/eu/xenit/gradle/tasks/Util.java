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
        TConfig config = TConfig.get();
        config.setArchiveDetector(new TArchiveDetector("war|amp", new JarDriver(IOPoolLocator.SINGLETON)));
        TFile archive = new TFile(warFile);
        try {
            closure.accept(archive);
        } finally {
            try {
                TVFS.umount(archive);
            } catch (FsSyncException ignored) {
                // This exception is intentionally ignored.
                // Throwing exceptions in a finally block would swallow the original exception
                // And it is no big deal if the archive can not be unmounted now, it will be unmounted during process shutdown anyways
            }
        }
    }
}
