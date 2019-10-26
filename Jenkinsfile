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
        stage('Publish the artifact') {
            when {
                branch 'master'
            }
            steps {
                sh './gradlew publish'
            }
        }
    }
    post {
        always {
            junit 'build/test-results/**/*.xml'
        }
    }
}

// czv