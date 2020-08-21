package main.exception;

public class TemplateException extends RuntimeException {
	
	private String path;
	
	public TemplateException() {
		super();
	}
	
	public TemplateException(Throwable t) {
		super(t);
	}
	
	public TemplateException(String message) {
		super(message);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
