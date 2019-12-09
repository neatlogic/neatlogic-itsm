package codedriver.framework.process.exception;

public class AttributeValidException extends Exception {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 8106412352004757576L;

	public AttributeValidException() {
		super();
	}

	public AttributeValidException(String msg) {
		super(msg);
	}

	public AttributeValidException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public AttributeValidException(Throwable cause) {
		super(cause);
	}
}
