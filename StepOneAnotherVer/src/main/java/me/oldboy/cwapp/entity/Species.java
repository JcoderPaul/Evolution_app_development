package me.oldboy.cwapp.entity;

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
