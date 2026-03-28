package com.subnetik.unlock.presentation.navigation

sealed class Routes(val route: String) {
    // Auth flow
    data object Onboarding : Routes("onboarding")
    data object Auth : Routes("auth")
    data object VerifyCode : Routes("verify_code/{email}") {
        fun createRoute(email: String) = "verify_code/$email"
    }

    // Main tabs
    data object Home : Routes("home")
    data object Homework : Routes("homework")
    data object Test : Routes("test")
    data object Profile : Routes("profile")

    // Vocabulary
    data object Vocabulary : Routes("vocabulary")
    data object VocabularyLevel : Routes("vocabulary/{level}") {
        fun createRoute(level: Int) = "vocabulary/$level"
    }
    data object Flashcards : Routes("flashcards/{level}") {
        fun createRoute(level: Int) = "flashcards/$level"
    }

    // Test
    data object TestSession : Routes("test_session/{level}") {
        fun createRoute(level: Int) = "test_session/$level"
    }
    data object TestResult : Routes("test_result/{level}/{score}/{total}") {
        fun createRoute(level: Int, score: Int, total: Int) = "test_result/$level/$score/$total"
    }

    // Profile sub-screens
    data object EditProfile : Routes("edit_profile")
    data object Notifications : Routes("notifications")
    data object Contact : Routes("contact")
    data object ShiFuChat : Routes("shifu_chat")

    // Student screens
    data object Schedule : Routes("schedule")
    data object SupportBooking : Routes("support_booking")
    data object Payment : Routes("payment")
    data object StudentPayments : Routes("student_payments")
    data object Market : Routes("market")
    data object Promocodes : Routes("promocodes")
    data object Referral : Routes("referral")
    data object Lead : Routes("lead")
    data object Teachers : Routes("teachers")
    data object Reviews : Routes("reviews")
    data object Blog : Routes("blog")
    data object BlogDetail : Routes("blog_detail/{articleId}") {
        fun createRoute(articleId: String) = "blog_detail/$articleId"
    }
    data object Calculator : Routes("calculator")
}
