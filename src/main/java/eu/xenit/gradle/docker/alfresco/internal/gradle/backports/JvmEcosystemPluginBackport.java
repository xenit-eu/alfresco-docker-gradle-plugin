package eu.xenit.gradle.docker.alfresco.internal.gradle.backports;

import eu.xenit.gradle.docker.internal.GradleVersionRequirement;
import java.lang.reflect.InvocationTargetException;
import org.gradle.api.Plugin;
import org.gradle.api.internal.artifacts.dsl.ComponentMetadataHandlerInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.component.external.model.JavaEcosystemVariantDerivationStrategy;

public class JvmEcosystemPluginBackport implements Plugin<ProjectInternal> {
    private static final Logger LOGGER = Logging.getLogger(JvmEcosystemPluginBackport.class);

    @Override
    public void apply(ProjectInternal projectInternal) {
        // Copied and adapted from JavaBasePlugin
        // https://github.com/gradle/gradle/blob/v6.6.0/subprojects/plugins/src/main/java/org/gradle/api/plugins/JavaBasePlugin.java#L131-L134
        // https://github.com/gradle/gradle/blob/v6.4.0/subprojects/plugins/src/main/java/org/gradle/api/plugins/JavaBasePlugin.java#L130-L133
        ComponentMetadataHandlerInternal metadataHandler = (ComponentMetadataHandlerInternal) projectInternal.getDependencies().getComponents();
        JavaEcosystemVariantDerivationStrategy ecosystemVariantDerivationStrategy = GradleVersionRequirement.ifAtLeast("6.5.0", JvmEcosystemPluginBackport::createWithInstance, JvmEcosystemPluginBackport::createWithReflection);
        metadataHandler.setVariantDerivationStrategy(ecosystemVariantDerivationStrategy);
    }

    private static JavaEcosystemVariantDerivationStrategy createWithInstance() {
        return JavaEcosystemVariantDerivationStrategy.getInstance();
    }

    private static JavaEcosystemVariantDerivationStrategy createWithReflection() {
        try {
            return JavaEcosystemVariantDerivationStrategy.class.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("Failed to create JavaEcosystemVariantDerivationStrategy with reflection.", e);
            GradleVersionRequirement.atLeast("6.5.0", "use platform() & enforcedPlatform() for dependencies");
        }
        // unreachable
        return null;
    }
}
