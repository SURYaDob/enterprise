package com.cdac.enterprise.constant;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_V1 = "/api/v1";

    public static final String AUTH = API_V1 + "/auth";
    public static final String HEALTH = API_V1 + "/health";

    public static final String COURSES = API_V1 + "/courses";
    public static final String ADMIN_COURSES = API_V1 + "/admin/courses";

    public static final String APPLICATIONS = API_V1 + "/applications";
    public static final String ADMIN_APPLICATIONS = API_V1 + "/admin/applications";

    public static final String DOCUMENTS = API_V1 + "/documents";
    public static final String NOTIFICATIONS = API_V1 + "/notifications";
    public static final String ADMIN_AUDIT_LOGS = API_V1 + "/admin/audit-logs";
    public static final String ADMIN_DASHBOARD = API_V1 + "/admin/dashboard";

    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String FORGOT_PASSWORD = "/forgot-password";
    public static final String RESET_PASSWORD = "/reset-password";
}