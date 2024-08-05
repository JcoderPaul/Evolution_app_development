package me.oldboy.cwapp.core.entity;

public enum Species {
    WORKPLACE ("Рабочее место"),
    HALL ("Конференц-зал");

    private final String strName;

    Species(String strName) {
        this.strName = strName;
    }

    public String getStrName() {
        return strName;
    }
}
