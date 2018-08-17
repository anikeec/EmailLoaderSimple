package com.apu.EmailLoaderSimple;

import java.io.IOException;
import java.io.PrintStream;

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
        new EmailProcess().getMessages();
    }
    
}
