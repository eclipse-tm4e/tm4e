pipeline {
	agent {
		label 'centos-latest'
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk17-latest'
	}
	options {
		buildDiscarder(logRotator(numToKeepStr:'10'))
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
				withMaven(maven:'apache-maven-latest', mavenLocalRepo: '$WORKSPACE/.m2') {
				withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh 'mvn clean verify \
						-Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true \
						-Psign -Dgpg.passphrase="${KEYRING_PASSPHRASE}"'

				}
				}
				}
			}
			post {
				always {
					junit '*/target/surefire-reports/TEST-*.xml'
					archiveArtifacts artifacts: '*/target/work/data/.metadata/.log'
				}
			}
		}
		stage('Deploy') {
			when {
				branch 'master'
				// TODO deploy all branch from Eclipse.org Git repo
			}
			steps {
				sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
					// TODO compute the target URL (snapshots) according to branch name (0.5-snapshots...)
					sh '''
						ssh genie.tm4e@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/tm4e/snapshots
						ssh genie.tm4e@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/tm4e/snapshots
						scp -r org.eclipse.tm4e.repository/target/repository/* genie.tm4e@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/tm4e/snapshots
						scp org.eclipse.tm4e.repository/target/org.eclipse.tm4e.repository-*-SNAPSHOT.zip genie.tm4e@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/tm4e/snapshots/repository.zip
					'''
				}
			}
		}
	}
}
