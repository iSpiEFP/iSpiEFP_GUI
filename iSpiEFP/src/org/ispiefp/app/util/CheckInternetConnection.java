package org.ispiefp.app.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CheckInternetConnection {

    public static boolean checkInternetConnection(){
        try {
            URL persistentURL = new URL("http://www.google.com");
            URLConnection connection = persistentURL.openConnection();
            connection.connect();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
