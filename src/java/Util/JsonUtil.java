package Util;

import com.google.gson.Gson;

/**
 * Utilidad JSON unificada.
 * Todas las respuestas usan: { "ok": boolean, "mensaje": "...", ... }
 */
public class JsonUtil {

    private static final Gson gson = new Gson();

    /* ── Escape seguro para strings en JSON manual ── */
    public static String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "")
                    .replace("\t", "\\t");
    }

    /* ── Respuesta estándar: { "ok": bool, "mensaje": "..." } ── */
    public static String ok(String mensaje) {
        return "{\"ok\":true,\"mensaje\":\"" + escapar(mensaje) + "\"}";
    }

    public static String error(String mensaje) {
        return "{\"ok\":false,\"mensaje\":\"" + escapar(mensaje) + "\"}";
    }

    /* ── Serializar cualquier objeto con Gson ── */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}