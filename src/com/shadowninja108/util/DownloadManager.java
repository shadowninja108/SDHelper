package com.shadowninja108.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

import com.shadowninja108.main.Frame;

public class DownloadManager {

	public Frame frame;

	public FileDownloaderThread thread;

	public ArrayList<DownloadHandle> toBeAdded;
	public ArrayList<DownloadHandle> handles;
	public ArrayList<String> completed;

	public int status = 0;

	public boolean running = false;

	public DownloadManager(Frame frame) {
		running = true;
		completed = new ArrayList<>();
		handles = new ArrayList<DownloadHandle>();
		toBeAdded = new ArrayList<DownloadHandle>();
		this.frame = frame;
		thread = new FileDownloaderThread();
		thread.start();
	}

	public boolean waitForDownload(String url) {
		while (!getTBHandles().isEmpty()) {
		}
		Iterator<DownloadHandle> it = getHandles().iterator();
		DownloadHandle found = null;
		while (it.hasNext()) {
			DownloadHandle search = it.next();
			try {
				if (search.getURL().sameFile(new URL(url))) {
					found = search;
					break;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (found == null) {
			return false;
		}
		while (!getHandles().isEmpty()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Frame.setStatus("Extracting...");
		Frame.progressBar.setStringPainted(false);
		Frame.progressBar.setIndeterminate(true);
		return true;
	}

	public void download(String web, File path, ExtractionTag tag) {
		try {
			toBeAdded.add(new DownloadHandle(new URL(web), path, tag));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		running = false;
		try {
			thread.join(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized ArrayList<DownloadHandle> getTBHandles() {
		return toBeAdded;
	}

	public synchronized ArrayList<DownloadHandle> getHandles() {
		return handles;
	}

	public class FileDownloaderThread extends Thread {
		@Override
		public void run() {
			while (running) {
				while (getHandles().size() > 0 || getTBHandles().size() > 0) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						Frame.error("Who interupted the thread?");
					}

					if (!getTBHandles().isEmpty() || !getHandles().isEmpty())
						getTBHandles().forEach(new Consumer<DownloadHandle>() {
							@Override
							public void accept(DownloadHandle handle) {
								getHandles().add(handle);
							}
						});
					getTBHandles().clear();

					Iterator<DownloadHandle> it = getHandles().iterator();
					while (it.hasNext()) {
						DownloadHandle handle = it.next();
						URL url = handle.getURL();
						String status = "Downloading: " + url.toString().substring(url.toString().lastIndexOf("/") + 1);
						Frame.setStatus(status);
						System.out.println("Downloading: " + url);
						while (!handle.isComplete()) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								Frame.error("Who interupted the thread?");
							}
							Frame.setProgress((int) handle.getProgress());
						}
					}
					ArrayList<DownloadHandle> chk = new ArrayList<>();
					for (int i = 0; i < getHandles().size(); i++)
						chk.add(getHandles().get(i));
					Frame.setStatus("Extracting..");
					frame.btnStart.setEnabled(false);
					Frame.progressBar.setIndeterminate(true);
					Frame.progressBar.setStringPainted(false);
					Iterator<DownloadHandle> itChk = chk.iterator();
					while (itChk.hasNext()) {
						DownloadHandle current = itChk.next();
						if (current.isComplete()) {
							completed.add(current.getURL().toString());
							current.runTag();
							getHandles().remove(current);
						}
					}
					frame.btnStart.setEnabled(true);
					Frame.progressBar.setString("All done!");
					Frame.progressBar.setIndeterminate(false);
					Frame.progressBar.setStringPainted(true);
					Frame.setStatus("Done!");
					Frame.setProgress(100);
					System.out.println("Done!");
				}
			}
		}
	}
}
