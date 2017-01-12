//credit to http://stackoverflow.com/a/14069976 for code

package com.shadowninja108.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

import com.shadowninja108.interpret.Interpreter;

// This class downloads a file from a URL.
public class DownloadHandle extends Observable implements Runnable {

	// Max size of download buffer.
	private static final int MAX_BUFFER_SIZE = 1024;

	// These are the status names.
	public static final String STATUSES[] = { "Downloading", "Paused", "Complete", "Cancelled", "Error" };

	// These are the status codes.
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;

	private URL url; // download URL
	private File path;
	private int size; // size of download in bytes
	private int downloaded; // number of bytes downloaded
	private int status; // current status of download

	public ExtractionTag tag;

	// Constructor for Download.
	public DownloadHandle(URL url, String path, ExtractionTag tag) {
		this.url = url;
		this.path = new File(Interpreter.root, path);
		if (tag != null)
			this.tag = tag;
		if (!this.path.exists())
			this.path.getParentFile().mkdirs();
		try {
			this.path.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		size = -1;
		downloaded = 0;
		status = DOWNLOADING;

		// Begin the download.
		download();
	}

	public void runTag() {
		if (tag != null)
			try {
				if (tag.m == null)
					System.out.println("wasdwa");
				tag.m.invoke(tag.interpreter, new Object[] { tag.node, tag });
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				tag.completed = true;
			}
	}

	// Get this download's URL.
	public URL getURL() {
		return url;
	}

	// Get this download's size.
	public int getSize() {
		return size;
	}

	// Get this download's progress.
	public float getProgress() {
		return ((float) downloaded / size) * 100;
	}

	// Get this download's status.
	public int getStatus() {
		return status;
	}

	// Pause this download.
	public void pause() {
		status = PAUSED;
		stateChanged();
	}

	// Resume this download.
	public void resume() {
		status = DOWNLOADING;
		stateChanged();
		download();
	}

	// Cancel this download.
	public void cancel() {
		status = CANCELLED;
		stateChanged();
	}

	// Mark this download as having an error.
	private void error() {
		status = ERROR;
		stateChanged();
	}

	// Start or resume downloading.
	private void download() {
		Thread thread = new Thread(this);
		thread.start();
	}

	// Get file name portion of URL.
	private String getFileName() {
		return path.getPath().substring(path.getPath().lastIndexOf('/') + 1);
	}

	// Download file.
	@Override
	public void run() {
		RandomAccessFile file = null;
		InputStream stream = null;

		try {
			/*
			 * if (Files.isReadable(Paths.get(getFileName()))) { String name =
			 * getFileName().substring(getFileName().lastIndexOf("/") + 1); int
			 * r = JOptionPane.showConfirmDialog(null, "File " + name +
			 * " already exists. Do you want to replace it?", "Message",
			 * JOptionPane.YES_NO_OPTION); if (r == JOptionPane.YES_OPTION) {
			 * Files.delete(Paths.get(getFileName())); } }
			 */

			// Open connection to URL.
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Specify what portion of file to download.
			connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

			// Connect to server.
			connection.connect();

			// Make sure response code is in the 200 range.
			if (connection.getResponseCode() / 100 != 2) {
				System.out.println("Response code out of range!");
				error();
			}

			// Check for valid content length.
			int contentLength = connection.getContentLength();
			if (contentLength < 1) {
				System.out.println("Invalid length!");
				error();
			}

			/*
			 * Set the size for this download if it hasn't been already set.
			 */
			if (size == -1) {
				size = contentLength;
				stateChanged();
			}

			// Open file and seek to the end of it.
			file = new RandomAccessFile(getFileName(), "rw");
			file.seek(downloaded);

			stream = connection.getInputStream();
			while (status == DOWNLOADING) {
				/*
				 * Size buffer according to how much of the file is left to
				 * download.
				 */
				byte buffer[];
				if (size - downloaded > MAX_BUFFER_SIZE) {
					buffer = new byte[MAX_BUFFER_SIZE];
				} else {
					buffer = new byte[size - downloaded];
				}

				// Read from server into buffer.
				int read = stream.read(buffer);
				if (read == -1)
					break;

				// Write buffer to file.
				file.write(buffer, 0, read);
				downloaded += read;
				stateChanged();
			}

			/*
			 * Change status to complete if this point was reached because
			 * downloading has finished.
			 */
			if (status == DOWNLOADING) {
				status = COMPLETE;
				stateChanged();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			error();
		} finally {
			// Close file.
			if (file != null) {
				try {
					file.close();
				} catch (Exception e) {
				}
			}

			// Close connection to server.
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
				}
			}
		}
	}

	// Notify observers that this download's status has changed.
	private void stateChanged() {
		setChanged();
		notifyObservers();
	}
}