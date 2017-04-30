package Balloon;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class BalloonDataGatherer {
	public static String fileLocation = "<YourDesiredFileDownloadLocationHere>";

	public static void main(String[] args) {
		String server = "ftp.ncdc.noaa.gov"; // ""/pub/data/igra/data/data-y2d/";
		int port = 21;
		String user = "anonymous";
		String pass = "";
		Unzip unzip = new Unzip();
		FileWriter fw, iw;
		BufferedWriter bw, ibw;

		try {
			fw = new FileWriter(fileLocation + "balloon.d");
			bw = new BufferedWriter(fw);

			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(server, port);
			boolean connected = ftpClient.login(user, pass);
			System.out.println("Connected ?= "+connected);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.changeWorkingDirectory("/pub/data/igra/data/data-y2d/");
			FTPFile[] files = ftpClient.listFiles();
			System.out.println(files.length);
		
			FTPFile[] workingFiles = new FTPFile[96];

			// get All weather balloon file names from US weather stations
			int i = 0;
			for (FTPFile file : files) {
				String name = file.getName();
				if (name.contains("USM")) { //Chooses all US stations.  There are files.length balloon stations around the world
					workingFiles[i] = file;
					i++;
				}
			}
			System.out.println("workingfiles.length = " + i);

			for (int j = 0; j < workingFiles.length; j++) {// workingFiles.length
				OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileLocation + workingFiles[j].getName()));
				boolean success = ftpClient.retrieveFile(workingFiles[j].getName(), outputStream);
				outputStream.close();
				if (success)
					System.out.println(workingFiles[j].getName() + " Successfully Downloaded");
				System.out.println("unzipping" + fileLocation + workingFiles[j].getName());
				try {
					unzip.unZipIt(fileLocation + workingFiles[j].getName(), fileLocation);
				} catch (Exception e) {
				}

				Deleter(fileLocation + workingFiles[j].getName());

				String str = RemoveZip(fileLocation + workingFiles[j].getName());
				str = str + ".txt";
				System.out.println(str);
				File file = new File(str);
				CrunchifyReverseLineReaderCore reader = new CrunchifyReverseLineReaderCore(file, "UTF-8");//Thank you Crunchify
				
				iw = new FileWriter(str + "inter");
				ibw = new BufferedWriter(iw);
				String line;
				line = reader.readLine();

				while ((line = reader.readLine()) != null) {
					if (line.charAt(0) == '1') {
						// Significant Level
						ibw.write(line + "\n");
					} else if (line.charAt(0) == '#') {
						// Station Identifier Line
						ibw.write(line);
						break;
					}
				}

				ibw.close(); // Write Intermediate file
				Deleter(str); //Delete str.txt (big file!)

				File intermediate = new File(str + "inter");
				CrunchifyReverseLineReaderCore ireader = new CrunchifyReverseLineReaderCore(intermediate, "UTF-8");
				String iline;
				i = 0;
				iline = ireader.readLine();
				bw.write("\n" + iline + "\n");

				while ((iline = ireader.readLine()) != null && i < 500) {
					if (iline.charAt(0) == '1') {
						// Significant Level
						bw.write(iline + "\n");
					} else if (iline.charAt(0) == '#') {
						// Station Identifier Line
						bw.write(iline);
					}
					i++;
				}
				Deleter(str + "inter");//delete intermediate (backwards) file
			}
			ftpClient.logout();
			ftpClient.disconnect();
			bw.close(); // Write balloon.d file
		} catch (Exception e) {
			System.out.println(e.getMessage() + " exception occured.  Program unsuccessful");
		}

		System.out.println("program successful");

	}

	public static String RemoveZip(String str) {
		if (str != null && str.length() > 0) {
			str = str.substring(0, str.length() - 16);
		}
		return str;
	}

	public static void Deleter(String fileName) {
		Path path = Paths.get(fileName);

		try {
			Files.delete(path);
		} catch (NoSuchFileException x) {
			System.err.format("%s: no such" + " file or directory%n", path);
		} catch (DirectoryNotEmptyException x) {
			System.err.format("%s not empty%n", path);
		} catch (IOException x) {
			// File permission problems are caught here.
			System.err.println(x);
		}
	}
}
// Original Source Code From:
// http://www.codejava.net/java-se/networking/ftp/java-ftp-file-upload-tutorial-and-example
// http://stackoverflow.com/questions/22348728/list-files-to-string-array-and-list-files-from-ftp-server
// http://www.codejava.net/java-se/networking/ftp/java-ftp-example-change-working-directory
