package Balloon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {

	
	
	public void unZipIt(String zipFile, String outputFolder){

	     byte[] buffer = new byte[1024];

	     try{

	    	//create output directory is not exists
	    	File folder = new File(outputFolder);
	    	if(!folder.exists()){
	    		folder.mkdir();
	    	}

	    	//get the zip file content
	    	ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
	    	//get the zipped file list entry
	    	ZipEntry ze = zis.getNextEntry();

	    	while(ze!=null){

	    	   String fileName = ze.getName();
	           File newFile = new File(outputFolder + File.separator + fileName);

	           System.out.println("file unzip : "+ newFile.getAbsoluteFile());

	            //create all non exists folders
	            //else you will hit FileNotFoundException for compressed folder
	            new File(newFile.getParent()).mkdirs();

	            FileOutputStream fos = new FileOutputStream(newFile);

	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	       		fos.write(buffer, 0, len);
	            }

	            fos.close();
	            ze = zis.getNextEntry();
	    	}

	        zis.closeEntry();
	    	zis.close();

	    	System.out.println("Done");

	    }catch(IOException ex){ex.printStackTrace();}
	}
}
//Sourced from: https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
//http://stackoverflow.com/questions/13122212/unzipping-file-zip-exception-invalid-entry-size-expected-193144-but-got-193138
