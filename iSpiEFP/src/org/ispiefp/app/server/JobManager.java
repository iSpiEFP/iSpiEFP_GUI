package org.ispiefp.app.server;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import ch.ethz.ssh2.StreamGobbler;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.ispiefp.app.Main;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Job Manager watches jobs, updates job status, inputs job data to database, and notifys user of job completion.
 * Job Manager also fetches finished job data from remote
 */
public class JobManager implements Runnable {
    private String username;
    private String password;
    private String hostname;
    private String jobID;
    private String title;
    private String date;
    private String status;
    private String type;
    private String outputFilename;
    private String stdoutputFilename;
    private String localWorkingDirectory;
    private String remoteWorkingDirectory;
    private ServerInfo server;
    private transient String keyPassword;
    private transient org.ispiefp.app.util.Connection conn;

    public JobManager(String username, String password, String hostname, String localWorkingDirectory, String jobID, String title,
                      String date, String status, String type) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.title = title;
        this.localWorkingDirectory = localWorkingDirectory;
        this.jobID = jobID;
        this.date = date;
        this.status = status;
        this.type = type;
        if (type.equalsIgnoreCase("LIBEFP")){
            remoteWorkingDirectory = "~/iSpiClient/Libefp/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            stdoutputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        } else if (type.equalsIgnoreCase("GAMESS")) {
            remoteWorkingDirectory = "~/iSpiClient/Gamess/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            stdoutputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        }
    }

    public JobManager(ServerInfo si, String localWorkingDirectory, String jobID, String title,
                      String date, String status, String type, String keyPassword){
        server = si;
        this.localWorkingDirectory = localWorkingDirectory;
        this.jobID = jobID;
        this.title = title;
        this.date = date;
        this.status = status;
        this.type = type;
        if (type.equalsIgnoreCase("LIBEFP")){
            remoteWorkingDirectory = "~/iSpiClient/Libefp/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            stdoutputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        } else if (type.equalsIgnoreCase("GAMESS")) {
            remoteWorkingDirectory = "~/iSpiClient/Gamess/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            stdoutputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        }
        this.keyPassword = keyPassword;
    }

    public JobManager(String username, String password, String hostname, String type) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.type = type;
    }

    public JobManager(String username, String password, String hostname) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
    }

    public JobManager() {

    }

    /*
     * Check whether the job is DONE or still in QUEUE
     */
    public boolean checkStatus(String jobID) throws IOException {
        boolean jobIsDone = false;
        org.ispiefp.app.util.Connection conn = new org.ispiefp.app.util.Connection(server, keyPassword);
        conn.connect();

        SCPClient scp = conn.createSCPClient();
        SCPInputStream scpos = null;
        try {
            if (this.type != null) {
                if (this.type.equals("LIBEFP")) {
                    scpos = scp.get(remoteWorkingDirectory + "output/" + title.replace(" ", "_") + ".out");
                    scpos.close();
                    jobIsDone = true;
                } else if (this.type.equals("GAMESS")) {
                    scpos = scp.get(remoteWorkingDirectory + "output/" + title.replace(" ", "_") + ".out");
                    scpos.close();
                    jobIsDone = true;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            System.out.printf("Job: %s is running!%n", jobID);
            System.out.println(e.getMessage());
        }
        conn.close();
        return jobIsDone;
    }

    public String generateJobID() {
        UUID jobID = UUID.randomUUID();
        /*
         * Should maybe check whether this ID exists already or not in the DATABASE
         * However the chance it does is REALLY REALLY REALLY low, also considering the
         * query has a time stamp as well, this is virtually unneccessary
         */
        return jobID.toString();
    }


    /*
     * Start a thread that watches a specific job and send a notification when the jobs completes
     */
    public void watchJobStatus() {
        (new Thread(new JobManager(this.server, this.localWorkingDirectory,
                this.jobID, this.title, this.date, this.status, this.type, this.keyPassword))).start();
    }

    /*
     * run thread for watch job status
     */
    public void run() {
        try {
            boolean jobIsDone = false;
            do {
                Thread.sleep(30000);
                System.out.println("polling");
                jobIsDone = checkStatus(this.jobID);
                if (jobIsDone) {
                    //update database
                    System.out.println("job finished...");
//                    updateDBStatus(this.jobID, this.title, this.date, "DONE", this.type);
                    notify(this.title, this.type);
                    if (this.type.equals("GAMESS")) {
                        //update the database with this efp file
                        String efp_file = getRemoteVmolOutput(this.jobID, this.type);
                        sendEFPFile(efp_file);
                    }
                }
            } while (!jobIsDone);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Job: " + this.jobID + " is still running");
        }
    }


    /*
     * Send notification when job finishes
     */
    private void notify(String title, String type) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Alert alert = new Alert(AlertType.CONFIRMATION);
                String msg = "Your job: " + jobID + " has finished.";

                alert.setTitle(type + " Job Submission");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                Optional<ButtonType> result = alert.showAndWait();

            }
        });
    }

    /*
     * Update Database Status Entry that the job is DONE
     */
    //TODO this should query with the hostname 
    public void updateDBStatus(String job_id, String title, String date, String status, String type) throws UnknownHostException, IOException {
        String serverName = Main.iSpiEFP_SERVER;
        int port = Main.iSpiEFP_PORT;


        //send over job data to database
        String query = "Update_Status";
        query += "$END$";
        query += job_id + "  " + title + "  " + date + "  " + status + "  " + type;
        query += "$ENDALL$";

        //Socket client = new Socket(serverName, port);
        iSpiEFPServer iSpiServer = new iSpiEFPServer();
        Socket client = iSpiServer.connect(serverName, port);
        if (client == null) {
            return;
        }
        OutputStream outToServer = client.getOutputStream();
        System.out.println(query);
        outToServer.write(query.getBytes("UTF-8"));
        client.close();
    }

    /*
     * Ask for a list of job histories from the user from particular server
     */
    public ArrayList<String[]> queryDatabaseforJobHistory(String type) throws UnknownHostException, IOException {
        String serverName = Main.iSpiEFP_SERVER;
        int port = Main.iSpiEFP_PORT;

        //send over job data to database
        String query = "Check";
        query += "$END$";
        query += username + "  " + hostname + "  " + type;
        query += "$ENDALL$";

        //Socket client;
        //client = new Socket(serverName, port);
        iSpiEFPServer iSpiServer = new iSpiEFPServer();
        Socket client = iSpiServer.connect(serverName, port);
        if (client == null) {
            return null;
        }
        OutputStream outToServer = client.getOutputStream();
        //DataOutputStream out = new DataOutputStream(outToServer);

        System.out.println(query);
        outToServer.write(query.getBytes("UTF-8"));

        InputStream inFromServer = client.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        StringBuilder sb = new StringBuilder();
        int i;
        char c;
        boolean start = false;
        while ((i = in.read()) != -1) {
            c = (char) i;
            sb.append(c);
        }

        String reply = sb.toString();
        System.out.println("Database Response:" + reply);

        client.close();

        ArrayList<String[]> jobHistory = parseDBJobResponse(reply);
        return jobHistory;
    }


    /*
     * Parse database job history into an array
     */
    private ArrayList<String[]> parseDBJobResponse(String reply) {
        ArrayList<String[]> result = new ArrayList<String[]>();

        String[] content = reply.split("\\$NEXT\\$");
        int n = content.length;
        for (String record : content) {
            String[] fields = new String[n];
            fields = record.split("\\s+");
            if (fields[0].length() != 0)
                result.add(fields);
        }
        return result;
    }

    /*
     * check status for an multitude of jobs, if status is changed, update database
     */
    public ArrayList<String[]> checkJobStatus(ArrayList<String[]> jobHistory) throws IOException {
        for (String[] line : jobHistory) {
            String job_id = line[0];
            String title = line[1];
            String date = line[2];
            String status = line[3];

            if (status.equals("QUEUE")) {
                // boolean done = askRemoteServerJobStatus(job_id, date);
                boolean done = checkStatus(job_id);
                //boolean done = askR
                if (done) {
                    System.out.println("Job:" + job_id + " done. updating database");
                    //updateDBStatus(job_id, title, date);
                    updateDBStatus(job_id, title, date, status, type);
                    line[3] = "DONE";
                }
            }
        }
        return jobHistory;
    }

    /*
     * get output file from a lib efp job
     */
    public String getRemoteVmolOutput(String job_stamp, String type) throws IOException {
        org.ispiefp.app.util.Connection conn = new org.ispiefp.app.util.Connection(this.server, keyPassword);
        conn.connect();
        String path = new String();
        if (type.equals("LIBEFP")) {
            path = "iSpiClient/Libefp/output/output_" + job_stamp;
        } else if (type.equals("GAMESS")) {
            path = "iSpiClient/Gamess/output/gamess_" + job_stamp + ".efp";
        }

        SCPClient scp = conn.createSCPClient();
        SCPInputStream scpos = scp.get(path);
        InputStream stdout = new StreamGobbler(scpos);
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            //System.out.println("gobbler");
            System.out.println(line);
            sb.append(line + "\n");
            //output.add(line);
            //System.out.println(line);
        }
        // in.close();
        //br.close();
        scpos.close();
        conn.close();
        //br.close(); //OVIEN STRANGE RESULTS HERE 

        return sb.toString();
    }

    /*
     * get output file from a lib efp job
     */
    public String getRemoteFile(String filename) throws IOException {
        SCPInputStream scpos = null;
        InputStream stdout = null;
        BufferedReader br = null;
        org.ispiefp.app.util.Connection conn = null;
        StringBuilder sb = new StringBuilder();

        try {
            conn = new org.ispiefp.app.util.Connection(server, keyPassword);
            conn.connect();

            SCPClient scp = conn.createSCPClient();
            scpos = scp.get(filename);
            stdout = new StreamGobbler(scpos);
            br = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                //System.out.println("gobbler");
                System.out.println(line);
                sb.append(line + "\n");
            }
            return sb.toString();

        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            conn.close();
        } finally {
            if (scpos != null) {
                //scpos.close();
            }
            if (stdout != null) {
                stdout.close();
            }
            if (br != null) {
                br.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return sb.toString();
    }

    //update database with new efp file
    private void sendEFPFile(String efp_file) {
        String serverName = Main.iSpiEFP_SERVER;
        int port = Main.iSpiEFP_PORT;

        String payload = "EFP_FILE";
        payload += "$END$";
        payload += efp_file;
        payload += "$ENDALL$";

        //Socket client = new Socket(serverName, port);
        iSpiEFPServer iSpiServer = new iSpiEFPServer();
        Socket client = iSpiServer.connect(serverName, port);
        if (client == null) {
            return;
        }
        OutputStream outToServer;
        try {
            outToServer = client.getOutputStream();
            outToServer.write(payload.getBytes("UTF-8"));
            client.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public org.ispiefp.app.util.Connection getConn() {
        return conn;
    }

    public ServerInfo getServer() {return server;}

    public String getDate() {
        return date;
    }

    public String getHostname() {
        return hostname;
    }

    public String getLocalWorkingDirectory() {
        return localWorkingDirectory;
    }

    public String getJobID() {
        return jobID;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getOutputFilename(){
        return outputFilename;
    }

    public String getStdoutputFilename() {
        return stdoutputFilename;
    }

    public void setConn(org.ispiefp.app.util.Connection conn) {
        this.conn = conn;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setLocalWorkingDirectory(String localWorkingDirectory) {
        this.localWorkingDirectory = localWorkingDirectory;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setOutputFilename(String outputFilename){
        this.outputFilename = outputFilename;
    }

    public void setStdoutputFilename(String stdoutputFilename) {
        this.stdoutputFilename = stdoutputFilename;
    }

    public String toJsonString(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

}
