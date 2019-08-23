package org.vmol.app;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/* The purpose of this class is to decode the information about which fields are contained in a metadata JSON bitmap */

public class MetaHandler {

    private Gson gson;                      /* Instance of gson used to parse a MetaData object */
    private MetaData currentMetaData;       /* The MetaData Object for the current fragment     */

    /* This class is essentially a wrapper for extracting all of the fields from a JSON String */
    class MetaData {

        private String fromFile;       /* The file from which this metadata was extracted          */
        private String fragmentName;   /* The name of the fragment from the file                   */
        private String scf_type;       /* The type of self-consistent field method used            */
        private String basis_set;      /* The basis_set in which this calculations was performed   */
        private String[] coordinates;    /* A list of coordinates strings                            */
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
                                               file will be extracted                                  */

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
}
