package org.springframework.async.io;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class Buffer implements Comparable<Buffer> {

	public static int SMALL_BUFFER_SIZE = 1024 * 16;
	public static int MAX_BUFFER_SIZE = 1024 * 1000;

	private ByteBuffer buffer;
	private final boolean dynamic;

	public Buffer() {
		dynamic = true;
	}

	public Buffer(int atLeast, boolean fixed) {
		if (fixed) {
			if (atLeast <= MAX_BUFFER_SIZE) {
				buffer = ByteBuffer.allocateDirect(atLeast);
			} else {
				throw new IllegalArgumentException("Requested buffer size exceeds maximum allowed (" + MAX_BUFFER_SIZE + ")");
			}
		} else {
			ensureCapacity(atLeast);
		}
		dynamic = !fixed;
	}

	public Buffer(Buffer bufferToCopy) {
		this.dynamic = bufferToCopy.dynamic;
		this.buffer = bufferToCopy.buffer.duplicate();
	}

	public Buffer(ByteBuffer bufferToStartWith) {
		this.dynamic = true;
		this.buffer = bufferToStartWith;
	}

	public int position() {
		return (null == buffer ? 0 : buffer.position());
	}

	public int capacity() {
		return (null == buffer ? SMALL_BUFFER_SIZE : buffer.capacity());
	}

	public int remaining() {
		return (null == buffer ? SMALL_BUFFER_SIZE : buffer.remaining());
	}

	public Buffer clear() {
		if (null != buffer) buffer = null;
		return this;
	}

	public Buffer flip() {
		if (null != buffer) buffer.flip();
		return this;
	}

	public Buffer rewind() {
		if (null != buffer) buffer.rewind();
		return this;
	}

	public byte get() {
		if (null != buffer) buffer.get();
		throw new BufferUnderflowException();
	}

	public Buffer get(byte[] b) {
		if (null != buffer) buffer.get(b);
		return this;
	}

	public String getAsString() {
		if (null != buffer) {
			byte[] b = new byte[buffer.remaining()];
			buffer.get(b, 0, b.length);
			return new String(b);
		} else {
			return null;
		}
	}

	public Buffer append(String s) {
		Objects.requireNonNull(s, "Cannot add null values to a buffer");
		ensureCapacity(s.length());
		buffer.put(s.getBytes());
		return this;
	}

	public Buffer append(ByteBuffer b) {
		Objects.requireNonNull(b, "Cannot add null values to a buffer");
		ensureCapacity(b.remaining());
		buffer.put(b);
		return this;
	}

	public Buffer append(byte b) {
		Objects.requireNonNull(b, "Cannot add null values to a buffer");
		ensureCapacity(1);
		buffer.put(b);
		return this;
	}

	public Buffer append(byte[] b) {
		Objects.requireNonNull(b, "Cannot add null values to a buffer");
		ensureCapacity(b.length);
		buffer.put(b);
		return this;
	}

	public int transferTo(WritableByteChannel channel) throws IOException {
		return (null != buffer ? channel.write(buffer) : 0);
	}

	public ByteBuffer getByteBuffer() {
		return buffer;
	}

	@Override public String toString() {
		return null != buffer ? buffer.toString() : null;
	}

	@Override public int compareTo(Buffer buffer) {
		return (null != buffer ? this.buffer.compareTo(buffer.buffer) : -1);
	}

	private void ensureCapacity(int atLeast) {
		if (null == buffer) {
			buffer = ByteBuffer.allocateDirect(SMALL_BUFFER_SIZE);
			return;
		}
		if (dynamic && buffer.remaining() < atLeast) {
			if (buffer.capacity() + SMALL_BUFFER_SIZE <= MAX_BUFFER_SIZE) {
				ByteBuffer newBuff = ByteBuffer.allocateDirect(buffer.limit() + SMALL_BUFFER_SIZE);
				buffer.flip();
				newBuff.put(buffer);
				buffer = newBuff;
			} else {
				throw new IllegalStateException("Requested buffer size exceeds maximum allowed (" + MAX_BUFFER_SIZE + ")");
			}
		}
	}

}
