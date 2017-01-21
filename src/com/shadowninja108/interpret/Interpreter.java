package com.shadowninja108.interpret;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.shadowninja108.info.ConsoleInfo;
import com.shadowninja108.main.Frame;
import com.shadowninja108.util.DownloadManager;
import com.shadowninja108.util.ActionTag;
import com.shadowninja108.util.UnZipper;

public class Interpreter {

	public static File root, download, sd;

	public static void setup() {
		root = new File(Frame.working_directory, "SDHelper");
		if (!root.exists())
			root.mkdir();
		download = new File(root, "download");
		sd = new File(root, "sd");
		if (!download.exists())
			download.mkdir();
		else
			cleanDirectory(download);
		if (!sd.exists())
			sd.mkdir();
		else
			cleanDirectory(sd); // should i do this?
	}

	public Document doc;

	public Element rootNode, general, downloads;

	public String status, title, author;

	public int version;

	public DownloadManager downManager;

	public Interpreter(File xml, Frame frame) {
		setup();

		downManager = new DownloadManager(frame);

		SAXBuilder builder = new SAXBuilder();

		try {
			doc = builder.build(xml);
		} catch (JDOMException | IOException e) {
			System.out
					.println("Error reading" + xml.getPath().substring(xml.getPath().lastIndexOf(File.separator) + 1));
			e.printStackTrace();
		}

		rootNode = doc.getRootElement();
		general = rootNode.getChild("general");
		downloads = rootNode.getChild("downloads");
		version = Integer.valueOf(general.getChildText("version"));
	}

	private static void cleanDirectory(File dir) {
		File[] files = scanDir(dir);
		for (int i = 0; i < files.length; i++)
			files[i].delete();
	}

	private static File[] scanDir(File dir) {
		ArrayList<File> bin = new ArrayList<>();
		File[] root = dir.listFiles();
		System.out.println("Scanning dir: " + dir.getName());
		for (int i = 0; i < root.length; i++) {
			File c = root[i];
			if (c.isFile())
				bin.add(c);
			else if (c.isDirectory()) {
				Collections.addAll(bin, scanDir(c));
				bin.add(c);
			}
		}
		File[] buffer = new File[bin.size()];
		bin.toArray(buffer);
		return buffer;
	}

	public void interpret(ConsoleInfo info) {
		Iterator<Element> it = downloads.getChildren().iterator();
		while (it.hasNext()) {
			Element current = it.next();
			System.out.println("Reading node: " + current.getName());
			switch (current.getName()) {
			case "global":
				interpretCommands(current);
				break;
			case "if":
				boolean verified = true;
				if (current.getAttribute("type") != null
						&& !current.getAttributeValue("type").equals(info.type.toString()))
					verified = false;
				if (current.getAttribute("region") != null
						&& !current.getAttributeValue("region").equals(info.region.toString()))
					verified = false;
				if (current.getChild("version") != null && !checkVersion(current.getChild("version"), info))
					verified = false;
				if (verified)
					interpretCommands(current.getChild("command"));
				else
					System.out.println("Console check failed.");
				break;
			}
		}
	}

	public boolean checkVersion(Element node, ConsoleInfo info) {
		if (node != null) {
			String from = node.getChildText("from");
			String to = node.getChildText("to");
			// version parsing ignores everything but the last number. yea i
			// know its bad. pls fix

			if ((to == null || to.isEmpty()) && (from != null && !from.isEmpty())) {
				return Integer.parseInt(node.getChild("from").getChildText("idk")) == info.ver.idk;
			} else if ((from == null || from.isEmpty()) && (to != null && !to.isEmpty())) {
				return info.ver.idk == Integer.parseInt(node.getChild("to").getChildText("idk"));
			} else {
				// version is a range
				return Integer.parseInt(node.getChild("from").getChildText("idk")) <= info.ver.idk
						&& info.ver.idk <= Integer.parseInt(node.getChild("to").getChildText("idk"));
			}
		} else
			return false;
	}

	public void interpretCommands(Element node) {
		downManager.stopQue = true;
		Iterator<Element> it = node.getChildren().iterator();
		while (it.hasNext()) {
			Element current = it.next();
			if (current.hasAttributes()) {
				Iterator<Attribute> at = current.getAttributes().iterator();
				System.out.println("Executing command: " + current.getName());
				String webSite;
				String srcName;
				File filePath;
				while (at.hasNext()) {
					Attribute currentAt = at.next();
					switch (currentAt.getValue()) {
					case "zip":
						webSite = current.getChildText("value");
						srcName = webSite.substring(webSite.lastIndexOf("/") + 1, webSite.lastIndexOf("."));
						filePath = new File(download, srcName + ".zip");
						downManager.tags.add(ActionTag.makeTag("decompressZip", current, this));
						directDownload(current, filePath);
						break;
					case "7zip":
						webSite = current.getChildText("value");
						srcName = webSite.substring(webSite.lastIndexOf("/") + 1, webSite.lastIndexOf("."));
						filePath = new File(download, srcName + ".7z");
						downManager.tags.add(ActionTag.makeTag("decompress7z", current, this));
						directDownload(current, filePath);
						break;
					case "direct":
						directDownload(current, new File(sd, current.getChildText("path")));
						break;
					case "web":
						webLink(current);
						break;
					}
				}
			}
		}
		downManager.stopQue = false;
	}

	public void decompressZip(Element node) {
		String webSite = node.getChildText("value");
		String path = node.getChildText("path");
		String extract = node.getChildText("extract");
		String srcName = webSite.substring(webSite.lastIndexOf("/") + 1, webSite.lastIndexOf("."));
		File filePath = new File(download, srcName + ".zip");
		File rootFolderPath = new File(download, srcName);
		File extractPath = (!extract.equals("/")) ? sd : new File(sd, extract);
		rootFolderPath.mkdir();
		File source = new File(rootFolderPath, extract);
		if (!source.exists())
			UnZipper.unZip(filePath, rootFolderPath);
		File destination = null;
		if (source.isFile()) {
			destination = new File(sd, path);
			try {
				destination.mkdirs();
				destination.createNewFile();
				System.out.println("Copying: " + source.getName());
				Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Frame.error("Failed to prepare/move " + source.getName());
			}
		} else {
			if (!path.equals("/"))
				destination = new File(sd, path);
			else
				destination = sd;
			try {
				final File fSrc = source;
				final File fDest = extractPath;
				// java 7 compatibility requires these to be final
				Files.walkFileTree(fSrc.toPath(), new SimpleFileVisitor<Path>() {
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						return copy(file);
					}

					private FileVisitResult copy(Path fileOrDir) throws IOException {
						Path finl = fDest.toPath().resolve(fSrc.toPath().relativize(fileOrDir));
						// get around exceptions
						finl.toFile().mkdirs();
						System.out.println("Moving: " + fileOrDir.getFileName());
						Files.copy(fileOrDir, fDest.toPath().resolve(fSrc.toPath().relativize(fileOrDir)),
								StandardCopyOption.REPLACE_EXISTING);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				Frame.error("Failed to recursively move: " + extractPath.getName());
			}
		}
	}

	public void decompress7z(Element node) {
		if (Frame.sevenZipEnabled) {
			String webSite = node.getChildText("value");
			String path = node.getChildText("path");
			String extract = node.getChildText("extract");
			String srcName = webSite.substring(webSite.lastIndexOf("/") + 1, webSite.lastIndexOf("."));
			File filePath = new File(download, srcName + ".7z");
			File rootFolderPath = new File(download, srcName);
			File extractPath = (!extract.equals("/")) ? sd : new File(sd, path);
			File source = new File(rootFolderPath, extract);
			if (!rootFolderPath.exists()) {
				rootFolderPath.mkdirs();
				UnZipper.un7zip(filePath, rootFolderPath);
			}
			File destination = null;
			if (source.isFile()) {
				destination = new File(sd, path);
				try {
					destination.mkdirs();
					destination.createNewFile();
					System.out.println("Copying: " + source.getName());
					Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					Frame.error("Failed to prepare/move " + source.getName());
				}
			} else {
				destination = (!path.equals("/")) ? sd : new File(sd, path);
				try {
					final File fSrc = source;
					final File fDest = extractPath;
					// java 7 compatibility requires these to be final
					Files.walkFileTree(fSrc.toPath(), new SimpleFileVisitor<Path>() {
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							return copy(file);
						}

						private FileVisitResult copy(Path fileOrDir) throws IOException {
							Path finl = fDest.toPath().resolve(fSrc.toPath().relativize(fileOrDir));
							// get around exceptions
							finl.toFile().mkdirs();
							System.out.println("Copying: " + fileOrDir.getFileName());
							Files.copy(fileOrDir, fDest.toPath().resolve(fSrc.toPath().relativize(fileOrDir)),
									StandardCopyOption.REPLACE_EXISTING);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					Frame.error("Failed to recursively move: " + extractPath.getName());
				}
			}

		} else
			System.out.println("7zip disabled. Skipping...");
	}

	public void directDownload(Element node, File file) {
		String webSite = node.getChildText("value");
		try {
			if (file.isFile())
				file.createNewFile();
		} catch (IOException e) {
			Frame.error("Failed to create file: " + file.getName());
			e.printStackTrace();
		}
		downManager.download(webSite, file);
	}

	public void webLink(Element node) {
		try {
			Desktop.getDesktop().browse(new URI(node.getChildText("value")));
		} catch (IOException | URISyntaxException e) {
			Frame.error("Failed to open " + node.getChildText("value"));
		}
	}

	public void dispose() {
		downManager.dispose();
	}
}
