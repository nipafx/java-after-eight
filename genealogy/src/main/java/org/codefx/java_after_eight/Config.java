package org.codefx.java_after_eight;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record Config(
		Path articleFolder,
		Path talkFolder,
		Path videoFolder,
		Optional<Path> outputFile) {

	private static final String CONFIG_FILE_NAME = "recommendations.config";

	// use static factory method(s)
	@Deprecated
	public Config { }

	private static Config fromRawConfig(String[] raw) {
		if (raw.length == 0)
			throw new IllegalArgumentException("No article path defined.");

		var articleFolder = readFolder(raw[0]);
		var talkFolder = readFolder(raw[1]);
		var videoFolder = readFolder(raw[2]);

		Optional<String> outputFileName = raw.length >= 4
				? Optional.of(raw[3])
				: Optional.empty();
		var outputFile = outputFileName
				.map(file -> Path.of(System.getProperty("user.dir")).resolve(file));
		outputFile.ifPresent(file -> {
			boolean notWritable = Files.exists(file) && !Files.isWritable(file);
			if (notWritable)
				throw new IllegalArgumentException("Output path is not writable: " + outputFile.get());
		});

		return new Config(articleFolder, talkFolder, videoFolder, outputFile);
	}

	private static Path readFolder(String raw) {
		var folder = Path.of(raw);
		if (!Files.exists(folder))
			throw new IllegalArgumentException("Path doesn't exist: " + folder);
		if (!Files.isDirectory(folder))
			throw new IllegalArgumentException("Path is no directory: " + folder);
		return folder;
	}

	public static CompletableFuture<Config> create(String[] args) {
		CompletableFuture<String[]> rawConfig = args.length > 0
				? CompletableFuture.completedFuture(args)
				: readProjectConfig()
				.exceptionallyComposeAsync(__ -> readUserConfig())
				.exceptionallyAsync(__ -> new String[0]);

		return rawConfig
				.thenApply(Config::fromRawConfig);
	}

	private static CompletableFuture<String[]> readProjectConfig() {
		var workingDir = Path.of(System.getProperty("user.dir")).resolve(CONFIG_FILE_NAME);
		return readConfig(workingDir);
	}

	private static CompletableFuture<String[]> readUserConfig() {
		var workingDir = Path.of(System.getProperty("user.home")).resolve(CONFIG_FILE_NAME);
		return readConfig(workingDir);
	}

	private static CompletableFuture<String[]> readConfig(Path workingDir) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Files.readAllLines(workingDir).toArray(String[]::new);
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		});
	}

}
