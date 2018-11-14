package org.vmol.app.server;

import java.io.Serializable;

public class ServerDetails implements Serializable {
	
	private static final long serialVersionUID = -8906882033167247261L; // Check the importance of this again!

	private String serverName = "";
	
	private String address = "";
	
	private String userName = "";
	
	private String serverType = "";
	
	private String workingDirectory = "";
	
	private String queueSystemType;
	
	private int port;
	
	private QueueOptions queueOptions;
	
	public ServerDetails () {
	}
	
	public ServerDetails (String serverName, String address, String serverType, int port) {
		this.serverName = serverName;
		this.address = address;
		this.serverType = serverType;
		this.port = port;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getQueueSystemType() {
		return queueSystemType;
	}

	public void setQueueSystemType(String queueSystemType) {
		this.queueSystemType = queueSystemType;
	}
	
	public QueueOptions getQueueOptions() {
		return queueOptions;
	}

	public void setQueueOptions(QueueOptions queueOptions) {
		this.queueOptions = queueOptions;
	}

	public class QueueOptions implements Serializable {

		private static final long serialVersionUID = 8020104900103328295L;
		private String submit;
		private String query;
		private String kill;
		private String jobFileList;
		private String queueInfo;
		private String runFileTemplate;
		private int updateIntervalSecs;
		
		public QueueOptions () {
			
		}

		public String getSubmit() {
			return submit;
		}

		public void setSubmit(String submit) {
			this.submit = submit;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getKill() {
			return kill;
		}

		public void setKill(String kill) {
			this.kill = kill;
		}

		public String getJobFileList() {
			return jobFileList;
		}

		public void setJobFileList(String jobFileList) {
			this.jobFileList = jobFileList;
		}

		public String getQueueInfo() {
			return queueInfo;
		}

		public void setQueueInfo(String queueInfo) {
			this.queueInfo = queueInfo;
		}

		public String getRunFileTemplate() {
			return runFileTemplate;
		}

		public void setRunFileTemplate(String runFileTemplate) {
			this.runFileTemplate = runFileTemplate;
		}

		public int getUpdateIntervalSecs() {
			return updateIntervalSecs;
		}

		public void setUpdateIntervalSecs(int updateIntervalSecs) {
			this.updateIntervalSecs = updateIntervalSecs;
		}
	}
	
}
