package com.cdac.enterprise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdac.enterprise.entity.Application;
import com.cdac.enterprise.entity.UploadedDocument;
import com.cdac.enterprise.entity.User;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {

    List<UploadedDocument> findByApplicationAndDeletedFalse(Application application);

    List<UploadedDocument> findByUserAndDeletedFalse(User user);

    long countByDeletedFalse();
}