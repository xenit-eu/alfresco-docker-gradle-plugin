package eu.xenit.gradle.docker.alfresco;

import eu.xenit.gradle.docker.DockerBuildBehavior;
import eu.xenit.gradle.docker.DockerConfigPlugin;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.alfresco.tasks.DockerfileWithWarsTask;
import eu.xenit.gradle.docker.alfresco.tasks.InjectFilesInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.InstallAmpsInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.MergeWarsTask;
import eu.xenit.gradle.docker.alfresco.tasks.PrefixLog4JWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.StripAlfrescoWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarEnrichmentTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarLabelOutputTask;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;
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
        DockerfileWithCopyTask dockerfile = getDockerFileTask(dockerAlfrescoExtension, project);

        DockerBuildBehavior dockerBuildBehavior = new DockerBuildBehavior(dockerAlfrescoExtension.getDockerBuild(),
                dockerfile);
        dockerBuildBehavior.apply(project);

        updateDockerFileTask(project, dockerfile, alfrescoWarEnrichmentTasks, shareEnrichmentTasks,
                dockerAlfrescoExtension);
    }

    @SuppressWarnings("deprecation")
    private DockerfileWithCopyTask getDockerFileTask(DockerAlfrescoExtension dockerAlfrescoExtension,
            Project project1) {
        DockerfileWithCopyTask dockerfile = Deprecation
                .whileDisabled(() -> project1.getTasks().create("createDockerFile",
                        DockerfileWithWarsTask.class));
        DockerfileWithWarsExtension.get(dockerfile).getBaseImage()
                .set(dockerAlfrescoExtension.getBaseImage());
        return dockerfile;
    }

    private void updateDockerFileTask(Project project, DockerfileWithCopyTask dockerfile,
            List<WarLabelOutputTask> alfrescoTasks, List<WarLabelOutputTask> shareTasks,
            DockerAlfrescoExtension dockerAlfrescoExtension) {

        DockerfileWithWarsExtension withWarsConvention = DockerfileWithWarsExtension.get(dockerfile);

        withWarsConvention.getRemoveExistingWar().set(dockerAlfrescoExtension.getLeanImage().map(b -> !b));

        project.afterEvaluate(project1 -> {

            Configuration alfrescoBaseWar = project1.getConfigurations().getByName(BASE_ALFRESCO_WAR);
            withWarsConvention.addWar("alfresco", dockerAlfrescoExtension.getLeanImage().flatMap(isLeanImage -> {
                if (isLeanImage || alfrescoBaseWar.isEmpty()) {
                    return project1.provider(() -> null);
                } else {
                    return project1.getLayout().file(project1.provider(alfrescoBaseWar::getSingleFile));
                }
            }));
            alfrescoTasks.forEach(t -> {
                withWarsConvention.addWar("alfresco",
                        project1.provider(() -> alfrescoBaseWar.isEmpty() ? null : t.getOutputWar().get()));
                dockerfile.dependsOn(t);
                dockerfile.withLabels(t.getLabels());
            });

            Configuration shareBaseWar = project1.getConfigurations().getByName(BASE_SHARE_WAR);
            withWarsConvention.addWar("share", dockerAlfrescoExtension.getLeanImage().flatMap(isLeanImage -> {
                if (isLeanImage || shareBaseWar.isEmpty()) {
                    return project1.provider(() -> null);
                } else {
                    return project1.getLayout().file(project1.provider(shareBaseWar::getSingleFile));
                }
            }));
            shareTasks.forEach(t -> {
                withWarsConvention.addWar("share",
                        project1.provider(() -> shareBaseWar.isEmpty() ? null : t.getOutputWar().get()));
                dockerfile.dependsOn(t);
                dockerfile.withLabels(t.getLabels());
            });
        });
    }

    private List<WarLabelOutputTask> warEnrichmentChain(Project project, final String warName) {
        Configuration baseWar = project.getConfigurations().getByName("base" + warName + "War");

        WarEnrichmentTask resolveTask = project.getTasks()
                .create("strip" + warName + "War", StripAlfrescoWarTask.class, stripAlfrescoWarTask -> {
                    stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.MANIFEST_FILE);
                    stripAlfrescoWarTask.addPathToCopy("WEB-INF/classes/alfresco/module/*/module.properties");
                    stripAlfrescoWarTask.addPathToCopy("WEB-INF/classes/log4j.properties");
                    if (warName.equals(ALFRESCO)) {
                        stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.VERSION_PROPERTIES);
                    }
                });
        resolveTask.setGroup(TASK_GROUP);
        resolveTask.setInputWar(baseWar);

        final List<WarEnrichmentTask> tasks = new ArrayList<>();

        tasks.add(project.getTasks()
                .create("prefix" + warName + "Log4j", PrefixLog4JWarTask.class,
                        prefixLog4JWarTask -> prefixLog4JWarTask.getPrefix().set(warName.toUpperCase()))
        );

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
            mergeWarsTask.addInputWar(task);
        }

        return outputTasks;
    }
}
