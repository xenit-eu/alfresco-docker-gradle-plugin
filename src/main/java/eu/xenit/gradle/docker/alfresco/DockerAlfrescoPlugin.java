package eu.xenit.gradle.docker.alfresco;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.alfresco.tasks.InjectFilesInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.InstallAmpsInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.MergeWarsTask;
import eu.xenit.gradle.docker.alfresco.tasks.PrefixLog4JWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.StripAlfrescoWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarEnrichmentTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarLabelOutputTask;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.alfresco.tasks.extension.LabelConsumerExtension;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithLabelExtensionImpl;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl;
import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.core.DockerPlugin;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.module.tool.WarHelperImpl;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;

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

        project.getPluginManager().apply(DockerPlugin.class);

        DockerExtension dockerExtension = project.getExtensions().getByType(DockerExtension.class);
        AlfrescoDockerExtension alfrescoDockerExtension = dockerExtension.getExtensions()
                .create("alfresco", AlfrescoDockerExtension.class);
        alfrescoDockerExtension.getLeanImage().convention(false);

        project.getPluginManager().apply(DockerAlfrescoLegacyPlugin.class);

        List<WarLabelOutputTask> alfrescoWarEnrichmentTasks = warEnrichmentChain(project, ALFRESCO);
        List<WarLabelOutputTask> shareEnrichmentTasks = warEnrichmentChain(project, SHARE);

        TaskProvider<Dockerfile> createDockerFile = project.getTasks().named("createDockerFile", Dockerfile.class);

        createDockerFile.configure(dockerfile -> {
            DockerfileWithLabelExtensionImpl.applyTo(dockerfile);
            DockerfileWithWarsExtensionImpl.applyTo(dockerfile);
            DockerfileWithWarsExtension warsExtension = DockerfileWithWarsExtension.get(dockerfile);
            warsExtension.getBaseImage().set(alfrescoDockerExtension.getBaseImage());
        });

        configureDockerFileTask(project, createDockerFile, alfrescoWarEnrichmentTasks, shareEnrichmentTasks,
                alfrescoDockerExtension);
    }

    private void configureDockerFileTask(Project project, TaskProvider<Dockerfile> dockerfile,
            List<WarLabelOutputTask> alfrescoTasks, List<WarLabelOutputTask> shareTasks,
            AlfrescoDockerExtension alfrescoExtension) {

        dockerfile.configure(dockerfile1 -> {
            DockerfileWithWarsExtension withWarsConvention = DockerfileWithWarsExtension.get(dockerfile1);
            withWarsConvention.getRemoveExistingWar().set(alfrescoExtension.getLeanImage().map(b -> !b));
        });

        Configuration alfrescoBaseWar = project.getConfigurations().getByName(BASE_ALFRESCO_WAR);

        dockerfile.configure(dockerfile1 -> {
            DockerfileWithWarsExtension withWarsConvention = DockerfileWithWarsExtension.get(dockerfile1);
            withWarsConvention.addWar("alfresco", alfrescoExtension.getLeanImage().flatMap(isLeanImage -> {
                if (isLeanImage || alfrescoBaseWar.isEmpty()) {
                    return project.provider(() -> null);
                } else {
                    return project.getLayout().file(project.provider(alfrescoBaseWar::getSingleFile));
                }
            }));
        });
        alfrescoTasks.forEach(t -> {
            dockerfile.configure(dockerfile1 -> {
                DockerfileWithWarsExtension withWarsConvention = DockerfileWithWarsExtension.get(dockerfile1);
                withWarsConvention.addWar("alfresco",
                        project.provider(() -> alfrescoBaseWar.isEmpty() ? null : t.getOutputWar().get()));
                dockerfile1.dependsOn(t);
                LabelConsumerExtension.get(dockerfile1).withLabels(t);
            });
        });

        Configuration shareBaseWar = project.getConfigurations().getByName(BASE_SHARE_WAR);

        dockerfile.configure(dockerfile1 -> {
            DockerfileWithWarsExtension withWarsConvention = DockerfileWithWarsExtension.get(dockerfile1);
            withWarsConvention.addWar("share", alfrescoExtension.getLeanImage().flatMap(isLeanImage -> {
                if (isLeanImage || shareBaseWar.isEmpty()) {
                    return project.provider(() -> null);
                } else {
                    return project.getLayout().file(project.provider(shareBaseWar::getSingleFile));
                }
            }));
        });
        shareTasks.forEach(t -> {
            dockerfile.configure(dockerfile1 -> {
                DockerfileWithWarsExtension withWarsConvention = DockerfileWithWarsExtension.get(dockerfile1);
                withWarsConvention.addWar("share",
                        project.provider(() -> shareBaseWar.isEmpty() ? null : t.getOutputWar().get()));
                dockerfile1.dependsOn(t);
                LabelConsumerExtension.get(dockerfile1).withLabels(t);
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
