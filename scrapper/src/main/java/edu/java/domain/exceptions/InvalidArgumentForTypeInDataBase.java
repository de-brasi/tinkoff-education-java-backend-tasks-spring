package edu.java.domain.exceptions;

// todo:
//  это в контроллере мапить в IncorrectRequestException (когда ошибка при обработке данных, например не получить url из URI)
public class InvalidArgumentForTypeInDataBase extends RuntimeException {
    public InvalidArgumentForTypeInDataBase(Exception e) {
        super(e);
    }
}
