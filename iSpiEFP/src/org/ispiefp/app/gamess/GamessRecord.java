package org.ispiefp.app.gamess;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/***
 * Object for holding Gamess data for selection form
 */
public class GamessRecord {
    private SimpleStringProperty choice;
    private SimpleBooleanProperty check;
    private int index;

    public GamessRecord(String choice, int index, boolean check) {
        this.choice = new SimpleStringProperty(choice);
        this.check = new SimpleBooleanProperty(check);
        this.index = index;
    }

    public String getChoice() {
        return choice.get();
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

    public void setCheck(boolean check) {
        checkProperty().set(check);
    }
}