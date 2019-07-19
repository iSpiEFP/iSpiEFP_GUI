package org.libefp.app.database;

/**
 * JSON helper object for storage.
 * Example:
 * This object would be written to a json similar to:
 * <p>
 * {
 * "chemicalFormula": "h20",
 * "efp_file": "cposincpisenpcnesk",
 * "xyz_file": "coesincoesncn",
 * "rmsd": 2039
 * }
 */
public class JsonFilePair {
    public String chemicalFormula = new String();
    public String efp_file = new String();
    public String xyz_file = new String();
    public String rmsd = new String();
}
