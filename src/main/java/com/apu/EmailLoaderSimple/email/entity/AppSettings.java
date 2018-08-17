package com.apu.EmailLoaderSimple.email.entity;

public class AppSettings {
    
    private EmailBoxProperties mailboxSrc = new EmailBoxProperties();
    private EmailBoxProperties mailboxDst = new EmailBoxProperties();
    private boolean imapDebugEnable = false;
    private boolean globalDebugEnable = false;
    private boolean saveToDiskEnable = false;
    private boolean copyToEmailEnable = false;
    
    public EmailBoxProperties mailboxSrc() {
        return mailboxSrc;
    }
    public void setMailboxSrc(EmailBoxProperties mailboxSrc) {
        this.mailboxSrc = mailboxSrc;
    }
    public EmailBoxProperties mailboxDst() {
        return mailboxDst;
    }
    public void setMailboxDst(EmailBoxProperties mailboxDst) {
        this.mailboxDst = mailboxDst;
    }
    public boolean isImapDebugEnable() {
        return imapDebugEnable;
    }
    public void setImapDebugEnable(boolean imapDebugEnable) {
        this.imapDebugEnable = imapDebugEnable;
    }
    public boolean isGlobalDebugEnable() {
        return globalDebugEnable;
    }
    public void setGlobalDebugEnable(boolean globalDebugEnable) {
        this.globalDebugEnable = globalDebugEnable;
    }
    public boolean isSaveToDiskEnable() {
        return saveToDiskEnable;
    }
    public void setSaveToDiskEnable(boolean saveToDiskEnable) {
        this.saveToDiskEnable = saveToDiskEnable;
    }
    public boolean isCopyToEmailEnable() {
        return copyToEmailEnable;
    }
    public void setCopyToEmailEnable(boolean copyToEmailEnable) {
        this.copyToEmailEnable = copyToEmailEnable;
    }
    
}
