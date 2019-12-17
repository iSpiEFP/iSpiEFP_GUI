package org.ispiefp.app;

import com.google.gson.Gson;
import org.ispiefp.app.installer.LocalBundleManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/* The purpose of this class is to decode the information about which fields are contained in a metadata JSON bitmap */

public class MetaHandler {

    private MetaData currentMetaData;           /* The MetaData Object for the current fragment         */
    private MetaDataFile metaFile;              /* Used when all MetaDataObjects are in the same file   */

    /**
     * Constructor used when parsing individual JSON files
     */
    public MetaHandler(){

    }
    /**
     * Constructor used when all meta JSONs are in the same file
     * note: Expects the file passed to be in the library_parameters workspace
     * @param fileName name of the file
     */
    public MetaHandler(String fileName){
        metaFile = new MetaDataFile(fileName);
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
         * Getter function for all of the metaData Objects inferred from the constructor
         * @return Array of MetaData Objects
         */

        public MetaData[] getMetaDataObjects() {
            return metaDataObjects;
        }
    }

    /* This class is essentially a wrapper for extracting all of the fields from a JSON String     */
    class MetaData {

        private String fromFile;       /* The file from which this metadata was extracted          */
        private String fragmentName;   /* The name of the fragment from the file                   */
        private String scf_type;       /* The type of self-consistent field method used            */
        private String basisSet;      /* The basisSet in which this calculations was performed   */
        private Coordinates[] coordinates;  /* A list of coordinates strings                            */
        /* Coordinate Object format:
                <atomId> <x_coord> <y_coord> <z_coord> <mass> <charge>

                    Field       Type
                    ------------------
                    atomID:     String
                    x:          double
                    y:          double
                    z:          double
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
                MetaData inferredClass = gson.fromJson(metaDataFileString, MetaData.class);
                this.fromFile = inferredClass.fromFile;
                this.fragmentName = inferredClass.fragmentName;
                this.scf_type = inferredClass.scf_type;
                this.basisSet = inferredClass.basisSet;
                this.coordinates = inferredClass.coordinates;
                this.bitmap = inferredClass.bitmap;
            }
            if (this.fromFile == null || this.fragmentName == null || this.scf_type == null
                    || this.basisSet == null || this.coordinates.length == 0 || this.bitmap == 0) {
                System.err.println("Malformed meta data file");
            }
        }

        /**
         * Returns bitmap encoding of contained fields
         *
         * @return int value of bitmap
         */
        public int getBitmap() {
            return bitmap;
        }

        /**
         * Returns the basis set the parameter generation was performed in
         *
         * @return the basis set as a String
         */
        public String getbasisSet() {
            return basisSet;
        }

        /**
         * Returns the name of the fragment
         *
         * @return the fragment as a String
         */
        public String getFragmentName() {
            return fragmentName;
        }

        /**
         * Returns the file from which this meta data was extracted
         *
         * @return the file name as a String
         */
        public String getFromFile() {
            return fromFile;
        }

        /**
         * Returns the self-conistent field method used to generate these parameters
         *
         * @return the self-consistent field method as a String
         */
        public String getScf_type() {
            return scf_type;
        }

        /**
         * Returns the coordinates of every atom in the fragment as a String in the following representation:
         * <p>
         * <atomId> <x_coord> <y_coord> <z_coord> <mass> <charge>
         * <p>
         * Field       Type
         * ------------------
         * atomId:     String
         * x_coord:    double
         * y_coord:    double
         * z_coord:    double
         * mass:       double
         * charge:     double
         *
         * @return an array of Strings of size number of coordinates
         */
        public Coordinates[] getCoordinates() {
            return coordinates;
        }

        class Coordinates {
            String atomID;
            double x;
            double y;
            double z;
            double mass;
            double charge;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MetaData)) return false;
            for (int i = 0; i < this.coordinates.length; i++){
                try {
                    if (this.coordinates[i] != ((MetaData) obj).coordinates[i]) return false;
                } catch(ArrayIndexOutOfBoundsException e){
                    return false;
                }
            }
            return ((MetaData) obj).basisSet.equals(this.basisSet) &&
                    ((MetaData) obj).bitmap == this.bitmap &&
                    ((MetaData) obj).fragmentName.equals(this.fragmentName) &&
                    ((MetaData) obj).scf_type.equals(this.scf_type);
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
     * Returns true iff original efp file contained LMO Centroids
     *
     * @return true iff original efp file contained LMO Centroids
     */
    public boolean containsLMOCentroids() {
        int bitmask = 2048;    /* bitmask = 0000 0000 0000 0000 0000 1000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 2048;
    }

    /**
     * Returns true iff original efp file contained canonical vectors
     *
     * @return true iff original efp file contained canonical vectors
     */
    public boolean containsCanonVec() {
        int bitmask = 4096;    /* bitmask = 0000 0000 0000 0000 0001 0000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 4096;
    }

    /**
     * Returns true iff original efp file contained canonical Fock Matrix
     *
     * @return true iff original efp file contained canonical Fock Matrix
     */
    public boolean containsCanonFock() {
        int bitmask = 8192;    /* bitmask = 0000 0000 0000 0000 0010 0000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 8192;
    }

    /**
     * Returns true iff original efp file contained screen2
     *
     * @return true iff original efp file contained screen2
     */
    public boolean containsScreen2() {
        int bitmask = 16384;    /* bitmask = 0000 0000 0000 0000 0100 0000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 16384;
    }

    /**
     * Returns true iff original efp file contained screen
     *
     * @return true iff original efp file contained screen
     */
    public boolean containsScreen() {
        int bitmask = 32768;    /* bitmask = 0000 0000 0000 0000 1000 0000 0000 0000 */
        return (currentMetaData.bitmap & bitmask) == 32768;
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
