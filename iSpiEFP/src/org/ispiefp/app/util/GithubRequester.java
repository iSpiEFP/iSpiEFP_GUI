/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

package org.ispiefp.app.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

// This class is responsible for making the request to the Git repository containing all of the EFP files
// I learned how to do this using this tutorial and a lot of the code was obtained from there:
// https://crunchify.com/in-java-how-to-read-github-file-contents-using-httpurlconnection-convert-stream-to-string-utility/
// I did not set it to handle redirections or examine headers because at this point, I don't see a reason to do so as we
// are the ones controlling the URL (by owning the repository)

public class GithubRequester {
    private final String GITHUB_FRAGMENT_REPO = "https://raw.githubusercontent.com/iSpiEFP/EFP_parameters/master/";    /* Prefix to URL    */
    private String fileName = null;                             /* Filename to append to the prefix to complete URL */
    private URL gitURL = null;                                  /* URL created w/ the conjunction of above Strings  */
    private HttpURLConnection gitConnection = null;             /* Connection opened using the above URL            */
    private Map<String, List<String>> gitHeaders = null;        /* Used for examining headers like 404              */
    private InputStream gitStream = null;                       /* Stream obtained from the HTTP connection         */
    private String fileContents = null;                         /* The string obtained from the above stream        */
    private BufferedReader gitReader = null;                    /* The reader which reads from gitStream            */

    public GithubRequester(String fileName){
        this.fileName = fileName;
        try{
            gitURL = new URL(GITHUB_FRAGMENT_REPO + fileName);
        } catch (MalformedURLException e){
            System.err.println("Malformed URL exception");
            e.printStackTrace();
        }
        try{
            gitConnection = (HttpURLConnection) gitURL.openConnection();
            gitHeaders = gitConnection.getHeaderFields();
            gitStream = gitConnection.getInputStream();
        } catch (IOException e){
            System.err.println("IOException upon opening connection");
            e.printStackTrace();
        }
    }

    /**
     * Get the contents of the EFPFile which is currently pointed to in the gitRepository at the URL Link which is
     * created by appending fileName to GITHUB_FRAGMENT_REPO. This will return "null" if the inputStream has not been
     * created or returns an error. Ensure that the GitHubRequester is examining a file before attempting to do this.
     *
     * @return A String containing the contents of an EFP file obtained from Github
     */

    public String getFileContents() {
        if (gitStream == null) {
            System.err.println("Stream never opened");
            return "null";
        } else {
            StringBuilder builder = new StringBuilder();
            String output = "";
            try {
                gitReader = new BufferedReader(new InputStreamReader(gitStream, "UTF-8"));
                String temp;
                while ((temp = gitReader.readLine()) != null) {
                    builder.append(String.format("%s%n",temp));
                }
                output = builder.toString();
            } catch (Exception e) {
                System.err.println("Error occurred while reading from stream");
                e.printStackTrace();
            }
            return output;
        }
    }

    /**
     * Returns a temp file containing a copy of the EFPFile which is currently pointed to in the gitRepository at the
     * URL Link which is created by appending fileName to GITHUB_FRAGMENT_REPO. This will return null if the if the
     * inputStream has not been created or creating the temp file returns an error. Ensure that the GitHubRequester is
     * examining a file before attempting to do this. The temp file will be deleted upon exiting iSpiEFP.
     *
     * @return A temporary file containing the contents of an EFP file obtained from Github or null if there is an error
     * or the stream to obtain the file has not been opened.
     */

    public File getEFPFile(){
        File returnFile = null;
        if (CheckInternetConnection.checkInternetConnection()) {
            try {
                returnFile = File.createTempFile(fileName, ".efp");
                returnFile.deleteOnExit();
                String fileContents = getFileContents();
                System.out.printf("Temp file name is %s%n", returnFile.getName());
                PrintWriter printWriter = new PrintWriter(returnFile);
                printWriter.print(fileContents);
                printWriter.flush();
            } catch (IOException e) {
                System.err.println("Unable to create a temp file to copy the EFP file into");
                e.printStackTrace();
            }
        } else{
            String noInternetWarning = String.format("Unable to retrieve %s from Github because you are not connected to" +
                    "the internet. Connect to the internet and try again.", fileName);

            System.err.println("Not connected to the internet");
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    noInternetWarning,
                    ButtonType.OK);
            alert.showAndWait();
        }
        return returnFile;
    }

    /**
     * Frees system resources associated with buffered reader when no longer needed. Primarily frees file descriptors
     **/
    public void cleanUp(){
        try {
            if (gitReader != null) gitReader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
