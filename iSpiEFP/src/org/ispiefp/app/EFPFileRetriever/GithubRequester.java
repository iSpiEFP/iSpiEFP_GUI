package org.ispiefp.app.EFPFileRetriever;

import org.ispiefp.app.util.CheckInternetConnection;

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
    private final String GITHUB_FRAGMENT_REPO = "https://github.com/iSpiEFP/EFP_parameters/";   /* Prefix to URL    */
    private String fileName = null;                             /* Filename to append to the prefix to complete URL */
    private URL gitURL = null;                                  /* URL created w/ the conjunction of above Strings  */
    private HttpURLConnection gitConnection = null;             /* Connection opened using the above URL            */
    private Map<String, List<String>> gitHeaders = null;        /* Used for examining headers like 404              */
    private InputStream gitStream = null;                       /* Stream obtained from the HTTP connection         */
    private String fileContents = null;                         /* The string obtained from the above stream        */

    public GithubRequester(String fileName){
        fileName = fileName;
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
            Writer writer = new StringWriter();
            char[] buffer = new char[4096];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(gitStream, "UTF-8"));
                int counter;
                while ((counter = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, counter);
                }
            } catch (Exception e) {
                System.err.println("Error occurred while reading from stream");
                e.printStackTrace();
                return "null";
            } finally {
                try {
                    gitStream.close();
                } catch (IOException e) {
                    System.err.println("IOException while closing stream");
                    e.printStackTrace();
                }

            }
            return writer.toString();
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
                PrintWriter printWriter = new PrintWriter(returnFile);
                printWriter.print(getFileContents());
            } catch (IOException e) {
                System.err.println("Unable to create a temp file to copy the EFP file into");
                e.printStackTrace();
            }
        } else{
            System.err.println("Not connected to the internet");
            //todo add an error pop-up showing not connected to the internet
        }
        return returnFile;
    }

}
