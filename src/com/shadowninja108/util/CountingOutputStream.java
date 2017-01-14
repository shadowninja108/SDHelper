package com.shadowninja108.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CountingOutputStream extends BufferedOutputStream {
	long count;

	public CountingOutputStream(OutputStream out) {
		super(out);
	}

	public synchronized void write(int b) throws IOException {
		super.write(b);
		this.count += 1;
	}

	public synchronized void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		this.count += len;
	}

	public void write(byte[] b) throws IOException {
		super.write(b);
		this.count += b.length;
	}

	public long getProgress() {
		return this.count;
	}
}
