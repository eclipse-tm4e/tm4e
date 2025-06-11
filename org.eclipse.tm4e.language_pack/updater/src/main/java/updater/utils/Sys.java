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
import static updater.utils.Strings.join;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import updater.utils.Log.Ansi;

/**
 * @author Sebastian Thomschke
 */
public abstract class Sys {

	static {
		sun.misc.Signal.handle(new sun.misc.Signal("INT"), // CTRL+C
				signal -> {
					final var threads = Thread.getAllStackTraces().keySet().stream() //
							.filter(t -> t != Thread.currentThread() && !t.isDaemon()).toList();
					threads.forEach(t -> {
						System.err.println("Interrupting thread '" + t.getName() + "'");
						t.interrupt();
					});
					threads.forEach(t -> {
						System.out.println("Waiting for thread '" + t.getName() + "' termination");
						try {
							t.join();
						} catch (final InterruptedException ex) {
							System.err.println("Shutdown interrupted");
						}
					});
					System.exit(1);
				});
	}

	public static final boolean IS_A_TTY = System.console() != null;

	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

	public static final Path TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"));

	public static void copyFile(final Path from, final Path to) throws IOException {
		logInfo("Copying file [" + to.getFileName() + "]... ", false);
		Files.createDirectories(to.getParent());
		Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
		logInfo("OK -> " + Files.size(to) + " bytes", true, false);
	}

	public static void downloadFile(final URL fileURL, final Path targetFile) throws IOException {
		logInfo("Downloading file [" + fileURL + "]... ", false);
		try (InputStream is = fileURL.openStream()) {
			Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
		logInfo("OK -> " + Files.size(targetFile) + " bytes", true, false);
	}

	public static List<String> execSilent(final Path workDir, final String cmdWithArgs) {
		final var args = Strings.splitLikeShell(cmdWithArgs);
		if (args.size() == 1)
			return execSilent(workDir, args.get(0), new String[0]);
		return execSilent(workDir, args.get(0), args.subList(1, args.size()).toArray(String[]::new));
	}

	/**
	 * @return null if execution failed, otherwise the command's stdout
	 */
	public static List<String> execSilent(final Path workDir, final String cmd, final String... args) {
		final var cmdWithArgs = Stream.concat(Stream.of(cmd), Arrays.stream(args)).toList();
		try {
			final Process process = new ProcessBuilder(cmdWithArgs) //
					.directory(workDir.toFile()) //
					.start();

			try {
				final var lines = new ArrayList<String>();
				try (var processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = processOutput.readLine()) != null) {
						if (Thread.interrupted())
							throw new InterruptedException();
						lines.add(line);
					}
				}
				final int exitCode = process.waitFor();
				if (exitCode != 0)
					return null;
				return lines.isEmpty() ? List.of("") : lines;
			} finally {
				process.destroy();
				process.descendants().forEach(ProcessHandle::destroy);
			}
		} catch (final Exception ex) {
			return null;
		}
	}

	public static void execVerbose(final Path workDir, final String cmdWithArgs) throws IOException, InterruptedException {
		final var args = Strings.splitLikeShell(cmdWithArgs);
		if (args.size() == 1) {
			execVerbose(workDir, args.get(0), new String[0]);
			return;
		}
		execVerbose(workDir, args.get(0), args.subList(1, args.size()).toArray(String[]::new));
	}

	public static void execVerbose(final Path workDir, final String cmd, final String... args) throws IOException, InterruptedException {
		final var cmdWithArgs = Stream.concat(Stream.of(cmd), Arrays.stream(args)).toList();
		logInfo("Executing [" + join(cmdWithArgs.stream().map(s -> s.isEmpty() || s.contains(" ") ? '"' + s + '"' : s), " ") + "]... ",
				false);
		final var builder = new ProcessBuilder(cmdWithArgs);
		builder.directory(workDir.toFile());
		builder.redirectErrorStream(true);
		boolean hasOutput = false;
		final Process process = builder.start();
		try (var l = withLogIndented()) {
			try (var processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = processOutput.readLine()) != null) {
					if (Thread.interrupted())
						throw new InterruptedException();
					if (hasOutput) {
						System.out.print(Ansi.ERASE_LINE + "\r");
						logInfo(line, false);
					} else {
						System.out.println();
						logInfo(line, false);
						hasOutput = true;
					}
				}
			}
			final int exitCode = process.waitFor();
			if (exitCode != 0)
				throw new IOException("Execution of " + cmdWithArgs + " failed with exit code " + exitCode);
			logInfo(hasOutput ? "" : "OK", true, false);
		} finally {
			process.destroy();
			process.descendants().forEach(ProcessHandle::destroy);
		}
	}

	public static Optional<Path> findFirstFile(final Path path, final Predicate<String> nameFilter) throws IOException {
		try (Stream<Path> files = Files.list(path)) {
			return files //
					.filter(Files::isRegularFile) //
					.filter(file -> nameFilter.test(file.getFileName().toString())).findFirst();
		}
	}

	public static String getFileExtension(final Path path) {
		return getFileExtension(path.getFileName().toString());
	}

	public static String getFileExtension(final String path) {
		final int dotIndex = path.lastIndexOf('.');
		return dotIndex == -1 ? "" : path.substring(dotIndex + 1);
	}

	public static void rmDir(final Path dir) throws IOException {
		if (Files.exists(dir)) {
			try (Stream<Path> files = Files.walk(dir)) {
				files.sorted(Comparator.reverseOrder()) //
						.map(Path::toFile) //
						.forEach(File::delete);
			}
		}
	}

	public static Path toAbsolutePath(final Path basePath, final Path relativePath) {
		return (relativePath.isAbsolute() ? relativePath : basePath.toAbsolutePath().resolve(relativePath)).normalize();
	}
}
