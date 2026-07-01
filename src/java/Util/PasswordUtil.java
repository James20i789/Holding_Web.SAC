/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author HP
 */
public class PasswordUtil {
     public static String hashSHA256(String textoPlano) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(textoPlano.getBytes("UTF-8"));
 
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
 
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Error al generar el hash de la contraseña", e);
        }
    }
 
    /**
     * Compara una contraseña en texto plano contra un hash almacenado.
     */
    public static boolean verificar(String textoPlano, String hashGuardado) {
        String hashCalculado = hashSHA256(textoPlano);
        return hashCalculado.equalsIgnoreCase(hashGuardado);
    }
}
