package org.ispiefp.app.util;
import org.ispiefp.app.EFPFileRetriever.GithubRequester;

import java.io.*;

public class CheckUpdates {
    private String[] versions;

    public CheckUpdates() {
        this.versions = this.accessVersions();
    }

    public String[] getVersions() { return this.versions; }

    /**
     * Get the user's version number from iSpiEFP_GUI/iSpiEFP/rescoures/userVersion.txt locally
     * and the most current version number from EFP_Parameters/currentVersion.txt on Github
     *
     * @return A String array of size 2, where the first index is the user's version and
     * the second index is the most current version
     */

    private String[] accessVersions() {
        String[] versions = new String[2];

        try {
            File file = new File("iSpiEFP/resources/userVersion.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            versions[0] = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GithubRequester githubRequester = new GithubRequester("currentVersion.txt");
        versions[1] = githubRequester.getFileContents();
        versions[1] = versions[1].substring(0, versions[1].length() - 2);
        return versions;
    }
}
