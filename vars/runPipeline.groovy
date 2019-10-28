def call(Map options = ['sonarqube': false, 'label': 'master', 'maxBuilds': '10']) {

    properties([disableConcurrentBuilds(),
                buildDiscarder(logRotator(numToKeepStr: options['maxBuilds']))])

    def version = ""
    pipeline {
        agent {
            node {
                label options['label']
            }
        }
        triggers {
            pollSCM('H/5 * * * *')
        }
        stages {
            stage('Determine version') {
//                when {
//                    anyOf { branch 'master'; branch '*-fix'; branch '*-build' }
//                }
                steps {
                    script {
                        if (env.BRANCH_NAME == 'master') {
                            version = sh(script: "git tag -l '[0-9]*' | grep '^[0-9]*\$' | sort -rn | head -1", returnStdout: true).trim()
                            version = (Integer.parseInt(version) + 1).toString()
                            echo "Master branch, next version is: ${version}"

                        } else if (env.BRANCH_NAME ==~ /.*-build$/) {
                            final String DIGITS_ONLY = "^[a-zA-Z]*[-_](\\d*)[-._\\w]*\$"
                            def jiraTicket = env.BRANCH_NAME.replaceAll(DIGITS_ONLY, "\$1")
                            println "Jira issue number for branch is: '${jiraTicket}'"

                            def lastBranchTag = sh(script: "git tag -l '*.${jiraTicket}.*' | sort -rn -t . -k 3 | head -1", returnStdout: true).trim()
                            if (!lastBranchTag.isEmpty()) {
                                println "Found previous branch build with tag: '${lastBranchTag}'"
                                def prefix = lastBranchTag.split("\\.").init().join(".")
                                def suffix = Integer.parseInt(lastBranchTag.split("\\.").last()) + 1
                                version = "${prefix}.${suffix}"
                            } else {
                                def prefix = sh(script: "git describe --tags", returnStdout: true).trim()
                                prefix = prefix.replaceAll("-.*", "") + "." + jiraTicket
                                println "New branch. Branch builds will be prefixed with: '${prefix}'"
                                version = "${tagprefix}.1"
                            }
                            echo "Feature branch, next version is: ${version}"
                        } else if (env.BRANCH_NAME ==~ /.*-fix$/) {
                            def branchPrefix = env.BRANCH_NAME.replaceAll("-fix", "").replaceAll("-", "")
                            def lastBranchTag = sh(script: "git tag -l '${branchPrefix}.*' | sort -rn -k 2 -t . | head -1", returnStdout: true).trim()
                            if (lastBranchTag.isEmpty()) {
                                version = branchPrefix + ".1"
                            } else {
                                def prefix = lastBranchTag.split("\\.").init().join(".")
                                def suffix = Integer.parseInt(lastBranchTag.split("\\.").last()) + 1
                                version = "${prefix}.${suffix}"
                            }
                            echo "Fix branch, next version is: ${version}"
                        }
                    }
                }
            }
            stage('Test') {
                steps {
                    sh "./gradlew clean build --stacktrace -PversionToBuild=${version}"
                    sh "git tag ${version}"
                    sh "git push --tags"
                }
            }
            stage('Dockerize') {
                steps {
                    echo "Disabled for now"
//                    sh "./gradlew application:buildDocker --stacktrace -PversionToBuild=${version}"
//                    sh "docker tag ${group}/${artifact}:${versionToBuild} ${dockerImage}:${version}"
//                    sh "docker login -u ${dockerUser} -p ${dockerPWD} docker-dev.valartifactorydev01.violabor.local"
//                    sh "docker push ${dockerImage}:${version}"
                }
            }
            stage('Finalize') {
                parallel {
                    stage('Sonar') {
                        when {
                            expression { return options['sonarqube'] }
                        }
                        steps {
                            sh "./gradlew --stacktrace -PversionToBuild=${version} sonarqube"
                        }
                    }
                    stage('Publish docker container artifact') {
                        when {
                            anyOf { branch 'master'; branch '*-build'; branch '*-fix' }
                        }
                        steps {
                            echo "Invoke docker push command"
                        }
                    }
                }
            }
        }
        post {
            always {
                junit '**/build/test-results/**/*.xml'
            }
        }
    }
}