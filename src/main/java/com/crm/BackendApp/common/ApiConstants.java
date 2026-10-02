package com.crm.BackendApp.common;

public final class ApiConstants {

    public static final String API_V1 = "/api/v1";

    public static final String AUTH_V1 = API_V1 + "/auth";
    public static final String USERS_V1 = API_V1 + "/users";
    public static final String PROJECTS_V1 = API_V1 + "/projects";
    public static final String TASKS_V1 = API_V1 + "/tasks";
    public static final String CLIENTS_V1 = API_V1 + "/clients";
    public static final String INVITATIONS_V1 = API_V1 + "/invitations";

    private ApiConstants() {
        // Prevent instantiation
    }
}
