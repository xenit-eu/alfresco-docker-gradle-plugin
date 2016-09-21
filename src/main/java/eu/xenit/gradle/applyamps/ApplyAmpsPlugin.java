package eu.xenit.gradle.applyamps;

import eu.xenit.gradle.docker.DockerBuildBehavior;
import eu.xenit.gradle.docker.DockerPlugin;
import eu.xenit.gradle.tasks.*;
import org.alfresco.repo.module.tool.WarHelperImpl;
import org.apache.log4j.Logger;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thijs on 9/20/16.
 */
public class ApplyAmpsPlugin implements Plugin<Project> {

    public static final String BASE_ALFRESCO_WAR = "baseAlfrescoWar";
    public static final String ALFRESCO_AMP = "alfrescoAmp";
    public static final String BASE_SHARE_WAR = "baseShareWar";
    public static final String SHARE_AMP = "shareAmp";
    public static final String ALFRESCO_DE = "alfrescoDE";
    public static final String ALFRESCO_SM = "alfrescoSM";
    public static final String SHARE_SM = "shareSM";
    public static final String ALFRESCO = "Alfresco";
    public static final String SHARE = "Share";
    public static final String LABEL_PREFIX = "eu.xenit.gradle-plugin.";
    private static final String TASK_GROUP = "Alfresco";

    public void apply(Project project) {
        project.getConfigurations().create(BASE_ALFRESCO_WAR);
        project.getConfigurations().create(ALFRESCO_AMP);
        project.getConfigurations().create(BASE_SHARE_WAR);
        project.getConfigurations().create(SHARE_AMP);
        project.getConfigurations().create(ALFRESCO_DE);
        project.getConfigurations().create(ALFRESCO_SM);
        project.getConfigurations().create(SHARE_SM);

        project.getPluginManager().apply(DockerPlugin.class);

        DockerAlfrescoExtension dockerAlfrescoExtension = project.getExtensions().create("dockerAlfresco", DockerAlfrescoExtension.class);

        List<WarLabelOutputTask> alfrescoWarEnrichmentTasks = warEnrichmentChain(project, ALFRESCO);
        List<WarLabelOutputTask> shareEnrichmentTasks = warEnrichmentChain(project, SHARE);
        DockerfileWithWarsTask dockerfile = getDockerFileTask(dockerAlfrescoExtension, project);

        DockerBuildBehavior dockerBuildBehavior = new DockerBuildBehavior(dockerAlfrescoExtension::getDockerBuild, dockerfile);
        dockerBuildBehavior.apply(project);

        project.afterEvaluate(project1 -> {
            updateDockerFileTask(project1, dockerfile, alfrescoWarEnrichmentTasks, shareEnrichmentTasks, dockerAlfrescoExtension);
        });
    }

    private DockerfileWithWarsTask getDockerFileTask(DockerAlfrescoExtension dockerAlfrescoExtension, Project project1) {
        DockerfileWithWarsTask dockerfile = project1.getTasks().create("createDockerFile",
                DockerfileWithWarsTask.class);
        dockerfile.setDestFile(new File(project1.getBuildDir().getAbsolutePath() + "/docker/Dockerfile"));
        dockerfile.setBaseImage(dockerAlfrescoExtension.getBaseImageSupplier());
        dockerfile.setLeanImageSupplier(() -> dockerAlfrescoExtension.getLeanImage());
        return dockerfile;
    }

    private void updateDockerFileTask(Project project1, DockerfileWithWarsTask dockerfile, List<WarLabelOutputTask> alfrescoTasks, List<WarLabelOutputTask> shareTasks, DockerAlfrescoExtension dockerAlfrescoExtension) {
        if(dockerAlfrescoExtension.getLeanImage()){
            dockerfile.setRemoveExistingWar(false);
        }
        Configuration alfrescoBaseWar = project1.getConfigurations().getByName(BASE_ALFRESCO_WAR);
        if(!alfrescoBaseWar.isEmpty()) {
            //don't add base Alfresco war when making lean image
            if(!dockerAlfrescoExtension.getLeanImage()) {
                dockerfile.addWar("alfresco", alfrescoBaseWar);
            }
            for (WarLabelOutputTask task : alfrescoTasks) {
                dockerfile.addWar("alfresco", task);
            }
        }
        Configuration shareBaseWar = project1.getConfigurations().getByName(BASE_SHARE_WAR);
        if(!shareBaseWar.isEmpty()) {
            //don't add base Share war when making lean image
            if(!dockerAlfrescoExtension.getLeanImage()){
                dockerfile.addWar("share", shareBaseWar);
            }
            for (WarLabelOutputTask task : shareTasks) {
                dockerfile.addWar("share", task);
            }
        }
    }

    private List<WarLabelOutputTask> warEnrichmentChain(Project project, final String warName) {
        Configuration baseWar = project.getConfigurations().getByName("base"+warName+"War");

        WarEnrichmentTask resolveTask = project.getTasks().create("resolve" + warName + "War", StripAlfrescoWarTask.class, stripAlfrescoWarTask -> {
            stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.MANIFEST_FILE);
            if(warName.equals(ALFRESCO)) {
                stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.VERSION_PROPERTIES);
            }
        });
        resolveTask.setGroup(TASK_GROUP);
        resolveTask.setInputWar(baseWar);


        final List<WarEnrichmentTask> tasks = new ArrayList<>();

        tasks.add(project.getTasks().create("apply"+warName+"SM", InjectFilesInWarTask.class, injectFilesInWarTask -> {
            injectFilesInWarTask.setTargetDirectory("/WEB-INF/lib/");
            injectFilesInWarTask.setSourceFiles(project.getConfigurations().getByName(warName.toLowerCase()+"SM"));
        }));
        if (warName.equals(ALFRESCO)) {
            tasks.add(project.getTasks().create("apply" + warName + "DE", InjectFilesInWarTask.class, injectFilesInWarTask -> {
                injectFilesInWarTask.setTargetDirectory("/WEB-INF/classes/dynamic-extensions/bundles/");
                injectFilesInWarTask.setSourceFiles(project.getConfigurations().getByName(warName.toLowerCase() + "DE"));
            }));
        }
        tasks.add(project.getTasks().create("apply"+warName+"Amp", InstallAmpsInWarTask.class, installAmpsInWarTask -> {
            installAmpsInWarTask.setSourceFiles(project.getConfigurations().getByName(warName.toLowerCase()+"Amp"));
        }));

        for(WarEnrichmentTask task: tasks) {
            task.setInputWar(resolveTask);
            task.setGroup(TASK_GROUP);
        }

        List<WarLabelOutputTask> outputTasks = new ArrayList<>(tasks);
        outputTasks.add(0, resolveTask);

        MergeWarsTask mergeWarsTask = project.getTasks().create("merge"+warName+"War", MergeWarsTask.class);
        mergeWarsTask.setGroup(TASK_GROUP);

        for(WarLabelOutputTask task: outputTasks) {
            mergeWarsTask.withLabels(task);
        }
        mergeWarsTask.setInputWars(() -> outputTasks.stream().map(WarOutputTask::getOutputWar).collect(Collectors.toList()));

        return outputTasks;
    }
}
