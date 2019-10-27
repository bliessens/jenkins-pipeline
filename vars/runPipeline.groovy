def call(Map attr = ['sonarqube': false]) {
    def version = ""
    pipeline {
        agent {
            node {
                label 'master'
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
                        version = sh(script: "git rev-list --count master", returnStdout: true).trim()
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
                    echo "Branch version is: ${version}"
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
                    echo "Running sonarqube analysis"
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