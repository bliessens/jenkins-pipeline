def call(Map attr = ['sonarqube': false]) {
    def version = ""
    pipeline {
        agent {
            node {
                label 'master'
                reuseNode = true
            }

        }
        triggers {
            cron('H/5 * * * *')
        }
        stages {
            stage('Master version') {
                when {
                    branch 'master'
                }
                steps {
                    script {
                        version = sh(script: "git tag -l '[0-9]*' | sort -rn | head -1", returnStdout: true).trim()
                        version = (Integer.parseInt(version) + 1).toString()
                    }
                    echo "Master branch, next version is: ${version}"
                }
            }
            stage('Branch version') {
                when {
                    branch '*-build'
                }
                steps {
                    script {
                        version = env.BRANCH_NAME + "." + sh(script: "git rev-list --count HEAD", returnStdout: true).trim()
                    }
                    echo "Branch is ${env.BRANCH_NAME}, next version is: ${version}"
                }
            }
            stage('Build') {
                steps {
                    sh "./gradlew build"
                }
            }
            stage('Sonar') {
                when {
                    expression { return attr['sonarqube'] }
                }
                steps {
                    sh "./gradlew sonarqube"
                }
            }
            stage('Publish the artifact') {
                when {
                    anyOf { branch 'master'; branch '*-build' }
                }
                steps {
                    sh "./gradlew publish"
                    sh "git tag ${version}"
                    sh "git push --tags"
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