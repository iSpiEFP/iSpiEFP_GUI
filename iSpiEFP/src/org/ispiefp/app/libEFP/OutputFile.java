package org.ispiefp.app.libEFP;

import org.ispiefp.app.visualizer.JmolMainPanel;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Ryan DeRue 7/8/2020
 * Class representing a general output from libEFP. Because the output files varies based on the type of run, be sure
 * to know which fields are relevant to the particular type of run. Every Output file has fields representing
 * the options used as well as an ArrayList of states which represent a snapshot of the system during the simulation.
 * For some types of runs, there may be only one state such as in "Single Point Energy Calculation", in others, there
 * may be many. States are abstract and are instantiated to either MolecularDynamicsState, which includes
 * additional information about the current state of the ensemble or StaticState for run types which don't require this
 * information. Fields are initialized to their default values in libEFP and are only changed if they were changed in
 * the input file.
 */
public class OutputFile {
    /* Mandatory Options */
    private String run_type             = "sp";                                 /* Type of job user selected */
    private String coord                = "xyzabc";                             /* Format of coordinates     */
    private boolean [] terms            = {true, true, true, true};             /* [E, P, D, XR] present     */
    private String elec_damp            = "screen";                             /* Damping used for E        */
    private String disp_damp            = "overlap";                            /* Damping used for D        */
    private String pol_damp             = "tt";                                 /* Damping used for P        */
    private String pol_driver           = "iterative";                          /* Method of computing P     */
    private boolean enable_ff           = false;                                /* Enable force fields       */
    private boolean enable_multistep    = false;                                /* Enable multistep MD       */
    private String ff_geometry          = "fraglib/params/amber99.prm";         /* Path to Geometry of MM    */
    private String ff_parameters        = "ff.xyz";                             /* Path to MM parameters     */
    private boolean single_params_file  = false;                                /* Use single EFP param file */
    private String efp_params_file      = "params.efp";                         /* Path to EFP param file    */
    private boolean enable_cutoff       = false;                                /* Cutoff distance for frags */
    private double swf_cutoff           = 10.0;                                 /* Magnitude of above cutoff */
    private long max_steps              = 100;                                  /* Max number of steps       */
    private int multistep_steps         = 1;                                    /* # of fast steps           */
    private String fraglib_path         = "<install_directory/share/libefp";    /* Path to fragment library  */
    private String userlib_path         = ".";                                  /* Path to user frag lib     */

    /* Pairwise Energy Analysis Options */
    private boolean enable_pairwise     = false;                                /* Pairwise Interactions     */
    private int ligand                  = 0;                                    /* Which fragment is ligand  */

    /* Periodic Boundary Conditions Options */
    private boolean enable_pbc          = false;                                /* Periodic boundary conds   */
    private String periodic_box         = "30.0 30.0 30.0 90.0 90.0 90.0";      /* Dimensions of PBC         */
    private boolean print_pbc           = false;                                /* Print coords as PBC       */

    /* Geometry Optimization Options */
    private double opt_tol              = 0.0001;                               /* Stop optimizing when < x  */

    /* Gradient Test Options */
    private double gtest_tol            = 0.000001;                             /* Stop gradient when < x    */
    private double ref_energy           = 0.0;                                  /* Reference Energy          */

    /* Hessian Calculation Options */
    private boolean hess_central        = false;                                /* Use central Hessian diffs */
    private double num_step_dist        = 0.001;                                /* Differential size (dist)  */
    private double num_step_angle       = 0.01;                                 /* Differential size (angle) */

    /* Molecular Dynamics Options */
    private String ensemble             = "nve";                                /* Type of ensemble          */
    private double time_step            = 1.0;                                  /* Time step size in fs      */
    private int print_step              = 1;                                    /* Print state every x steps */
    private boolean velocitize          = false;                                /* Generate initial velocity */
    private double temperature          = 300.0;                                /* Temperature of MD run     */
    private double pressure             = 300.0;                                /* Pressure of MD run        */
    private double thermostat_tau       = 1000.0;                               /* Temp relaxation time      */
    private double barostat_tau         = 10000.0;                              /* Pressure relaxation time  */

    /* Necessary Class Fields */
    private ArrayList<State> states;                                            /* Saved States              */

    /**
     * Abstract class defining a state in the system. The state can be the initial state, final state,
     * or an intermediate state. All states have 3 fields:
     *  1. Geometry         - Describes the current coordinates of all fragments
     *  2. RestartData      - Minimal information required to restart the computation from this state. Not meaningful in
     *                        run types which always have one state such as Single Point Energy
     *  3. EnergyComponents - Breakdown of the total energy and its constituent components. Some run types will give
     *                        additional information such as the gradient change. These fields are 0 when irrelevant.
     */
    public abstract class State {
        private Geometry geometry;
        private RestartData restartData;
        private EnergyComponents energyComponents;

        /**
         * Geometry is described as an ArrayList of containing Atoms which describe atom names and positions
         * transforming a section of the file which looks like:
         * ---
         * A01C1                6.875881    -0.963149     5.264420
         * A02C2                5.805451    -0.571045     4.462804
         * ...
         * A03C8                4.497796    -0.815568     4.878016
         * ---
         * -> [<A01C1, 6.875881, -0.963149, 5.264420>, ... <A03C8, 4.497796, -0.815568, 4.878016>]
         */
        public class Geometry {
            private ArrayList<Atom> atoms;

            /**
             * Every atom has a name and a triple of coordinates (x,y,z) such that a line like:
             * ---
             * A01C1                6.875881    -0.963149     5.264420
             * ---
             * ->
             * Atom{
             * atomID="A01C1",
             * x=6.875881,
             * y=-0.963149,
             * z=5.264420
             * };
             */
            public class Atom {
                private final String atomID;
                private final double x;
                private final double y;
                private final double z;

                public Atom(String atomID, double x, double y, double z){
                    this.atomID = atomID;
                    this.x = x;
                    this.y = y;
                    this.z = z;
                }

                public String getAtomID() {
                    return atomID;
                }

                public double getX() {
                    return x;
                }

                public double getY() {
                    return y;
                }

                public double getZ() {
                    return z;
                }

                @Override
                public String toString() {
                    return "Atom{" +
                            "atomID='" + atomID + '\'' +
                            ", x=" + x +
                            ", y=" + y +
                            ", z=" + z +
                            '}';
                }
            }

            private Geometry(){
                atoms = new ArrayList<>();
            }

            void addAtom(String atomID, double x, double y, double z){
                atoms.add(new Atom(atomID, x, y, z));
            }

            public ArrayList<Atom> getAtoms() {
                return atoms;
            }
        }

        /**
         * Class containing the bare minimum amount of information required to start the calculation again from the
         * current state. In some cases this will include velocities, in others it will not. In cases where it will not,
         * the velocities ArrayList will be null
         */
        public class RestartData {
            private ArrayList<Fragment> fragments;
            private ArrayList<String> velocities;

            public class Fragment{
                private final String name;
                private final double[] coords = new double[6];

                public Fragment(String nameString, String coordString){
                    name = nameString.trim();
                    String[] coordsStringArray = coordString.trim().split("[ ]+");
                    for(int i = 0; i < 6; i++){
                        //System.out.println("COORDS[" + i + "]: " + coords[i]);
                        coords[i] = Double.parseDouble(coordsStringArray[i]);
                    }
                }

                @Override
                public String toString() {
                    return "Fragment{" +
                            "name='" + name + '\'' +
                            ", coords=" + Arrays.toString(coords) +
                            '}';
                }

            }
            RestartData() {
                fragments = new ArrayList<>();
            }

            public ArrayList<Fragment> getFragments() {
                return fragments;
            }

            void addFragment(String nameString, String coordString){
                fragments.add(new Fragment(nameString, coordString));
            }

            void addVelocity(String velocityString) {
                if (velocities == null) velocities = new ArrayList<>();
                velocities.add(velocityString);
            }

            @Override
            public String toString() {
                return "RestartData{" +
                        "fragments=" + fragments +
                        '}';
            }
        }

       public class EnergyComponents {

            private final double eEnergy;
            private final double pEnergy;
            private final double dEnergy;
            private final double xrEnergy;
            private final double pcEnergy;
            private final double cpEnergy;
            private final double totalEnergy;
            private final double energyChange;
            private final double rmsGradient;
            private final double maxGradient;

            EnergyComponents(double eEnergy, double pEnergy, double dEnergy, double xrEnergy, double pcEnergy,
                                    double cpEnergy, double totalEnergy, double energyChange, double rmsGradient,
                                    double maxGradient){
                this.eEnergy = eEnergy;
                this.pEnergy = pEnergy;
                this.dEnergy = dEnergy;
                this.xrEnergy = xrEnergy;
                this.pcEnergy = pcEnergy;
                this.cpEnergy = cpEnergy;
                this.totalEnergy = totalEnergy;
                this.energyChange = energyChange;
                this.rmsGradient = rmsGradient;
                this.maxGradient = maxGradient;
            }

            public double getElectrostaticEnergy() {
                return eEnergy;
            }

            public double getPolarizationEnergy() {
                return pEnergy;
            }

            public double getDispersionEnergy() {
                return dEnergy;
            }

            public double getExchangeRepulsionEnergy() {
                return xrEnergy;
            }

            public double getPointChargesEnergy(){
                return pcEnergy;
            }

            public double getChargePenetrationEnergyEnergy(){
                return cpEnergy;
            }

            public double getTotalEnergy(){
                return totalEnergy;
            }

            public double getEnergyChange(){
                return energyChange;
            }

            public double getRMSGradient() {
                return rmsGradient;
            }

            public double getMaximumGradient() {
                return maxGradient;
            }
        }
        public State(){
            geometry = new Geometry();
            restartData = new RestartData();
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public RestartData getRestartData() {
            return restartData;
        }

        public EnergyComponents getEnergyComponents() {
            return energyComponents;
        }

        void setEnergyComponents(double eEnergy, double pEnergy, double dEnergy, double xrEnergy, double pcEnergy,
                                        double cpEnergy, double totalEnergy, double energyChange, double rmsGradient,
                                        double maxGradient){
            energyComponents = new EnergyComponents(eEnergy, pEnergy, dEnergy, xrEnergy, pcEnergy,
                                                    cpEnergy, totalEnergy, energyChange, rmsGradient, maxGradient);
        }
    }

    public class MolecularDynamicsState extends State {
        private double kineticEnergy;
        private double invariant;
        private double temperature;

        MolecularDynamicsState(){
            super();
        }

        public double getKineticEnergy() {
            return kineticEnergy;
        }

        public double getInvariant() {
            return invariant;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setKineticEnergy(double kineticEnergy) {
            this.kineticEnergy = kineticEnergy;
        }

        public void setInvariant(double invariant) {
            this.invariant = invariant;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }

    class StaticState extends State {
        StaticState(){
            super();
        }
    }

    public ArrayList<State> getStates() {
        return states;
    }

    public String getRun_type() {
        return run_type;
    }

    public String getCoord() {
        return coord;
    }

    public boolean[] getTerms() {
        return terms;
    }

    public String getElec_damp() {
        return elec_damp;
    }

    public String getDisp_damp() {
        return disp_damp;
    }

    public String getPol_damp() {
        return pol_damp;
    }

    public String getPol_driver() {
        return pol_driver;
    }

    public boolean isEnable_ff() {
        return enable_ff;
    }

    public boolean isEnable_multistep() {
        return enable_multistep;
    }

    public String getFf_geometry() {
        return ff_geometry;
    }

    public String getFf_parameters() {
        return ff_parameters;
    }

    public boolean isSingle_params_file() {
        return single_params_file;
    }

    public String getEfp_params_file() {
        return efp_params_file;
    }

    public boolean isEnable_cutoff() {
        return enable_cutoff;
    }

    public double getSwf_cutoff() {
        return swf_cutoff;
    }

    public long getMax_steps() {
        return max_steps;
    }

    public int getMultistep_steps() {
        return multistep_steps;
    }

    public String getFraglib_path() {
        return fraglib_path;
    }

    public String getUserlib_path() {
        return userlib_path;
    }

    public boolean isEnable_pbc() {
        return enable_pbc;
    }

    public String getPeriodic_box() {
        return periodic_box;
    }

    public double getOpt_tol() {
        return opt_tol;
    }

    public double getGtest_tol() {
        return gtest_tol;
    }

    public double getRef_energy() {
        return ref_energy;
    }

    public boolean isHess_central() {
        return hess_central;
    }

    public double getNum_step_dist() {
        return num_step_dist;
    }

    public double getNum_step_angle() {
        return num_step_angle;
    }

    public String getEnsemble() {
        return ensemble;
    }

    public double getTime_step() {
        return time_step;
    }

    public int getPrint_step() {
        return print_step;
    }

    public boolean isVelocitize() {
        return velocitize;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public double getThermostat_tau() {
        return thermostat_tau;
    }

    public double getBarostat_tau() {
        return barostat_tau;
    }

    public int getLigand() {
        return ligand;
    }

    public boolean isEnable_pairwise() {
        return enable_pairwise;
    }

    public boolean isPrint_pbc() {
        return print_pbc;
    }

    public OutputFile(String filepath) throws IOException {
        states = new ArrayList<>();
        File outFile = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(outFile));
        String line1 = "";
        String line2 = "";
        try {
            while ((line1 = br.readLine()) != null) {
                if (line1.equals("")) continue;
                String keyword = line1.split(" ")[0];

                switch (keyword) {
                    case "run_type":
                        run_type = line1.split(" ")[1];
                        break;
                    case "coord":
                        coord = line1.split(" ")[1];
                        break;
                    case "terms":
                        String[] termsArray = line1.split(" ");
                        for (int i = 0; i < termsArray.length; i++) {
                            switch (termsArray[i]) {
                                case "elec":
                                    terms[0] = true;
                                    break;
                                case "pol":
                                    terms[1] = true;
                                    break;
                                case "disp":
                                    terms[2] = true;
                                    break;
                                case "xr":
                                    terms[3] = true;
                                    break;
                            }
                        }
                        break;
                    case "elec_damp":
                        elec_damp = line1.split(" ")[1];
                        break;
                    case "disp_damp":
                        disp_damp = line1.split(" ")[1];
                        break;
                    case "pol_damp":
                        pol_damp = line1.split(" ")[1];
                        break;
                    case "pol_driver":
                        pol_driver = line1.split(" ")[1];
                        break;
                    case "enable_ff":
                        enable_ff = line1.split(" ")[1].equals("true");
                        break;
                    case "enable_multistep":
                        enable_multistep = line1.split(" ")[1].equals("true");
                        break;
                    case "ff_geometry":
                        ff_geometry = line1.split(" ")[1];
                        break;
                    case "ff_parameters":
                        ff_parameters = line1.split(" ")[1];
                        break;
                    case "single_params_file":
                        single_params_file = line1.split(" ")[1].equals("true");
                        break;
                    case "efp_params_file":
                        efp_params_file = line1.split(" ")[1];
                        break;
                    case "enable_cutoff":
                        enable_cutoff = line1.split(" ")[1].equals("true");
                        break;
                    case "swf_cutoff":
                        //NOTE: YOU CHANGED THIS, TELL RYAN
                        swf_cutoff = Double.parseDouble(line1.split(" ")[1]);
                        break;
                    case "max_steps":
                        max_steps = Long.parseLong(line1.split(" ")[1]);
                        break;
                    case "multistep_steps":
                        multistep_steps = Integer.parseInt(line1.split(" ")[1]);
                        break;
                    case "fraglib_path":
                        fraglib_path = line1.split(" ")[1];
                        break;
                    case "userlib_path":
                        userlib_path = line1.split(" ")[1];
                        break;
                    case "enable_pbc":
                        enable_pbc = line1.split(" ")[1].equals("true");
                        break;
                    case "periodic_box":
                        periodic_box = line1.substring(line1.indexOf(" ") + 1);
                        break;
                    case "opt_tol":
                        opt_tol = Double.parseDouble(line1.split(" ")[1]);
                        break;
                    case "gtest_tol":
                        gtest_tol = Double.parseDouble(line1.split(" ")[1]);
                        break;
                    case "ref_energy":
                        ref_energy = Double.parseDouble(line1.split(" ")[1]);
                        break;
                    case "hess_central":
                        hess_central = line1.split(" ")[1].equals("true");
                        break;
                    case "num_step_dist":
                        num_step_dist = Double.parseDouble(line1.split(" ")[1]);
                        break;
                    case "num_step_angle":
                        num_step_angle = Double.parseDouble(line1.split(" ")[1]);
                        break;
                    case "ensemble":
                        ensemble = line1.split(" ")[1];
                        break;
                    case "time_step":
                        time_step = Integer.parseInt(line1.split(" ")[1]);
                        break;
                    case "print_step":
                        print_step = Integer.parseInt(line1.split(" ")[1]);
                        break;
                    case "velocitize":
                        velocitize = line1.split(" ")[1].equals("true");
                        break;
                    case "temperature":
                        temperature = Long.parseLong(line1.split(" ")[1]);
                        break;
                    case "pressure":
                        pressure = Integer.parseInt(line1.split(" ")[1]);
                        break;
                    case "thermostat_tau":
                        thermostat_tau = Long.parseLong(line1.split(" ")[1]);
                        break;
                    case "barostat_tau":
                        barostat_tau = Long.parseLong(line1.split(" ")[1]);
                        break;
                    case "ligand":
                        ligand = Integer.parseInt(line1.split(" ")[1]);
                        break;
                    case "enable_pairwise":
                        enable_pairwise = line1.split(" ")[1].equals("true");
                        break;
                    case "print_pbc":
                        print_pbc = line1.split(" ")[1].equals("true");
                        break;
                    case "MOLECULAR":
                        parseMolecularDynamicsJob(br, line1);
                        break;
                    case "ENERGY":
                        parseEnergyMinimizationJob(br, line1);
                        break;
                    case "SINGLE":
                        parseSinglePointEnergyJob(br, line1);
                    default:
                        continue;
                }
            }
        }catch (IOException e){
            System.err.println("Error while parsing");
        } finally {
            br.close();
        }
    }

    private void parseMolecularDynamicsJob(BufferedReader br, String currentLine) throws IOException {
        do {
            if (currentLine.equals("")) continue;
            String keyword = currentLine.trim().split(" ")[0];
            MolecularDynamicsState currentState = new MolecularDynamicsState();
            switch (keyword) {
                case "INITIAL": //Starting state
                case "STATE": //Intermediate state
                case "FINAL": //Final state
                    br.readLine(); //Consume empty line
                    br.readLine(); //Consume GEOMETRY(ANGSTROMS)
                    br.readLine(); //Consume empty line
                    while (!(currentLine = br.readLine()).equals("")) {
                        String[] atomLineArray = getTokens(currentLine);
                        String atomID = atomLineArray[0];
                        double x = Double.parseDouble(atomLineArray[1]);
                        double y = Double.parseDouble(atomLineArray[2]);
                        double z = Double.parseDouble(atomLineArray[3]);
                        currentState.getGeometry().addAtom(atomID, x, y, z);
                    }
                    while (br.readLine().equals("")) ;
                    //Begin RESTART DATA
                    br.readLine(); //Consume RESTART DATA
                    br.readLine(); //Consume empty line
                    while (!(currentLine = br.readLine()).equals("")) {
                        //BufferedReader should be focused on the first line of the fragment
                        String line2 = br.readLine();
                        currentState.getRestartData().addFragment(currentLine, line2);
                        br.readLine(); //Consume velocity
                        currentState.getRestartData().addVelocity(br.readLine());
                        br.readLine(); //Consume emptyLine;
                    }
                    //Begin ENERGY COMPONENTS (ATOMIC UNITS)
                    br.readLine(); //Consume ENERGY COMPONENTS (ATOMIC UNITS)
                    br.readLine(); //Consume emptyLine

                    double eEnergy = 0.0;
                    double pEnergy = 0.0;
                    double dEnergy = 0.0;
                    double xrEnergy = 0.0;
                    double pcEnergy = 0.0;
                    double cpEnergy = 0.0;
                    double totalEnergy = 0.0;
                    double energyChange = 0.0;
                    double rmsGradient = 0.0;
                    double maxGradient = 0.0;
                    while (true) {
                        boolean finished = false;
                        currentLine = br.readLine();
                        String[] energyStringArray = getTokens(currentLine);
                        switch (energyStringArray[0]) {
                            case "ELECTROSTATIC":
                                eEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "POLARIZATION":
                                pEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "DISPERSION":
                                dEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "EXCHANGE":
                                xrEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "POINT":
                                pcEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "CHARGE":
                                cpEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "TOTAL":
                                totalEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "Kinetic":
                                currentState.setKineticEnergy(Double.parseDouble(energyStringArray[2]));
                                break;
                            case "INVARIANT":
                                currentState.setInvariant(Double.parseDouble(energyStringArray[1]));
                                break;
                            case "TEMPERATURE":
                                currentState.setTemperature(Double.parseDouble(energyStringArray[2]));
                                finished = true;
                                break;
                            default:
                                continue;
                        }
                        if (finished) {
                            currentState.setEnergyComponents(eEnergy, pEnergy, dEnergy, xrEnergy, pcEnergy, cpEnergy,
                                    totalEnergy, energyChange, rmsGradient, maxGradient);
                            states.add(currentState);
                            br.readLine(); //Consume emptyLine
                            br.readLine(); //Consume emptyLine
                            break;
                        }
                    }
                    break;
            }
        } while ((currentLine = br.readLine()) != null);
    }

    private void parseEnergyMinimizationJob(BufferedReader br, String currentLine) throws IOException {
        do {
            if (currentLine.equals("")) continue;
            String keyword = currentLine.trim().split(" ")[0];

            switch (keyword) {
                case "INITIAL": //Starting state
                case "STATE": //Intermediate state
                case "FINAL": //Final state
                    br.readLine(); //Consume empty line
                    br.readLine(); //Consume GEOMETRY(ANGSTROMS)
                    br.readLine(); //Consume empty line
                    State currentState = new StaticState();
                    while (!(currentLine = br.readLine()).equals("")) {
                        String[] atomLineArray = getTokens(currentLine);
                        String atomID = atomLineArray[0];
                        double x = Double.parseDouble(atomLineArray[1]);
                        double y = Double.parseDouble(atomLineArray[2]);
                        double z = Double.parseDouble(atomLineArray[3]);
                        currentState.geometry.addAtom(atomID, x, y, z);
                    }
                    while (br.readLine().equals(""));
                    //Begin RESTART DATA
                    br.readLine(); //Consume RESTART DATA
//                    br.readLine(); //Consume empty line
                    while (!(currentLine = br.readLine()).equals("")) {
                        //BufferedReader should be focused on the first line of the fragment
                        String line2 = br.readLine();
                        currentState.restartData.addFragment(currentLine, line2);
                        br.readLine(); //Consume emptyLine;
                    }
                    //Begin ENERGY COMPONENTS (ATOMIC UNITS)
                    br.readLine(); //Consume ENERGY COMPONENTS (ATOMIC UNITS)
                    br.readLine(); //Consume emptyLine

                    double eEnergy = 0.0;
                    double pEnergy = 0.0;
                    double dEnergy = 0.0;
                    double xrEnergy = 0.0;
                    double pcEnergy = 0.0;
                    double cpEnergy = 0.0;
                    double totalEnergy = 0.0;
                    double energyChange = 0.0;
                    double rmsGradient = 0.0;
                    double maxGradient = 0.0;
                    while (true) {
                        boolean finished = false;
                        currentLine = br.readLine();
                        String[] energyStringArray = getTokens(currentLine);
                        switch (energyStringArray[0]) {
                            case "ELECTROSTATIC":
                                eEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "POLARIZATION":
                                pEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "DISPERSION":
                                dEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "EXCHANGE":
                                xrEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "POINT":
                                pcEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "CHARGE":
                                cpEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "TOTAL":
                                totalEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "ENERGY":
                                energyChange = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "RMS":
                                rmsGradient = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "MAXIMUM":
                                maxGradient = Double.parseDouble(energyStringArray[2]);
                                finished = true;
                                break;
                            default:
                                continue;
                        }
                        if (finished) {
                            currentState.setEnergyComponents(eEnergy, pEnergy, dEnergy, xrEnergy, pcEnergy, cpEnergy,
                                    totalEnergy, energyChange, rmsGradient, maxGradient);
                            states.add(currentState);
                            br.readLine(); //Consume emptyLine
                            br.readLine(); //Consume emptyLine
                            break;
                        }
                    }
                    break;
            }
        } while ((currentLine = br.readLine()) != null);
    }

    private void parseSinglePointEnergyJob(BufferedReader br, String currentLine) throws IOException {
        do {
            if (currentLine.equals("")) continue;
            String keyword = currentLine.trim().split(" ")[0];
            State currentState = new StaticState();

            switch (keyword) {
                case "GEOMETRY": //Starting state
                    br.readLine(); //Consume empty line
                    while (!(currentLine = br.readLine()).equals("")) {
                        String[] atomLineArray = getTokens(currentLine);
                        String atomID = atomLineArray[0];
                        double x = Double.parseDouble(atomLineArray[1]);
                        double y = Double.parseDouble(atomLineArray[2]);
                        double z = Double.parseDouble(atomLineArray[3]);
                        currentState.geometry.addAtom(atomID, x, y, z);
                    }
                case "ENERGY":
                    double eEnergy = 0.0;
                    double pEnergy = 0.0;
                    double dEnergy = 0.0;
                    double xrEnergy = 0.0;
                    double pcEnergy = 0.0;
                    double cpEnergy = 0.0;
                    double totalEnergy = 0.0;
                    double energyChange = 0.0;
                    double rmsGradient = 0.0;
                    double maxGradient = 0.0;
                    while (true) {
                        boolean finished = false;
                        currentLine = br.readLine();
                        String[] energyStringArray = getTokens(currentLine);
                        switch (energyStringArray[0]) {
                            case "ELECTROSTATIC":
                                eEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "POLARIZATION":
                                pEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "DISPERSION":
                                dEnergy = Double.parseDouble(energyStringArray[2]);
                                break;
                            case "EXCHANGE":
                                xrEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "POINT":
                                pcEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "CHARGE":
                                cpEnergy = Double.parseDouble(energyStringArray[3]);
                                break;
                            case "TOTAL":
                                totalEnergy = Double.parseDouble(energyStringArray[2]);
                                finished = true;
                                break;
                            default:
                                continue;
                        }
                        if (finished) {
                            currentState.setEnergyComponents(eEnergy, pEnergy, dEnergy, xrEnergy, pcEnergy, cpEnergy,
                                    totalEnergy, energyChange, rmsGradient, maxGradient);
                            states.add(currentState);
                            break;
                        }
                    }
                    break;
            }
        } while ((currentLine = br.readLine()) != null);
    }

    public String[] getTokens(String line){
        return line.trim().split("[ ]+");
    }

    public void viewState(JmolMainPanel jmolViewer, int index) {
        State state = states.get(index);
        State.Geometry geometry = state.getGeometry();
        String finalOut = "";
        File tempOutFile = new File("testTemp.xyz");
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(tempOutFile));
        } catch (IOException e){
            e.printStackTrace();
        }
        for(State.Geometry.Atom atom : geometry.getAtoms()){
            finalOut += String.format("%s %s\n",
                    atom.atomID.substring(1).replaceAll("[0-9]", ""),
                    String.format("%f %f %f", atom.x, atom.y, atom.z));
        }
        finalOut = geometry.getAtoms().size() + "\n\n" + finalOut;
        if (bufferedWriter != null) {
            try {
                bufferedWriter.write(finalOut);
                bufferedWriter.close();
                jmolViewer.removeAll();
                jmolViewer.openFile(tempOutFile);
                tempOutFile.delete();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }


}
