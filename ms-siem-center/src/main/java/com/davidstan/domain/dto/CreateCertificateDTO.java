package com.davidstan.domain.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class CreateCertificateDTO {
    // 64 is the industry standard for common name max length
    @Size(min=4, max=64, message="IssuerCN must be between 3 and 65 characters long")
	private String issuerCN;
    // 64 is the industry standard for common name max length
    @Size(min=4, max=64, message="IssuerCN must be between 3 and 65 characters long")
    private String subjectCN;
    @Min(1)
    @Max(25)
    private int yearsValid;

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

    public CreateCertificateDTO(String issuerCN, String subjectCN, int yearsValid) {
        this.issuerCN = issuerCN;
        this.subjectCN = subjectCN;
        this.yearsValid = yearsValid;
    }
}
