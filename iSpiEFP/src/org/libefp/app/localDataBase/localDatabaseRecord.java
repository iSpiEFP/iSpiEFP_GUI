package org.libefp.app.localDataBase;

import javafx.beans.property.SimpleStringProperty;

public class localDatabaseRecord {
    private SimpleStringProperty choice;
    private SimpleStringProperty rmsd;
    private SimpleStringProperty parameters;

    public localDatabaseRecord(String choice, String rmsd, String parameters) {
        this.choice = new SimpleStringProperty(choice);
        this.rmsd = new SimpleStringProperty(rmsd);
        this.parameters = new SimpleStringProperty(parameters);
    }

    public String getChoice() {
        return choice.get();
    }

    public String getRmsd() {
        return rmsd.get();
    }

    public SimpleStringProperty getParameters() {
        return parameters;
    }


    public void setChoice(String choice) {
        this.choice = new SimpleStringProperty(choice);
    }

    public void setRmsd(String rmsd) {
        this.rmsd = new SimpleStringProperty(rmsd);
    }

    public void setParameters(String parameters) {
        this.parameters = new SimpleStringProperty(parameters);
    }


}
