package com.shadowninja108.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.shadowninja108.main.Frame;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class UnZipper {

	public static void unZip(File zipFile, File dir) {
		int BUFFER = 2048;

		System.out.println("Extracting: " + zipFile.getName());
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
		} catch (IOException e1) {
			Frame.error("Failed to open: " + zipFile.getName());
		}
		try {
			dir.getParentFile().mkdirs();
			Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				File destFile = new File(dir, currentEntry);
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					BufferedInputStream is = null;
					FileOutputStream fos = null;
					BufferedOutputStream dest = null;
					try {
						is = new BufferedInputStream(zip.getInputStream(entry));
						int currentByte;
						byte data[] = new byte[BUFFER];
						fos = new FileOutputStream(destFile);
						dest = new BufferedOutputStream(fos, BUFFER);
						while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, currentByte);
						}
					} catch (Exception e) {
						Frame.error("Failed to extract: " + entry.getName());
						e.printStackTrace();
					} finally {
						if (dest != null)
							dest.close();
						if (fos != null)
							fos.close();
						if (is != null)
							is.close();
					}
				} else
					destFile.mkdirs();
			}
		} catch (Exception e) {
			Frame.error("Failed to extract zip: " + zipFile.getName());
			e.printStackTrace();
		} finally {
			System.out.println("Extraction complete!");
		}
		try {
			zip.close();
		} catch (IOException e) {
			Frame.error("Failed to close: " + zipFile.getName());
			e.printStackTrace();
		}
	}

	public static void un7zip(File filePath, File path) {
		System.out.println("Extracting 7z: " + filePath.getName());
		RandomAccessFile file = null;
		RandomAccessFileInStream in = null;
		ISimpleInArchive archive = null;
		try {
			file = new RandomAccessFile(filePath, "r");
			in = new RandomAccessFileInStream(file);
			archive = SevenZip.openInArchive(null, in).getSimpleInterface();
			Iterator<ISimpleInArchiveItem> it = Arrays.asList(archive.getArchiveItems()).iterator();
			while (it.hasNext()) {
				ISimpleInArchiveItem item = it.next();
				File dest = path.toPath().resolve(item.getPath()).toFile();
				if (!item.isFolder()) {
					if (!dest.exists())
						dest.createNewFile();
					final FileOutputStream fout = new FileOutputStream(dest);

					ExtractOperationResult result = item.extractSlow(new ISequentialOutStream() {

						@Override
						public int write(byte[] data) throws SevenZipException {
							try {
								fout.write(data);
							} catch (IOException e) {
								Frame.error("Failed to write data!");
							}
							return data.length;
						}
					});
					if (result != ExtractOperationResult.OK) {
						Frame.error("Failed to extract: " + item.getPath());
					}
					fout.close();
				} else
					dest.mkdirs();
			}

		} catch (

		IOException e) {
			Frame.error("Error occured extracting 7zip file: " + filePath.getName());
			e.printStackTrace();
		} finally {
			try {
				archive.close();
				in.close();
				file.close();
			} catch (IOException e) {
				Frame.error("Failed to close: " + filePath.getName() + "!\nPANIC PANIC");
				e.printStackTrace();
			} finally {
				System.out.println("Extraction complete!");
			}
		}
	}
}
