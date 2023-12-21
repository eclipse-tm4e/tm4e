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

/**
 * @author Sebastian Thomschke
 */
public abstract class Log {

	public abstract static class WithToString {
		@Override
		public String toString() {
			return Log.toString(this);
		}
	}

	interface Ansi {
		String ERASE_LINE = "\033[2K";
		String RESET = "\033[0m";
		String BOLD = "\033[1m";
		String RED = "\033[31m";
		String GREEN = "\033[32m";
		String YELLOW = "\033[33m";
		String BLUE = "\033[34m";
		String MAGENTA = "\033[35m";
		String CYAN = "\033[36m";
		String WHITE = "\033[37m";
		String GRAY = "\033[90m";
	}

	private static String logIndentation = "";

	public static class LogDedentingCloseable implements AutoCloseable {
		@Override
		public void close() {
			logIndentation = logIndentation.length() < 2 ? "" : logIndentation.substring(0, logIndentation.length() - 2);
		}
	}

	public static void logError(final Exception ex) {
		final var mainColor = Ansi.BOLD + Ansi.RED;
		final var accentColor = Ansi.BOLD + Ansi.WHITE;
		System.err.println(mainColor + "[ERROR] " + ex.getClass().getSimpleName() + ": " + ex.getMessage().replace("[", "[" + accentColor)
				.replace("]", mainColor + "]") + Ansi.RESET);
		System.err.flush();
	}

	public static void logHeader(final String msg) {
		final var mainColor = Ansi.BOLD + Ansi.CYAN;
		final var accentColor = Ansi.BOLD + Ansi.MAGENTA;
		System.out.println(logIndentation + mainColor + "==================================================================");
		System.out.println(logIndentation + msg.replace("[", "[" + accentColor).replace("]", mainColor + "]"));
		System.out.println(logIndentation + mainColor + "==================================================================" + Ansi.RESET);
		System.out.flush();
	}

	public static void logInfo(final String msg) {
		logInfo(msg, true);
	}

	public static void logInfo(final String msg, final boolean withNewLine) {
		logInfo(msg, withNewLine, true);
	}

	public static void logInfo(final String msg, final boolean withNewLine, final boolean withIndentation) {
		final var mainColor = Ansi.BOLD + (logIndentation.length() % 4 == 0 ? Ansi.WHITE : Ansi.GRAY);
		final var accentColor = Ansi.BOLD + (logIndentation.length() % 4 == 0 ? Ansi.MAGENTA : Ansi.BLUE);
		System.out.print( //
				(withIndentation ? logIndentation : "") //
						+ mainColor //
						+ msg.replace("[", "[" + accentColor).replace("]", mainColor + "]") //
						+ Ansi.RESET);
		if (withNewLine) {
			System.out.println();
		}
		System.out.flush();
	}

	public static <T> String toString(final T obj) {
		final var sb = new StringBuilder();
		for (final var field : obj.getClass().getFields()) {
			if (!field.canAccess(obj)) {
				field.trySetAccessible();
			}

			if (!sb.isEmpty()) {
				sb.append(", ");
			}
			sb.append(field.getName()).append("=");
			Object fieldValue = null;
			try {
				fieldValue = field.get(obj);

			} catch (final IllegalAccessException ex) {
				fieldValue = "<IllegalAccessException: " + ex.getMessage() + ">";
			}
			sb.append(fieldValue);
		}
		return obj.getClass().getSimpleName() + " {" + sb.toString() + "}";
	}

	public static LogDedentingCloseable withLogIndented() {
		logIndentation += "  ";
		return new LogDedentingCloseable();
	}
}
