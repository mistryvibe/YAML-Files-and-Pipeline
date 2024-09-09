node{
    stage("Git clone"){
        git branch: 'main', credentialsId: 'GIT_CRED', url: 'https://github.com/mistryvibe/spring-boot-mongo-docker.git'
    }
    
    stage("Mavel clean build"){
        def mavenHome = tool name: '3.9.8', type: 'maven'
        def mavenCMD = "${mavenHome}/bin/mvn"
        sh "${mavenCMD} clean package"
    }
    
    stage("Build docker image"){
        echo "${BUILD_NUMBER}"
        sh "docker build -t mistryvibe/spring-boot-mongo-docker:${BUILD_NUMBER} ."
    }
    
    stage("Docker image push"){
        withCredentials([usernamePassword(credentialsId: 'Docker_Hub_Pwd', usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PWD')]) {
            sh "docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PWD}"
            sh "docker push mistryvibe/spring-boot-mongo-docker:${BUILD_NUMBER}"
        }    
    }
    /*
    stage("Update tag"){
        sh 'sed -i "s/mistryvibe\\/java-web-app-docker:[0-9]\\+/mistryvibe\\/java-web-app-docker:${BUILD_NUMBER}/" docker-compose.yml'
    }
    */
    stage("Deploy app in k8s"){
        sh "kubectl apply -f springdeployment.yaml"
    }
}
    
    
