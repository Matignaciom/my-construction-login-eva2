pipeline {
  agent any

  options {
    skipDefaultCheckout()
  }

  environment {
    MVN_CMD = 'mvn -B -ntp'
    ARTIFACTORY_URL_DOCKER_DEFAULT = 'http://artifactory:8082/artifactory'
    ARTIFACTORY_URL_WINDOWS_DEFAULT = 'http://localhost:8082/artifactory'
    ARTIFACTORY_REPO_DEFAULT = 'libs-release-local'
    ARTIFACTORY_TARGET_PATH_DEFAULT = 'cl/myconstruction/my-construction-login'
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
          String url = (env.ARTIFACTORY_URL?.trim()) ? env.ARTIFACTORY_URL.trim() : (isUnix() ? env.ARTIFACTORY_URL_DOCKER_DEFAULT : env.ARTIFACTORY_URL_WINDOWS_DEFAULT)
          boolean hasUrl = url?.trim()
          boolean hasToken = env.ARTIFACTORY_TOKEN?.trim() || env.ARTIFACTORY_TOKEN_CREDENTIALS_ID?.trim()
          boolean hasUserPass = (env.ARTIFACTORY_USER?.trim() && env.ARTIFACTORY_PASSWORD?.trim()) || env.ARTIFACTORY_CREDENTIALS_ID?.trim()
          return hasUrl && (hasToken || hasUserPass)
        }
      }
      steps {
        script {
          String baseUrl = (env.ARTIFACTORY_URL?.trim()) ? env.ARTIFACTORY_URL.trim() : (isUnix() ? env.ARTIFACTORY_URL_DOCKER_DEFAULT : env.ARTIFACTORY_URL_WINDOWS_DEFAULT)
          String repo = (env.ARTIFACTORY_REPO?.trim()) ? env.ARTIFACTORY_REPO.trim() : env.ARTIFACTORY_REPO_DEFAULT
          String targetPathBase = (env.ARTIFACTORY_TARGET_PATH?.trim()) ? env.ARTIFACTORY_TARGET_PATH.trim() : env.ARTIFACTORY_TARGET_PATH_DEFAULT
          String targetPath = "${targetPathBase}/${env.BUILD_NUMBER ?: 'local'}"

          String jar
          if (isUnix()) {
            jar = sh(script: "ls -1 target/*shaded.jar 2>/dev/null | head -n 1", returnStdout: true).trim()
            if (!jar) {
              jar = sh(script: "ls -1 target/*.jar 2>/dev/null | grep -v '^target/original-' | head -n 1", returnStdout: true).trim()
            }
          } else {
            jar = bat(script: "@echo off\r\nfor /f \"delims=\" %%f in ('dir /b target\\*shaded.jar 2^>nul') do (echo target\\%%f & exit /b 0)\r\nfor /f \"delims=\" %%f in ('dir /b target\\*.jar 2^>nul ^| findstr /v /i \"^original-\"') do (echo target\\%%f & exit /b 0)\r\necho.\r\n", returnStdout: true).trim()
          }

          if (!jar) {
            if (isUnix()) {
              sh "ls -la target || true"
            } else {
              bat "dir target"
            }
            error("No se encontró ningún archivo target/*shaded.jar para publicar.")
          }

          String fileName = jar.replace('\\', '/').tokenize('/').last()
          String uploadUrl = "${baseUrl.replaceAll('/+$', '')}/${repo}/${targetPath}/${fileName}"

          if (env.ARTIFACTORY_TOKEN_CREDENTIALS_ID?.trim()) {
            withCredentials([string(credentialsId: env.ARTIFACTORY_TOKEN_CREDENTIALS_ID, variable: 'AF_TOKEN')]) {
              if (isUnix()) {
                sh "curl --fail --show-error --silent -H \"Authorization: Bearer $AF_TOKEN\" -T \"${jar}\" \"${uploadUrl}\""
              } else {
                bat "curl --fail --show-error --silent -H \"Authorization: Bearer %AF_TOKEN%\" -T \"${jar}\" \"${uploadUrl}\""
              }
            }
            return
          }

          if (env.ARTIFACTORY_TOKEN?.trim()) {
            if (isUnix()) {
              sh "curl --fail --show-error --silent -H \"Authorization: Bearer ${env.ARTIFACTORY_TOKEN}\" -T \"${jar}\" \"${uploadUrl}\""
            } else {
              bat "curl --fail --show-error --silent -H \"Authorization: Bearer %ARTIFACTORY_TOKEN%\" -T \"${jar}\" \"${uploadUrl}\""
            }
            return
          }

          if (env.ARTIFACTORY_USER?.trim() && env.ARTIFACTORY_PASSWORD?.trim()) {
            if (isUnix()) {
              sh "curl --fail --show-error --silent -u \"${env.ARTIFACTORY_USER}:${env.ARTIFACTORY_PASSWORD}\" -T \"${jar}\" \"${uploadUrl}\""
            } else {
              bat "curl --fail --show-error --silent -u \"%ARTIFACTORY_USER%:%ARTIFACTORY_PASSWORD%\" -T \"${jar}\" \"${uploadUrl}\""
            }
            return
          }

          withCredentials([usernamePassword(credentialsId: env.ARTIFACTORY_CREDENTIALS_ID, usernameVariable: 'AF_USER', passwordVariable: 'AF_PASS')]) {
            if (isUnix()) {
              sh "curl --fail --show-error --silent -u \"$AF_USER:$AF_PASS\" -T \"${jar}\" \"${uploadUrl}\""
            } else {
              bat "curl --fail --show-error --silent -u \"%AF_USER%:%AF_PASS%\" -T \"${jar}\" \"${uploadUrl}\""
            }
          }
        }
      }
    }
  }
}
