def call(Map attr = ['sonarqube': false]) {

    def version = ""
    pipeline {
        agent {
            node {
                label 'master'
            }
        }
        triggers {
            pollSCM('H/5 * * * *')
        }
        options {
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }
        stages {
            stage('Determine version') {
//                when {
//                    anyOf { branch 'master'; branch '*-fix'; branch '*-build' }
//                }
                steps {
                    script {
                        if (env.BRANCH_NAME == 'master') {
                            version = sh(script: "git tag -l '[0-9]*' | sort -rn | head -1", returnStdout: true).trim()
                            version = (Integer.parseInt(version) + 1).toString()
                            echo "Master branch, next version is: ${version}"

                        } else if (env.BRANCH_NAME ==~ /.*-build$/) {
                            final String DIGITS_ONLY = "^[a-zA-Z]*[-_](\\d*)[-._\\w]*\$"
                            def jiraTicket = env.BRANCH_NAME.replaceAll(DIGITS_ONLY, "\$1")
                            println "Jira issue number for branch is: '${jiraTicket}'"

                            def branchTags = sh(script: "git tag -l '*.${jiraTicket}.*'", returnStdout: true).trim()
                            def tagprefix = ""
                            if (branchTags) {
                                lastBranchTag = branchTags.readLines().sort().reverse()[0]
                                println "Found previous branch build with tag: '${lastBranchTag}'"
                                tagprefix = lastBranchTag.split("\\.").init().join(".")
                            } else {
                                tagprefix = sh(script: "git describe --tags", returnStdout: true).trim()
                                tagprefix = tagprefix.replaceAll("-.*", "")
                                println "New branch. Branch builds be tagged with prefix: '${tagprefix}'"
                                tagprefix = tagprefix + "." + jiraTicket
                            }
                            version = tagprefix + "." + env.BUILD_NUMBER
                            echo "Feature branch, next version is: ${version}"
                        } else if (env.BRANCH_NAME ==~ /.*-fix$/) {
                            version = env.BRANCH_NAME.replaceAll("-fix", "").replaceAll("-", "") + "." + env.BUILD_NUMBER
                            echo "Fix branch, next version is: ${version}"
                        }
                    }
                }
            }
            stage('Test') {
                steps {
                    sh "./gradlew clean build --stacktrace --info -PversionToBuild=${version}"
                    sh "git tag ${version}"
                    sh "git push --tags"
                }
            }
            stage('Dockerize') {
                steps {
                    echo "Disabled for now"
//                    sh "./gradlew application:buildDocker --stacktrace --info -PversionToBuild=${version}"
//                    sh "docker tag ${group}/${artifact}:${versionToBuild} ${dockerImage}:${version}"
//                    sh "docker login -u ${dockerUser} -p ${dockerPWD} docker-dev.valartifactorydev01.violabor.local"
//                    sh "docker push ${dockerImage}:${version}"
                }
            }
            stage ('Finalize') {
                parallel {
                    stage('Sonar') {
                        when {
                            expression { return attr['sonarqube'] }
                        }
                        steps {
                            sh "./gradlew sonarqube"
                        }
                    }
                    stage('Publish docker container artifact') {
                        when {
                            anyOf { branch 'master'; branch '*-build' ; branch '*-fix' }
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