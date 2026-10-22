pipeline {
    agent any

    tools {
        jdk 'jdk-17'
        maven 'maven-3'
    }

    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=${env.HOME}/.m2/repository"

        SONAR_HOST_URL = "https://sonarcloud.io"
        SONAR_ORG = "altermanns"
        SONAR_PROJECT_KEY = "saboreate-app"
    }

    stages {
        stage('1. Integración al Repo Privado') {
            steps {
                echo '=== [ETAPA 1] Descargando código ==='
                checkout scm
                script {
                    echo "Rama en ejecución: ${env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'main'}"
                }
            }
        }

        stage('2. Build (Compilación)') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'feature/**'
                }
            }
            steps {
                echo '=== [ETAPA 2] Compilando el proyecto Java/Maven ==='
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('3. Pruebas Unitarias y Calidad de Código') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'feature/**'
                }
            }
            parallel {
                stage('Pruebas Unitarias (JUnit 5)') {
                    steps {
                        echo 'Ejecutando suite de pruebas...'
                        sh 'mvn test'
                    }
                }

                stage('Análisis Estático (SonarCloud)') {
                    steps {
                        echo 'Enviando código a SonarCloud...'
                        withCredentials([string(credentialsId: 'sonarcloud-token', variable: 'SONAR_TOKEN')]) {
                            sh '''
                                mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                  -Dsonar.projectKey="${SONAR_PROJECT_KEY}" \
                                  -Dsonar.organization="${SONAR_ORG}" \
                                  -Dsonar.host.url="${SONAR_HOST_URL}" \
                                  -Dsonar.login="${SONAR_TOKEN}"
                            '''
                        }
                    }
                }
            }
        }

        stage('4. Pruebas E2E (Selenium / Cypress)') {
            when { branch 'develop' }
            steps {
                echo '=== [ETAPA 4] Ejecutando pruebas E2E ==='
                // Si el perfil e2e-tests no existe, elimina esta etapa o impleméntalo.
                sh 'mvn verify -P e2e-tests'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completado con éxito. Enviando mensaje a Telegram...'
            script {
                def msg = """
*Pipeline Jenkins Exitoso*
*Proyecto:* ${env.JOB_NAME}
*Build #:* ${env.BUILD_NUMBER}
*Rama:* ${env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'main'}
*Duración:* ${currentBuild.durationString}
*Resultado:* Compilación, Análisis (Sonar) y Pruebas superadas.
*Enlace:* ${env.BUILD_URL}
                """.trim()
                enviarTelegram(msg)
            }
        }

        failure {
            echo 'El Pipeline ha fallado. Enviando alerta a Telegram...'
            script {
                def msg = """
*Pipeline Jenkins Fallido*
*Proyecto:* ${env.JOB_NAME}
*Build #:* ${env.BUILD_NUMBER}
*Rama:* ${env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'main'}
*Causa:* Error en compilación, análisis de calidad o fallo en tests.
*Ver log de error:* ${env.BUILD_URL}console
                """.trim()
                enviarTelegram(msg)
            }
        }

        always {
            echo 'Pipeline finalizado. Archivando reportes y limpiando workspace...'
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            cleanWs()
        }
    }
}

def enviarTelegram(String mensaje) {
    withCredentials([
            string(credentialsId: '001', variable: 'TELEGRAM_TOKEN'),
            string(credentialsId: '0001', variable: 'TELEGRAM_CHAT')
        ]) {
        withEnv(["TG_MSG=${mensaje}"]) {
            sh '''
                RESP=$(curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_TOKEN}/sendMessage" \
                    --data-urlencode "chat_id=${TELEGRAM_CHAT}" \
                    --data-urlencode "text=${TG_MSG}")
                echo ">>> [TELEGRAM API RESPUESTA]: $RESP"
            '''
        }
    }
}