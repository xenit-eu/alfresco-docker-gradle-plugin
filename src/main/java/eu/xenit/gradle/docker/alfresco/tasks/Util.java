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

final class Util {

    private static final Object TVFS_LOCK = new Object();

    private Util() {
    }

    static void withGlobalTvfsLock(Runnable runnable) {
        Objects.requireNonNull(runnable);
        synchronized (TVFS_LOCK) {
            try {
                runnable.run();
            } finally {
                try {
                    // After doing something with truezip, unmount all the filesystems.
                    // This code can be run in the gradle daemon, and truezip is keeping global caches there
                    // When files change from underneath it, it may have cached wrong information about it
                    TVFS.umount();
                } catch (FsSyncException e) {
                    // This exception is intentionally ignored.
                    // Throwing exceptions in a finally block would swallow the original exception
                    // And it is no big deal if the archive can not be unmounted now, it will be unmounted during process shutdown anyways
                }
            }
        }
    }

    static void withWar(File warFile, Consumer<TFile> closure) {
        Objects.requireNonNull(warFile, "warFile");
        Objects.requireNonNull(closure, "closure");
        withGlobalTvfsLock(() -> {
            TConfig config = TConfig.get();
            config.setArchiveDetector(new TArchiveDetector("war|amp", new JarDriver(IOPoolLocator.SINGLETON)));
            TFile archive = new TFile(warFile);
            closure.accept(archive);
        });
    }
}
