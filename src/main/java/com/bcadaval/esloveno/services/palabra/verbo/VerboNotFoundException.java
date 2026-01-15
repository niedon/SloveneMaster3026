package com.bcadaval.esloveno.services.palabra.verbo;

import java.io.Serial;

public class VerboNotFoundException extends Exception {

	@Serial
    private static final long serialVersionUID = -5993620109310158885L;

	public VerboNotFoundException() {
		super();
	}

	public VerboNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public VerboNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public VerboNotFoundException(String message) {
		super(message);
	}

	public VerboNotFoundException(Throwable cause) {
		super(cause);
	}
}
