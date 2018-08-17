package com.apu.EmailLoaderSimple.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FileUtils {
    
    private static Logger LOGGER = LogManager.getLogger(FileUtils.class.getName());

    public static void writeToFile(String folderName, String fileName, Object data) {
        try {
            new File(folderName).mkdirs();            
            OutputStream fos = new FileOutputStream(folderName + "\\" + fileName);
            InputStream is = null;
            if(data instanceof String) {
                String dataString =(String)data;
                is = new ByteArrayInputStream(dataString.getBytes());
            } else if(data instanceof File) {
                File dataFile = (File)data;
                is = new FileInputStream(dataFile);                
            }
            
            if(is != null) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                is.close();
            }
            
            fos.close();
        } catch (FileNotFoundException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }
    
}
