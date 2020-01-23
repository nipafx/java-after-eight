package org.codefx.demo.java_after_eight.article;

final class Utils {

	private Utils() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static String removeOuterQuotationMarks(String string) {
		return string.replaceAll("^\"|\"$", "");
	}

}
