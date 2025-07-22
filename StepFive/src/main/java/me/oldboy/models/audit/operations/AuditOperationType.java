package me.oldboy.models.audit.operations;

/**
 * Enumeration representing different operation with entity (Place, Slot, User, Reservation).
 * Список наиболее возможных операций с сущностями приложения.
 */
public enum AuditOperationType {
    CREATE_RESERVATION,
    DELETE_RESERVATION,
    UPDATE_RESERVATION,
    CREATE_SLOT,
    DELETE_SLOT,
    UPDATE_SLOT,
    CREATE_PLACE,
    DELETE_PLACE,
    UPDATE_PLACE,
    DELETE_USER,
    UPDATE_USER
}