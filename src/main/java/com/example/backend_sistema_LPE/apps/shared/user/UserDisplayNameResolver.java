package com.example.backend_sistema_LPE.apps.shared.user;

public final class UserDisplayNameResolver {

    public UserDisplayNameResolver() {
    }

     public static String resolve(User user) {
        if (user == null) {
            return "";
        }

        String firstName = normalize(user.getName());
        String lastName = normalize(user.getLastName());
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }

        return normalize(user.getUserName());
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
