package org.vmol.app.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.vmol.app.Main;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import ch.ethz.ssh2.StreamGobbler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class JobManager implements Runnable {
    private String username;
    private String password;
    private String hostname;
    private String jobID;
    private String title;
    private String date;
    private String status;
    private String type;
    private Connection conn;

    public JobManager(String username, String password, String hostname, String jobID, String title, String date, String status, String type) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.jobID = jobID;
        this.title = title;
        this.date = date;
        this.status = status;
        this.type = type;
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
    
    public JobManager(Connection conn) {
        this.conn = conn;
    }
    
    public JobManager() {
        
    }
    
    /*
     * Check whether the job is DONE or still in QUEUE
    */
    public boolean checkStatus(String jobID) throws IOException {
        boolean jobIsDone = false;
        
        Connection conn = new Connection(hostname);
        conn.connect();
        boolean isAuthenticated = conn.authenticateWithPassword(username, password);
        if (!isAuthenticated)
            throw new IOException("Authentication failed.");
        
        SCPClient scp = conn.createSCPClient();        
        SCPInputStream scpos = null;
        try {
            if(this.type != null){
                if(this.type.equals("LIBEFP")) {
                    scpos = scp.get("iSpiClient/Libefp/output/output_"+jobID);
                    scpos.close();
                    jobIsDone = true;
                } else if(this.type.equals("GAMESS")) {
                    System.out.println("iSpiClient/Gamess/output/gamess_"+jobID+".efp");
                    scpos = scp.get("iSpiClient/Gamess/output/gamess_"+jobID+".efp");
                    scpos.close();
                    jobIsDone = true;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("Job is running!");
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
        (new Thread(new JobManager(this.username, this.password, this.hostname, this.jobID, this.title, this.date, this.status, this.type))).start();
    }
    
    /*
     * run thread for watch job status
     */
    public void run() {
        try {
            boolean jobIsDone = false;
            do {
                Thread.sleep(3000);
                System.out.println("polling");
                jobIsDone = checkStatus(this.jobID);
                if(jobIsDone){
                    //update database
                    System.out.println("job finished...");
                    updateDBStatus(this.jobID, this.title, this.date, "DONE", this.type);
                    notify(this.title, this.type);
                    if(this.type.equals("GAMESS")){
                        //update the database with this efp file
                        String efp_file = getRemoteVmolOutput(this.jobID, this.type);
                        sendEFPFile(efp_file);
                    }
                }
            } while(!jobIsDone);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /*
     * Send notification when job finishes
     */
    private void notify(String title, String type) {
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Alert alert = new Alert(AlertType.CONFIRMATION);
                String msg = "Your job:" + title + " has finished.";
                
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
        query+= "$ENDALL$";
        
        //Socket client = new Socket(serverName, port);
        iSpiEFPServer iSpiServer = new iSpiEFPServer();
        Socket client = iSpiServer.connect(serverName, port);
        if(client == null) {
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
    public ArrayList<String []> queryDatabaseforJobHistory(String type) throws UnknownHostException, IOException {
        String serverName = Main.iSpiEFP_SERVER;
        int port = Main.iSpiEFP_PORT;
        
        //send over job data to database
        String query = "Check2";
        query += "$END$";
        query += username + "  " + hostname + "  " + type;
        query+= "$ENDALL$";
        
        //Socket client;
        //client = new Socket(serverName, port);
        iSpiEFPServer iSpiServer = new iSpiEFPServer();
        Socket client = iSpiServer.connect(serverName, port);
        if(client == null) {
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
        while (( i = in.read())!= -1) {
            c = (char)i;
            sb.append(c);
        }
            
        String reply = sb.toString();
        System.out.println("Database Response:" + reply);
        
        client.close();
        
        ArrayList<String []> jobHistory = parseDBJobResponse(reply);
        return jobHistory;
    }
    
    
    /*
     * Parse database job history into an array
     */
    private ArrayList<String []> parseDBJobResponse(String reply) {
        ArrayList<String []> result = new ArrayList<String []>();
        
        String[] content = reply.split("\\$NEXT\\$"); 
        int n = content.length;
        for(String record : content) {
            String [] fields = new String [n];
            fields = record.split("\\s+");
            if(fields[0].length() != 0)
                result.add(fields);
        }
        return result;
    }
    
    /*
     * check status for an multitude of jobs, if status is changed, update database
     */
    public ArrayList<String []> checkJobStatus(ArrayList<String []> jobHistory) throws IOException {
        for (String [] line : jobHistory) {
            String job_id = line[0];
            String title = line[1];
            String date = line[2];
            String status = line[3];
           
            if(status.equals("QUEUE")) {
               // boolean done = askRemoteServerJobStatus(job_id, date);
                boolean done = checkStatus(date);
                //boolean done = askR
                if(done) {
                    System.out.println("Job:"+job_id + " done. updating database");
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
        Connection conn = new Connection(this.hostname);
        conn.connect();
        
        boolean isAuthenticated = conn.authenticateWithPassword(this.username, this.password);
        if (!isAuthenticated)
            throw new IOException("Authentication failed.");
        
        
        String path = new String();
        if(type.equals("LIBEFP")) {
            path = "iSpiClient/Libefp/output/output_"+job_stamp;
        } else if(type.equals("GAMESS")) {
            path = "iSpiClient/Gamess/output/gamess_"+job_stamp+".efp";
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
            sb.append(line+"\n");
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
    
    //update database with new efp file
    private void  sendEFPFile(String efp_file){
        String serverName = Main.iSpiEFP_SERVER;
        int port = Main.iSpiEFP_PORT;
      
        String payload = "EFP_FILE";
        payload += "$END$";
        payload += efp_file;
        payload += "$ENDALL$";
        
        //Socket client = new Socket(serverName, port);
        iSpiEFPServer iSpiServer = new iSpiEFPServer();
        Socket client = iSpiServer.connect(serverName, port);
        if(client == null) {
            return;
        }
        OutputStream outToServer;
        try {
            outToServer = client.getOutputStream();
            outToServer.write(payload.getBytes("UTF-8"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //DataOutputStream out = new DataOutputStream(outToServer);
        
    }

}
