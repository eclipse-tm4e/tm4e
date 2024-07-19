pipeline {
	options {
		timeout(time: 20, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr: '10'))
	}

	agent {
		label 'centos-latest'
	}

	tools {
		maven 'apache-maven-latest' // https://wiki.eclipse.org/Jenkins#Apache_Maven
		jdk 'temurin-jdk17-latest'
	}

	stages {

		stage('initialize PGP') {
			when {
				branch 'main'
			}
			steps {
				withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
					sh 'gpg --batch --import "${KEYRING}"'
					sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" | gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
				}
			}
		}

		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					script {
						if (env.BRANCH_NAME == 'main') {
							withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
								sh '''mvn clean deploy -B \
									-Dmaven.test.failure.ignore=true \
									-Dsurefire.rerunFailingTestsCount=3 \
									-Psign -Dgpg.passphrase="${KEYRING_PASSPHRASE}"
								'''
							}
						} else {
							sh '''mvn clean verify -B \
								-Dmaven.test.failure.ignore=true \
								-Dsurefire.rerunFailingTestsCount=3
							'''
						}
					}
				}
			}
			post {
				always {
					archiveArtifacts artifacts: 'org.eclipse.tm4e.repository/target/repository/**/*,org.eclipse.tm4e.repository/target/*.zip,*/target/work/data/.metadata/.log'
					junit '*/target/surefire-reports/TEST-*.xml'
				}
			}
		}

		stage('Deploy Snapshot') {
			when {
				branch 'main'
			}
			steps {
				sshagent (['projects-storage.eclipse.org-bot-ssh']) {
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
