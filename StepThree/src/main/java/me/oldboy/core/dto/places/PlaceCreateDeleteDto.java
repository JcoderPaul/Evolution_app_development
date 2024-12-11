package me.oldboy.core.dto.places;

import me.oldboy.core.model.database.entity.options.Species;

import javax.validation.constraints.*;

/*
При быстрой валидации с использованием средств javax.validation.constraints
нужно учитывать особенности использования той или иной аннотации и то над
какими полями (string, integer, enum) их можно/нельзя использовать, см.
https://docs.oracle.com/javaee/7/api/javax/validation/constraints/package-summary.html
https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/package-summary.html
https://docs.oracle.com/javaee/6/api/javax/validation/constraints/package-summary.html
*/
public record PlaceCreateDeleteDto(@NotNull(message = "Species can not be blank/empty")
                                   Species species,
                                   @NotNull
                                   @Positive(message = "PlaceNumber can not be null/negative")
                                   Integer placeNumber){
    @Override
    public String toString() {
        return "PlaceCreateDeleteDto { " +
                "species = " + species +
                ", placeNumber = " + placeNumber +
                '}';
    }
}
