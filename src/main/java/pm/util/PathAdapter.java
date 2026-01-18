package pm.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gson Adapter for serializing/deserializing Path objects.
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class PathAdapter extends TypeAdapter<Path> {

    /**
     * Writes the Path object to JSON as a String.
     *
     * @param out the JSON writer
     * @param value the Path to serialize
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(JsonWriter out, Path value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    /**
     * Reads a String from JSON and converts it back into a Path object.
     *
     * @param in the JSON reader
     * @return the deserialized Path
     * @throws IOException if an I/O error occurs
     */
    @Override
    public Path read(JsonReader in) throws IOException {
        String pathString = in.nextString();
        return Paths.get(pathString);
    }
}