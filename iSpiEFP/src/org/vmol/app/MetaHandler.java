package org.vmol.app;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/* The purpose of this class is to decode the information about which fields are contained in a metadata JSON bitmap */

public class MetaHandler {

    private Gson gson;                      /* Instance of gson used to parse a MetaData object    */
    private MetaData currentMetaData;       /* The MetaData Object for the current fragment        */

    /* This class is essentially a wrapper for extracting all of the fields from a JSON String     */
    class MetaData {

        private String fromFile;       /* The file from which this metadata was extracted          */
        private String fragmentName;   /* The name of the fragment from the file                   */
        private String scf_type;       /* The type of self-consistent field method used            */
        private String basis_set;      /* The basis_set in which this calculations was performed   */
        private String[] coordinates;  /* A list of coordinates strings                            */
        /* Coordinate String format:
                <atomId> <x_coord> <y_coord> <z_coord> <mass> <charge>

                    Field       Type
                    ------------------
                    atomId:     String
                    x_coord:    double
                    y_coord:    double
                    z_coord:    double
                    mass:       double
                    charge:     double
                                                                                                   */
        private int bitmap;         /* Integer from which fields present in original
                                               file will be extracted                              */

        /**
         * Constructor for a MetaData object
         *
         * @param metaDataFilePath The path of the file containing the meta data JSON
         */
        private MetaData(String metaDataFilePath) {
            String metaDataFileString;      /* The content of the metDataFile as a JSON string or null if IOException */
            Gson gson = new Gson();         /* Instance of Gson used to parse the string to an object                 */
            try {
                metaDataFileString = new String(Files.readAllBytes(Paths.get(metaDataFilePath)));
            } catch (IOException e) {
                metaDataFileString = null;
            }

            if (metaDataFileString != null) {
                gson.fromJson(metaDataFileString, MetaData.class);
            }
        }
    }

    /**
     * Sets the MetaDataHandler's currentMetaData to be that which is contained in the passed file
     *
     * @param metaDataFile The file containing the meta data JSON of interest
     */
    public void setCurrentMetaData(String metaDataFile) {
        this.currentMetaData = new MetaData(metaDataFile);
    }

    /**
     * Returns true iff original efp file contained coordinates
     *
     * @return Returns true iff original efp file contained coordinates
     */
    public boolean containsCoordinates() {
        int bitmask = 1;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0001 */
        return (currentMetaData.bitmap & bitmask) == 1;
    }

    /**
     * Returns true iff original efp file contained monopoles
     *
     * @return true iff original efp file contained monopoles
     */
    public boolean containsMonopoles() {
        int bitmask = 2;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0010 */
        return (currentMetaData.bitmap & bitmask) == 2;
    }

    /**
     * Returns true iff original efp file contained dipoles
     *
     * @return true iff original efp file contained dipoles
     */
    public boolean containsDipoles() {
        int bitmask = 4;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0100 */
        return (currentMetaData.bitmap & bitmask) == 4;
    }

    /**
     * Returns true iff original efp file contained quadrupoles
     *
     * @return true iff original efp file contained quadrupoles
     */
    public boolean containsQuadrupoles() {
        int bitmask = 8;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 1000 */
        return (currentMetaData.bitmap & bitmask) == 8;
    }

    /**
     * Returns true iff original efp file contained octupoles
     *
     * @return true iff original efp file contained octupoles
     */
    public boolean containsOctupoles() {
        int bitmask = 16;    /* bitmask = 0000 0000 0000 0000 0000 0000 0001 0000 */
        return (currentMetaData.bitmap & bitmask) == 16;
    }

    /**
     * Returns true iff original efp file contained Polarizable Points
     *
     * @return true iff original efp file contained Polarizable Points
     */
    public boolean containsPolarizablePts() {
        int bitmask = 32;    /* bitmask = 0000 0000 0000 0000 0000 0000 0010 0000 */
        return (currentMetaData.bitmap & bitmask) == 32;
    }

    /**
     * Returns true iff original efp file contained Dynamic Polarizable Points
     *
     * @return true iff original efp file contained Dynamic Polarizable Points
     */
    public boolean containsDynPolarizablePts() {
        int bitmask = 64;    /* bitmask = 0000 0000 0000 0000 0000 0000 0100 0000 */
        return (currentMetaData.bitmap & bitmask) == 64;
    }

    /**
     * Returns true iff original efp file contained projection basis
     *
     * @return true iff original efp file contained projection basis
     */
    public boolean containsProjectionBasis() {
        int bitmask = 128;    /* bitmask = 0000 0000 0000 0000 0000 0000 1000 0000 */
        return (currentMetaData.bitmap & bitmask) == 128;
    }

    /**
     * Returns true iff original efp file contained multiplicity
     *
     * @return true iff original efp file contained multiplicity
     */
    public boolean containsMultiplicity() {
        int bitmask = 256;    /* bitmask = 0000 0000 0000 0000 0000 0001 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 256;
    }

    /**
     * Returns true iff original efp file contained projection wavefunction
     *
     * @return true iff original efp file contained projection wavefunction
     */
    public boolean containsProjectionWavefunction() {
        int bitmask = 512;    /* bitmask = 0000 0000 0000 0000 0000 0010 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 512;
    }

    /**
     * Returns true iff original efp file contained Fock matrix elements
     *
     * @return true iff original efp file contained Fock matrix elements
     */
    public boolean containsFockMatrixElements() {
        int bitmask = 1024;    /* bitmask = 0000 0000 0000 0000 0000 0100 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 1024;
    }

    /**
     * Returns true iff original efp file contained canonical vectors
     *
     * @return true iff original efp file contained canonical vectors
     */
    public boolean containsCanonVec() {
        int bitmask = 2048;    /* bitmask = 0000 0000 0000 0000 0000 1000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 2048;
    }

    /**
     * Returns true iff original efp file contained canonical Fock Matrix
     *
     * @return true iff original efp file contained canonical Fock Matrix
     */
    public boolean containsCanonFock() {
        int bitmask = 4096;    /* bitmask = 0000 0000 0000 0000 0001 0000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 4096;
    }

    /**
     * Returns true iff original efp file contained screen2
     *
     * @return true iff original efp file contained screen2
     */
    public boolean containsScreen2() {
        int bitmask = 8192;    /* bitmask = 0000 0000 0000 0000 0010 0000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 8192;
    }

    /**
     * Returns true iff original efp file contained screen
     *
     * @return true iff original efp file contained screen
     */
    public boolean containsScreen() {
        int bitmask = 16384;    /* bitmask = 0000 0000 0000 0000 0100 0000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 16384;
    }

    /**
     * Returns true iff original efp file contained all of the information for electrostatics
     * A file has all of the electrostatic information if it has monopoles, dipoles, quadrupoles, and octupoles
     *
     * @return true iff original efp file contained all of the information for electrostatics
     */
    public boolean containsElectrostatics() {
        return containsMonopoles() && containsDipoles() && containsQuadrupoles() && containsOctupoles();
    }

}
