---
title: "Code-First Unix Domain Socket Tutorial"
tags: [java-16]
date: 2021-03-03
slug: java-unix-domain-sockets
description: "Java's socket channel / server-socket channel API can use Unix domain sockets for faster and more secure inter-process communication on the same host"
canonicalText: "I originally created this tutorial as a part of [a hands-on Lab for modern Java features](https://delabassee.com/odl-16-lab/)."
featuredImage: unix-domain-sockets
repo: java-x-demo
---

Java's [`SocketChannel`](https://download.java.net/java/early_access/jdk16/docs/api/java.base/java/nio/channels/SocketChannel.html) / [`ServerSocketChannel`](https://download.java.net/java/early_access/jdk16/docs/api/java.base/java/nio/channels/ServerSocketChannel.html) API provides blocking and multiplexed non-blocking access to sockets.
Before Java 16, this was limited to TCP/IP sockets - with [JEP 380](https://openjdk.java.net/jeps/380) it is now also possible to access Unix domain sockets.
They are addressed by filesystem path names and you can use them for inter-process communication on the same host.
Unix domain sockets are supported on Unix based operating system (Linux, MacOS) and - despite their name - Windows 10 and Windows Server 2019.

Here's a quick tutorial on how to use the API.

## Accessing a Unix Domain Socket

As mentioned, Unix domain sockets are based on path names, so the first thing we need is a path that we can then turn into a socket address.
This can be any path, but to make sure we have the required permission, I'll use a file in the home directory.

```java
Path socketFile = Path
	.of(System.getProperty("user.home"))
	.resolve("server.socket");
UnixDomainSocketAddress address =
	UnixDomainSocketAddress.of(socketFile);
```

The next step is to launch a server and a client on that address.
To create them, we need to pass the new `StandardProtocolFamily.UNIX` to the respective [static factory methods](effective-java-static-factory-methods):

```java
// server
ServerSocketChannel serverChannel = ServerSocketChannel
	.open(StandardProtocolFamily.UNIX);
serverChannel.bind(address);

// client
SocketChannel channel = SocketChannel
	.open(StandardProtocolFamily.UNIX);
```

The third and final step before we can send messages is the client and server connecting to one another:

```java
// server
SocketChannel channel = serverChannel.accept();

// client
channel.connect(address);
```

If you want to play around with this, the server and client need to have their own source files `Server.java` and `Client.java`:

```java
// in Server.java
public static void main(String[] args)
		 throws IOException, InterruptedException {
	Path socketFile = Path
		.of(System.getProperty("user.home"))
		.resolve("server.socket");
	// in case the file is left over from the last run,
	// this makes the demo more robust
	Files.deleteIfExists(socketFile);
	UnixDomainSocketAddress address =
		UnixDomainSocketAddress.of(socketFile);

	ServerSocketChannel serverChannel = ServerSocketChannel
		.open(StandardProtocolFamily.UNIX);
	serverChannel.bind(address);

	System.out.println("[INFO] Waiting for client to connect...");
	SocketChannel channel = serverChannel.accept();
	System.out.println("[INFO] Client connected");

	// start receiving messages
}

// in Client.java
public static void main(String[] args)
		 throws IOException, InterruptedException {
	Path socketFile = Path
		.of(System.getProperty("user.home"))
		.resolve("server.socket");
	UnixDomainSocketAddress address =
		UnixDomainSocketAddress.of(socketFile);

	SocketChannel channel = SocketChannel
		.open(StandardProtocolFamily.UNIX);
	channel.connect(address);

	// start receiving messages
}
```

Let's give this a go!
You may of course be using your IDE to compile and launch the two classes, but for this tutorial I'll stick to [executing the source files directly](scripting-java-shebang) with `java` from two terminals.
In the first one, launch the server as follows:

```shell
$ java Server.java
# [INFO] Waiting for client to connect...
```

As you can see, the server launches and then waits for a connection.
Now launch the client in the second terminal:

```shell
$ java Client.java
```

Because the server is already running, the client can immediately connect and then exit the program.
Checking back with the first terminal, you will see that the server registered the connection and shut down - that's because neither client nor server pass any messages.
So let's do that next.


## Passing Messages

The `SocketChannel` and `ServerSocketChannel` classes have existed since Java 4 and what kind of socket they use to connect makes no difference in how messages are passed between them.
That means the following code is not specific to Unix domain sockets and works the same with TCP/IP.

Both server and client can send and receive messages, but for simplicity's sake, we're just going to send from the client to the server.

### Sending Messages

For the client to send a message, we need to create a `ByteBuffer`, fill it with the message's bytes, flip it for sending, and then write to the channel:

```java
// in Client.java
private static void writeMessageToSocket(
		SocketChannel socketChannel, String message)
		throws IOException {
	ByteBuffer buffer= ByteBuffer.allocate(1024);
	buffer.clear();
	buffer.put(message.getBytes());
	buffer.flip();
	while(buffer.hasRemaining()) {
		socketChannel.write(buffer);
	}
}
```

We now use this method to send a few messages to the server:

```java
// in Client.java
public static void main(String[] args)
		throws IOException, InterruptedException {

	// as above

	Thread.sleep(3_000);
	writeMessageToSocket(channel, "Hello");
	Thread.sleep(1_000);
	writeMessageToSocket(channel, "Unix domain sockets");
}
```

### Receiving Messages

On the receiving side, we do similar steps, but in reverse: read from the channel, flip the bytes, turn them into a message:

```java
// in Server.java
private static Optional<String> readMessageFromSocket(
		SocketChannel channel)
		throws IOException {
	ByteBuffer buffer = ByteBuffer.allocate(1024);
	int bytesRead = channel.read(buffer);
	if (bytesRead < 0)
		return Optional.empty();

	byte[] bytes = new byte[bytesRead];
	buffer.flip();
	buffer.get(bytes);
	String message = new String(bytes);
	return Optional.of(message);
}
```

```java
// in Server.java
public static void main(String[] args)
		throws IOException, InterruptedException {

	// as above

	while (true) {
		readMessageFromSocket(channel)
			.ifPresent(System.out::println);
		Thread.sleep(100);
	}
}
```

This creates an infinite loop that checks every 100 ms whether a new message was written to the socket and, if so, outputs it.
This means that the server will now run indefinitely until you shut it down in the terminal by hitting CTRL-C.

If you launch the server and client (in that order) as before, you will now see that the messages sent by the client are printed to the output by the server.


## Real-life Complexities

The code presented above just scratches the surface of how to implement the communication via sockets.
You will notice that the server always needs to be launched first, that it can only accept one connection, that as soon as that's abandoned by the client, it can never create a new one (try to launch `Client.java` several times), and that it runs indefinitely until forced to shut down.
It does no clean-up (like deleting the created `server.socket` file) and neither does the client (by closing the connection).

Compared to TCP/IP loopback connections, Unix domain sockets have a few advantages:

* Because they can only be used for communication on the same host, opening them instead of a TCP/IP socket has no risk to accept remote connections.
* Access control is applied with file-based mechanisms, which are detailed, well understood, and enforced by the operating system.
* Unix domain sockets have faster setup times and higher data throughput than TCP/IP loopback connections.

Note that you can even use Unix domain sockets for communication between containers on the same system as long as you create the sockets on a shared volume.


## Reflection

In this tutorial, we have used the socket channel API to establish inter-process communication on the same host with Unix domain sockets, which were added to the pre-existing API in Java 16.
The new code paths boil down to:

* create a `UnixDomainSocketAddress`
* create `ServerSocketChannel` and `SocketChannel` with `StandardProtocolFamily.UNIX`
* bind the server and connect the client to the address

Unix domain sockets are both more secure and more efficient than TCP/IP loopback connections and supported on all Unix-based operating system as well as modern Windows versions.

If you want to div deeper into the topic, check out [Michael McMahon's article on Inside Java](https://inside.java/2021/02/03/jep380-unix-domain-sockets-channels/).
