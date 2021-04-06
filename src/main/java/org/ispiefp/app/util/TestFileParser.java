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

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFileParser {
    private final String testFileSettingsPath = this.getClass().getResource("/testFileSettings.config").getPath();
    private File settingsFile;
    private Settings settings;

    class Settings {
        private String hostname;
        private String username;
        private String sshKeyPath;
        private String pythonPath;

        Settings() {
            this.hostname = "xxx";
            this.username = "xxx";
            this.sshKeyPath = "xxx";
            this.pythonPath = "xxx";
        }

        Settings(String hostname, String username, String sshKeyPath, String pythonPath) {
            this.hostname = hostname;
            this.username = username;
            this.sshKeyPath = sshKeyPath;
            this.pythonPath = pythonPath;
        }
    }

    public TestFileParser() {
        settingsFile = new File(testFileSettingsPath);
        Gson gson = new Gson();
        String settingsString = null;
        try {
            settingsString = new String(Files.readAllBytes(Paths.get(testFileSettingsPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        settings = gson.fromJson(settingsString, Settings.class);
        System.out.println(gson.toJson(settings));
    }

    public boolean fileExists() {
        return settingsFile.exists();
    }

    public String getHostname() {
        return settings.hostname;
    }

    public String getUsername() {
        return settings.username;
    }

    public String getSshKeyPath() {
        return settings.sshKeyPath;
    }

    public String getPythonPath() {
        return settings.pythonPath;
    }
}
