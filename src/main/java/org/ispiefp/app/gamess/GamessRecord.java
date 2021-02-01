/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

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