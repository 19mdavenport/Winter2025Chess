package exception;

/**
 * Exception for any server-specific irregularity during execution
 */
public class ResponseException extends Exception {
    private final Reason reason;

    public ResponseException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public ResponseException(Reason reason, Throwable throwable) {
        super(throwable);
        this.reason = reason;
    }

    public ResponseException(Reason reason, String message, Throwable cause) {
        super(message, cause);
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
