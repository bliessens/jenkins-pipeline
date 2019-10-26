pipeline {
  agent {
    node {
      label 'master'
    }

  }
  stages {
    stage('stage1') {
      steps {
        sh './gradlew build'
      }
    }
  }
}