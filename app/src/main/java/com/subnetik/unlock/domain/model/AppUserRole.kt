package com.subnetik.unlock.domain.model

enum class AppUserRole {
    GUEST,
    USER,
    STUDENT,
    TEACHER,
    ADMIN;

    companion object {
        fun resolve(isLoggedIn: Boolean, role: String?): AppUserRole {
            if (!isLoggedIn) return GUEST
            return when (role?.trim()?.lowercase()) {
                "student" -> STUDENT
                "teacher" -> TEACHER
                "admin", "manager" -> ADMIN
                "user" -> USER
                else -> GUEST
            }
        }
    }
}
