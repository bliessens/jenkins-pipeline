pipeline {
    agent {
        node {
            label 'master'
        }

    }
    stages {
        stage('test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('build') {
            when
            steps {
                sh './gradlew build'
            }
        }
    }
}