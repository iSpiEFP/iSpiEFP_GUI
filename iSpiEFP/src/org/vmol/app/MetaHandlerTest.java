package org.vmol.app;

import org.junit.Assert;
import org.junit.Test;

public class MetaHandlerTest {

    @Test
    public void testElectrostatics()  {
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.setCurrentMetaData("iSpiEFP/Tests/TestResources/acetone_l.json");
        System.out.println(metaHandler.containsPolarization());
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
    //TODO Fix c6h6_mal_xr_l.json to be missing one of the xr fields
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
    //TODO Fix ccl4_mal_dis_l.json to be missing one of the dispersion fields
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
        Assert.assertTrue(metaHandler.containsDispersion());
        Assert.assertFalse(metaHandler.containsExchangeRepulsion());
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
    //TODO MESSED UP FIELD TESTS

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
}
