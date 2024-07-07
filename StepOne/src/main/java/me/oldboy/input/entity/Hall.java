package me.oldboy.input.entity;

/**
 * Hall entity.
 */
public class Hall implements Place {

    private String species = "Конференц-зал";

    private Integer hallNumber;

    public Hall(Integer hallName) {
        this.hallNumber = hallName;
    }

    /**
     * Get hall species
     *
     * @return species
     */
    @Override
    public String getSpecies() {
        return species;
    }

    /**
     * Get hall number
     *
     * @return hallNumber
     */
    @Override
    public Integer getNumber() {
        return hallNumber;
    }

    @Override
    public String toString() {
        return "Hall{" +
                "species='" + species + '\'' +
                ", hallNumber=" + hallNumber +
                '}';
    }
}
