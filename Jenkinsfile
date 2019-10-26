def version = ""
pipeline {
    agent {
        node {
            label 'master'
        }

    }
    stages {
        stage('Determine version number') {
            when {
                branch 'master'
            }
            steps {
                version = sh(script: "git rev-list --count master", returnStdout: true).trim()
                echo "Master branch, next version is: ${version}"
            }
        }
        stage('run unit test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Publish the artifact') {
            when {
                branch 'master'
            }
            steps {
                sh './gradlew publish'
                sh "git tag ${version}"
                sh "git push --tags"
            }
        }
    }
    post {
        always {
            junit 'build/test-results/**/*.xml'
        }
    }
}