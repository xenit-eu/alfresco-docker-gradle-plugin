
node {

    stage ('Checkout')    {
        checkout scm

        // env.GIT_BRANCH is null !!!
        //  so workaround:
        //def gitBranch = sh(returnStdout: true, script: "git branch | grep '^*' | cut -d' ' -f 2").trim()

    }
            def gitBranch = env.BRANCH_NAME // https://issues.jenkins-ci.org/browse/JENKINS-30252

            println "********** GIT_BRANCH: [${gitBranch}]"
            println "********** BUILD_NUMBER: [" + env.BUILD_NUMBER + "]"

            def buildNr = "SNAPSHOT"
            def publishJarTask = "publishMavenJavaPublicationToSnapshotRepository"
            if (gitBranch == "release") {
                buildNr = env.BUILD_NUMBER
                publishJarTask = "publishMavenJavaPublicationToReleaseRepository"
            }

    try {
        stage ('build and test') {
          sh "./gradlew clean build --continue -i"
        }

        stage ('Publishing') {
          sh "./gradlew :${publishJarTask} --continue -i"
        }

    } catch (err) {
        currentBuild.result = "FAILED"
        println err
    } finally {
        step([$class: "JUnitResultArchiver", testResults: "**/build/**/TEST-*.xml"])
    }

}
