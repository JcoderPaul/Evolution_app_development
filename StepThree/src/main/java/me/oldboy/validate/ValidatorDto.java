package me.oldboy.validate;

import javax.validation.*;
import java.util.Set;

public class ValidatorDto {

    private static ValidatorDto instance;

    private ValidatorDto(){
    }

    public static ValidatorDto getInstance(){
        if (instance == null){
            instance = new ValidatorDto();
        }
        return instance;
    }

    public <T> void isValidData(T t) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<T>> validationResult = validator.validate(t);
        if (!validationResult.isEmpty()) {
            throw new ConstraintViolationException(validationResult);
        }
    }
}
