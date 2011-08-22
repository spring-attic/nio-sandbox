package org.springframework.async;

import static org.junit.Assert.*;

import java.nio.BufferOverflowException;
import java.util.Random;

import org.junit.Test;
import org.springframework.async.io.Buffer;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class BufferTests {

	private final String HELLO_WORLD = "Hello World!";

	@Test
	public void testBufferAsString() {
		String hw = new Buffer().append(HELLO_WORLD).flip().getAsString();

		assertEquals(HELLO_WORLD, hw);
	}

	@Test
	public void testExpandingBuffer() {
		Buffer buffer = new Buffer();

		Random r = new Random();
		for (int i = 0; i < 3; i++) {
			byte[] b = new byte[10000];
			r.nextBytes(b);
			buffer.append(b);
		}

		assertEquals(30000, buffer.position());
	}

	@Test(expected = BufferOverflowException.class)
	public void testFixedBuffer() {
		new Buffer(10, true).append(HELLO_WORLD);
	}

}
