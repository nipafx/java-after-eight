package org.codefx.demo.java_after_eight;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Main {

	// text similarities: https://medium.com/@adriensieg/text-similarities-da019229c894
	// Stanford CoreNLP: https://github.com/stanfordnlp/CoreNLP

	public static final Path ARTICLE_FOLDER = Paths.get("/home/nipa/code/nipafx.org/blog-import/output");

	public static void main(String[] args) throws IOException {
		Files.list(ARTICLE_FOLDER)
				.filter(Files::isRegularFile)
				.filter(file -> file.toString().endsWith(".md"))
				.forEach(Main::process);
	}

	private static void process(Path article) {
		try {
			List<String> articleContent = Files.lines(article).collect(toList());
			System.out.println(articleContent.get(articleContent.size() - 1));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
