package de.fu_berlin.imp.apiua.entity.model;

public class EntityDataException extends Exception {
	private static final long serialVersionUID = 1L;

	public EntityDataException(String error, Throwable throwable) {
		super(error, throwable);
	}
}
