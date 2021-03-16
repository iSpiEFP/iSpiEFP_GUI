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
