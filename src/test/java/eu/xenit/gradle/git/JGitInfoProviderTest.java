package eu.xenit.gradle.git;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Created by thijs on 1/23/17.
 */
public class JGitInfoProviderTest {

    private GitInfoProvider gitInfo;

    @Before
    public void setup() throws IOException {
        gitInfo = new JGitInfoProvider(new File(".git"));
    }

    @Test
    public void testGetBranch() throws IOException {
        System.out.println(gitInfo.getBranch());
        assertNotNull(gitInfo.getBranch());
    }

    @Test
    public void testGetCommitChecksum() throws IOException {
        System.out.println(gitInfo.getCommitChecksum());
        assertNotNull(gitInfo.getCommitChecksum());
    }

    @Test
    public void testGetGitRepository() {
        System.out.println(gitInfo.getOrigin());
        assertNotNull(gitInfo.getOrigin());
    }

    @Test
    public void testGetCommitURL() throws CannotConvertToUrlException {
        try {
            System.out.println(gitInfo.getCommitURL());
            assertNotNull(gitInfo.getCommitURL());
        } catch(CannotConvertToUrlException ex) {
            assumeTrue(false); // Skip test
        }
    }

    @Test
    public void testGetCommitAuthor(){
        System.out.println(gitInfo.getCommitAuthor());
        assertNotNull(gitInfo.getCommitAuthor());
    }

    @Test
    public void testGetCommitMessage(){
        System.out.println(gitInfo.getCommitMessage());
        assertNotNull(gitInfo.getCommitMessage());
    }
}
