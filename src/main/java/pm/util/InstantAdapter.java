package pm.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

/**
 * Gson Adapter for serializing/deserializing Instant objects.
 *
 * Gson cannot serialize Instant directly in Java 17+
 * due to reflection restrictions in the java.base module.
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class InstantAdapter extends TypeAdapter<Instant> {

    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        String timestamp = in.nextString();
        return Instant.parse(timestamp);
    }
}