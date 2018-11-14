package org.vmol.app.database;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class DatabaseRecord {
	private SimpleStringProperty choice;
	private SimpleStringProperty rmsd;
	private SimpleBooleanProperty check;
	public DatabaseRecord(String choice, String rmsd, boolean check) {
		this.choice = new SimpleStringProperty(choice);
		this.rmsd = new SimpleStringProperty(rmsd);
		this.check = new SimpleBooleanProperty(check);
	}
	
	public String getChoice() {
		return choice.get();
	}
	
	public String getRmsd() {
		return rmsd.get();
	}
	
	public boolean getCheck() {
		return check.get();
	}
	
	public SimpleBooleanProperty checkProperty() {
		return check;
	}
	
	public void setChoice(String choice) {
		this.choice = new SimpleStringProperty(choice);
	}
	
	public void setRmsd(String rmsd) {
		this.rmsd = new SimpleStringProperty(rmsd);
	}
	
	public void setCheck(boolean check) {
		checkProperty().set(check);
	}
}
