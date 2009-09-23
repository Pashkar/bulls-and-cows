package paxus.bnc;

public class BncException extends Exception {
	public BncException() {
		super();
	}

	public BncException(String message, Throwable cause) {
		super(message, cause);
	}

	public BncException(String message) {
		super(message);
	}

	public BncException(Throwable cause) {
		super(cause);
	}
}
