package me.oldboy.cwapp.core.entity;

public enum Role {
    ADMIN ("Администратор"),
    USER ("Пользователь");

    private final String strName;

    Role(String strName) { this.strName = strName; }

    public String getStrName() { return strName; }
}
