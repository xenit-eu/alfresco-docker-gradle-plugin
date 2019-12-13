package eu.xenit.gradle.docker;

import eu.xenit.gradle.docker.internal.Deprecation;
import groovy.lang.GString;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerBuildExtension {

    private String repository;

    public DockerBuildExtension(Project project) {
        repository = project.getName();
    }


    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    private List<String> tags = new ArrayList<>();

    public List<String> getTags() {
        return tags;
    }


    public void setTags(List<Object> tags) {
        List<String> converted = new ArrayList<String>();
        for (int i = 0; i < tags.size(); i++) {
            Object tag = tags.get(i);
            if (!(tag instanceof String) && !(tag instanceof GString)) {
                throw new IllegalArgumentException("Only strings and gstrings are supported.");
            }
            converted.add(tag.toString());
        }
        this.tags = converted;
    }

    /**
     * Do not modify tags before tagging the docker image
     */
    private boolean automaticTags = false;

    public boolean getAutomaticTags() {
        return automaticTags;
    }

    public void setAutomaticTags(boolean automaticTags) {
        this.automaticTags = automaticTags;
    }


    private boolean pull = true;

    public boolean getPull() {
        return pull;
    }

    public void setPull(boolean pull) {
        this.pull = pull;
    }

    /**
     * Do not use cache when building the image (default false)
     */
    private boolean noCache;

    public boolean getNoCache() {
        return noCache;
    }

    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    /**
     * Remove intermediate containers after a successful build (default true)
     */
    private boolean remove = true;

    public boolean getRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }
}
