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
import com.shadowninja108.util.ExtractionTag;
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

	private ConsoleInfo info;

	public DownloadManager downManager;

	public Interpreter(File xml, ConsoleInfo info, Frame frame) {
		setup();
		this.info = info;

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

	public void interpret() {
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
				if (current.getChild("version") != null && !checkVersion(current.getChild("version")))
					verified = false;
				if (verified)
					interpretCommands(current.getChild("command"));
				else
					System.out.println("Console check failed.");
				break;
			}
		}
	}

	public boolean checkVersion(Element node) {
		if (node != null) {
			String from = node.getChildText("from");
			String to = node.getChildText("to");
			// version parsing ignores everything but the last number. yea i
			// know its bad

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
		Iterator<Element> it = node.getChildren().iterator();
		while (it.hasNext()) {
			Element current = it.next();
			if (current.hasAttributes()) {
				Iterator<Attribute> at = current.getAttributes().iterator();
				System.out.println("Executing command: " + current.getName());
				while (at.hasNext()) {
					Attribute currentAt = at.next();
					switch (currentAt.getValue()) {
					case "zip":
						decompressZip(current, null);
						break;
					case "7zip":
						// decompress7z(current, null);
						break;
					case "direct":
						directDownload(current);
						break;
					case "web":
						webLink(current);
						break;
					}
				}
			}
		}
	}

	public void decompressZip(Element node, ExtractionTag tag) {
		String webSite = node.getChildText("value");
		String path = node.getChildText("path");
		String extract = node.getChildText("extract");
		String srcName = webSite.substring(webSite.lastIndexOf("/") + 1, webSite.lastIndexOf("."));
		File filePath = new File(download, srcName + ".zip");
		File rootFolderPath = new File(download, srcName);
		File extractPath = (!extract.equals("/")) ? sd : new File(sd, extract);
		if ((tag != null && !tag.completed) || (rootFolderPath.exists())) {
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
							Files.move(fileOrDir, fDest.toPath().resolve(fSrc.toPath().relativize(fileOrDir)),
									StandardCopyOption.REPLACE_EXISTING);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					Frame.error("Failed to recursively move: " + extractPath.getName());
				}
			}
		} else {
			try {
				filePath.createNewFile();
			} catch (IOException e) {
				System.out.println("Error!");
			}
			ExtractionTag ntag = new ExtractionTag();
			try {
				ntag.m = this.getClass().getMethod("decompressZip",
						new Class<?>[] { Element.class, ExtractionTag.class });
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			ntag.completed = false;
			ntag.node = node;
			ntag.interpreter = this;
			downManager.download(webSite, filePath, ntag);
		}
	}

	public void decompress7z(Element node, ExtractionTag tag) {
		System.out.println("7zip support unimplimented! Skipping...");
		/*
		 * String web = node.getChildText("value"); String path =
		 * web.substring(web.lastIndexOf("/") + 1, web.lastIndexOf(".")); File
		 * filePath = new File(download, path + ".7z"); File folderPath =
		 * Paths.get(download.toString(), path).toFile(); if ((tag != null &&
		 * !tag.completed) || (folderPath.exists()) && Frame.sevenZipEnabled) {
		 * folderPath.mkdir(); File source = new File(download, path +
		 * "\\" + node.getChildText("extract")); if (!source.exists())
		 * UnZipper.un7zip(filePath, new File(download, path).toPath()); File
		 * destination = null; if (source.isFile()) { destination = new File(sd,
		 * node.getChildText("path")); try { destination.mkdirs();
		 * destination.createNewFile(); Files.copy(source.toPath(),
		 * destination.toPath(), StandardCopyOption.REPLACE_EXISTING); } catch
		 * (IOException e) { e.printStackTrace(); } } else { File dest; if
		 * (!node.getChildText("path").equals("/")) dest = new File(sd,
		 * node.getChildText("path")); else dest = sd; try {
		 * Files.walkFileTree(source.toPath(), new SimpleFileVisitor<Path>() {
		 * public FileVisitResult visitFile(Path file, BasicFileAttributes
		 * attrs) throws IOException { return copy(file); }
		 * 
		 * private FileVisitResult copy(Path fileOrDir) throws IOException {
		 * Path finl =
		 * dest.toPath().resolve(source.toPath().relativize(fileOrDir)); // get
		 * around exceptions finl.toFile().mkdirs(); Files.move(fileOrDir,
		 * dest.toPath().resolve(source.toPath().relativize(fileOrDir)),
		 * StandardCopyOption.REPLACE_EXISTING); return
		 * FileVisitResult.CONTINUE; } }); } catch (IOException e) {
		 * e.printStackTrace(); } } } if (!Frame.sevenZipEnabled) { System.out.
		 * println("Skipped 7z because of the inability to extract it"); } else
		 * { try { filePath.createNewFile(); } catch (IOException e) {
		 * System.out.println("Error!"); } ExtractionTag ntag = new
		 * ExtractionTag(); try { ntag.m =
		 * this.getClass().getMethod("decompress7z", new Class<?>[] {
		 * Element.class, ExtractionTag.class }); } catch (NoSuchMethodException
		 * | SecurityException e) { e.printStackTrace(); } ntag.completed =
		 * false; ntag.node = node; ntag.interpreter = this;
		 * downManager.download(web, "/download/" + path + ".zip", ntag); }
		 */
	}

	public void directDownload(Element node) {
		String webSite = node.getChildText("value");
		String path = node.getChildText("path");
		downManager.download(webSite, new File(sd, path), null);
	}

	public void webLink(Element node) {
		try {
			Desktop.getDesktop().browse(new URI(node.getChildText("value")));
		} catch (IOException | URISyntaxException e) {
			Frame.error("Failed to open " + node.getChildText("value"));
		}
	}

	public Interpreter getInstance() {
		return this;
	}

	public void dispose() {
		downManager.dispose();
	}
}
