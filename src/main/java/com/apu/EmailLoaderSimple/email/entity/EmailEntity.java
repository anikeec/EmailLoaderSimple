package com.apu.EmailLoaderSimple.email.entity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.apu.EmailLoaderSimple.email.utils.EmailDecoding;
import com.apu.EmailLoaderSimple.email.utils.EmailUtils;

public class EmailEntity {
    
    private Message originalMsg;
    private Object  originalMsgContent;
    private String  contentType;
    private String  from;
    private String  msgID;
    private String  msgUniqueID;
    private Date    receivedDate;
    private Date    sentDate;
    private int     size;
    private String  subject;    
    private String[] to;
    private String  directoryName;
    private File    directory;
    
    public Message getOriginalMsg() {
        return originalMsg;
    }
    public void setOriginalMsg(Message originalMsg) {
        this.originalMsg = originalMsg;
    }
    public Object getOriginalMsgContent() {
        return originalMsgContent;
    }
    public void setOriginalMsgContent(Object originalMsgContent) {
        this.originalMsgContent = originalMsgContent;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getMsgID() {
        return msgID;
    }
    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }
    public String getMsgUniqueID() {
        return msgUniqueID;
    }
    public void setMsgUniqueID(String msgUniqueID) {
        this.msgUniqueID = msgUniqueID;
    }
    public Date getReceivedDate() {
        return receivedDate;
    }
    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }
    public Date getSentDate() {
        return sentDate;
    }
    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String[] getTo() {
        return to;
    }
    public void setTo(String[] to) {
        this.to = to;
    }
    public String getDirectoryName() {
        return directoryName;
    }
    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }
    public File getDirectory() {
        return directory;
    }
    public void setDirectory(File directory) {
        this.directory = directory;
    } 
    
    public EmailEntity() {
        // TODO Auto-generated constructor stub
    }
    
    public EmailEntity(javax.mail.Message msg) throws MessagingException, UnsupportedEncodingException {
        EmailEntity entity = this;
        
        entity.setOriginalMsg(msg);
        
        entity.setContentType(msg.getContentType());
        entity.setFrom(EmailDecoding.getDecodedStr("" + msg.getFrom()[0]));
        entity.setMsgID(msg.getHeader("Message-ID")[0]);
        Date date = msg.getReceivedDate();
        if(date != null)
            entity.setReceivedDate(date);
        date = msg.getSentDate();
        if(date != null)
            entity.setSentDate(date);
        
        entity.setSubject(msg.getSubject());
        
        String msgUniqueID = entity.getMsgID() + "-" + 
                            entity.getReceivedDate() + "-" +
                            entity.getSubject().hashCode();
        
        entity.setMsgUniqueID(msgUniqueID);
        
        Address[] addresses = msg.getAllRecipients();
        String[] strs = new String[addresses.length];
        for(int i=0; i<addresses.length; i++) {
            strs[i] = EmailDecoding.getDecodedStr("" + addresses[i]);
        }
        entity.setTo(strs);
        
        entity.setSize(msg.getSize());
        
        entity.setDirectoryName(EmailUtils.getDirectoryNameFromMessage(entity));
    }
    
}
