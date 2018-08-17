package com.apu.EmailLoaderSimple.email.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.apu.EmailLoaderSimple.email.entity.EmailEntity;
import com.apu.EmailLoaderSimple.email.entity.AppSettings;
import com.apu.EmailLoaderSimple.email.process.EmailProcess;
import com.apu.EmailLoaderSimple.storage.SettingsStorage;
import com.apu.EmailLoaderSimple.utils.string.StringUtils;

public class EmailUtils {
    
    private static Logger LOGGER = LogManager.getLogger(EmailUtils.class.getName());

    private static int EMAIL_SUBJECT_MAX_LENGTH = 80;
    
    public static AppSettings loadSettings() throws IOException {
        AppSettings settings = new AppSettings();
        
        Properties emailProperties = SettingsStorage.loadPropertiesFromFile();            
        
        if(emailProperties.getProperty("imap.debug.enable").equals("true"))
            settings.setImapDebugEnable(true);
        else
            settings.setImapDebugEnable(false);
        
        if(emailProperties.getProperty("global.debug.enable").equals("true")) 
            settings.setGlobalDebugEnable(true);
        else
            settings.setGlobalDebugEnable(false);
        
        if(emailProperties.getProperty("savetodisk").equals("true"))
            settings.setSaveToDiskEnable(true);
        else
            settings.setSaveToDiskEnable(false);            
        
        if(emailProperties.getProperty("copytoemail").equals("true")) 
            settings.setCopyToEmailEnable(true);
        else 
            settings.setCopyToEmailEnable(false);

        settings.mailboxSrc().setDirectory(
                emailProperties.getProperty("email.directory"));
        settings.mailboxSrc().setHost(
                emailProperties.getProperty("email.host"));
        settings.mailboxSrc().setLogin(
                emailProperties.getProperty("email.login"));
        settings.mailboxSrc().setPassword(
                emailProperties.getProperty("email.password"));
        settings.mailboxSrc().setServer(
                emailProperties.getProperty("email.server"));
        settings.mailboxSrc().setStorageFileName(
                emailProperties.getProperty("email.storage.filename"));
        
        settings.mailboxDst().setDirectory(
                emailProperties.getProperty("sec.email.directory"));
        settings.mailboxDst().setHost(
                emailProperties.getProperty("sec.email.host"));
        settings.mailboxDst().setLogin(
                emailProperties.getProperty("sec.email.login"));
        settings.mailboxDst().setPassword(
                emailProperties.getProperty("sec.email.password"));
        settings.mailboxDst().setServer(
                emailProperties.getProperty("sec.email.server"));
        settings.mailboxDst().setStorageFileName(
                emailProperties.getProperty("sec.email.storage.filename"));
        
        return settings;
    }

    public static String checkEmailSubject(String subject) {
        if (subject == null)
                subject = "";
        subject = StringUtils.removePunct(subject);
        return subject;
    }

    public static String checkEmailDirectory(String str) {
        return StringUtils.lengthRestriction(str, EMAIL_SUBJECT_MAX_LENGTH);
    }
    
//    private static String checkForErrorDecoding(String str) throws UnsupportedEncodingException {               
//        
//        String text = MimeUtility.decodeText(str);
//        
//        byte bytes[] = str.getBytes("cp1251");
//        CharsetDetector detector = new CharsetDetector();
//        detector.setText(bytes);
//        CharsetMatch charset = detector.detect();
//        CharsetMatch charsets[] = detector.detectAll();
//        String charsetName = charset.getName();
//        if(charsetName.equalsIgnoreCase("windows-1251")) 
//            return str;
//        bytes = str.getBytes(charsetName);//"ISO-8859-1"
//        
//        detector.setText(bytes);
//        charset = detector.detect();
//        charsetName = charset.getName();        
//        String retStr = new String(bytes, charsetName);
//        
//        return retStr;         
//    }
    
    private static final String PUNCT_SUBJECT_EMAIL = "!\"#$%&'()*+,/:;?[\\]^`{|}~";
    
    public static String removePunctFromEmail(String str) {
        String result = str.replaceAll("(\\r\\n)+", "");
        return StringUtils.removePunct(result, PUNCT_SUBJECT_EMAIL);
    }    
    
//    public static String getFileExpansion(String contentType) {
//        String retValue = "";
//        int index = contentType.indexOf(";");
//        if (index != (-1)) {
//            contentType = contentType.substring(0, index);
//            if (contentType.equalsIgnoreCase("text/plain")) {
//                retValue = ".txt";
//            } else if (contentType.equalsIgnoreCase("text/html")) {
//                retValue = ".html";
//            } else if (contentType.equalsIgnoreCase("application/rar")) {
//                retValue = ".rar";
//            } else if (contentType.equalsIgnoreCase("application/x-rar-compressed")) {
//                retValue = ".rar";
//            } else if (contentType.equalsIgnoreCase("application/zip")) {
//                retValue = ".zip";
//            } else if (contentType.equalsIgnoreCase("application/x-zip-compressed")) {
//                retValue = ".zip";
//            } else if (contentType.equalsIgnoreCase("application/exe")) {
//                retValue = ".zip";
//            }
//        }
//        return retValue;
//    }
    
    public static String getDirectoryNameFromMessage(EmailEntity message) throws MessagingException {
        String emailSubject = "";
        try {
            emailSubject = EmailDecoding.chechFileNameCoding(message.getSubject());
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        String emailAddress;
        if(EmailProcess.SENDED_MESSAGES) {
            String[] recipients = message.getTo();
            if(recipients.length > 1) {
                emailAddress = recipients[0] + " c";
            } else {
                emailAddress = recipients[0];
            }            
        } else {
            emailAddress = message.getFrom();
        }
        Date emailSentDate;
        if (message.getSentDate() != null)
            emailSentDate = message.getSentDate();
        else
            emailSentDate = message.getReceivedDate();
        String directoryName = StringUtils.dateFormat(emailSentDate) + " - " + emailAddress + " - "
                + EmailUtils.checkEmailSubject(emailSubject);
        directoryName = EmailUtils.checkEmailDirectory(directoryName).trim();
        return "out\\" + directoryName;
    }
    
    public static File emailBodypartToFile(BodyPart bodyPart) throws MessagingException, IOException {
        String fileName = EmailDecoding.getDecodedStr(bodyPart.getFileName());      
        if (fileName == null) {
            fileName = "1";
        } else {
            fileName = EmailDecoding.chechFileNameCoding(fileName);
        }
//        String contentType = bodyPart.getContentType();
//        fileName = fileName + EmailUtils.getFileExpansion(contentType);
        new File("temp").mkdirs();
        File file = new File("temp\\" + fileName);
        InputStream in = ((MimeBodyPart) bodyPart).getInputStream();
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[524288];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
        } finally {
            // close streams, but don't mask original exception, if any
            try {
                if (in != null)
                    in.close();
            } catch (IOException ex) {
            }
            try {
                if (out != null)
                    out.close();
            } catch (IOException ex) {
            }
        }
        return file;
    }
    
    public static void logMessageInfo(EmailEntity entity) {
        LOGGER.info("Subject: " + entity.getSubject());
        LOGGER.info("From: " + entity.getFrom());
        StringBuilder sb = new StringBuilder();
        for(String str:entity.getTo()) {
            sb.append(str).append("; ");
        }
        LOGGER.info("To: " + sb.toString());        
        LOGGER.info("Date: " + entity.getReceivedDate());
        LOGGER.info("Size: " + entity.getSize());
        LOGGER.info("MessageID: " + entity.getMsgID());
        LOGGER.info("ContentType: " + entity.getContentType());
    }    

}
