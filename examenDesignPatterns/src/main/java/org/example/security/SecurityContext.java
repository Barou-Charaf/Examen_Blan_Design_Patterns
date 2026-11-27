package org.example.security;

import java.util.Objects;

// Contexte de démonstration : l'authentification réelle reste hors du projet.
public final class SecurityContext {
    private static final ThreadLocal<Role> CURRENT = new ThreadLocal<>();
    private SecurityContext() {}

    public static void setRole(Role role) { CURRENT.set(Objects.requireNonNull(role)); }
    public static void clear() { CURRENT.remove(); }

    public static void require(Role required) {
        Role actual = CURRENT.get();
        if (actual == null || (actual != Role.ADMIN && actual != required)) {
            throw new SecurityException("Accès refusé : rôle " + required + " requis");
        }
    }
}
