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

    private static final String PROP_UMOUNT_EXPERIMENT_BASE = "eu.xenit.gradle.docker.flags.TrueZipUmountExperiment";
    private static final String PROP_UMOUNT_EXPERIMENT_DISABLE_UMOUNT = PROP_UMOUNT_EXPERIMENT_BASE + ".disableUmount";
    private static final String PROP_UMOUNT_EXPERIMENT_LOGGING = PROP_UMOUNT_EXPERIMENT_BASE + ".logging";
    private static final String PROP_UMOUNT_EXPERIMENT_IGNORE_EXCEPTION =
            PROP_UMOUNT_EXPERIMENT_BASE + ".ignoreException";

    private static boolean isEnabled(String property) {
        return Boolean.parseBoolean(System.getProperty(property, "false"));
    }


    private Util() {
    }

    private static void umountExperimentLog(String message) {
        if (isEnabled(PROP_UMOUNT_EXPERIMENT_LOGGING)) {
            long threadId = Thread.currentThread().getId();
            System.err.println(String.format("TrueZipUmountExperiment[threadId=%d]: %s", threadId, message));
        }
    }

    static void withGlobalTvfsLock(Runnable runnable) {
        Objects.requireNonNull(runnable);
        umountExperimentLog("About to acquire global TVFS lock: " + TVFS_LOCK.toString());
        synchronized (TVFS_LOCK) {
            umountExperimentLog("Acquired global TVFS lock: " + TVFS_LOCK.toString());
            try {
                if (tvfsUmountException != null) {
                    if (isEnabled(PROP_UMOUNT_EXPERIMENT_IGNORE_EXCEPTION)) {
                        umountExperimentLog(
                                "Would have thrown IllegalStateException because of previous umount failure");
                    } else {
                        throw new IllegalStateException(
                                "A previous truezip unmount operation failed. This process (gradle daemon?) is in an undefined state and can not perform truezip operations anymore.",
                                tvfsUmountException);
                    }
                }
                runnable.run();
            } finally {
                try {
                    // After doing something with truezip, unmount all the filesystems.
                    // This code can be run in the gradle daemon, and truezip is keeping global caches there
                    // When files change from underneath it, it may have cached wrong information about it
                    if (isEnabled(PROP_UMOUNT_EXPERIMENT_DISABLE_UMOUNT)) {
                        umountExperimentLog("Not calling TVFS.umount() because umount is disabled");
                    } else {
                        umountExperimentLog("Calling TVFS.umount()");
                        TVFS.umount();
                        umountExperimentLog("Finished TVFS.umount()");
                    }
                } catch (FsSyncException e) {
                    if (isEnabled(PROP_UMOUNT_EXPERIMENT_LOGGING)) {
                        umountExperimentLog("TVFS.umount() threw exception");
                        e.printStackTrace(System.err);
                    }
                    tvfsUmountException = e;
                    // This exception is intentionally ignored.
                    // Throwing exceptions in a finally block would swallow the original exception
                }
            }
            umountExperimentLog("About to release global TVFS lock: " + TVFS_LOCK.toString());
        }
        umountExperimentLog("Released global TVFS lock: " + TVFS_LOCK.toString());
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
