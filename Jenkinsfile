pipeline {
  agent any

  environment {
    MVN_CMD = 'mvn -B -ntp'
    ARTIFACTORY_SERVER_ID = 'artifactory-server'
    ARTIFACTORY_REPO = 'libs-release-local'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        script {
          def cmd = "${MVN_CMD} clean test package"
          if (isUnix()) {
            sh cmd
          } else {
            bat cmd
          }
        }
      }
    }

    stage('Publish to Artifactory') {
      when {
        expression {
          return env.ARTIFACTORY_SERVER_ID?.trim() &&
                 env.ARTIFACTORY_URL?.trim() &&
                 env.ARTIFACTORY_CREDENTIALS_ID?.trim()
        }
      }
      steps {
        rtServer (
          id: "${ARTIFACTORY_SERVER_ID}",
          url: "${ARTIFACTORY_URL}",
          credentialsId: "${ARTIFACTORY_CREDENTIALS_ID}"
        )

        rtUpload (
          serverId: "${ARTIFACTORY_SERVER_ID}",
          spec: """{
            "files": [
              {
                "pattern": "target/*shaded.jar",
                "target": "${ARTIFACTORY_REPO}/cl/myconstruction/my-construction-login/${BUILD_NUMBER}/"
              }
            ]
          }"""
        )

        rtPublishBuildInfo (serverId: "${ARTIFACTORY_SERVER_ID}")
      }
    }
  }
}
