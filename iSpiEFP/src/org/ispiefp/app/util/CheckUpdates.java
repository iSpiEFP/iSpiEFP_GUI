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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
//            String temp;
//            while (true) {
//                temp = br.readLine();
//                if (temp == null) {
//                    break;
//                }
//                versions[0] += temp;
//            }
            versions[0] = br.readLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GithubRequester githubRequester = new GithubRequester("currentVersion.txt");
        versions[1] = githubRequester.getFileContents();
        versions[1] = versions[1].substring(0, versions[1].length() - 2);
        return versions;
    }
}
