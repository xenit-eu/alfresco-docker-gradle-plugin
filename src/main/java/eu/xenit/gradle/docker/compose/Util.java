package eu.xenit.gradle.docker.compose;

final class Util {

    private Util() {
    }

    public static String safeEnvironmentVariableName(String original) {
        return original.replaceAll("[A-Z]", "_$0").toUpperCase().replaceAll("[^A-Za-z0-9]", "_");
    }

}
