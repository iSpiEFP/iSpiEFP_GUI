package org.ispiefp.app.MetaData;

import com.google.gson.Gson;

import java.io.*;
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

    /**
     * This method creates an xyz file from the MetaData's coordinate field. The file will automatically be deleted
     * upon exiting iSpiEFP, so it will not take up system resources permanently. These files are to be used for
     * rendering fragments through the file->select fragment option. I suppose a user could use up all their system
     * resources by selecting fragments all day and never closing iSpiEFP, but this seems unlikely.
     * @return
     * @throws IOException
     */
    public File createTempXYZ() throws IOException {
        BufferedWriter bw = null;
        File xyzFile = null;
        try{
            //Create a temp xyz file
            xyzFile = File.createTempFile(fromFile, "xyz");
            xyzFile.deleteOnExit();
            bw = new BufferedWriter(new FileWriter(xyzFile));

            //Write number of atoms in XYZ file
            bw.write(coordinates.length);
            //Blank line
            bw.write(System.getProperty("line.separator"));
            //Write the coordinates of each atom to the file
            for (int i = 0; i < coordinates.length; i++){
                //Don't include dummy atoms
                if (coordinates[i].atomID.startsWith("B")){
                    continue;
                }
                //Get the atom type by stripping all numbers from the atomID and removing the leading A
                String atomType = coordinates[i].atomID.replaceAll("[^A-Za-z]", "");
                atomType = atomType.substring(1);
                //Follow XYZ file format for each atom
                bw.write(String.format("%s\t%.5f\t%.5f\t%.5f%n",
                        atomType,
                        coordinates[i].x,
                        coordinates[i].y,
                        coordinates[i].z));
            }
        } catch(IOException e){
            System.err.println("Unable to create a temporary file.");
        }
        finally {
            if (bw != null) bw.close();
        }
        return xyzFile;
    }
}
