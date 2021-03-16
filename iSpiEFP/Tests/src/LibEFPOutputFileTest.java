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

import org.ispiefp.app.libEFP.LibEFPOutputFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class LibEFPOutputFileTest {

    @Test
    public void testOptimizationJob() {
        LibEFPOutputFile of;
        try {
            of = new LibEFPOutputFile("iSpiEFP/Tests/TestResources/opt_1.out");
            Assert.assertEquals(of.getStates().size(), 49);
            LibEFPOutputFile.State state = of.getStates().get(20);
            Assert.assertEquals(state.getEnergyComponents().getTotalEnergy(), -0.0386047900, 0.00000001);
            state.getGeometry().getAtoms();
            for (LibEFPOutputFile.State.Geometry.Atom a : state.getGeometry().getAtoms()) {
                System.out.println(a.toString());
            }
        } catch (IOException e) {
            System.err.println("Issue while parsing");
            e.printStackTrace();
        }
    }

    @Test
    public void testSinglePointEnergyJob() {
        LibEFPOutputFile of;
        try {
            of = new LibEFPOutputFile("iSpiEFP/Tests/TestResources/w6b2_energy.out");
        } catch (IOException e) {
            System.err.println("Issue while parsing");
            e.printStackTrace();
        }
    }

    @Test
    public void testMolecularDynamicsJob() {
        LibEFPOutputFile of;
        try {
            of = new LibEFPOutputFile("iSpiEFP/Tests/TestResources/w6b2_md.out");
        } catch (IOException e) {
            System.err.println("Issue while parsing");
            e.printStackTrace();
        }
    }
}
