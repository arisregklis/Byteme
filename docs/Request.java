package gr.aueb.byteme.common;
public class Request {
    private final int whereFrom;
    private final int whereTo;

    public Request( int whereFrom,  int whereTo) {
        if (whereFrom < 0 || whereTo < 0) {
            throw new InvalidDataException("whereFrom and whereTo must be non-negative");
        }
        if (whereFrom == whereTo) {
            throw new InvalidDataException("whereFrom and whereTo must be different");
        }
    }
}