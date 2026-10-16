package eu.xenit.gradle.docker.alfresco.internal.gradle.backports;

import org.gradle.api.Plugin;
import org.gradle.api.internal.artifacts.dsl.ComponentMetadataHandlerInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.component.external.model.JavaEcosystemVariantDerivationStrategy;

public class JvmEcosystemPluginBackport implements Plugin<ProjectInternal> {
    @Override
    public void apply(ProjectInternal projectInternal) {
        // Copied and adapted from JavaBasePlugin
        // https://github.com/gradle/gradle/blob/v6.6.0/subprojects/plugins/src/main/java/org/gradle/api/plugins/JavaBasePlugin.java#L131-L134
        // https://github.com/gradle/gradle/blob/v6.4.0/subprojects/plugins/src/main/java/org/gradle/api/plugins/JavaBasePlugin.java#L130-L133
        ComponentMetadataHandlerInternal metadataHandler = (ComponentMetadataHandlerInternal) projectInternal.getDependencies().getComponents();
        JavaEcosystemVariantDerivationStrategy ecosystemVariantDerivationStrategy = projectInternal.getObjects()
                .newInstance(JavaEcosystemVariantDerivationStrategy.class);
        metadataHandler.setVariantDerivationStrategy(ecosystemVariantDerivationStrategy);
    }
}
