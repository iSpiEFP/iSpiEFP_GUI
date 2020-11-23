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
