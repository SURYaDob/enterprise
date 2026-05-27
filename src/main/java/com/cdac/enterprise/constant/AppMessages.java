package com.cdac.enterprise.constant;

public final class AppMessages {

    private AppMessages() {
    }

    public static final String APPLICATION_RUNNING_SUCCESSFULLY = "Application is running successfully";

    public static final String USER_REGISTERED_SUCCESSFULLY = "User registered successfully";
    public static final String USER_NOT_FOUND = "User not found";

    public static final String EMAIL_ALREADY_REGISTERED = "Email is already registered";
    public static final String DEFAULT_STUDENT_ROLE_NOT_CONFIGURED = "Default student role is not configured";

    public static final String LOGIN_SUCCESSFUL = "Login successful";
    public static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";

    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    public static final String ACCESS_DENIED = "You do not have permission to access this resource";
    public static final String AUTHENTICATION_REQUIRED = "Authentication is required to access this resource";

    public static final String COURSE_CREATED_SUCCESSFULLY = "Course created successfully";
    public static final String COURSE_UPDATED_SUCCESSFULLY = "Course updated successfully";
    public static final String COURSE_DELETED_SUCCESSFULLY = "Course deleted successfully";
    public static final String COURSE_FETCHED_SUCCESSFULLY = "Course fetched successfully";
    public static final String COURSES_FETCHED_SUCCESSFULLY = "Courses fetched successfully";
    public static final String COURSE_CODE_ALREADY_EXISTS = "Course code already exists";
    public static final String COURSE_NOT_FOUND = "Course not found";
    public static final String COURSE_NOT_ACTIVE = "Course is not active for applications";

    public static final String APPLICATION_SUBMITTED_SUCCESSFULLY = "Application submitted successfully";
    public static final String APPLICATION_FETCHED_SUCCESSFULLY = "Application fetched successfully";
    public static final String APPLICATIONS_FETCHED_SUCCESSFULLY = "Applications fetched successfully";
    public static final String APPLICATION_UPDATED_SUCCESSFULLY = "Application updated successfully";
    public static final String APPLICATION_NOT_FOUND = "Application not found";
    public static final String APPLICATION_ALREADY_EXISTS = "You have already applied for this course";

    public static final String DOCUMENT_UPLOADED_SUCCESSFULLY = "Document uploaded successfully";
    public static final String DOCUMENTS_FETCHED_SUCCESSFULLY = "Documents fetched successfully";
    public static final String FILE_IS_EMPTY = "File is empty";
    public static final String FILE_TYPE_NOT_ALLOWED = "File type is not allowed";
    public static final String FILE_SIZE_EXCEEDED = "File size exceeds allowed limit";
    public static final String FILE_STORAGE_FAILED = "Failed to store file";

    public static final String NOTIFICATIONS_FETCHED_SUCCESSFULLY = "Notifications fetched successfully";
    public static final String NOTIFICATION_MARKED_AS_READ = "Notification marked as read";
    public static final String NOTIFICATION_NOT_FOUND = "Notification not found";

    public static final String AUDIT_LOGS_FETCHED_SUCCESSFULLY = "Audit logs fetched successfully";

    public static final String DASHBOARD_STATS_FETCHED_SUCCESSFULLY = "Dashboard stats fetched successfully";

    // Forgot/Reset Password
    public static final String RESET_TOKEN_SENT = "Password reset link has been sent to your email";
    public static final String PASSWORD_RESET_SUCCESSFULLY = "Password has been reset successfully";
    public static final String INVALID_OR_EXPIRED_RESET_TOKEN = "Invalid or expired reset token";
    public static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with given email";
}