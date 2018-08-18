package com.apu.EmailLoaderSimple.email.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
//import org.springframework.integration.file.FileHeaders;
//import org.springframework.integration.support.MessageBuilder;
//import org.springframework.messaging.Message;

import com.apu.EmailLoaderSimple.email.entity.EmailEntity;
import com.apu.EmailLoaderSimple.email.entity.EmailFragment;
import com.apu.EmailLoaderSimple.email.entity.AppSettings;
import com.apu.EmailLoaderSimple.email.utils.EmailDecoding;
import com.apu.EmailLoaderSimple.email.utils.EmailUtils;
import com.apu.EmailLoaderSimple.storage.FileStorage;
import com.apu.EmailLoaderSimple.storage.FileUtils;
import com.apu.EmailLoaderSimple.storage.Storage;
import com.apu.EmailLoaderSimple.utils.string.StringUtils;
import com.sun.mail.imap.IMAPBodyPart;
import com.sun.mail.imap.IMAPFolder;

public class EmailProcess {

    private static Logger LOGGER = LogManager.getLogger(EmailProcess.class.getName());
    
    public static boolean SENDED_MESSAGES = true;
    
    private AppSettings settings;
    
    public void loadAppSettings() {
        try {
            settings = EmailUtils.loadSettings();
        } catch (IOException e) {
            LOGGER.error("Error loading settings from property file.");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }
    
    public void getFolders() throws MessagingException {
        Store store = null;
        
        if(settings == null)
            loadAppSettings();
        
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            if(settings.isImapDebugEnable())
                props.setProperty("mail.debug", "true");
            else
                props.setProperty("mail.debug", "false");

            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect(
                    settings.mailboxSrc().getServer(), 
                    settings.mailboxSrc().getLogin() + "@" + settings.mailboxSrc().getHost(), 
                    settings.mailboxSrc().getPassword());
            Folder[] folders = store.getDefaultFolder().list();
            StringBuilder sb = new StringBuilder();
            sb.append("\r\nSource email folders:\r\n");                
            for(Folder fld:folders) {
                sb.append("     " + fld.getFullName() + "\r\n");
            } 
            LOGGER.info(sb.toString());
            store.close();
            store = null;
            
            store = session.getStore("imaps");
            store.connect(
                    settings.mailboxDst().getServer(), 
                    settings.mailboxDst().getLogin() + "@" + settings.mailboxDst().getHost(), 
                    settings.mailboxDst().getPassword());
            folders = store.getDefaultFolder().list("*");
            sb = new StringBuilder();
            sb.append("\r\nDestination email folders:\r\n");                
            for(Folder fld:folders) {
                sb.append("     " + fld.getFullName() + "\r\n");
            } 
            LOGGER.info(sb.toString());
            store.close();
            store = null;
            
        } catch (NoSuchProviderException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } catch (MessagingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    public void getMessages() throws MessagingException {
        IMAPFolder folder = null, secondFolder = null;
        Store store = null, secondStore = null;              
        
        if(settings == null)
            loadAppSettings();
            
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            if(settings.isImapDebugEnable())
                props.setProperty("mail.debug", "true");
            else
                props.setProperty("mail.debug", "false");

            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect(
                    settings.mailboxSrc().getServer(), 
                    settings.mailboxSrc().getLogin() + "@" + settings.mailboxSrc().getHost(), 
                    settings.mailboxSrc().getPassword());

            folder = (IMAPFolder) store.getFolder(settings.mailboxSrc().getDirectory());

            if (!folder.isOpen())
                folder.open(Folder.READ_ONLY);
            
            if(settings.isCopyToEmailEnable()) {
                //connect to second mailbox
                Session secondSession = Session.getDefaultInstance(props, null);
                secondStore = secondSession.getStore("imaps");
                secondStore.connect(
                        settings.mailboxDst().getServer(), 
                        settings.mailboxDst().getLogin() + "@" + settings.mailboxDst().getHost(), 
                        settings.mailboxDst().getPassword());
            
                secondFolder = (IMAPFolder) secondStore.getFolder(settings.mailboxDst().getDirectory()); 
                if (!secondFolder.isOpen())
                    secondFolder.open(Folder.READ_WRITE);
            }
            
            javax.mail.Message[] emailMessages = folder.getMessages();
            LOGGER.info("No of Messages : " + folder.getMessageCount());
            LOGGER.info("No of Unread Messages : " + folder.getUnreadMessageCount());
            LOGGER.info(emailMessages.length);
            
            if((settings.isCopyToEmailEnable() == false) &&
               (settings.isSaveToDiskEnable() == false)) {
                LOGGER.info("Saving to HDD and copying to other mailbox disabled");
                return;
            }

//            List<Message<?>> messages = new ArrayList<Message<?>>();

            Storage<String> storage = new FileStorage<>();
            storage.setFileName(settings.mailboxSrc().getStorageFileName());
            
            Storage<String> storageMailbox = new FileStorage<>();
            storageMailbox.setFileName(settings.mailboxDst().getStorageFileName());
            
            int amountOfMessages = emailMessages.length;
            
            for (int i = 0; i < amountOfMessages; i++) {
                try {
                    LOGGER.info("*****************************************************************************");
                    javax.mail.Message originalMsg = emailMessages[i];
                    
                    EmailEntity msg = new EmailEntity(originalMsg);                    
                    
                    if(settings.isCopyToEmailEnable()) {
                        // check if this message has already copied to other mailbox
                        if (storageMailbox.find(msg.getMsgUniqueID())) {
                            LOGGER.info("MESSAGE with id " + msg.getMsgUniqueID() + " already copied to mailbox.");
                            continue;
                        }
                        secondFolder.addMessages(new javax.mail.Message[] {originalMsg});   //?????
                        storageMailbox.add(msg.getMsgUniqueID());
                    }
                    
                    if(settings.isSaveToDiskEnable() == false)
                        continue;

                    // check if this message has already received
                    if (storage.find(msg.getMsgUniqueID())) {
                        LOGGER.info("MESSAGE with id " + msg.getMsgUniqueID() + " exist.");
                        continue;
                    }
                    
                    LOGGER.info("MESSAGE " + (i + 1) + ":");
                    EmailUtils.logMessageInfo(msg);

                    msg.setOriginalMsgContent(originalMsg.getContent());
                    Object contentObject = msg.getOriginalMsgContent();
                    List<EmailFragment> emailFragments = new ArrayList<>();
                    emailFragments.add(fillHeaderFile(msg));
                    if (contentObject instanceof MimeMultipart) {
                        MimeMultipart mimeMessage = (MimeMultipart) contentObject;
                        try {
                            extractDetailsAndDownload(msg, mimeMessage, emailFragments);
                        } catch (MessagingException | IOException e) {
                            LOGGER.error(ExceptionUtils.getStackTrace(e));
                        }
                    } else if (contentObject instanceof String) {
                        extractDetailsAndDownload(msg, null, emailFragments);
                    } else {
                        LOGGER.info(contentObject.getClass());
                        extractDetailsAndDownload(msg, null, emailFragments);
                    }

                    for (EmailFragment emailFragment : emailFragments) {
                        FileUtils.writeToFile(emailFragment.getDirectory().getAbsolutePath(),
                                emailFragment.getFilename(), emailFragment.getData());

                        // Message<?> message = MessageBuilder.withPayload(emailFragment.getData())
                        // .setHeader(FileHeaders.FILENAME, emailFragment.getFilename())
                        // .setHeader("directory", emailFragment.getDirectory()).build();
                        // messages.add(message);
                    }

                    storage.add(msg.getMsgUniqueID());
                } catch (MessagingException e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                    // e.printStackTrace();
                } catch (IOException e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (NoSuchProviderException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } catch (MessagingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }
            if (store != null) {
                store.close();
            }
            if (secondFolder != null && secondFolder.isOpen()) {
                secondFolder.close(true);
            }
            if (secondStore != null) {
                secondStore.close();
            }
        }
    }

    private EmailFragment fillHeaderFile(EmailEntity eMailMessage)
            throws MessagingException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("Subject: ")
                .append(EmailDecoding.chechFileNameCoding(eMailMessage.getSubject()))
                .append("\n");

            sb.append("From: ")
                .append(EmailDecoding.getDecodedStr(eMailMessage.getFrom() + "\n"));

            if (eMailMessage.getSentDate() != null) {
                sb.append("Sent date: ")
                    .append(StringUtils.dateFormat(eMailMessage.getSentDate()))
                    .append("\n");
            }

            if (eMailMessage.getReceivedDate() != null) {
                sb.append("Received date: ")
                    .append(StringUtils.dateFormat(eMailMessage.getReceivedDate()))
                    .append("\n");
            }

            sb.append("To: ");
            for (String str:eMailMessage.getTo()) {
                sb.append(EmailDecoding.getDecodedStr(str + "; "));
            }
            sb.append("\n");

            sb.append("MessageID: ")
                .append(eMailMessage.getMsgID())
                .append("\n");

        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return new EmailFragment(new File(eMailMessage.getDirectoryName()), "Header.txt",
                (Object) sb.toString());
    }

    

    private String extractDetailsAndDownload(EmailEntity message, Multipart multipart,
            List<EmailFragment> fragments) throws MessagingException, IOException {

        String retValue = "";

        if (message.getSentDate() != null)
            retValue = StringUtils.dateFormat(message.getSentDate());
        else
            retValue = StringUtils.dateFormat(message.getReceivedDate());

        // create new directory
        File directory;
        if(message.getDirectory() == null) {
            directory = new File(message.getDirectoryName());
            message.setDirectory(directory);
        } else {
            directory = message.getDirectory();
        }

        if (multipart == null) {
            fragments.add(new EmailFragment(directory, "message.txt", message.getOriginalMsgContent()));
            return retValue;
        }

        LOGGER.info("Mail has " + multipart.getCount() + " elements.");

        Object content;
        String fileName;
        BodyPart bodyPart;
        String disposition;
        for (int j = 0; j < multipart.getCount(); j++) {

            bodyPart = multipart.getBodyPart(j);

            disposition = bodyPart.getDisposition();

            if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) { 
                                                                                       
                // Download mail attachments
                LOGGER.info("Mail has some attachments");

                content = EmailUtils.emailBodypartToFile(bodyPart);

                fragments.add(
                        new EmailFragment(directory, EmailDecoding.chechFileNameCoding(bodyPart.getFileName()), content));
            } else {
                // Log mail contents
                if (bodyPart instanceof IMAPBodyPart) {
                    content = ((IMAPBodyPart) bodyPart).getContent();
                } else if (bodyPart instanceof MimeBodyPart) {
                    content = ((MimeBodyPart) bodyPart).getContent();
                } else {
                    content = bodyPart.getContent();
                }
                if (content instanceof Multipart) {
                    Multipart innerMultipart = (Multipart) content;
                    extractDetailsAndDownload(message, innerMultipart, fragments);
                } else if (content instanceof InputStream) {
                    content = EmailUtils.emailBodypartToFile(bodyPart);
                    fileName = EmailDecoding.chechFileNameCoding(bodyPart.getFileName());
                    if(fileName == null)
                            fileName = "Message" + j;
                    fragments.add(new EmailFragment(directory, fileName, content));
                    // } else if (content instanceof String) {
                    // fragments.add(new EmailFragment(directory, "message.txt", content));
                } else {
                    fileName = bodyPart.getFileName();
                    fileName = EmailDecoding.chechFileNameCoding(fileName);
                    if (fileName == null) {
                        if (bodyPart instanceof MimeBodyPart) {
                            
                            String contentType;
                            try {
                                contentType = bodyPart.getContentType();
                            } catch (MessagingException e) {
                                throw new IllegalStateException("Unable to retrieve body part meta data.", e);
                            }

                            String messageType = "txt";
                            int index = contentType.indexOf(";");
                            if (index != (-1)) {
                                String messageTypeStr = contentType.substring(0, index);
                                if (messageTypeStr.equalsIgnoreCase("text/plain")) {
                                    messageType = "txt";
                                } else if (messageTypeStr.equalsIgnoreCase("text/html")) {
                                    messageType = "html";
                                }
                            }
                            
                            fileName = "Message" + j + "." + messageType;
                        }
                    }
                    fragments.add(new EmailFragment(directory, fileName, content));
                }
            }
        }
        return retValue;
    }

}
