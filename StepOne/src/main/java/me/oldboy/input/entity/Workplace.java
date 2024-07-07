package me.oldboy.input.entity;

/**
 * Workplace entity
 */
public class Workplace implements Place {

    private String species = "Рабочее место";
    private Integer workplaceNumber;

    public Workplace(Integer workplaceNumber) {
        this.workplaceNumber = workplaceNumber;
    }

    /**
     * Get workplace species
     *
     * @return species of place
     */
    @Override
    public String getSpecies() {
        return species;
    }

    /**
     * Get workplace number
     *
     * @return workplaceNumber
     */
    @Override
    public Integer getNumber() {
        return workplaceNumber;
    }

    @Override
    public String toString() {
        return "Workplace{" +
                "species='" + species + '\'' +
                ", workplaceNumber=" + workplaceNumber +
                '}';
    }
}
