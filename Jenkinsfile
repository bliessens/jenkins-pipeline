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
        post {
            always {
                junit '**/build/reports/**/*.xml'
            }
        }
        stage('Build the artifact') {
            steps {
                sh './gradlew build'
            }
        }
    }
}