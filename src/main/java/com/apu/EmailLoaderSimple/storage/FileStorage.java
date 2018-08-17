package com.apu.EmailLoaderSimple.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FileStorage<T> implements Storage<T> {
    
    private static Logger LOGGER = LogManager.getLogger(FileStorage.class.getName());
    private String storageFileName = "storage.txt";
    
    @Override
    public void setFileName(String fileName) {
        this.storageFileName = fileName;
    }

    @Override
    public void add(T data) {
        if(!(data instanceof String)) return;
        
        try {
            FileWriter writer = new FileWriter(storageFileName, true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write((String)data + "\r\n");
            bufferWriter.close();
        }
        catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }               
    }

    @Override
    public boolean find(T data) {
        if(!(data instanceof String)) return false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(storageFileName))));
            String str;
            while((str = reader.readLine()) != null) {
                if(str.equals((String)data))
                        return true;
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                if(reader != null)
                    reader.close();
            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return false;
    }

}
