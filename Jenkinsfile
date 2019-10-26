pipeline {
    agent {
        node {
            label 'master'
        }

    }
    stages {
        stage('run unit test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Build the artifact') {
            steps {
                sh './gradlew build'
            }
        }
    }
    post {
        always {
            junit 'build/test-results/**/*.xml'
        }
    }
}