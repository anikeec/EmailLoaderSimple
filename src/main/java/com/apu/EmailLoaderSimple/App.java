package com.apu.EmailLoaderSimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import javax.mail.MessagingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.apu.EmailLoaderSimple.email.process.EmailProcess;
import com.apu.EmailLoaderSimple.utils.log.LoggingOutputStream;

public class App 
{
    public static void main( String[] args ) throws MessagingException, IOException
    {
        System.setOut(new PrintStream(new LoggingOutputStream(Logger.getLogger("outLog"), Level.ALL), true));
        EmailProcess instance = new EmailProcess();
        boolean exitFlag = false;
        while(exitFlag == false) {
            System.out.println("Select what do you want: \r\n"
                    + "0 - exit, \r\n"
                    + "1 - get available folders from mailboxes, \r\n"
                    + "2 - get/copy messages according to file properties. \r\n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            String inputStr;
            boolean returnToMenu = false;
            while(returnToMenu == false) {
                inputStr = reader.readLine();
                int value = 0;
                try {
                    value = Integer.parseInt(inputStr);
                
                    switch(value) {
                        case 0:
                                return;
                        case 1:
                                instance.getFolders();
                                returnToMenu = true;
                                break;
                        case 2:
                                instance.getMessages();
                                exitFlag = true;
                                break;
                        default:
                                throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error value. Try again.");
                    continue;
                }
            }
        }
    }
    
}
