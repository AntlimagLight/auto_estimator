package by.kaminsky.utils;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ValidationUtils {

    public static <T> T assertExistence(Optional<T> opt, String exceptionMassage) throws RuntimeException {
        if (opt.isEmpty()) {
            throw new RuntimeException(exceptionMassage);
        }
        return opt.get();
    }

    public static <T> T assertExistence(T entity, String exceptionMassage) throws RuntimeException {
        if (entity == null) {
            throw new RuntimeException(exceptionMassage);
        }
        return entity;
    }

    public static <T> void assertNotExistence(Optional<T> opt, String exceptionMassage) throws RuntimeException {
        if (opt.isPresent()) {
            throw new RuntimeException(exceptionMassage);
        }
    }

    //TODO add custom exeptions

}
