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
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
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
        NamedDomainObjectProvider<Configuration> baseAlfrescoWar = createConfiguration(project, BASE_ALFRESCO_WAR);
        createConfiguration(project, ALFRESCO_AMP);
        NamedDomainObjectProvider<Configuration> baseShareWar = createConfiguration(project, BASE_SHARE_WAR);
        createConfiguration(project, SHARE_AMP);
        createConfiguration(project, ALFRESCO_DE);
        createConfiguration(project, ALFRESCO_SM);
        createConfiguration(project, SHARE_SM);

        project.getPluginManager().apply(DockerPlugin.class);

        DockerExtension dockerExtension = project.getExtensions().getByType(DockerExtension.class);
        AlfrescoDockerExtension alfrescoDockerExtension = dockerExtension.getExtensions()
                .create("alfresco", AlfrescoDockerExtension.class);
        alfrescoDockerExtension.getLeanImage().convention(false);

        project.getPluginManager().apply(DockerAlfrescoLegacyPlugin.class);

        List<TaskProvider<? extends WarLabelOutputTask>> alfrescoWarEnrichmentTasks = warEnrichmentChain(project,
                ALFRESCO);
        List<TaskProvider<? extends WarLabelOutputTask>> shareEnrichmentTasks = warEnrichmentChain(project, SHARE);

        TaskProvider<Dockerfile> createDockerFile = project.getTasks().named("createDockerFile", Dockerfile.class);

        createDockerFile.configure(dockerfile -> {
            DockerfileWithLabelExtensionImpl.applyTo(dockerfile);
            DockerfileWithWarsExtensionImpl.applyTo(dockerfile);
            DockerfileWithWarsExtension warsExtension = DockerfileWithWarsExtension.get(dockerfile);
            warsExtension.getBaseImage().set(alfrescoDockerExtension.getBaseImage());
            warsExtension.getRemoveExistingWar().set(alfrescoDockerExtension.getLeanImage().map(b -> !b));
        });

        configureAddWarDockerFile(project, ALFRESCO.toLowerCase(), createDockerFile, alfrescoWarEnrichmentTasks,
                baseAlfrescoWar,
                alfrescoDockerExtension);
        configureAddWarDockerFile(project, SHARE.toLowerCase(), createDockerFile, shareEnrichmentTasks, baseShareWar,
                alfrescoDockerExtension);

    }

    private static NamedDomainObjectProvider<Configuration> createConfiguration(Project project,
            String configurationName) {
        return project.getConfigurations().register(configurationName, config -> {
            config.setCanBeResolved(true);
            config.setCanBeConsumed(false);
        });
    }

    private static void configureAddWarDockerFile(Project project, String name, TaskProvider<Dockerfile> dockerfile,
            List<TaskProvider<? extends WarLabelOutputTask>> tasks, Provider<Configuration> configuration,
            AlfrescoDockerExtension extension) {

        dockerfile.configure(dockerfile1 -> {
            DockerfileWithWarsExtension withWarsConvention = DockerfileWithWarsExtension.get(dockerfile1);

            dockerfile1.dependsOn(configuration);
            withWarsConvention.addWar(name, extension.getLeanImage().flatMap(isLeanImage -> {
                if (isLeanImage) {
                    return project.provider(() -> null);
                }
                return configuration.map(Configuration::isEmpty).flatMap(isEmpty -> {
                    if (isEmpty) {
                        return project.provider(() -> null);
                    }
                    return project.getLayout().file(configuration.map(Configuration::getSingleFile));
                });
            }));

            tasks.forEach(taskProvider -> {
                dockerfile1.dependsOn(taskProvider);
                withWarsConvention.addWar(name, configuration.map(Configuration::isEmpty).flatMap(isEmpty -> {
                    if (isEmpty) {
                        return project.provider(() -> null);
                    }
                    return taskProvider.flatMap(WarLabelOutputTask::getOutputWar);
                }));

                LabelConsumerExtension.get(dockerfile1).withLabels(taskProvider);
            });
        });
    }


    private List<TaskProvider<? extends WarLabelOutputTask>> warEnrichmentChain(Project project, final String warName) {
        Configuration baseWar = project.getConfigurations().getByName("base" + warName + "War");

        TaskProvider<? extends WarLabelOutputTask> resolveTask = project.getTasks()
                .register("strip" + warName + "War", StripAlfrescoWarTask.class, stripAlfrescoWarTask -> {
                    stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.MANIFEST_FILE);
                    stripAlfrescoWarTask.addPathToCopy("WEB-INF/classes/alfresco/module/*/module.properties");
                    stripAlfrescoWarTask.addPathToCopy("WEB-INF/classes/log4j.properties");
                    if (warName.equals(ALFRESCO)) {
                        stripAlfrescoWarTask.addPathToCopy(WarHelperImpl.VERSION_PROPERTIES);
                    }
                    stripAlfrescoWarTask.setGroup(TASK_GROUP);
                    stripAlfrescoWarTask.setInputWar(baseWar);
                });

        final List<TaskProvider<? extends WarEnrichmentTask>> tasks = new ArrayList<>();

        tasks.add(project.getTasks()
                .register("prefix" + warName + "Log4j", PrefixLog4JWarTask.class,
                        prefixLog4JWarTask -> prefixLog4JWarTask.getPrefix().set(warName.toUpperCase()))
        );

        tasks.add(project.getTasks()
                .register("apply" + warName + "SM", InjectFilesInWarTask.class, injectFilesInWarTask -> {
                    injectFilesInWarTask.getTargetDirectory().set("/WEB-INF/lib/");
                    injectFilesInWarTask.getSourceFiles()
                            .from(project.getConfigurations().named(warName.toLowerCase() + "SM"));
                }));
        if (warName.equals(ALFRESCO)) {
            tasks.add(project.getTasks()
                    .register("apply" + warName + "DE", InjectFilesInWarTask.class, injectFilesInWarTask -> {
                        injectFilesInWarTask.getTargetDirectory().set("/WEB-INF/classes/dynamic-extensions/bundles/");
                        injectFilesInWarTask.getSourceFiles()
                                .from(project.getConfigurations().named(warName.toLowerCase() + "DE"));
                    }));
        }
        tasks.add(project.getTasks()
                .register("apply" + warName + "Amp", InstallAmpsInWarTask.class, installAmpsInWarTask -> {
                    installAmpsInWarTask.getSourceFiles()
                            .from(project.getConfigurations().named(warName.toLowerCase() + "Amp"));
                }));

        for (TaskProvider<? extends WarEnrichmentTask> taskProvider : tasks) {
            taskProvider.configure(task -> {
                task.dependsOn(resolveTask);
                task.setInputWar(resolveTask);
                task.setGroup(TASK_GROUP);
            });
        }

        List<TaskProvider<? extends WarLabelOutputTask>> outputTasks = new ArrayList<>(tasks);
        outputTasks.add(0, resolveTask);

        project.getTasks().register(warName.toLowerCase() + "War", MergeWarsTask.class, mergeWarsTask -> {
            mergeWarsTask.setGroup(TASK_GROUP);
            mergeWarsTask.dependsOn(baseWar);
            mergeWarsTask.addInputWar(project.provider(baseWar::getSingleFile));
            for (TaskProvider<? extends WarLabelOutputTask> task : outputTasks) {
                mergeWarsTask.dependsOn(task);
                mergeWarsTask.addInputWar(task);
            }
        });

        return outputTasks;
    }
}
