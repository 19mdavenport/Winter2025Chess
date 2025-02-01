package service;

import java.security.PrivilegedActionException;

/**
 * Exception for any server-specific irregularity during execution
 */
public class ChessServerException extends Exception {
    private final Reason reason;

    public ChessServerException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public ChessServerException(Reason reason, Throwable throwable) {
        super(throwable);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        BAD_INPUT,
        BAD_AUTH,
        ITEM_TAKEN,
        INTERNAL_ERROR
    }
}
