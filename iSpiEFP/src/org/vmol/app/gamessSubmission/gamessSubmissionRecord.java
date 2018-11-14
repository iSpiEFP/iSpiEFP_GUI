package org.vmol.app.gamessSubmission;

public class gamessSubmissionRecord {
	private String name;
	private String status;
	private String time;
	public gamessSubmissionRecord(String name, String status, String time) {
		this.name = name;
		this.status = status;
		this.time = time;
	}
	
	public String getName() {
		return name;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
}
