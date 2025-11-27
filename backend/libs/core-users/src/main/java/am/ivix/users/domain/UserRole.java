package am.ivix.users.domain;

/**
 * Production-grade набор ролей для IviX.
 *
 * USER      – обычный клиент/покупатель
 * PROVIDER  – исполнитель услуг
 * MERCHANT  – продавец товаров / магазин
 * MODERATOR – модератор платформы
 * ADMIN     – администратор системы
 */
public enum UserRole {
    USER,
    PROVIDER,
    MERCHANT,
    MODERATOR,
    ADMIN
}
