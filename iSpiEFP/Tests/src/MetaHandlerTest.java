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

import org.ispiefp.app.Initializer;
import org.ispiefp.app.Main;
import org.ispiefp.app.MetaData.LocalFragmentTree;
import org.ispiefp.app.MetaData.MetaData;
import org.ispiefp.app.MetaData.MetaHandler;
import org.ispiefp.app.util.CheckInternetConnection;
import org.ispiefp.app.util.VerifyPython;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MetaHandlerTest {

    @Test
    public void testElectrostatics()  {
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/acetone_l.json");
        Assert.assertTrue(metaHandler.containsElectrostatics());
        Assert.assertTrue(metaHandler.containsPolarization());
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertTrue(metaHandler.containsExchangeRepulsion());
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/acetone_mal_es_l.json");
        Assert.assertFalse(metaHandler.containsElectrostatics());
        Assert.assertTrue(metaHandler.containsPolarization());
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertTrue(metaHandler.containsExchangeRepulsion());
    }

    @Test
    public void testPolarization() {
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/c2h5oh_l.json");
        Assert.assertTrue(metaHandler.containsElectrostatics());
        Assert.assertTrue(metaHandler.containsPolarization());
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertTrue(metaHandler.containsExchangeRepulsion());
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/c2h5oh_mal_pol_l.json");
        Assert.assertTrue(metaHandler.containsElectrostatics());
        Assert.assertFalse(metaHandler.containsPolarization());
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertTrue(metaHandler.containsExchangeRepulsion());
    }

    @Test
    public void testExchangeRepulsion() {
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/c6h6_l.json");
        Assert.assertTrue(metaHandler.containsElectrostatics());
        Assert.assertTrue(metaHandler.containsPolarization());
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertTrue(metaHandler.containsExchangeRepulsion());
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/c6h6_mal_xr_l.json");
        Assert.assertTrue(metaHandler.containsElectrostatics());
        Assert.assertTrue(metaHandler.containsPolarization());
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertFalse(metaHandler.containsExchangeRepulsion());
    }

    @Test
    public void testDispersion() {
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/ccl4_l.json");
        Assert.assertTrue(metaHandler.containsElectrostatics());
        Assert.assertTrue(metaHandler.containsPolarization());
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertTrue(metaHandler.containsExchangeRepulsion());
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/ccl4_mal_dis_l.json");
        Assert.assertTrue(metaHandler.containsElectrostatics());
        Assert.assertTrue(metaHandler.containsPolarization());
        Assert.assertFalse(metaHandler.containsDispersion());
        Assert.assertTrue(metaHandler.containsExchangeRepulsion());
    }

    @Test
    public void testFields1(){
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/ch3oh_mal_bit1_l.json");
        Assert.assertFalse(metaHandler.containsCoordinates());
        Assert.assertTrue(metaHandler.containsMonopoles());
        Assert.assertFalse(metaHandler.containsDipoles());
        Assert.assertTrue(metaHandler.containsQuadrupoles());
        Assert.assertFalse(metaHandler.containsOctupoles());
        Assert.assertTrue(metaHandler.containsPolarizablePts());
        Assert.assertTrue(metaHandler.containsDynPolarizablePts());
        Assert.assertFalse(metaHandler.containsProjectionBasis());
        Assert.assertFalse(metaHandler.containsMultiplicity());
        Assert.assertTrue(metaHandler.containsProjectionWavefunction());
        Assert.assertFalse(metaHandler.containsFockMatrixElements());
        Assert.assertTrue(metaHandler.containsCanonVec());
        Assert.assertTrue(metaHandler.containsCanonFock());
        Assert.assertTrue(metaHandler.containsScreen2());
        Assert.assertFalse(metaHandler.containsScreen());
    }

    @Test
    public void testFields2(){
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/ch4_mal_bit2_l.json");
        Assert.assertFalse(metaHandler.containsCoordinates());
        Assert.assertFalse(metaHandler.containsMonopoles());
        Assert.assertFalse(metaHandler.containsDipoles());
        Assert.assertTrue(metaHandler.containsQuadrupoles());
        Assert.assertFalse(metaHandler.containsOctupoles());
        Assert.assertFalse(metaHandler.containsPolarizablePts());
        Assert.assertTrue(metaHandler.containsDynPolarizablePts());
        Assert.assertFalse(metaHandler.containsProjectionBasis());
        Assert.assertFalse(metaHandler.containsMultiplicity());
        Assert.assertTrue(metaHandler.containsProjectionWavefunction());
        Assert.assertFalse(metaHandler.containsFockMatrixElements());
        Assert.assertTrue(metaHandler.containsCanonVec());
        Assert.assertTrue(metaHandler.containsCanonFock());
        Assert.assertTrue(metaHandler.containsScreen2());
        Assert.assertFalse(metaHandler.containsScreen());
    }

    @Test
    public void testSingleFile(){
        MetaHandler metaHandler = new MetaHandler("iSpiEFP/Tests/TestResources/validMetaDataFile.json");
        metaHandler.examineMetaData(2);
        Assert.assertTrue(metaHandler.containsCoordinates());
        Assert.assertTrue(metaHandler.containsMonopoles());
        Assert.assertTrue(metaHandler.containsDipoles());
        Assert.assertTrue(metaHandler.containsQuadrupoles());
        Assert.assertTrue(metaHandler.containsOctupoles());
        Assert.assertTrue(metaHandler.containsPolarizablePts());
        Assert.assertTrue(metaHandler.containsDynPolarizablePts());
        Assert.assertTrue(metaHandler.containsProjectionBasis());
        Assert.assertTrue(metaHandler.containsMultiplicity());
        Assert.assertTrue(metaHandler.containsProjectionWavefunction());
        Assert.assertTrue(metaHandler.containsFockMatrixElements());
        Assert.assertTrue(metaHandler.containsCanonVec());
        Assert.assertTrue(metaHandler.containsCanonFock());
        Assert.assertTrue(metaHandler.containsScreen2());
        Assert.assertTrue(metaHandler.containsScreen());
    }

    @Test
    public void testCreateXYZ(){
        Initializer initializer = new Initializer();
        initializer.init();
        MetaData methaneData = Main.fragmentTree.getMetaData("ch4.efp");
        File testFile = null;
        try {
            testFile = methaneData.createTempXYZ();
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(testFile.exists());
    }

    @Test
    public void testLocalFragmentTree(){
        Initializer initializer = new Initializer();
        initializer.init();
        LocalFragmentTree lft = new LocalFragmentTree();
        MetaData metaData = new MetaData("iSpiEFP/Tests/TestResources/ch4_mal_bit2_l.json");
        lft.addFragment("iSpiEFP/Tests/TestResources/ch4_mal_bit2_l.json");
        Assert.assertTrue(metaData.equals(lft.getMetaData("ch4.efp")));
    }

    @Test
    public void testInternetConnection(){
        Assert.assertTrue(CheckInternetConnection.checkInternetConnection());
    }

    @Test
    public void testPythonInterpreter(){
        Initializer init = new Initializer();
        init.init();
        Assert.assertTrue(VerifyPython.isValidPython());
    }
}
