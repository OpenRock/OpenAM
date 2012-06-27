package org.forgerock.openam.authentication.modules.passphrase.exception;

public class MandatoryAttributeException extends Exception{

	public MandatoryAttributeException()
	{
		super();
	}

	public MandatoryAttributeException(String msg)
	{
		super(msg);
	}

	public MandatoryAttributeException(Throwable cause)
	{
		super(cause);
	}

	public MandatoryAttributeException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

}
