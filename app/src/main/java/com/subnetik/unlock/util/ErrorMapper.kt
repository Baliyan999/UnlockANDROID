package com.subnetik.unlock.util

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Централизованный маппинг HTTP и сетевых ошибок в человеко-понятные сообщения.
 */
object ErrorMapper {

    /** Общий маппинг для любого Exception */
    fun map(e: Exception, context: ErrorContext = ErrorContext.GENERAL): String {
        return when (e) {
            is HttpException -> mapHttp(e.code(), context)
            is ConnectException, is UnknownHostException -> "Нет подключения к интернету. Проверьте соединение"
            is SocketTimeoutException -> "Время ожидания истекло. Попробуйте ещё раз"
            else -> e.message?.let { mapRawMessage(it) } ?: "Произошла неизвестная ошибка"
        }
    }

    /** Маппинг по HTTP коду с учётом контекста */
    fun mapHttp(code: Int, context: ErrorContext = ErrorContext.GENERAL): String {
        return when (context) {
            ErrorContext.LOGIN -> mapLoginError(code)
            ErrorContext.REGISTER -> mapRegisterError(code)
            ErrorContext.PROFILE -> mapProfileError(code)
            ErrorContext.HOMEWORK -> mapHomeworkError(code)
            ErrorContext.PAYMENT -> mapPaymentError(code)
            ErrorContext.GENERAL -> mapGeneralError(code)
        }
    }

    private fun mapLoginError(code: Int): String = when (code) {
        400 -> "Неверный логин или пароль"
        401 -> "Неверный логин или пароль"
        403 -> "Аккаунт заблокирован. Обратитесь к администратору"
        404 -> "Пользователь не найден"
        429 -> "Слишком много попыток. Подождите и попробуйте снова"
        in 500..599 -> "Сервер временно недоступен. Попробуйте позже"
        else -> "Ошибка входа. Попробуйте ещё раз"
    }

    private fun mapRegisterError(code: Int): String = when (code) {
        400 -> "Проверьте введённые данные. Возможно, такой логин уже существует"
        409 -> "Пользователь с таким логином уже существует"
        422 -> "Некорректные данные. Проверьте все поля"
        429 -> "Слишком много попыток. Подождите и попробуйте снова"
        in 500..599 -> "Сервер временно недоступен. Попробуйте позже"
        else -> "Ошибка регистрации. Попробуйте ещё раз"
    }

    private fun mapProfileError(code: Int): String = when (code) {
        400 -> "Некорректные данные профиля"
        401 -> "Сессия истекла. Войдите снова"
        403 -> "Нет прав для изменения профиля"
        413 -> "Файл слишком большой. Максимум 5 МБ"
        in 500..599 -> "Сервер временно недоступен. Попробуйте позже"
        else -> "Ошибка обновления профиля"
    }

    private fun mapHomeworkError(code: Int): String = when (code) {
        400 -> "Проверьте данные задания. Заполните все обязательные поля"
        401 -> "Сессия истекла. Войдите снова"
        403 -> "Нет прав для создания задания"
        404 -> "Группа не найдена"
        in 500..599 -> "Сервер временно недоступен. Попробуйте позже"
        else -> "Ошибка при работе с заданием"
    }

    private fun mapPaymentError(code: Int): String = when (code) {
        400 -> "Некорректные данные квитанции"
        401 -> "Сессия истекла. Войдите снова"
        413 -> "Файл слишком большой. Максимум 10 МБ"
        in 500..599 -> "Сервер временно недоступен. Попробуйте позже"
        else -> "Ошибка при загрузке квитанции"
    }

    private fun mapGeneralError(code: Int): String = when (code) {
        400 -> "Некорректный запрос. Проверьте введённые данные"
        401 -> "Сессия истекла. Войдите снова"
        403 -> "У вас нет прав доступа"
        404 -> "Данные не найдены"
        409 -> "Конфликт данных. Попробуйте обновить страницу"
        413 -> "Файл слишком большой"
        422 -> "Некорректные данные"
        429 -> "Слишком много запросов. Подождите немного"
        in 500..599 -> "Сервер временно недоступен. Попробуйте позже"
        else -> "Произошла ошибка (код $code)"
    }

    /** Очистка сырых сообщений от технических деталей */
    private fun mapRawMessage(msg: String): String {
        return when {
            msg.contains("HTTP 4", ignoreCase = true) || msg.contains("HTTP 5", ignoreCase = true) -> {
                val code = Regex("\\d{3}").find(msg)?.value?.toIntOrNull()
                if (code != null) mapGeneralError(code) else "Произошла ошибка. Попробуйте ещё раз"
            }
            msg.contains("Unable to resolve host", ignoreCase = true) -> "Нет подключения к интернету"
            msg.contains("timeout", ignoreCase = true) -> "Время ожидания истекло"
            msg.contains("connect", ignoreCase = true) && msg.contains("refused", ignoreCase = true) -> "Сервер недоступен"
            else -> msg
        }
    }

    enum class ErrorContext {
        GENERAL, LOGIN, REGISTER, PROFILE, HOMEWORK, PAYMENT
    }
}
