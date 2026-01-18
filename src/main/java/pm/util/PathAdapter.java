package pm.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Adapter de Gson para serializar/deserializar objetos Path.
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class PathAdapter extends TypeAdapter<Path> {

    @Override
    public void write(JsonWriter out, Path value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    @Override
    public Path read(JsonReader in) throws IOException {
        String pathString = in.nextString();
        return Paths.get(pathString);
    }
}