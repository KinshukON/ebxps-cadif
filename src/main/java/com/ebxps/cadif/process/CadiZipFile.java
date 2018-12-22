package com.ebxps.cadif.process;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.service.OperationException;

public class CadiZipFile {


	private  Category log = CadiRepository.getCategory();

	/**
	 * <b>extract</b> a named zip file into the extract folder
	 * @param zipFile - fully qualified file path of the zip file to process.
	 * @param extractFolder - target folder to receive the extracted files.
	 * @throws Exception 
	 */

	public void extract(File file,String extractFolder) throws Exception 
	{

		log.debug(String.format("UNZIPPING file [%s] to folder [%s]", file.getName(), extractFolder));

		try
		{
			FileUtils.deleteDirectory(new File(extractFolder));
		}catch (Exception e) 
		{
			//Folder does not exist, continue
		}

		try
		{
			int BUFFER = 2048;

			ZipFile zip = new ZipFile(file);
			String newPath = extractFolder;

			new File(newPath).mkdir();
			Enumeration zipFileEntries = zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements())
			{
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();

				File destFile = new File(newPath, currentEntry);
				//destFile = new File(newPath, destFile.getName());
				File destinationParent = destFile.getParentFile();

				// create the parent directory structure if needed
				destinationParent.mkdirs();

				if (!entry.isDirectory())
				{
					BufferedInputStream is = new BufferedInputStream(zip
							.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos,
							BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					// Windows locked file fix log error if fail to close stream
					try{
						dest.close();
					}catch (IOException ioe){
						OperationException oe = OperationException.createError("Cannot close destination - "+ioe.getMessage());
						log.error(oe.getMessage());
					}
					// Windows locked file fix log error if fail to close stream
					try{
						is.close();
					}catch (IOException ioe){
						OperationException oe = OperationException.createError("Cannot close source - "+ioe.getMessage());
						log.error(oe.getMessage());
					}
				}
			}
		}
		catch (Exception e) 
		{
			throw e;
		}

	}




}
