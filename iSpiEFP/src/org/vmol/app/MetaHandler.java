package org.vmol.app;

import com.google.gson.Gson;

/* The purpose of this class is to decode the information about which fields are contained in a metadata JSON bitmap */

public class MetaHandler {

    private Gson gson;                      /* Instance of gson used to parse a MetaData object */
    private MetaData currentMetaData;       /* The MetaData Object for the current fragment     */

    /* This class is essentially a wrapper for extracting all of the fields from a JSON String */
    class MetaData{

        private String      fromFile;       /* The file from which this metadata was extracted          */
        private String      fragmentName;   /* The name of the fragment from the file                   */
        private String      scf_type;       /* The type of self-consistent field method used            */
        private String      basis_set;      /* The basis_set in which this calculations was performed   */
        private String[]    coordinates;    /* A list of coordinates strings                            */
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
        private int         bitmap;         /* Integer from which fields present in original
                                               file will be extracted                                  */

        /**
         * Constructor for a MetaData object
         * @param metaDataFile The name of the file containing the meta data JSON
         */
        private MetaData(String metaDataFile){

        }
    }
}
