package org.codefx.java_after_eight;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Config {

	private static final String CONFIG_FILE_NAME = ".recs.config";

	private final Path articleFolder;
	private final Path talkFolder;
	private final Path videoFolder;
	private final Optional<Path> outputFile;

	private Config(String[] raw) {
		if (raw.length == 0)
			throw new IllegalArgumentException("No article path defined.");

		this.articleFolder = readFolder(raw[0]);
		this.talkFolder = readFolder(raw[1]);
		this.videoFolder = readFolder(raw[2]);

		Optional<String> outputFile = raw.length >= 4
				? Optional.of(raw[3])
				: Optional.empty();
		this.outputFile = outputFile
				.map(file -> Paths.get(System.getProperty("user.dir")).resolve(file));
		this.outputFile.ifPresent(file -> {
			boolean notWritable = Files.exists(file) && !Files.isWritable(file);
			if (notWritable)
				throw new IllegalArgumentException("Output path is not writable: " + this.outputFile.get());
		});
	}

	private static Path readFolder(String raw) {
		Path folder = Paths.get(raw);
		if (!Files.exists(folder))
			throw new IllegalArgumentException("Path doesn't exist: " + folder);
		if (!Files.isDirectory(folder))
			throw new IllegalArgumentException("Path is no directory: " + folder);
		return folder;
	}

	public Path articleFolder() {
		return articleFolder;
	}

	public Path talkFolder() {
		return talkFolder;
	}

	public Path videoFolder() {
		return videoFolder;
	}

	public Optional<Path> outputFile() {
		return outputFile;
	}

	public static CompletableFuture<Config> create(String[] args) {
		CompletableFuture<String[]> rawConfig = args.length > 0
				? CompletableFuture.completedFuture(args)
				: readProjectConfig()
				.exceptionally(__ -> readUserConfig().join())
				.exceptionally(__ -> new String[0]);

		return rawConfig
				.thenApply(Config::new);
	}

	private static CompletableFuture<String[]> readProjectConfig() {
		Path workingDir = Paths.get(System.getProperty("user.dir")).resolve(CONFIG_FILE_NAME);
		return readConfig(workingDir);
	}

	private static CompletableFuture<String[]> readUserConfig() {
		Path workingDir = Paths.get(System.getProperty("user.home")).resolve(CONFIG_FILE_NAME);
		return readConfig(workingDir);
	}

	private static CompletableFuture<String[]> readConfig(Path workingDir) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Files.readAllLines(workingDir).toArray(new String[0]);
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		});
	}

}
