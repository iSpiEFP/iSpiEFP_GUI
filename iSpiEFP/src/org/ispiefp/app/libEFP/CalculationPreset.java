package org.ispiefp.app.libEFP;

public class CalculationPreset {
    private String title;
    private String runType;
    private String format;
    private String elecDamp;
    private String dispDamp;
    private Boolean[] terms; // [<elec>, <pol>, <disp>, <xr>]
    private String polDamp;
    private String polSolver;

    public CalculationPreset(String definedString){ //separates each term with a ;%;
        String[] parsedTerms = definedString.split(";%;");
        terms = new Boolean[4];
        title = parsedTerms[0];
        runType = parsedTerms[1];
        format = parsedTerms[2];
        elecDamp = parsedTerms[3];
        dispDamp = parsedTerms[4];
        terms[0] = parsedTerms[5].equals("true");
        terms[1] = parsedTerms[6].equals("true");
        terms[2] = parsedTerms[7].equals("true");
        terms[3] = parsedTerms[8].equals("true");
        polDamp = parsedTerms[9];
        polSolver = parsedTerms[10];
    }
    public CalculationPreset(
            String title,
            String runType,
            String format,
            String elecDamp,
            String dispDamp,
            Boolean[] terms,
            String polDamp,
            String polSolver
    ){
        this.title = title;
        this.runType = runType;
        this.format = format;
        this.elecDamp = elecDamp;
        this.dispDamp = dispDamp;
        this.terms = terms;
        this.polDamp = polDamp;
        this.polSolver = polSolver;
    }

    public String getCalculationPresetDefinedString(){
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        sb.append(";%;");
        sb.append(runType);
        sb.append(";%;");
        sb.append(format);
        sb.append(";%;");
        sb.append(elecDamp);
        sb.append(";%;");
        sb.append(dispDamp);
        sb.append(";%;");
        for (int i = 0; i < 4; i++){
            sb.append(terms[i].toString());
            sb.append(";%;");
        }
        sb.append(polDamp);
        sb.append(";%;");
        sb.append(polSolver);
        sb.append(";%;");
        return sb.toString();
    }

    public String getTitle() {
        return title;
    }

    public String getRunType() {
        return runType;
    }

    public String getFormat() {
        return format;
    }

    public String getElecDamp() {
        return elecDamp;
    }

    public String getDispDamp() {
        return dispDamp;
    }

    public Boolean[] getTerms() {
        return terms;
    }

    public String getPolDamp() {
        return polDamp;
    }

    public String getPolSolver() {
        return polSolver;
    }
}
