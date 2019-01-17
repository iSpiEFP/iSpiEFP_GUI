package org.vmol.app.database;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jmol.viewer.Viewer;
import org.vmol.app.MainViewController;
import org.vmol.app.util.Atom;
import org.vmol.app.util.PDBParser;

import javafx.scene.control.ListView;

public class DatabaseController2 {
	
	private Viewer jmolViewer;
	private Viewer auxiliaryJmolViewer;
	private ListView<String> auxiliary_list;

	public DatabaseController2(Viewer jmolViewer, Viewer auxiliaryJmolViewer, ListView<String> auxiliary_list) {
		this.jmolViewer = jmolViewer;
		this.auxiliaryJmolViewer = auxiliaryJmolViewer;
		this.auxiliary_list = auxiliary_list;
		//int atom_num = jmolViewer.ms.at.length;
	}
	
	public void run() throws IOException {
		System.out.println("Initializing DB controller!!!!");
		ArrayList<List<DatabaseRecord>> items = new ArrayList<List<DatabaseRecord>>();
		int groups = jmolViewer.ms.at.length;
		//query database
		String dbResponse = queryDatabase(groups);
		
		//process response
		//if there is no response return a sad face :( (need gamess then)
		//if there is response, package the response, put it in directory, and load it up in the auxiliary panel
		processDBresponse(dbResponse);
	}
	
	//query remote database from AWS server, and return response
	private String queryDatabase(int groups) throws IOException {
        String response = null;
	    
	    ArrayList<Atom> pdb;
		
		pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
		
		//legacy server name
		//String serverName = "ec2-18-219-71-66.us-east-2.compute.amazonaws.com";
		//new server name
		String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";
		int port = 8080;
	
		
		System.out.println("Atoms count: " + groups);
	
		for (int x = 0; x < 1; x ++) {
    		ArrayList<DatabaseRecord> drs = new ArrayList<DatabaseRecord>();
    		ArrayList<ArrayList> molecules = new ArrayList<ArrayList>();
    		int index = 1;
    		try {
    			String query = "Query";
    			for (int j = 0; j < groups; j ++) {
    				Atom current_atom = (Atom) pdb.get((Integer) j);
    				if (current_atom.type.matches(".*\\d+.*")) { // atom symbol has digits, treat as charged atom
    					String symbol = current_atom.type;
    					String sign = symbol.substring(symbol.length() - 1);
    					String digits = symbol.replaceAll("\\D+", "");
    					String real_symbol = symbol.substring(0, symbol.length() - 2 - digits.length());
    					query += "$END$" + real_symbol + "  " + current_atom.x + "  " + current_atom.y + "  " + current_atom.z;
    					
    				} else {
    					query += "$END$" + current_atom.type + "  " + current_atom.x + "  " + current_atom.y + "  " + current_atom.z;
    				}
    			}
    			query+="$ENDALL$";
    				
    	        Socket client = new Socket(serverName, port);
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
    	        	if (c == '#')
    	        		start = true;
    	        	if (start == true)
    	        		sb.append(c);
    	        }
    	        //System.out.println(reply);
    	        String reply = sb.toString();
    	        System.out.println("Database Response:" + reply);
    	        response = reply;
    	        
    	        client.close();
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    } 
		}
		return response;
	}
	
	//INPUT: Raw response from Database
	private void processDBresponse(String res) {
	    String reply = res;
	    reply = reply.substring(1);
        
        String[] current_xyzs = reply.split("\\$NEXT\\$");
        System.out.println("Current Files:" + current_xyzs.length + current_xyzs[0]);
        
        if(current_xyzs.length <= 1){
            //no response
        } else {
            //parse response and dump in folders
            for(int i = 1; i < current_xyzs.length; i++) {
                //dump string into tmp file
            }
        }
	}
    	
}
