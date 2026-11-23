package gr.aueb.byteme.common;

public class InvalidDataException extends IllegalArgumentException {

    public InvalidDataException(String message) {
        super(message);
    }
}