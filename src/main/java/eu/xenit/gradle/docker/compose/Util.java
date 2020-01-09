package eu.xenit.gradle.docker.compose;

final class Util {

    private Util() {
    }

    public static String safeEnvironmentVariableName(String original) {
        return original.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase().replaceAll("[^A-Za-z0-9]", "_");
    }

}
