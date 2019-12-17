import org.ispiefp.app.LocalFragmentTree;
import org.ispiefp.app.MetaHandler;
import org.ispiefp.app.installer.LocalBundleManager;
import org.junit.Assert;
import org.junit.Test;

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
    public void testLocalFragmentTree(){
        LocalFragmentTree lft = new LocalFragmentTree();
        MetaHandler mh = new MetaHandler("iSpiEFP/out/production/parameters/libraryMeta.json");
        mh.setCurrentMetaData("iSpiEFP/Tests/TestResources/ch4_mal_bit2_l.json");
        lft.addFragment("iSpiEFP/Tests/TestResources/ch4_mal_bit2_l.json");
        //Assert.assertTrue(lft.getMetaData("ch4.efp").equals(mh.getCurrentMetaData()));
    }
}
