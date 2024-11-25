public class SqlException extends Exception {
    public SqlException(String message) {
        super(message);
    }

    public SqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
