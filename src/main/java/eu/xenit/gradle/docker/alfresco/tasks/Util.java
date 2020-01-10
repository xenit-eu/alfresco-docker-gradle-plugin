package eu.xenit.gradle.docker.alfresco.tasks;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

class Util {

    private static final Object TVFS_LOCK = new Object();

    static void withGlobalTvfsLock(Runnable runnable) {
        Objects.requireNonNull(runnable);
        synchronized (TVFS_LOCK) {
            runnable.run();
        }
    }

    static void withWar(File warFile, Consumer<TFile> closure) {
        Objects.requireNonNull(warFile, "warFile");
        Objects.requireNonNull(closure, "closure");
        withGlobalTvfsLock(() -> {
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
        });
    }
}
