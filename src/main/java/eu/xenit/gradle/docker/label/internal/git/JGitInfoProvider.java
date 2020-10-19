package eu.xenit.gradle.docker.label.internal.git;

import eu.xenit.gradle.docker.internal.JenkinsUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.URIish;
import org.gradle.api.file.ProjectLayout;

/**
 * Created by thijs on 1/23/17.
 */
public class JGitInfoProvider {

    public static JGitInfoProvider createProviderForProject(ProjectLayout project) {
        Path projectFolder = project.getProjectDirectory().getAsFile().toPath().toAbsolutePath();
        Path gitFolder = projectFolder.resolve(".git");
        while (Files.notExists(gitFolder)) {
            projectFolder = projectFolder.getParent();
            if (projectFolder == null) {
                return null;
            }
            gitFolder = projectFolder.resolve(".git");
        }

        try {
            return new JGitInfoProvider(gitFolder.toFile());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Repository gitRepo;
    private Git git;

    public JGitInfoProvider(File gitFolder) throws IOException {
        this.gitRepo = new FileRepository(gitFolder);
        this.git = new Git(gitRepo);
    }

    public String getBranch() {
        //workaround because Jenkins uses git in detached head state
        if (!"local".equals(JenkinsUtil.getBranch())) {
            return JenkinsUtil.getBranch();
        }
        try {
            return gitRepo.getBranch();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getCommitChecksum() {
        try {
            return getLastRevCommit().getName();
        } catch (NoHeadException e) {
            return "<none>";
        }
    }

    private RevCommit getLastRevCommit() throws NoHeadException {
        final RevCommit[] commit = new RevCommit[1];
        try {
            git.log().setMaxCount(1).call().forEach(revCommit -> {
                commit[0] = revCommit;
            });
        } catch (NoHeadException e) {
            throw e;
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
        return commit[0];
    }

    public String getOrigin() {
        final URIish origin = getUrIish();
        if (origin == null) {
            return null;
        }
        return origin.toString();
    }

    private URIish getUrIish() {
        final URIish[] origin = new URIish[1];
        try {
            git.remoteList().call().forEach(remoteConfig -> {
                if ("origin".equals(remoteConfig.getName())) {
                    origin[0] = remoteConfig.getURIs().get(0);
                }
            });
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
        return origin[0];
    }

    public URL getCommitURL() throws CannotConvertToUrlException {
        final URIish origin = getUrIish();
        if (origin == null) {
            throw new CannotConvertToUrlException("No origin is set");
        }
        String path = origin.getPath().split("\\.git\\z")[0];
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        try {
            if ("bitbucket.org".equals(origin.getHost())) {
                return new URL("https", origin.getHost(), "/" + path + "/commits/" + getCommitChecksum());
            } else if ("github.com".equals(origin.getHost())) {
                return new URL("https", origin.getHost(), "/" + path + "/commit/" + getCommitChecksum());
            } else {
                throw new CannotConvertToUrlException("The host is unknown");
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getCommitAuthor() {
        try {
            PersonIdent person = getLastRevCommit().getAuthorIdent();
            return person.getName() + " <" + person.getEmailAddress() + ">";
        } catch (NoHeadException e) {
            return System.getProperty("user.name");
        }
    }

    public String getCommitMessage() {
        try {
            return getLastRevCommit().getFullMessage().trim();
        } catch (NoHeadException e) {
            return "<none>";
        }
    }

}
