package com.ebxps.cadif.process;

import java.io.*;
import java.security.MessageDigest;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.org.apache.log4j.Category;

public class FileCheckSum {
	
	   public static byte[] getMD5Checksum(String filename) throws Exception {

		   InputStream fis =  new FileInputStream(filename);

	       byte[] buffer = new byte[1024];
	       MessageDigest complete = MessageDigest.getInstance("MD5");
	       int numRead;

	       do {
	           numRead = fis.read(buffer);
	           if (numRead > 0) {
	               complete.update(buffer, 0, numRead);
	           }
	       } while (numRead != -1);

	       fis.close();
	       return complete.digest();
	   }
	
	   public static byte[] getFileChecksum(String checksumFilename){
		   
		   //TODO add code to read checksum value from file
		   return null;
	   }
	   
	
}
