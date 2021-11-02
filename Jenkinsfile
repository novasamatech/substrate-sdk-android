def dockerImage = 'build-tools/android-build-box:latest'
def jenkinsAgent = 'android'
def deploymentBranches = ['master', 'develop']

node(jenkinsAgent) {
    properties(
	    [
		    disableConcurrentBuilds(),
            buildDiscarder(steps.logRotator(numToKeepStr: '20'))
		]
    )
    timestamps {
        try {
            stage('Git pull'){
                checkout scm
            }
            withCredentials([
                [$class: 'UsernamePasswordMultiBinding', credentialsId: 'bot-soramitsu-rw', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD']
                ])
            {
                docker.withRegistry('https://docker.soramitsu.co.jp', 'bot-build-tools-ro') {
                    docker.image("${dockerImage}").inside() {
                        stage('Lint') {
                            sh "./gradlew ktlint"
                        }
                        stage('Tests') {
                            sh "./gradlew testDebugUnitTest"
                        }
                        stage('Build library') {
                            sh "./gradlew clean build"
                        }
                        if (env.BRANCH_NAME in deploymentBranches) {
                            stage('Deploy library') {
                                sh "./gradlew publish"
                            }
                        }
                    }
                }
            }
        } catch (e) {
            print e
            currentBuild.result = 'FAILURE'
        } finally {
            cleanWs()
        }
    }
}