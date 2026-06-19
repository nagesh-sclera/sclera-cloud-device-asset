package io.sclera.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility for storing and removing document and media files on the local server filesystem and
 * returning their public URLs.
 */
@Component
public class FileUtils {

	// Document storage is config-driven so the served URL matches the active profile's port
	// (e.g. :8085 under the docker profile) instead of a hard-coded host. Defaults preserve the
	// previous values for profiles that do not set them.
	@Value("${sclera.server-document-absolute-path:/home/sclera/images/document/}")
	private String absoluteDocumentPath;

	@Value("${sclera.server-document-url:http://localhost:8085/images/document/}")
	private String documentUrl;

	private static final String ABSOLUTE_MEDIA_PATH =  "/home/sclera/images/media/";
	private static final String DIRECTORY_MEDIA_PATH =  "http://localhost:8888/images/media/";




	/**
	 * Writes the given document file to the document storage path and returns its public URL, or null
	 * if the inputs are missing. Creates the storage directory if absent and propagates write failures
	 * so callers can abort before persisting a dangling record.
	 */
	public String addDocumentToServer(String fileName, MultipartFile documentFile) throws IOException
	{
		if(fileName != null && documentFile != null)
		{
			Path filePath = Paths.get(absoluteDocumentPath + fileName);
			Files.createDirectories(filePath.getParent());
			Files.write(filePath, documentFile.getBytes());
            return documentUrl + fileName;

		}
		return null;
	}


	/**
	 * Deletes the named document file from the document storage path if it exists.
	 */
	public void removeDocumentFromServer(String fileName)
	{
		File file = new File(absoluteDocumentPath + fileName);
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


	/**
	 * Writes the given media file to the media storage path and returns its public URL, or null if the
	 * inputs are missing.
	 */
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


	/**
	 * Deletes the named media file from the media storage path if it exists.
	 */
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
