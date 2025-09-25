package exception;

public class BlacklistedException extends RuntimeException{
    public BlacklistedException(String message) {
        super(message);
    }
}
