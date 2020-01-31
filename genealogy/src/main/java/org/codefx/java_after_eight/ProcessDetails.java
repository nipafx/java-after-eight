package org.codefx.java_after_eight;

import sun.management.VMManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static java.lang.String.format;

public class ProcessDetails {

	public static String details() {
		return format(
				"Process ID: %s | Major Java version: %s",
				getPid().map(Object::toString).orElse("unknown"),
				getMajorJavaVersion().map(Object::toString).orElse("unknown"));
	}

	public static Optional<Long> getPid() {
		Optional<Long> pid = getPidFromMxBeanName();
		if (pid.isPresent())
			return pid;
		pid = getPidFromMxBeanInternal();
		if (pid.isPresent())
			return pid;
		pid = getPidFromProcSelfSymlink();
		if (pid.isPresent())
			return pid;
		pid = getPidFromBashPid();
		if (pid.isPresent())
			return pid;

		return Optional.empty();
	}

	private static Optional<Long> getPidFromMxBeanName() {
		// on many VMs `ManagementFactory.getRuntimeMXBean().getName()`
		// returns something like 1234@localhost
		String[] pidAndHost = ManagementFactory.getRuntimeMXBean().getName().split("@");
		try {
			return Optional.of(Long.parseLong(pidAndHost[0]));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	private static Optional<Long> getPidFromMxBeanInternal() {
		// crosses fingers
		try {
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			Field jvm_field = runtime.getClass().getDeclaredField("jvm");
			jvm_field.setAccessible(true);
			VMManagement mgmt = (VMManagement) jvm_field.get(runtime);

			Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);
			int pid = (int) pid_method.invoke(mgmt);

			return Optional.of((long) pid);
		} catch (ClassCastException | ReflectiveOperationException ex) {
			return Optional.empty();
		}
	}

	private static Optional<Long> getPidFromProcSelfSymlink() {
		try {
			String pid = new File("/proc/self").getCanonicalFile().getName();
			return Optional.of(Long.parseLong(pid));
		} catch (IOException | NumberFormatException ex) {
			return Optional.empty();
		}
	}

	private static Optional<Long> getPidFromBashPid() {
		// if we're on Linux, this should work on all POSIX shells
		try {
			InputStream echoPid = new ProcessBuilder("sh", "-c", "echo $PPID").start().getInputStream();
			String pid = new BufferedReader(new InputStreamReader(echoPid)).readLine();
			return Optional.of(Long.parseLong(pid));
		} catch (IOException | NumberFormatException ex) {
			return Optional.empty();
		}
	}

	public static Optional<Integer> getMajorJavaVersion() {
		try {
			String version = System.getProperty("java.version");
			if (version.startsWith("1."))
				return Optional.of(Integer.parseInt(version.substring(2, 3)));

			if (version.contains("."))
				return Optional.of(Integer.parseInt(version.split("\\.")[0]));

			// hail mary
			return Optional.of(Integer.parseInt(version));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

}
