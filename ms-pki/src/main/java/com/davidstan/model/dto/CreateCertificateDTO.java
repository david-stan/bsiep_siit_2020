package com.davidstan.model.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateCertificateDTO {
    // 64 is the industry standard for common name max length
    @Size(min=3, max=64, message="IssuerCN must be between 3 and 65 characters long")
    private String issuerCN;
    @Size(min=3, max=64, message="SubjectCN must be between 3 and 65 characters long")
    private String subjectCN;
    @Min(value=1, message="Number of years valid should be larger than 0")
    @Max(value=25, message="Number of years valid should be smaller than 26")
    private int yearsValid;
    @JsonProperty
    private boolean isCA;

    public void setCA(boolean CA) {
        isCA = CA;
    }

    public boolean isCA() {
        return isCA;
    }

    public void setIssuerCN(String issuerCN) {
        this.issuerCN = issuerCN;
    }

    public void setSubjectCN(String subjectCN) {
        this.subjectCN = subjectCN;
    }

    public void setYearsValid(int yearsValid) {
        this.yearsValid = yearsValid;
    }

    public String getIssuerCN() {
        return issuerCN;
    }

    public String getSubjectCN() {
        return subjectCN;
    }

    public int getYearsValid() {
        return yearsValid;
    }

    public CreateCertificateDTO() {
    }

    public CreateCertificateDTO(String issuerCN, String subjectCN, int yearsValid, boolean isCA) {
        this.issuerCN = issuerCN;
        this.subjectCN = subjectCN;
        this.yearsValid = yearsValid;
        this.isCA = isCA;
    }
}
