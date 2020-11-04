package com.davidstan.converter;

import java.awt.print.Pageable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import com.davidstan.model.SubjectData;
import com.davidstan.model.dto.SubjectDataDTO;

@Component
public class SubjectDataConverter {
    ModelMapper modelMapper = new ModelMapper();

    public SubjectDataConverter() {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
    }

    public SubjectDataDTO convertToDTO(SubjectData sd) {
        SubjectDataDTO sddto = new SubjectDataDTO();
        sddto.setEndDate(sd.getEndDate());
        sddto.setPublicKey(sd.getPublicKey());
        sddto.setSerialNumber(sd.getSerialNumber());
        sddto.setStartDate(sd.getStartDate());
        sddto.setX500name(sd.getX500name());

        return sddto;
    }

    public SubjectData convertToEntity(SubjectDataDTO sddto) {
        return modelMapper.map(sddto, SubjectData.class);
    }

    public List<SubjectDataDTO> entityListToDTOList(List<SubjectData> certList) {
        return certList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public SubjectData convertX509CertificateToSubjectData(X509Certificate cert) throws CertificateEncodingException {
        X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();

        return new SubjectData(cert.getPublicKey(), x500name,
                String.valueOf(cert.getSerialNumber()), cert.getNotBefore(), cert.getNotAfter());
    }
}
