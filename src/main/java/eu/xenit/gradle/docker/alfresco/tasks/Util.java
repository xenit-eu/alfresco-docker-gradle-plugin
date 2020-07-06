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
    private static Exception tvfsUmountException = null;

    private Util() {
    }

    static void withGlobalTvfsLock(Runnable runnable) {
        Objects.requireNonNull(runnable);
        synchronized (TVFS_LOCK) {
            try {
                if (tvfsUmountException != null) {
                    throw new IllegalStateException(
                            "A previous truezip unmount operation failed. This process (gradle daemon?) is in an undefined state and can not perform truezip operations anymore.",
                            tvfsUmountException);
                }
                runnable.run();
            } finally {
                try {
                    // After doing something with truezip, unmount all the filesystems.
                    // This code can be run in the gradle daemon, and truezip is keeping global caches there
                    // When files change from underneath it, it may have cached wrong information about it
                    TVFS.umount();
                } catch (FsSyncException e) {
                    // Something went wrong during unmounting.
                    // We can't do anything about that here. Throwing exceptions in a finally block would swallow the original exception.
                    // We now know that unmounting TVFS has failed, so we remember that.
                    // We throw it the next time a truezip operation is attempted. (It will likely fail with a strange message otherwise)
                    tvfsUmountException = e;
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
