pipeline { 
  agent { label 'Building-VM' }
  stages {
    stage('Pull New Changes') {
        steps {
            sh 'cd /home/azureuser/dev-tarasol-officetempo && ./pull-code.sh'
        }
    }

    stage ('Build Docker Image') {
        steps {
            sh 'cd  Services/Correspondences/OfficeTempo.CMS.Api/ && docker build -t  tarasol-officetempo:dev_${BUILD_NUMBER} -f Dockerfile-staging .'
        }
    }

    stage ('tag Image With Commit ID ') {
        steps {
            sh 'sudo docker login 40.0.0.0:8070 && docker tag tarasol-officetempo:dev_${BUILD_NUMBER} 40.117.91.79:8070/tarasol-officetempo:dev-$(git rev-parse --short HEAD)'
        }
    }
    stage('Push Docker Image With Commit ID To Nexus Repository') {
        steps {
            GIT_COMMIT_ID = sh (
              script: 'echo $(git rev-parse --short HEAD)',
              returnStdout: true
            ).trim()

             sh ("docker push 40.0.0.0:8070/tarasol-officetempo:dev-${GIT_COMMIT_ID}")
        }
    }
    stage ('Deploy to dev Enviroment') {
        steps {
            sh ("kubectl --kubeconfig /root/.local/bin/kubectl/config-dev -ndev set image deployment/tarasol-officetempo tarasol-officetempo=/root/.local/bin/kubectl/tarasol-officetempo:dev-${GIT_COMMIT_ID}")
        }
    }
  }
}