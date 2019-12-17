package org.ispiefp.app;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/* This class is essentially a wrapper for extracting all of the fields from a JSON String     */
public class MetaData {

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
    public MetaData(String metaDataFilePath) {
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
        for (int i = 0; i < coordinates.length; i++){
            try {
                if (!coordinates[i].atomID.equals(((MetaData) obj).coordinates[i].atomID) ||
                        coordinates[i].charge != ((MetaData) obj).coordinates[i].charge ||
                        coordinates[i].mass != ((MetaData) obj).coordinates[i].mass ||
                        coordinates[i].x != ((MetaData) obj).coordinates[i].x ||
                        coordinates[i].y != ((MetaData) obj).coordinates[i].y ||
                        coordinates[i].z != ((MetaData) obj).coordinates[i].z) return false;
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
