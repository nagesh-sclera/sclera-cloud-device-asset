package io.sclera.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileUtils {
	
	private static final String ABSOLUTE_DOCUMENT_PATH =  "/home/sclera/images/document/";
	private static final String DIRECTORY_DOCUMENT_PATH =  "http://localhost:8888/images/document/";
	
	private static final String ABSOLUTE_MEDIA_PATH =  "/home/sclera/images/media/";
	private static final String DIRECTORY_MEDIA_PATH =  "http://localhost:8888/images/media/";
	
	
	
	
	public String addDocumentToServer(String fileName, MultipartFile documentFile)
	{
		if(fileName != null && documentFile != null)
		{
			Path filePath = Paths.get(ABSOLUTE_DOCUMENT_PATH + fileName);
			try {
				Files.write(filePath, documentFile.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block0
				System.out.println(e);
			}
            return DIRECTORY_DOCUMENT_PATH + fileName;

		}
		return null;
	}
	
	
	public void removeDocumentFromServer(String fileName)
	{
		File file = new File(ABSOLUTE_DOCUMENT_PATH + fileName);
		if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        } else {
            System.out.println("File does not exist");
        }
	}
	
	
	public String addMediaToServer(String fileName, MultipartFile mediaFile)
	{
		if(fileName != null && mediaFile != null)
		{
			Path filePath = Paths.get(ABSOLUTE_MEDIA_PATH + fileName);
			try {
				Files.write(filePath, mediaFile.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}
            return DIRECTORY_MEDIA_PATH + fileName;

		}
		return null;
	}
	
	
	public void removeMediaFromServer(String fileName)
	{
		File file = new File(ABSOLUTE_MEDIA_PATH + fileName);
		if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        } else {
            System.out.println("File does not exist");
        }
	}


}
