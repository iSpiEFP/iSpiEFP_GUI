package org.vmol.app.database;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Object Holder for database data. Used in the auxilary table list
 */
public class DatabaseRecord {
    private SimpleStringProperty choice;
    private SimpleStringProperty rmsd;
    private SimpleBooleanProperty check;
    private int index;

    public DatabaseRecord(String choice, String rmsd, boolean check) {
        this.choice = new SimpleStringProperty(choice);
        this.rmsd = new SimpleStringProperty(rmsd);
        this.check = new SimpleBooleanProperty(check);
        this.index = 0;
    }

    public DatabaseRecord(String choice, String rmsd, boolean check, int index) {
        this.choice = new SimpleStringProperty(choice);
        this.rmsd = new SimpleStringProperty(rmsd);
        this.check = new SimpleBooleanProperty(check);
        this.index = index;
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

    public int getIndex() {
        return index;
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
