package org.ispiefp.app.libEFP;

import org.ispiefp.app.visualizer.JmolMainPanel;
import org.jmol.api.JmolViewer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class OutputFile {
    private String run_type;
    private String coord;
    private final boolean [] terms = new boolean[4];
    private String elec_damp;
    private String disp_damp;
    private String pol_damp;
    private String pol_driver;
    private boolean enable_ff;
    private boolean enable_multistep;
    private String ff_geometry;
    private String ff_parameters;
    private boolean single_params_file;
    private String efp_params_file;
    private boolean enable_cutoff;
    private int swf_cutoff;
    private long max_steps;
    private int multistep_steps;
    private String fraglib_path;
    private String userlib_path;
    private boolean enable_pbc;
    private String periodic_box;
    private double opt_tol;
    private String gtest_tol;
    private String ref_energy;
    private boolean hess_central;
    private double num_step_dist;
    private double num_step_angle;
    private String ensemble;
    private int time_step;
    private int print_step;
    private boolean velocitize;
    private long temperature;
    private int pressure;
    private long thermostat_tau;
    private long barostat_tau;
    private int ligand;
    private boolean enable_pairwise;
    private boolean print_pbc;
    private ArrayList<State> states;

    class State {
        private Geometry geometry;
        private RestartData restartData;
        private EnergyComponents energyComponents;

        class Geometry {
            private ArrayList<Atom> atoms;

            class Atom {
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

            public void addAtom(String atomID, double x, double y, double z){
                atoms.add(new Atom(atomID, x, y, z));
            }

            public ArrayList<Atom> getAtoms() {
                return atoms;
            }
        }

        class RestartData {
            private ArrayList<Fragment> fragments;

            class Fragment{
                private final String name;
                private final double[] coords = new double[6];

                public Fragment(String nameString, String coordString){
                    name = nameString;
                    String[] coordsStringArray = coordString.split(" ");
                    for(int i = 0; i < 6; i++){
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
            public RestartData() {
                fragments = new ArrayList<>();
            }

            public ArrayList<Fragment> getFragments() {
                return fragments;
            }

            public void addFragment(String nameString, String coordString){
                fragments.add(new Fragment(nameString, coordString));
            }

            @Override
            public String toString() {
                return "RestartData{" +
                        "fragments=" + fragments +
                        '}';
            }
        }

        class EnergyComponents {
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

            public EnergyComponents(double eEnergy, double pEnergy, double dEnergy, double xrEnergy, double pcEnergy,
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

        public void setEnergyComponents(double eEnergy, double pEnergy, double dEnergy, double xrEnergy, double pcEnergy,
                                        double cpEnergy, double totalEnergy, double energyChange, double rmsGradient,
                                        double maxGradient){
            energyComponents = new EnergyComponents(eEnergy, pEnergy, dEnergy, xrEnergy, pcEnergy,
                                                    cpEnergy, totalEnergy, energyChange, rmsGradient, maxGradient);
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

    public int getSwf_cutoff() {
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

    public String getGtest_tol() {
        return gtest_tol;
    }

    public String getRef_energy() {
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

    public int getTime_step() {
        return time_step;
    }

    public int getPrint_step() {
        return print_step;
    }

    public boolean isVelocitize() {
        return velocitize;
    }

    public long getTemperature() {
        return temperature;
    }

    public int getPressure() {
        return pressure;
    }

    public long getThermostat_tau() {
        return thermostat_tau;
    }

    public long getBarostat_tau() {
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
                        swf_cutoff = Integer.parseInt(line1.split(" ")[1]);
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
                        gtest_tol = line1.split(" ")[1];
                        break;
                    case "ref_energy":
                        ref_energy = line1.split(" ")[1];
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
                    case "INITIAL": //Starting state
                    case "STATE": //Intermediate state
                    case "FINAL": //Final state
                        br.readLine(); //Consume empty line
                        br.readLine(); //Consume GEOMETRY(ANGSTROMS)
                        br.readLine(); //Consume empty line
                        State initialState = new State();
                        while (!(line1 = br.readLine()).equals("")) {
                            String[] atomLineArray = line1.split(" ");
                            String atomID = atomLineArray[0];
                            double x = Double.parseDouble(atomLineArray[1]);
                            double y = Double.parseDouble(atomLineArray[2]);
                            double z = Double.parseDouble(atomLineArray[3]);
                            initialState.geometry.addAtom(atomID, x, y, z);
                        }
                        while (br.readLine().equals("")) ;
                        //Begin RESTART DATA
                        br.readLine(); //Consume RESTART DATA
                        br.readLine(); //Consume empty line
                        while (!(line1 = br.readLine()).equals("")) {
                            //BufferedReader should be focused on the first line of the fragment
                            line2 = br.readLine();
                            initialState.restartData.addFragment(line1, line2);
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
                            line1 = br.readLine();
                            String[] energyStringArray = line1.split(" ");
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
                                    xrEnergy = Double.parseDouble(energyStringArray[2]);
                                    break;
                                case "POINT":
                                    pcEnergy = Double.parseDouble(energyStringArray[2]);
                                    break;
                                case "CHARGE":
                                    cpEnergy = Double.parseDouble(energyStringArray[2]);
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
                                    break;
                                default:
                                    continue;
                            }
                            if (finished) {
                                initialState.setEnergyComponents(eEnergy, pEnergy, dEnergy, xrEnergy, pcEnergy, cpEnergy,
                                        totalEnergy, energyChange, rmsGradient, maxGradient);
                                states.add(initialState);
                                br.readLine(); //Consume emptyLine
                                br.readLine(); //Consume emptyLine
                                break;
                            }
                        }
                        break;
                }
            }
        } finally { br.close(); }
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
