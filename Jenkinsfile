pipeline {
    agent {
        node {
            label 'master'
        }

    }
    stages {
        stage('run unit test') {
            steps {
                sh './gradlew check'
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
            junit 'build/reports/**/*.xml'
        }
    }
}