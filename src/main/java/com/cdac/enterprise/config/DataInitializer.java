package com.cdac.enterprise.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.constant.AuditAction;
import com.cdac.enterprise.constant.DocumentType;
import com.cdac.enterprise.constant.NotificationType;
import com.cdac.enterprise.constant.RoleName;
import com.cdac.enterprise.entity.Application;
import com.cdac.enterprise.entity.AuditLog;
import com.cdac.enterprise.entity.Course;
import com.cdac.enterprise.entity.Notification;
import com.cdac.enterprise.entity.Role;
import com.cdac.enterprise.entity.UploadedDocument;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.repository.ApplicationRepository;
import com.cdac.enterprise.repository.AuditLogRepository;
import com.cdac.enterprise.repository.CourseRepository;
import com.cdac.enterprise.repository.NotificationRepository;
import com.cdac.enterprise.repository.RoleRepository;
import com.cdac.enterprise.repository.UploadedDocumentRepository;
import com.cdac.enterprise.repository.UserRepository;

@Component
@Profile("dev")
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String DEMO_STUDENT_EMAIL = "demo.student@cdac.local";
    private static final String DEMO_STUDENT_PASSWORD = "Demo@123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            ApplicationRepository applicationRepository,
            NotificationRepository notificationRepository,
            AuditLogRepository auditLogRepository,
            UploadedDocumentRepository uploadedDocumentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting demo data initialization...");

        // Essential roles and admin user are seeded by EssentialDataInitializer (runs in all profiles).

        // 1. Create demo courses
        createDemoCourses();

        // 2. Create demo student
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new IllegalStateException("ROLE_STUDENT not found — EssentialDataInitializer may not have run yet"));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found — EssentialDataInitializer may not have run yet"));

        User student = createDemoStudentIfNotExists(studentRole);

        User admin = userRepository.findByEmailAndDeletedFalse("admin@cdac.local")
                .orElse(null);

        // 3. Create demo applications, notifications, documents & audit logs
        if (admin != null && applicationRepository.countByDeletedFalse() == 0) {
            createDemoApplications(student, admin);
        }

        log.info("Demo data initialization completed successfully.");
    }

    // ========================
    // Demo Courses
    // ========================

    private void createDemoCourses() {
        if (courseRepository.countByDeletedFalse() > 0) {
            log.info("Courses already exist, skipping demo course creation.");
            return;
        }

        List<Course> courses = List.of(
                Course.builder()
                        .code("CDAC-PGDAC")
                        .name("PG Diploma in Advanced Computing")
                        .description("Flagship 9-month postgraduate diploma covering OOP, DBMS, Web Technologies, Cloud Computing, and DevOps. Industry-oriented curriculum with intensive lab sessions and a major project.")
                        .durationInMonths(9)
                        .fee(new BigDecimal("150000.00"))
                        .capacity(240)
                        .active(true)
                        .build(),
                Course.builder()
                        .code("CDAC-DESD")
                        .name("Diploma in Embedded Systems Design")
                        .description("6-month course covering embedded C, microcontrollers, RTOS, IoT protocols, and FPGA design. Hands-on training with ARM Cortex-M development boards.")
                        .durationInMonths(6)
                        .fee(new BigDecimal("95000.00"))
                        .capacity(60)
                        .active(true)
                        .build(),
                Course.builder()
                        .code("CDAC-DBDA")
                        .name("Diploma in Big Data Analytics")
                        .description("6-month course covering Hadoop, Spark, Python for data science, machine learning, and data visualization. Practical exposure to real-world datasets.")
                        .durationInMonths(6)
                        .fee(new BigDecimal("105000.00"))
                        .capacity(80)
                        .active(true)
                        .build(),
                Course.builder()
                        .code("CDAC-DAC")
                        .name("Diploma in Advanced Computing")
                        .description("Comprehensive 6-month program focusing on Java, .NET, Python, web development frameworks, and software engineering practices. Includes capstone project.")
                        .durationInMonths(6)
                        .fee(new BigDecimal("110000.00"))
                        .capacity(120)
                        .active(true)
                        .build(),
                Course.builder()
                        .code("CDAC-DITIS")
                        .name("Diploma in IT Infrastructure & System Security")
                        .description("6-month course on networking, Linux administration, cloud infrastructure (AWS/Azure), cybersecurity fundamentals, and ethical hacking.")
                        .durationInMonths(6)
                        .fee(new BigDecimal("100000.00"))
                        .capacity(60)
                        .active(true)
                        .build(),
                Course.builder()
                        .code("CDAC-ML")
                        .name("Certificate in Machine Learning & AI")
                        .description("3-month intensive course on supervised/unsupervised learning, neural networks, NLP, and computer vision. Uses TensorFlow, PyTorch, and scikit-learn.")
                        .durationInMonths(3)
                        .fee(new BigDecimal("60000.00"))
                        .capacity(40)
                        .active(true)
                        .build(),
                Course.builder()
                        .code("CDAC-OLD")
                        .name("Legacy VB.NET Course (Not Active)")
                        .description("Older course no longer offered. Kept for reference.")
                        .durationInMonths(6)
                        .fee(new BigDecimal("50000.00"))
                        .capacity(0)
                        .active(false)
                        .build()
        );

        courseRepository.saveAll(courses);
        log.info("Seeded {} demo courses", courses.size());
    }

    // ========================
    // Demo Student
    // ========================

    private User createDemoStudentIfNotExists(Role studentRole) {
        return userRepository.findByEmailAndDeletedFalse(DEMO_STUDENT_EMAIL)
                .orElseGet(() -> {
                    User student = User.builder()
                            .firstName("Demo")
                            .lastName("Student")
                            .email(DEMO_STUDENT_EMAIL)
                            .password(passwordEncoder.encode(DEMO_STUDENT_PASSWORD))
                            .phoneNumber("9876543210")
                            .enabled(true)
                            .emailVerified(true)
                            .accountNonLocked(true)
                            .deleted(false)
                            .roles(Set.of(studentRole))
                            .build();
                    User saved = userRepository.save(student);
                    log.info("Seeded demo student: {}", DEMO_STUDENT_EMAIL);
                    return saved;
                });
    }

    // ========================
    // Demo Applications, Notifications, Documents, Audit Logs
    // ========================

    private void createDemoApplications(User student, User admin) {
        List<Course> activeCourses = courseRepository.findByActiveTrueAndDeletedFalse(
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        if (activeCourses.size() < 3) {
            log.warn("Not enough active courses to create demo applications");
            return;
        }

        Course pgdac = activeCourses.stream().filter(c -> "CDAC-PGDAC".equals(c.getCode())).findFirst().orElse(activeCourses.get(0));
        Course desd = activeCourses.stream().filter(c -> "CDAC-DESD".equals(c.getCode())).findFirst().orElse(activeCourses.get(1));
        Course dbda = activeCourses.stream().filter(c -> "CDAC-DBDA".equals(c.getCode())).findFirst().orElse(activeCourses.get(2));

        // --- Application 1: APPROVED ---
        Application app1 = Application.builder()
                .user(student)
                .course(pgdac)
                .status(ApplicationStatus.APPROVED)
                .statementOfPurpose("I have a B.Tech in Computer Science with 2 years of industry experience. I am passionate about advanced computing and want to deepen my knowledge in distributed systems, cloud computing, and DevOps practices. CDAC's PG-DAC program is the ideal platform to upskill and transition into a senior engineering role.")
                .adminRemarks("Strong academic background with relevant work experience. Statement of purpose is well-articulated. Approved for enrollment.")
                .submittedAt(LocalDateTime.now().minusDays(15))
                .reviewedAt(LocalDateTime.now().minusDays(10))
                .build();
        app1 = applicationRepository.save(app1);

        createNotification(student, NotificationType.APPLICATION_APPROVED,
                "Application Approved - PG-DAC",
                "Congratulations! Your application for PG-DAC (PG Diploma in Advanced Computing) has been approved. Please check your email for enrollment instructions.");
        createAuditLog(admin, AuditAction.APPLICATION_REVIEWED, app1.getId(), "Application approved for course: PG-DAC");

        // --- Application 2: UNDER_REVIEW ---
        Application app2 = Application.builder()
                .user(student)
                .course(desd)
                .status(ApplicationStatus.UNDER_REVIEW)
                .statementOfPurpose("I have been working as a firmware engineer for 3 years and want to specialize in embedded systems. The DESD course at CDAC offers the perfect curriculum to bridge my knowledge gaps in RTOS and FPGA design.")
                .adminRemarks("Application is under review. Documents are being verified.")
                .submittedAt(LocalDateTime.now().minusDays(5))
                .reviewedAt(LocalDateTime.now().minusDays(2))
                .build();
        app2 = applicationRepository.save(app2);

        createNotification(student, NotificationType.APPLICATION_UNDER_REVIEW,
                "Application Under Review - DESD",
                "Your application for DESD (Diploma in Embedded Systems Design) is currently under review. We will notify you once a decision is made.");
        createAuditLog(admin, AuditAction.APPLICATION_REVIEWED, app2.getId(), "Application moved to under review for course: DESD");

        // --- Application 3: SUBMITTED (pending) ---
        Application app3 = Application.builder()
                .user(student)
                .course(dbda)
                .status(ApplicationStatus.SUBMITTED)
                .statementOfPurpose("I am a data analyst looking to upskill in big data technologies. CDAC's DBDA course covers exactly the tools and frameworks I need - Hadoop, Spark, and machine learning - to advance my career in data engineering.")
                .submittedAt(LocalDateTime.now().minusDays(1))
                .build();
        app3 = applicationRepository.save(app3);

        createNotification(student, NotificationType.APPLICATION_SUBMITTED,
                "Application Submitted - DBDA",
                "Your application for DBDA (Diploma in Big Data Analytics) has been submitted successfully. Our team will review it shortly.");
        createAuditLog(student, AuditAction.APPLICATION_SUBMITTED, app3.getId(), "Application submitted for course: DBDA");

        // --- Application 4: REJECTED ---
        Application app4 = Application.builder()
                .user(student)
                .course(activeCourses.get(3))
                .status(ApplicationStatus.REJECTED)
                .statementOfPurpose("I want to join this course to improve my programming skills.")
                .adminRemarks("Statement of purpose lacks detail and specific goals. Applicant's background does not meet the minimum eligibility criteria. Encouraged to reapply after gaining required prerequisites.")
                .submittedAt(LocalDateTime.now().minusDays(20))
                .reviewedAt(LocalDateTime.now().minusDays(18))
                .build();
        app4 = applicationRepository.save(app4);

        createNotification(student, NotificationType.APPLICATION_REJECTED,
                "Application Rejected",
                "Unfortunately, your application for DAC (Diploma in Advanced Computing) has been rejected. Reason: Statement of purpose lacks sufficient detail. You may reapply with a more comprehensive application.");
        createAuditLog(admin, AuditAction.APPLICATION_REVIEWED, app4.getId(), "Application rejected for course: DAC");

        log.info("Seeded 4 demo applications with associated notifications, documents, and audit logs");

        // --- Uploaded Documents ---
        createDemoDocuments(app1, student);
    }

    // ========================
    // Demo Documents
    // ========================

    private void createDemoDocuments(Application approvedApplication, User student) {
        if (uploadedDocumentRepository.countByDeletedFalse() > 0) {
            return;
        }

        List<UploadedDocument> documents = List.of(
                UploadedDocument.builder()
                        .application(approvedApplication)
                        .user(student)
                        .documentType(DocumentType.PHOTO)
                        .originalFileName("passport_photo.jpg")
                        .storedFileName("demo_photo_" + System.currentTimeMillis() + ".jpg")
                        .filePath("/uploads/documents/")
                        .contentType("image/jpeg")
                        .fileSize(245760L)
                        .build(),
                UploadedDocument.builder()
                        .application(approvedApplication)
                        .user(student)
                        .documentType(DocumentType.ID_PROOF)
                        .originalFileName("aadhar_card.pdf")
                        .storedFileName("demo_id_" + System.currentTimeMillis() + ".pdf")
                        .filePath("/uploads/documents/")
                        .contentType("application/pdf")
                        .fileSize(512000L)
                        .build(),
                UploadedDocument.builder()
                        .application(approvedApplication)
                        .user(student)
                        .documentType(DocumentType.MARKSHEET)
                        .originalFileName("sem_8_marksheet.pdf")
                        .storedFileName("demo_marksheet_" + System.currentTimeMillis() + ".pdf")
                        .filePath("/uploads/documents/")
                        .contentType("application/pdf")
                        .fileSize(1024000L)
                        .build(),
                UploadedDocument.builder()
                        .application(approvedApplication)
                        .user(student)
                        .documentType(DocumentType.RESUME)
                        .originalFileName("resume_john_doe.pdf")
                        .storedFileName("demo_resume_" + System.currentTimeMillis() + ".pdf")
                        .filePath("/uploads/documents/")
                        .contentType("application/pdf")
                        .fileSize(307200L)
                        .build()
        );

        uploadedDocumentRepository.saveAll(documents);
        log.info("Seeded {} demo documents", documents.size());
    }

    // ========================
    // Notifications Helper
    // ========================

    private void createNotification(User user, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    // ========================
    // Audit Log Helper
    // ========================

    private void createAuditLog(User actor, AuditAction action, Long entityId, String description) {
        AuditLog auditLog = AuditLog.builder()
                .actorUser(actor)
                .actorEmail(actor.getEmail())
                .action(action)
                .entityType("APPLICATION")
                .entityId(entityId)
                .description(description)
                .ipAddress("127.0.0.1")
                .build();
        auditLogRepository.save(auditLog);
    }
}