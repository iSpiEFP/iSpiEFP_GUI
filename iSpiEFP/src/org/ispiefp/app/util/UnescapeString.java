package org.ispiefp.app.util;

public class UnescapeString {

    public static String unescapeSlashes(String s){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '\\') sb.append("\\\\");
            else sb.append(s.charAt(i));
        }
        return sb.toString();
    }
}
