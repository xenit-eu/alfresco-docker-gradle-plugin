package eu.xenit.gradle.git;


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
