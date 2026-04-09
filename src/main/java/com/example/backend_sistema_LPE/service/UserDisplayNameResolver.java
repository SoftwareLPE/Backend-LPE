package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.User;

final class UserDisplayNameResolver {

    private UserDisplayNameResolver() {
    }

    static String resolve(User user) {
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

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
