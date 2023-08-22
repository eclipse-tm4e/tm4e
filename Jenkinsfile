pipeline {
	options {
		timeout(time: 20, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr: '10'))
	}
	agent {
		label 'centos-latest'
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'temurin-jdk17-latest'
	}
	stages {
		stage('initialize PGP') {
			steps {
				withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
					sh 'gpg --batch --import "${KEYRING}"'
					sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
				}
			}
		}
		stage('Build') {
			steps {
				withMaven(maven:'apache-maven-latest', mavenLocalRepo: '$WORKSPACE/.m2/repository') {
				withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh '''mvn clean verify \
						-Dmaven.test.failure.ignore=true \
						-Dsurefire.rerunFailingTestsCount=3 \
						-Psign -Dgpg.passphrase="${KEYRING_PASSPHRASE}"
					'''
				}}}
			}
			post {
				always {
					junit '*/target/surefire-reports/TEST-*.xml'
					archiveArtifacts artifacts: 'org.eclipse.tm4e.repository/target/repository/**/*,org.eclipse.tm4e.repository/target/*.zip,*/target/work/data/.metadata/.log'
				}
			}
		}
		stage('Deploy Snapshot') {
			when {
				branch 'main'
				// TODO deploy all branch from Eclipse.org Git repo
			}
			steps {
				sshagent (['projects-storage.eclipse.org-bot-ssh']) {
					// TODO compute the target URL (snapshots) according to branch name (0.5-snapshots...)
					sh '''
						DOWNLOAD_AREA=/home/data/httpd/download.eclipse.org/tm4e/snapshots/
						echo DOWNLOAD_AREA=$DOWNLOAD_AREA
						ssh genie.tm4e@projects-storage.eclipse.org "\
							rm -rf ${DOWNLOAD_AREA}/* && \
							mkdir -p ${DOWNLOAD_AREA}"
						scp -r org.eclipse.tm4e.repository/target/repository/* genie.tm4e@projects-storage.eclipse.org:${DOWNLOAD_AREA}
						scp org.eclipse.tm4e.repository/target/org.eclipse.tm4e.repository-*-SNAPSHOT.zip genie.tm4e@projects-storage.eclipse.org:${DOWNLOAD_AREA}/repository.zip
					'''
				}
			}
		}
	}
}
