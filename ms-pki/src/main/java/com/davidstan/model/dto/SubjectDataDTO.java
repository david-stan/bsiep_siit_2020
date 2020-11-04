package com.davidstan.model.dto;

import org.bouncycastle.asn1.x500.X500Name;

import javax.validation.constraints.Size;
import java.security.PublicKey;
import java.util.Date;

public class SubjectDataDTO {
    private PublicKey publicKey;
    // max size from IBM website
    @Size(min=4, max=1024)
    private X500Name x500name;
    private String serialNumber;
    private Date startDate;
    private Date endDate;

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public X500Name getX500name() {
        return x500name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setX500name(X500Name x500name) {
        this.x500name = x500name;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SubjectDataDTO() {
    }

    public SubjectDataDTO(PublicKey publicKey, X500Name x500name, String serialNumber, Date startDate, Date endDate) {
        this.publicKey = publicKey;
        this.x500name = x500name;
        this.serialNumber = serialNumber;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
