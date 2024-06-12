/**
 * Copyright (c) 2023 Sebastian Thomschke and contributors.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * based on https://github.com/sebthom/extra-syntax-highlighting-eclipse-plugin/blob/main/plugin/updater
 */
package updater.utils;

import static updater.utils.Log.*;
import static updater.utils.Sys.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

import updater.utils.Log.WithToString;

/**
 * @author Sebastian Thomschke
 */
public abstract class Git {

	public static class GitCheckoutConfig extends WithToString {

		protected GitCheckoutConfig() {
			// for jackson
		}

		public GitCheckoutConfig(final String repo, final String path, final String ref) {
			this.repo = repo;
			this.path = path;
			this.ref = ref;
		}

		/** git clone URL or github repo name (<b>&lt;org>/&lt;repo></b>) */
		public @JsonProperty(required = true) String repo;

		/** null or sub directory to sparsely checkout */
		public String path;

		/** branch or tag */
		public String ref;
	}

	public static class GitCheckoutState extends GitCheckoutConfig {

		/** commit hash */
		public String commit;

		protected GitCheckoutState() {
			// for jackson
		}

		public GitCheckoutState(final String repo, final String path, final String ref, final String commit) {
			super(repo, path, ref);
			this.commit = commit;
		}
	}

	public static GitCheckoutState gitSparseCheckout(final Path localPath, final GitCheckoutConfig gitCheckoutCfg) throws IOException,
			InterruptedException {
		final var repoURL = gitCheckoutCfg.repo.contains("://") ? gitCheckoutCfg.repo : "https://github.com/" + gitCheckoutCfg.repo;

		logInfo("Sparse checkout [" + repoURL + "]...");
		try (var l = withLogIndented()) {
			logInfo("Local repo dir: [" + localPath + "]");
			final var gitPullArgs = new ArrayList<String>();
			gitPullArgs.add("pull");
			gitPullArgs.add("--ff-only");
			gitPullArgs.add("--depth");
			gitPullArgs.add("1");
			if (IS_A_TTY) {
				gitPullArgs.add("--progress");
			}
			var ref = gitCheckoutCfg.ref;
			if (ref == null) {
				logInfo("Determining default branch of [" + repoURL + "]... ", false);
				final var out = execSilent(Path.of("."), "git", "ls-remote", "--symref", repoURL, "HEAD");
				if (out == null)
					throw new IllegalArgumentException("[ref] is null and cannot be determined automatically.");
				final var refMatcher = Pattern.compile("refs/heads/([^\\s]*)").matcher(out.get(0));
				if (!refMatcher.find())
					throw new IllegalArgumentException("[ref] is null and cannot be determined automatically from: " + out);
				ref = refMatcher.group(1);
				logInfo("OK -> " + ref, true, false);
			}
			gitPullArgs.add("origin");
			gitPullArgs.add(ref);

			// check if required origin ref is checked out already
			if (Files.exists(localPath.resolve(".git"))) {
				// final var originRef = execSilent(localPath, "git", "rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}")
				// if (originRef != null && ("origin/" + ref).equals(originRef.get(0))) {
				final var localRef = execSilent(localPath, "git", "symbolic-ref", "--short", "HEAD");
				if (localRef != null && localRef.get(0).equals(ref)) {
					try {
						execVerbose(localPath, "git", gitPullArgs.toArray(String[]::new));
						final var commitHash = execSilent(localPath, "git", "rev-parse", "HEAD");
						return new GitCheckoutState(gitCheckoutCfg.repo, gitCheckoutCfg.path, ref, commitHash.get(0));
					} catch (final Exception ex) {
						// ignore
					}
				}

				// delete local git repo if not in desired state
				try (Stream<Path> files = Files.walk(localPath)) {
					files.sorted(Comparator.reverseOrder()) //
							.map(Path::toFile) //
							.forEach(File::delete);
				}
			}

			Files.createDirectories(localPath);
			execVerbose(localPath, "git", "-c", "init.defaultBranch=" + ref, "init");
			if (IS_WINDOWS) {
				// workaround for potential "error: invalid path" on git pull
				execVerbose(localPath, "git", "config", "core.protectNTFS", "false");
			}
			execVerbose(localPath, "git", "remote", "add", "origin", repoURL);
			if (gitCheckoutCfg.path != null) {
				execVerbose(localPath, "git", "sparse-checkout", "set", gitCheckoutCfg.path);
			}
			execVerbose(localPath, "git", gitPullArgs.toArray(String[]::new));
			final var commitHash = execSilent(localPath, "git", "rev-parse", "HEAD");
			return new GitCheckoutState(gitCheckoutCfg.repo, gitCheckoutCfg.path, ref, commitHash.get(0));
		}
	}

	public static Path gitSparseCheckoutTemp(final GitCheckoutConfig gitCheckoutCfg) throws IOException, InterruptedException {
		final Path tempDir = Files.createTempDirectory("temp_gitrepo");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				rmDir(tempDir);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}));
		gitSparseCheckout(tempDir, gitCheckoutCfg);
		return tempDir;
	}
}
