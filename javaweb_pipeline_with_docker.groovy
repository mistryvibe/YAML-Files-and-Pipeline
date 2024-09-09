pipeline {
    agent any
    
    stages {
        stage('SCM Checkout') {
            steps {
                git url: 'https://github.com/mistryvibe/java-web-app-docker.git', branch: 'master'
            }
        }

        stage('Maven Clean Package') {
            steps {
                script {
                    def mavenHome = tool name: '3.9.8', type: 'maven'
                    def mavenCMD = "${mavenHome}/bin/mvn"
                    sh "${mavenCMD} clean package"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "${BUILD_NUMBER}" // This will print the Jenkins build number
                    sh "docker build -t mistryvibe/java-web-app-docker:${BUILD_NUMBER} ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'Docker_Hub_Pwd', usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PWD')]) {
                    sh "docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PWD}"
                    sh "docker push mistryvibe/java-web-app-docker:${BUILD_NUMBER}"
                }
            }
        }

        stage('Update Tag in Compose File') {
            steps {
                sh 'sed -i "s/mistryvibe\\/java-web-app-docker:[0-9]\\+/mistryvibe\\/java-web-app-docker:${BUILD_NUMBER}/" docker-compose.yml'
            }
        }

        stage('Run Docker Compose') {
            steps {
                sh "docker-compose up -d"
            }
        }
    }
}
