package org.ispiefp.app.loginPack;


import ch.ethz.ssh2.crypto.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * AN UNFINISHED CLASS WORK IN PROGRESS THAT HAS YET TO BE IMPLEMENTED
 */
public class Security {

    //And This is why its not safe
    private static String key = "wF^vG=*dL4?@-7%5"; // 128 bit key
    private static String initVector = "8@YyxkjveUU+fjfJ"; // 16 bytes IV

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);


            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            System.out.println("encrypted string: "
                    + (Base64.encode(encrypted)).toString());

            return Base64.encode(encrypted).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decode(encrypted.toCharArray()));

            return new String(original.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}