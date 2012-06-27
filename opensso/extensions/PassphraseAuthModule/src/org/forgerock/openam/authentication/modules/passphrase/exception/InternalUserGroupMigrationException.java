package org.forgerock.openam.authentication.modules.passphrase.exception;

public class InternalUserGroupMigrationException extends Exception{

	public InternalUserGroupMigrationException()
	{
		super();
	}

	public InternalUserGroupMigrationException(String msg)
	{
		super(msg);
	}

	public InternalUserGroupMigrationException(Throwable cause)
	{
		super(cause);
	}

	public InternalUserGroupMigrationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

}
