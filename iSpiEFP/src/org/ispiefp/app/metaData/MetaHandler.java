package org.ispiefp.app.metaData;

import com.google.gson.Gson;
import org.ispiefp.app.efpFileRetriever.GithubRequester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/* The purpose of this class is to decode the information about which fields are contained in a metadata JSON bitmap */

public class MetaHandler {

    private MetaData currentMetaData;           /* The MetaData Object for the current fragment         */
    private MetaDataFile metaFile;              /* Used when all MetaDataObjects are in the same file   */


    /**
     * Constructor used when all meta JSONs are in the same file saved locally
     * note: Expects the file passed to be in the library_parameters workspace
     * @param fileName name of the file
     */
    public MetaHandler(String fileName){
        metaFile = new MetaDataFile(fileName);
    }

    /**
     * Constructor used when all meta JSONs are retrieved from the master file
     * on Github
     */
    public MetaHandler(){
        metaFile = new MetaDataFile();
    }

    /**
     * Returns the underlying MetaDataFile object containing each of the metaData instances
     * @return MetaDataFile object
     */
    public MetaDataFile getMetaFile() {
        return metaFile;
    }

    class MetaDataFile {
        private MetaData[] metaDataObjects;     /* All of the MetaData Objects in  the file             */

        /**
         * Constructor for the MetaDataFile which contains all the MetaData objects
         * note: Expects the file passed to be in the library_parameters workspace
         *
         * @param fileName name of the file
         */

        public MetaDataFile(String fileName) {
            String metaDataFileString;
            String pathToFile = fileName; //LocalBundleManager.LIBEFP_PARAMETERS + LocalBundleManager.FILE_SEPERATOR + fileName;
            Gson gson = new Gson();         /* Instance of Gson used to parse the string to an object                 */
            try {
                metaDataFileString = new String(Files.readAllBytes(Paths.get(pathToFile)));
            } catch (IOException e) {
                System.err.println("The MetaDataFile was not in the expected directory: " + pathToFile);
                metaDataFileString = null;
            }
            if (metaDataFileString != null) {
                MetaDataFile inferredClass = gson.fromJson(metaDataFileString, MetaDataFile.class);
                this.metaDataObjects = inferredClass.metaDataObjects;
            }
        }

        /**
         * Constructor for the MetaDataFile which contains all the MetaData objects
         * from Github
         *
         */

        public MetaDataFile() {
            System.out.println("get here");
            Gson gson = new Gson();         /* Instance of Gson used to parse the string to an object                 */
            GithubRequester gr = new GithubRequester("libraryMeta.json");
            String metaDataFileString;
            try {
                metaDataFileString = gr.getFileContents();

            } catch (Exception e) {
                System.err.println("The MetaDataFile could not be retrieved from Github");
                metaDataFileString = null;
            }
            if (metaDataFileString != null) {
                MetaDataFile inferredClass = gson.fromJson(metaDataFileString, MetaDataFile.class);
                this.metaDataObjects = inferredClass.metaDataObjects;
            }
        }

        /**
         * Getter function for all of the metaData Objects inferred from the constructor
         * @return Array of MetaData Objects
         */

        public MetaData[] getMetaDataObjects() {
            return metaDataObjects;
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
     * Returns the current MetaData object that the handler is looking at
     *
     * @return the MetaData object that the handler is looking at
     */
    public MetaData getCurrentMetaData() {
        return currentMetaData;
    }

    public void examineMetaData(int i){
        this.currentMetaData = metaFile.metaDataObjects[i];
    }
    /**
     * Returns true iff original efp file contained coordinates
     *
     * @return Returns true iff original efp file contained coordinates
     */
    public boolean containsCoordinates() {
        int bitmask = 1;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0001 */
        return (currentMetaData.getBitmap() & bitmask) == 1;
    }

    /**
     * Returns true iff original efp file contained monopoles
     *
     * @return true iff original efp file contained monopoles
     */
    public boolean containsMonopoles() {
        int bitmask = 2;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0010 */
        return (currentMetaData.getBitmap() & bitmask) == 2;
    }

    /**
     * Returns true iff original efp file contained dipoles
     *
     * @return true iff original efp file contained dipoles
     */
    public boolean containsDipoles() {
        int bitmask = 4;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0100 */
        return (currentMetaData.getBitmap() & bitmask) == 4;
    }

    /**
     * Returns true iff original efp file contained quadrupoles
     *
     * @return true iff original efp file contained quadrupoles
     */
    public boolean containsQuadrupoles() {
        int bitmask = 8;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 1000 */
        return (currentMetaData.getBitmap() & bitmask) == 8;
    }

    /**
     * Returns true iff original efp file contained octupoles
     *
     * @return true iff original efp file contained octupoles
     */
    public boolean containsOctupoles() {
        int bitmask = 16;    /* bitmask = 0000 0000 0000 0000 0000 0000 0001 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 16;
    }

    /**
     * Returns true iff original efp file contained Polarizable Points
     *
     * @return true iff original efp file contained Polarizable Points
     */
    public boolean containsPolarizablePts() {
        int bitmask = 32;    /* bitmask = 0000 0000 0000 0000 0000 0000 0010 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 32;
    }

    /**
     * Returns true iff original efp file contained Dynamic Polarizable Points
     *
     * @return true iff original efp file contained Dynamic Polarizable Points
     */
    public boolean containsDynPolarizablePts() {
        int bitmask = 64;    /* bitmask = 0000 0000 0000 0000 0000 0000 0100 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 64;
    }

    /**
     * Returns true iff original efp file contained projection basis
     *
     * @return true iff original efp file contained projection basis
     */
    public boolean containsProjectionBasis() {
        int bitmask = 128;    /* bitmask = 0000 0000 0000 0000 0000 0000 1000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 128;
    }

    /**
     * Returns true iff original efp file contained multiplicity
     *
     * @return true iff original efp file contained multiplicity
     */
    public boolean containsMultiplicity() {
        int bitmask = 256;    /* bitmask = 0000 0000 0000 0000 0000 0001 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 256;
    }

    /**
     * Returns true iff original efp file contained projection wavefunction
     *
     * @return true iff original efp file contained projection wavefunction
     */
    public boolean containsProjectionWavefunction() {
        int bitmask = 512;    /* bitmask = 0000 0000 0000 0000 0000 0010 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 512;
    }

    /**
     * Returns true iff original efp file contained Fock matrix elements
     *
     * @return true iff original efp file contained Fock matrix elements
     */
    public boolean containsFockMatrixElements() {
        int bitmask = 1024;    /* bitmask = 0000 0000 0000 0000 0000 0100 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 1024;
    }

    /**
     * Returns true iff original efp file contained LMO Centroids
     *
     * @return true iff original efp file contained LMO Centroids
     */
    public boolean containsLMOCentroids() {
        int bitmask = 2048;    /* bitmask = 0000 0000 0000 0000 0000 1000 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 2048;
    }

    /**
     * Returns true iff original efp file contained canonical vectors
     *
     * @return true iff original efp file contained canonical vectors
     */
    public boolean containsCanonVec() {
        int bitmask = 4096;    /* bitmask = 0000 0000 0000 0000 0001 0000 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 4096;
    }

    /**
     * Returns true iff original efp file contained canonical Fock Matrix
     *
     * @return true iff original efp file contained canonical Fock Matrix
     */
    public boolean containsCanonFock() {
        int bitmask = 8192;    /* bitmask = 0000 0000 0000 0000 0010 0000 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 8192;
    }

    /**
     * Returns true iff original efp file contained screen2
     *
     * @return true iff original efp file contained screen2
     */
    public boolean containsScreen2() {
        int bitmask = 16384;    /* bitmask = 0000 0000 0000 0000 0100 0000 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 16384;
    }

    /**
     * Returns true iff original efp file contained screen
     *
     * @return true iff original efp file contained screen
     */
    public boolean containsScreen() {
        int bitmask = 32768;    /* bitmask = 0000 0000 0000 0000 1000 0000 0000 0000 */
        return (currentMetaData.getBitmap() & bitmask) == 32768;
    }

    /**
     * Returns true iff original efp file contained all of the information for electrostatics
     * A file has all of the electrostatic information if it has monopoles, dipoles, quadrupoles, and octupoles
     *
     * @return true iff original efp file contained all of the information for electrostatics
     */
    public boolean containsElectrostatics() {
        return containsCoordinates() && containsMonopoles() && containsDipoles()
                && containsQuadrupoles() && containsOctupoles() && containsScreen2();
    }

    /**
     * Returns true iff original efp file contained all of the information for Polarization
     * A file has all of the Exchange Repulsion information if it has Polarizable Points
     *
     * @return true iff original efp file contained all of the information for Polarization
     */
    public boolean containsPolarization() {
        return containsPolarizablePts();
    }

    /**
     * Returns true iff original efp file contained all of the information for Exchange Repulsion
     * A file has all of the Exchange Repulsion information if it has Projection Basis Set, Multiplicity,
     * Projection Wave Function, Fock Matrix Elements, LMO Centroids
     *
     * @return true iff original efp file contained all of the information for Exchange Repulsion
     */
    public boolean containsExchangeRepulsion() {
        return containsProjectionBasis() && containsMultiplicity() && containsProjectionWavefunction()
                && containsFockMatrixElements() && containsLMOCentroids();
    }

    /**
     * Returns true iff original efp file contained all of the information for Dispersion
     * A file has all of the Exchange Repulsion information if it has Dynamic Polarizable Points
     *
     * @return true iff original efp file contained all of the information for Dispersion
     */
    public boolean containsDispersion() {
        return containsDynPolarizablePts();
    }
}
