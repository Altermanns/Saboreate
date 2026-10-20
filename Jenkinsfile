pipeline {
    agent {
        docker { image 'maven:3.9-eclipse-temurin-17' }
    }

    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
        SONAR_HOST_URL = "https://sonarcloud.io"
        SONAR_ORG = "altermanns"
        SONAR_PROJECT_KEY = "saboreate-app"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            when {
                anyOf { branch 'main'; branch 'develop'; branch 'feature/*' }
            }
            steps {
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Test & QA') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'mvn test'
                    }
                }
                stage('Static Analysis (SonarCloud)') {
                    steps {
                        withCredentials([string(credentialsId: 'sonarcloud-token', variable: 'SONAR_TOKEN')]) {
                            sh 'mvn sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.organization=${SONAR_ORG} -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}'
                        }
                    }
                }
            }
        }

        stage('E2E Tests') {
            when {
                branch 'develop'
            }
            steps {
                sh 'mvn verify -P e2e-tests'
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh './scripts/deploy.sh'
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            cleanWs()
        }
        success {
            telegramSend message: " Éxito en ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}"
        }
        failure {
            telegramSend message: " Fallo en ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}"
        }
    }
}