package com.shadowninja108.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

import com.shadowninja108.main.Frame;

public class DownloadManager {
	public FileDownloaderThread thread;

	public ArrayList<DownloadHandle> toBeAdded;
	public ArrayList<DownloadHandle> handles;

	public ArrayList<ActionTag> tags;

	public int status = 0;

	public boolean running = false;

	public boolean stopQue = false;

	public DownloadManager(Frame frame) {
		running = true;
		tags = new ArrayList<>();
		handles = new ArrayList<>();
		toBeAdded = new ArrayList<>();
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
		return true;
	}

	public void download(String web, File path) {
		try {
			toBeAdded.add(new DownloadHandle(new URL(web), path));
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
				while ((getHandles().size() > 0 || getTBHandles().size() > 0) && !stopQue) {
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

					if (!tags.isEmpty()) {
						Frame.setStatus("Extracting..");
						Frame.btnStart.setEnabled(false);
						Frame.progressBar.setIndeterminate(true);
						Frame.progressBar.setStringPainted(false);

						Iterator<ActionTag> itTags = tags.iterator();
						itTags.forEachRemaining(new Consumer<ActionTag>() {
							@Override
							public void accept(ActionTag tag) {
								try {
									tag.m.invoke(tag.interpreter, new Object[] { tag.node });
								} catch (IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e) {
									Frame.error("Failed to invoke method: " + tag.m.getName() + "! PANIC PANIC");
									e.printStackTrace();
								}
							}
						});
					}
					tags.clear();
					handles.clear();

					Frame.btnStart.setEnabled(true);
					Frame.progressBar.setIndeterminate(false);
					Frame.progressBar.setStringPainted(true);
					Frame.setStatus("Done!");
					Frame.setProgress(100);
					Frame.progressBar.setString("All done!");
					System.out.println("Done!");
				}
			}
		}
	}
}
