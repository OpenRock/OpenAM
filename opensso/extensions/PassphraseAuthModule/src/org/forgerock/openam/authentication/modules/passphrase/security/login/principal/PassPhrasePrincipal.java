package org.forgerock.openam.authentication.modules.passphrase.security.login.principal;

import java.security.Principal;

public class PassPhrasePrincipal implements Principal, java.io.Serializable {

	private String name;

	public PassPhrasePrincipal(String name) {
		this.name = name;
	}

	/**
	 * Return a string representation of this <code>Principal</code>.
	 * 
	 * @return a string representation of this <code>Principal</code>.
	 */
	public String toString() {
		return ("Principal:  " + name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}