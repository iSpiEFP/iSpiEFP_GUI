package org.ispiefp.app.util;
import java.io.*;

public class CheckUpdates {
    private String updates;

    public CheckUpdates() {
        this.updates = this.updateScript();
    }

    public String getUpdates() { return this.updates; }

    private String updateScript() {
        String s = new String();

        try {
            File file = new File("iSpiEFP/resources/version.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            s += br.readLine();
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(s);
        return s;
        /*String testPythonScriptPath = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL resource = CheckUpdates.class.getResource("/scripts/checkUpdates.py");
            System.out.println("resource = " + resource);
            File file = Paths.get(resource.toURI()).toFile();
            System.out.println("file = " + file);
            testPythonScriptPath = file.getAbsolutePath();
        } catch (URISyntaxException e){
            e.printStackTrace();
        }
        System.out.println(UserPreferences.getPythonPath());
        if (!UserPreferences.pythonPathExists()) return null;
        String commandInput = String.format("%s %s",UserPreferences.getPythonPath(),
                testPythonScriptPath);
        //String[] arr = { testPythonScriptPath, ""};
        try{
            Process p = Runtime.getRuntime().exec(commandInput);   /* The path of the directory to write to */
            /*BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream())); // getInputStream() returns output stream
            String s1 = "";
            String s2 = "";
            while ((s1 = errReader.readLine()) != null || (s2 = outReader.readLine()) != null){
                sb.append(s1);
                sb.append(s2);
            }
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
        System.out.println(sb.toString());
        return sb.toString();*/
            //return null;
    }
}
