package org.ispiefp.app.util;

import java.util.HashMap;

public class PeriodicTable {

    private HashMap<String, Double> table;

    public PeriodicTable() {
        this.table = new HashMap<String, Double>();
        initTable(this.table);
    }

    public void initTable(HashMap<String, Double> table) {
        //Load Periodic Table
        table.put("H", 1.0);
        table.put("He", 2.0);
        table.put("Li", 3.0);
        table.put("Be", 4.0);
        table.put("B", 5.0);
        table.put("C", 6.0);
        table.put("N", 7.0);
        table.put("O", 8.0);
        table.put("F", 9.0);
        table.put("Ne", 10.0);
        table.put("Na", 11.0);
        table.put("Mg", 12.0);
        table.put("Al", 13.0);
        table.put("Si", 14.0);
        table.put("P", 15.0);
        table.put("S", 16.0);
        table.put("Cl", 17.0);
        table.put("Ar", 18.0);
        table.put("K", 19.0);
        table.put("Ca", 20.0);
        table.put("Sc", 21.0);
        table.put("Ti", 22.0);
        table.put("V", 23.0);

    }

}
