package org.ispiefp.app.database;

import java.util.ArrayList;

//java helper object for holding json coordinate pairs
public class JsonFragment {
    public String chemicalFormula = "";
    public ArrayList<JsonCoordinatePair> coords = new ArrayList<JsonCoordinatePair>();

    public void setChemicalFormula(String chemFormula) {
        this.chemicalFormula = chemFormula;
    }

    public ArrayList<JsonCoordinatePair> getCoords() {
        return coords;
    }
}
