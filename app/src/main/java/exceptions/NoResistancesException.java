package exceptions;

public class NoResistancesException extends Exception {
    public NoResistancesException(String message) {
        super(message);
    }
    public NoResistancesException() {
        super("Cannot return empty list of resistances to compute on.");
    }
}
