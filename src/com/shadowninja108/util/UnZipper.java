package com.shadowninja108.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.shadowninja108.main.Frame;

public class UnZipper {

	public static void unZip(File zipFile, File dir) {
		int BUFFER = 2048;

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
		}
		try {
			zip.close();
		} catch (IOException e) {
			Frame.error("Failed to close: " + zipFile.getName());
			e.printStackTrace();
		}
	}

	public static void un7zip(File filePath, Path path) {
		/*
		 * RandomAccessFile randomAccessFile = null; IInArchive inArchive =
		 * null; try { randomAccessFile = new RandomAccessFile(filePath, "r");
		 * inArchive = SevenZip.openInArchive(null, new
		 * RandomAccessFileInStream(randomAccessFile));
		 * 
		 * // Getting simple interface of the archive inArchive ISimpleInArchive
		 * simpleInArchive = inArchive.getSimpleInterface();
		 * 
		 * System.out.println("   Hash   |    Size    | Filename");
		 * System.out.println("----------+------------+---------");
		 * 
		 * for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
		 * final int[] hash = new int[] { 0 }; if (!item.isFolder()) {
		 * ExtractOperationResult result;
		 * 
		 * final long[] sizeArray = new long[1]; result = item.extractSlow(new
		 * ISequentialOutStream() { public int write(byte[] data) throws
		 * SevenZipException { hash[0] ^= Arrays.hashCode(data); sizeArray[0] +=
		 * data.length; return data.length; } });
		 * 
		 * if (result == ExtractOperationResult.OK) {
		 * System.out.println(String.format("%9X | %10s | %s", hash[0],
		 * sizeArray[0], item.getPath())); } else {
		 * System.err.println("Error extracting item: " + result); } } } } catch
		 * (Exception e) { System.err.println("Error occurs: " + e); } finally {
		 * if (inArchive != null) { try { inArchive.close(); } catch
		 * (SevenZipException e) { System.err.println("Error closing archive: "
		 * + e); } } if (randomAccessFile != null) { try {
		 * randomAccessFile.close(); } catch (IOException e) {
		 * System.err.println("Error closing file: " + e); } } }
		 */
	}

}
