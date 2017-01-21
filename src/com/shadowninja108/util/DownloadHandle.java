//credit to http://stackoverflow.com/a/14069976 for code

package com.shadowninja108.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Observable;

import com.shadowninja108.main.Frame;

public class DownloadHandle extends Observable implements Runnable {

	private URL url;
	private File path;
	private int size;
	private long downloaded;
	private boolean complete = false;

	// Constructor for Download.
	public DownloadHandle(URL url, File path) {
		this.url = url;
		this.path = path;
		if (!this.path.exists())
			this.path.getParentFile().mkdirs();
		try {
			this.path.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		size = -1;
		downloaded = 0;
		download();
	}

	public URL getURL() {
		return url;
	}

	public int getSize() {
		return size;
	}

	public float getProgress() {
		return ((float) downloaded / size) * 100;
	}

	private void download() {
		Thread thread = new Thread(this);
		thread.start();
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void run() {
		CountingOutputStream out;
		InputStream in;
		in = null;
		out = null;
		try {
			int len;
			URLConnection url = getURL().openConnection();
			in = url.getInputStream();
			out = new CountingOutputStream(new FileOutputStream(path));

			size = url.getContentLength();

			byte[] buffer = new byte[4096];

			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
				downloaded = out.getProgress();
			}
		} catch (IOException e) {
			Frame.error("Error occured downloading: " + path.getName() + "!");
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				Frame.error("Failed to close streams!");
			}
			complete = true;
			setChanged();
		}
	}
}