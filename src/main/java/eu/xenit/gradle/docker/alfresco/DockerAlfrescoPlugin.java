package eu.xenit.gradle.docker.alfresco;

import eu.xenit.gradle.docker.DockerBuildBehavior;
import eu.xenit.gradle.docker.DockerConfigPlugin;
import eu.xenit.gradle.docker.alfresco.tasks.DockerfileWithWarsTask;
import eu.xenit.gradle.docker.alfresco.tasks.InjectFilesInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.InstallAmpsInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.MergeWarsTask;
import eu.xenit.gradle.docker.alfresco.tasks.StripAlfrescoWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarEnrichmentTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarLabelOutputTask;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.module.tool.WarHelperImpl;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * Created by thijs on 9/20/16.
 */
public class DockerAlfrescoPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.docker-alfresco";

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

        project.getPluginManager().apply(DockerConfigPlugin.class);

        DockerAlfrescoExtension dockerAlfrescoExtension = project.getExtensions()
                .create("dockerAlfresco", DockerAlfrescoExtension.class, project);

        List<WarLabelOutputTask> alfrescoWarEnrichmentTasks = warEnrichmentChain(project, ALFRESCO);
        List<WarLabelOutputTask> shareEnrichmentTasks = warEnrichmentChain(project, SHARE);
        DockerfileWithWarsTask dockerfile = getDockerFileTask(dockerAlfrescoExtension, project);

        DockerBuildBehavior dockerBuildBehavior = new DockerBuildBehavior(dockerAlfrescoExtension.getDockerBuild(),
                dockerfile);
        dockerBuildBehavior.apply(project);

        project.afterEvaluate(project1 -> {
            updateDockerFileTask(project1, dockerfile, alfrescoWarEnrichmentTasks, shareEnrichmentTasks,
                    dockerAlfrescoExtension);
        });
    }

    private DockerfileWithWarsTask getDockerFileTask(DockerAlfrescoExtension dockerAlfrescoExtension,
            Project project1) {
        DockerfileWithWarsTask dockerfile = project1.getTasks().create("createDockerFile",
                DockerfileWithWarsTask.class);
        dockerfile.getBaseImage().set(dockerAlfrescoExtension.getBaseImage());
        return dockerfile;
    }

    private void updateDockerFileTask(Project project1, DockerfileWithWarsTask dockerfile,
            List<WarLabelOutputTask> alfrescoTasks, List<WarLabelOutputTask> shareTasks,
            DockerAlfrescoExtension dockerAlfrescoExtension) {
        dockerfile.getRemoveExistingWar().set(dockerAlfrescoExtension.getLeanImage().map(b -> !b));
        Configuration alfrescoBaseWar = project1.getConfigurations().getByName(BASE_ALFRESCO_WAR);
        dockerfile.addWar("alfresco", dockerAlfrescoExtension.getLeanImage().flatMap(isLeanImage -> {
            if (alfrescoBaseWar.isEmpty()) {
                return project1.provider(() -> null);
            } else {
                return project1.getLayout().file(project1.provider(alfrescoBaseWar::getSingleFile));
            }
        }));
        alfrescoTasks.forEach(t -> {
            dockerfile.addWar("alfresco",
                    project1.provider(() -> alfrescoBaseWar.isEmpty() ? null : t.getOutputWar().get()));
            dockerfile.dependsOn(t);
        });

        Configuration shareBaseWar = project1.getConfigurations().getByName(BASE_SHARE_WAR);
        dockerfile.addWar("share", dockerAlfrescoExtension.getLeanImage().flatMap(isLeanImage -> {
            if (shareBaseWar.isEmpty()) {
                return project1.provider(() -> null);
            } else {
                return project1.getLayout().file(project1.provider(shareBaseWar::getSingleFile));
            }
        }));
        shareTasks.forEach(t -> {
            dockerfile.addWar("share", project1.provider(() -> shareBaseWar.isEmpty() ? null : t.getOutputWar().get()));
            dockerfile.dependsOn(t);
        });
    }

    private List<WarLabelOutputTask> warEnrichmentChain(Project project, final String warName) {
        Configuration baseWar = project.getConfigurations().getByName("base" + warName + "War");

        WarEnrichmentTask resolveTask = project.getTasks()
                .create("strip" + warName + "War", StripAlfrescoWarTask.class, stripAlfrescoWarTask -> {
                    stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.MANIFEST_FILE);
                    if (warName.equals(ALFRESCO)) {
                        stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.VERSION_PROPERTIES);
                    }
                });
        resolveTask.setGroup(TASK_GROUP);
        resolveTask.getInputWar().set(project.getLayout().file(project.provider(() -> {
            if (baseWar.isEmpty()) {
                return null;
            }
            return baseWar.getSingleFile();
        })));

        final List<WarEnrichmentTask> tasks = new ArrayList<>();

        tasks.add(project.getTasks()
                .create("apply" + warName + "SM", InjectFilesInWarTask.class, injectFilesInWarTask -> {
                    injectFilesInWarTask.getTargetDirectory().set("/WEB-INF/lib/");
                    injectFilesInWarTask
                            .setSourceFiles(project.getConfigurations().getByName(warName.toLowerCase() + "SM"));
                }));
        if (warName.equals(ALFRESCO)) {
            tasks.add(project.getTasks()
                    .create("apply" + warName + "DE", InjectFilesInWarTask.class, injectFilesInWarTask -> {
                        injectFilesInWarTask.getTargetDirectory().set("/WEB-INF/classes/dynamic-extensions/bundles/");
                        injectFilesInWarTask
                                .setSourceFiles(project.getConfigurations().getByName(warName.toLowerCase() + "DE"));
                    }));
        }
        tasks.add(project.getTasks()
                .create("apply" + warName + "Amp", InstallAmpsInWarTask.class, installAmpsInWarTask -> {
                    installAmpsInWarTask
                            .setSourceFiles(project.getConfigurations().getByName(warName.toLowerCase() + "Amp"));
                }));

        for (WarEnrichmentTask task : tasks) {
            task.setInputWar(resolveTask);
            task.setGroup(TASK_GROUP);
        }

        List<WarLabelOutputTask> outputTasks = new ArrayList<>(tasks);
        outputTasks.add(0, resolveTask);

        MergeWarsTask mergeWarsTask = project.getTasks().create(warName.toLowerCase() + "War", MergeWarsTask.class);
        mergeWarsTask.setGroup(TASK_GROUP);

        mergeWarsTask.addInputWar(project.provider(baseWar::getSingleFile));
        for (WarLabelOutputTask task : outputTasks) {
            mergeWarsTask.withLabels(task);
            mergeWarsTask.addInputWar(project.provider(() -> task.getOutputWar().get().getAsFile()));
        }

        return outputTasks;
    }
}
