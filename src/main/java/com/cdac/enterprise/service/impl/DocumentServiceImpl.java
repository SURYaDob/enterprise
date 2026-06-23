package com.cdac.enterprise.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.constant.AuditAction;
import com.cdac.enterprise.constant.DocumentType;
import com.cdac.enterprise.constant.NotificationType;
import com.cdac.enterprise.dto.UploadedDocumentResponse;
import com.cdac.enterprise.entity.Application;
import com.cdac.enterprise.entity.UploadedDocument;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.exception.ResourceNotFoundException;
import com.cdac.enterprise.repository.ApplicationRepository;
import com.cdac.enterprise.repository.UploadedDocumentRepository;
import com.cdac.enterprise.repository.UserRepository;
import com.cdac.enterprise.service.AuditLogService;
import com.cdac.enterprise.service.DocumentService;
import com.cdac.enterprise.service.NotificationService;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final String ENTITY_TYPE_DOCUMENT = "DOCUMENT";

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.max-size-bytes}")
    private long maxSizeBytes;

    public DocumentServiceImpl(
            UploadedDocumentRepository uploadedDocumentRepository,
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public UploadedDocumentResponse uploadDocument(
            String studentEmail,
            String ipAddress,
            Long applicationId,
            DocumentType documentType,
            MultipartFile file
    ) {
        User student = getStudentByEmail(studentEmail);

        Application application = applicationRepository
                .findByIdAndUserAndDeletedFalse(applicationId, student)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.APPLICATION_NOT_FOUND));

        validateFile(file);

        String originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
        String extension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetPath = uploadPath.resolve(storedFileName).normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw new IllegalStateException(AppMessages.FILE_STORAGE_FAILED);
        }

        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException ex) {
            throw new IllegalStateException(AppMessages.FILE_STORAGE_FAILED);
        }

        UploadedDocument uploadedDocument = UploadedDocument.builder()
                .application(application)
                .user(student)
                .documentType(documentType)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .filePath(uploadDir + "/" + storedFileName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        UploadedDocument savedDocument = uploadedDocumentRepository.save(uploadedDocument);

        notificationService.createNotification(
                student,
                NotificationType.DOCUMENT_UPLOADED,
                "Document uploaded",
                "Your " + documentType.name() + " document has been uploaded successfully."
        );

        auditLogService.logAction(
                student,
                studentEmail,
                AuditAction.DOCUMENT_UPLOADED,
                ENTITY_TYPE_DOCUMENT,
                savedDocument.getId(),
                "Document uploaded for application ID: " + application.getId(),
                ipAddress
        );

        return mapToUploadedDocumentResponse(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadedDocumentResponse> getMyDocuments(String studentEmail) {
        User student = getStudentByEmail(studentEmail);

        return uploadedDocumentRepository.findByUserAndDeletedFalse(student)
                .stream()
                .map(this::mapToUploadedDocumentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadedDocumentResponse> getDocumentsByApplication(Long applicationId) {
        Application application = applicationRepository.findByIdAndDeletedFalse(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.APPLICATION_NOT_FOUND));

        return uploadedDocumentRepository.findByApplicationAndDeletedFalse(application)
                .stream()
                .map(this::mapToUploadedDocumentResponse)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException(AppMessages.FILE_IS_EMPTY);
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalStateException(AppMessages.FILE_SIZE_EXCEEDED);
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalStateException(AppMessages.FILE_TYPE_NOT_ALLOWED);
        }
    }

    private String sanitizeOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "unknown-file";
        }

        return Paths.get(originalFileName)
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            return "";
        }

        return fileName.substring(dotIndex);
    }

    private User getStudentByEmail(String studentEmail) {
        return userRepository.findByEmailAndDeletedFalse(studentEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND));
    }

    private UploadedDocumentResponse mapToUploadedDocumentResponse(UploadedDocument uploadedDocument) {
        return UploadedDocumentResponse.builder()
                .id(uploadedDocument.getId())
                .applicationId(uploadedDocument.getApplication().getId())
                .userId(uploadedDocument.getUser().getId())
                .documentType(uploadedDocument.getDocumentType())
                .originalFileName(uploadedDocument.getOriginalFileName())
                .storedFileName(uploadedDocument.getStoredFileName())
                .contentType(uploadedDocument.getContentType())
                .fileSize(uploadedDocument.getFileSize())
                .uploadedAt(uploadedDocument.getUploadedAt())
                .build();
    }
}