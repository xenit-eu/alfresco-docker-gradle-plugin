package eu.xenit.gradle.docker.internal.git;


import java.net.URL;

/**
 * Created by thijs on 1/23/17.
 */
public interface GitInfoProvider {

    String getBranch();

    String getCommitChecksum();

    String getOrigin();

    URL getCommitURL() throws CannotConvertToUrlException;

    String getCommitAuthor();

    String getCommitMessage();
}
