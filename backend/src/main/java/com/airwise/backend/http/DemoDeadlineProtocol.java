package com.airwise.backend.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.SocketBufferHandler;

/**
 * HTTP/1.1 NIO transport for the local demo. During its synchronous body-read scope each physical
 * read, including chunk framing, waits only for the remaining absolute deadline. Ordinary reads
 * retain Tomcat's implementation. TLS and HTTP/2 require a separately validated transport.
 */
public final class DemoDeadlineProtocol extends Http11NioProtocol {
    public DemoDeadlineProtocol() { super(new DeadlineEndpoint()); }

    @Override public void init() throws Exception {
        if (isSSLEnabled() || findUpgradeProtocols().length != 0) {
            throw new IllegalStateException("The demo deadline transport requires plain HTTP/1.1 NIO");
        }
        super.init();
    }

    private static final class DeadlineEndpoint extends NioEndpoint {
        @Override protected NioChannel createChannel(SocketBufferHandler buffer) {
            return new DeadlineChannel(buffer);
        }
    }

    private static final class DeadlineChannel extends NioChannel {
        DeadlineChannel(SocketBufferHandler buffer) { super(buffer); }

        @Override public int read(ByteBuffer destination) throws IOException {
            BodyReadDeadline deadline = BodyReadDeadline.current();
            if (deadline == null || !destination.hasRemaining()) return super.read(destination);
            deadline.remainingMillis();
            int count = super.read(destination);
            if (count != 0) return count;
            // SocketChannel is nonblocking and may be registered with more than one selector.
            // This local selector is used only while the container's synchronous read lock is held.
            // It never dispatches work, mutates Tomcat's poller key or survives this method.
            try (Selector selector = Selector.open()) {
                SelectionKey key = getIOChannel().register(selector, SelectionKey.OP_READ);
                while (true) {
                    selector.select(deadline.remainingMillis());
                    deadline.remainingMillis();
                    if (Thread.currentThread().isInterrupted()) throw new IOException("Body read interrupted");
                    if (!key.isValid()) throw new ClosedChannelException();
                    selector.selectedKeys().clear();
                    count = super.read(destination);
                    if (count != 0) return count;
                }
            }
        }

        @Override public long read(ByteBuffer[] destinations) throws IOException {
            return read(destinations, 0, destinations.length);
        }

        @Override public long read(ByteBuffer[] destinations, int offset, int length) throws IOException {
            if (BodyReadDeadline.current() == null) return super.read(destinations, offset, length);
            java.util.Objects.checkFromIndexSize(offset, length, destinations.length);
            for (int i = offset; i < offset + length; i++) {
                if (destinations[i].hasRemaining()) return read(destinations[i]);
            }
            return 0;
        }
    }
}
