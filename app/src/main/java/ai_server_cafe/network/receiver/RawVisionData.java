package ai_server_cafe.network.receiver;

public class RawVisionData {
	private final String message;
	public RawVisionData(String message) {
		this.message = message;
	}
	
	public RawVisionData() {
		this.message = "init";
	}
	
	public String getMessage() {
		return this.message;
	}
}
