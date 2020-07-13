package org.ispiefp.app.libEFP;

import javafx.scene.layout.FlowPane;

public class CalculationPreset {
    private String title;
    private String runType;
    private String format;
    private String elecDamp;
    private String dispDamp;
    private Boolean[] terms; // [<elec>, <pol>, <disp>, <xr>]
    private String polDamp;
    private String polSolver;
    private String cutoff;
    private Float cutoff_radius;
    private String boundary_condition;
    private Float boxsize_x;
    private Float boxsize_y;
    private Float boxsize_z;
    private Float boxsize_alpha;
    private Float boxsize_beta;
    private Float boxsize_gamma;
    private String pairwise_analysis;
    private Integer ligand;
    private Integer optimization;

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
            String polSolver,
            String cutoff,
            Float cutoff_radius,
            String boundary_consition,
            Float boxsize_x,
            Float boxsize_y,
            Float boxsize_z,
            Float boxsize_alpha,
            Float boxsize_beta,
            Float boxsize_gamma,
            String pairwise_analysis,
            Integer ligand,
            Integer optimization
    ){
        this.title = title;
        this.runType = runType;
        this.format = format;
        this.elecDamp = elecDamp;
        this.dispDamp = dispDamp;
        this.terms = terms;
        this.polDamp = polDamp;
        this.polSolver = polSolver;
        this.cutoff=cutoff;
        this.cutoff_radius=cutoff_radius;
        this.boundary_condition=boundary_consition;
        this.boxsize_x=boxsize_x;
        this.boxsize_y=boxsize_y;
        this.boxsize_z=boxsize_z;
        this.boxsize_alpha=boxsize_alpha;
        this.boxsize_beta=boxsize_beta;
        this.boxsize_gamma=boxsize_gamma;
        this.pairwise_analysis=pairwise_analysis;
        this.ligand=ligand;
        this.optimization=optimization;
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

    public String getcutoff() {
        return cutoff;
    }

    public Float  getcutoff_radius() {
        return cutoff_radius;
    }

    public String getboundary_condition() {
        return boundary_condition;
    }

    public Float getboxsize_x() {
        return boxsize_x;
    }

    public Float getboxsize_y() {
        return boxsize_y;
    }

    public Float getboxsize_z() {
        return boxsize_z;
    }

    public Float getboxsize_alpha() {
        return boxsize_alpha;
    }

    public Float getboxsize_beta() {
        return boxsize_beta;
    }

    public Float getboxsize_gamma() {
        return boxsize_gamma;
    }

    public String getpairwise_analysis() {
        return pairwise_analysis;
    }

    public Integer getligand() {
        return ligand;
    }

    public Integer getoptimization() {
        return optimization;
    }




}
