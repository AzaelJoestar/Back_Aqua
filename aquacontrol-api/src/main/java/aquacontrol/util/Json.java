package aquacontrol.util;

import com.google.gson.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public final class Json {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new FechaAdapter())
            .serializeNulls()
            .create();

    private Json() {}

    public static Gson gson() {
        return GSON;
    }

    public static String leerCuerpo(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String linea;
            while ((linea = r.readLine()) != null) sb.append(linea);
        }
        if (sb.toString().isBlank()) throw new ApiException(400, "El cuerpo de la petición está vacío");
        return sb.toString();
    }

    public static <T> T desde(String json, Class<T> clase) {
        T r = GSON.fromJson(json, clase);
        if (r == null) throw new ApiException(400, "Cuerpo JSON inválido");
        return r;
    }

    public static JsonObject objeto(String json) {
        JsonElement e = JsonParser.parseString(json);
        if (!e.isJsonObject()) throw new ApiException(400, "Se esperaba un objeto JSON");
        return e.getAsJsonObject();
    }

    /** Acepta "2026-10-01T14:40", "2026-10-01T20:40:00.000Z" (toISOString de JS) y "2026-10-01". */
    public static LocalDateTime fecha(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e1) {
            try {
                return OffsetDateTime.parse(s).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                return LocalDate.parse(s).atStartOfDay();
            }
        }
    }

    private static class FechaAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime v, Type t, JsonSerializationContext c) {
            return new JsonPrimitive(v.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement j, Type t, JsonDeserializationContext c) {
            return j.isJsonNull() ? null : fecha(j.getAsString());
        }
    }
}
