// https://github.com/eclipse-cbi/jiro/wiki/CI-Best-Practices
// https://github.com/eclipse-cbi/jiro/wiki/FAQ#how-do-i-run-a-javamaven-build-on-the-cluster-based-infrastructure
pipeline {
	agent {
		// https://eclipse.dev/cbi/jiro-agent/
		// https://github.com/eclipse-cbi/jiro-agents/
		// https://github.com/eclipse-cbi/jiro-agents/tree/master/ubuntu
		label 'ubuntu-latest'
	}

	// https://www.jenkins.io/doc/book/pipeline/syntax/#triggers
	// https://www.jenkins.io/doc/pipeline/steps/params/pipelinetriggers/
	triggers {
		githubPush()
	}

	// https://www.jenkins.io/doc/book/pipeline/syntax/#options
	options {
		timeout(time: 20, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr: '10'))
		disableConcurrentBuilds(abortPrevious: false)
	}

	tools {
		// https://github.com/eclipse-cbi/jiro/wiki/Tools-(JDK,-Maven,-Ant)#apache-maven
		// https://eclipse.dev/cbi/jiro/Tools/#apache-maven
		maven 'apache-maven-latest'

		// https://github.com/eclipse-cbi/jiro/wiki/Tools-(JDK,-Maven,-Ant)#eclipse-temurin
		// https://eclipse.dev/cbi/jiro/Tools/#eclipse-temurin
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
					def jdk21 = tool name: 'temurin-jdk21-latest', type: 'jdk'

					// Generate toolchains.xml for Maven Toolchains plugin
					writeFile file: "/tmp/toolchains.xml", text: """
<toolchains>
	<toolchain>
		<type>jdk</type>
		<provides>
			<version>21</version>
			<vendor>temurin</vendor>
		</provides>
		<configuration>
			<jdkHome>${jdk21}</jdkHome>
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
				// https://github.com/eclipse-cbi/jiro/wiki/FAQ#how-do-i-run-ui-tests-on-the-cluster-based-infrastructure
				wrap([$class: 'Xvnc', takeScreenshot: false, useXauthority: true]) {
					script {
						def maven_opts = (env.MAVEN_OPTS ?: '')

						if (isUnix()) {
							// https://www.baeldung.com/java-security-egd#bd-testing-the-effect-of-javasecurityegd
							maven_opts += ' -Djava.security.egd=file:/dev/./urandom'
						} else {
							// https://stackoverflow.com/questions/58991966/what-java-security-egd-option-is-for/59097932#59097932
							maven_opts += ' -Djava.security.egd=file:/dev/urandom'
						}

						// https://stackoverflow.com/questions/5120470/how-to-time-the-different-stages-of-maven-execution/49494561#49494561
						maven_opts += ' -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss,SSS'

						def maven_args = [
							'--no-transfer-progress',
							'--update-snapshots',
							'--batch-mode',
							'-t /tmp/toolchains.xml'
						].join(' ')

						withEnv(["MAVEN_OPTS=${maven_opts}"]) {
							echo "MAVEN_OPTS: ${env.MAVEN_OPTS}"

							if (env.BRANCH_NAME == 'main') {
								withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
									sh """./mvnw ${maven_args} -Psign -Dgpg.passphrase="\$KEYRING_PASSPHRASE" clean deploy"""
								}
							} else {
								sh "./mvnw ${maven_args} clean verify"
							}
						}
					}
				}
			}
			post {
				always {
					archiveArtifacts artifacts: 'org.eclipse.tm4e.repository/target/repository/**/*,org.eclipse.tm4e.repository/target/*.zip,*/target/work/data/.metadata/.log'
					junit '**/target/surefire-reports/TEST-*.xml'
				}
			}
		}

		stage('Deploy Snapshot') {
			when {
				branch 'main'
			}
			steps {
				// https://github.com/eclipse-cbi/jiro/wiki/FAQ#how-do-i-deploy-artifacts-to-downloadeclipseorg
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
