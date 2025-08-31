pipeline {
	options {
		timeout(time: 20, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr: '10'))
	}

	agent {
		label 'centos-latest'
	}

	tools {
		jdk 'temurin-jdk21-latest'
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

		stage('Setup Maven Toolchains') {
			steps {
				script { try {
					def jdk17 = tool name: 'temurin-jdk17-latest', type: 'jdk'

					// Generate toolchains.xml for Maven Toolchains plugin
					writeFile file: "/tmp/toolchains.xml", text: """
<toolchains>
	<toolchain>
		<type>jdk</type>
		<provides>
			<version>17</version>
			<vendor>temurin</vendor>
		</provides>
		<configuration>
			<jdkHome>${jdk17}</jdkHome>
		</configuration>
	</toolchain>
</toolchains>
""".trim()
				} catch (e) {
					echo "Failed to setup toolchains: ${e}"
					error("Toolchain setup failed.")
				} }
			}
		}

		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					script {
						if (env.BRANCH_NAME == 'main') {
							withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
								sh '''./mvnw clean deploy -B \
									-t /tmp/toolchains.xml \
									-Dmaven.test.failure.ignore=true \
									-Dsurefire.rerunFailingTestsCount=3 \
									-Psign -Dgpg.passphrase="${KEYRING_PASSPHRASE}"
								'''
							}
						} else {
							sh '''./mvnw clean verify -B \
								-t /tmp/toolchains.xml \
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
