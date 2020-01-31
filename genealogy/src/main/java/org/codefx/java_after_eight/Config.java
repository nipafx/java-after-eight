package org.codefx.java_after_eight;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Config {

	private static String CONFIG_FILE_NAME = ".recs.config";

	private final Path articleFolder;
	private final Optional<Path> outputFile;

	private Config(String[] raw) {
		if (raw.length == 0)
			throw new IllegalArgumentException("No article path defined.");

		this.articleFolder = Paths.get(raw[0]);
		if (!Files.exists(articleFolder))
			throw new IllegalArgumentException("Article path doesn't exist: " + articleFolder);
		if (!Files.isDirectory(articleFolder))
			throw new IllegalArgumentException("Article path is no directory: " + articleFolder);

		Optional<String> outputFile = raw.length >= 2
				? Optional.of(raw[1])
				: Optional.empty();
		this.outputFile = outputFile
				.map(file -> Paths.get(System.getProperty("user.dir")).resolve(file));
		this.outputFile.ifPresent(file -> {
			boolean notWritable = Files.exists(file) && !Files.isWritable(file);
			if (notWritable)
				throw new IllegalArgumentException("Output path is not writable: " + this.outputFile.get());
		});
	}

	public Path articleFolder() {
		return articleFolder;
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
