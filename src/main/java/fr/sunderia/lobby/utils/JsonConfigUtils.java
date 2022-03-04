package fr.sunderia.lobby.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

public class JsonConfigUtils {

    private final File file;
    private final Gson gson;

    public record TypeAdapter(Class<?> clazz, Object instance) {}

    public JsonConfigUtils(File file, TypeAdapter... adapters) throws IOException {
        this.file = file;
        if(!file.exists()) {
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            Files.write(file.toPath(), "{}".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }
        var builder = new GsonBuilder().setPrettyPrinting().serializeNulls().enableComplexMapKeySerialization();
        for (TypeAdapter adapter : adapters) {
            builder.registerTypeAdapter(adapter.clazz, adapter.instance);
        }
        gson = builder.create();
    }

    public JsonElement readConfig() throws IOException {
        return readConfig(JsonElement.class);
    }

    public <T> T readConfig(Class<T> clazz) throws IOException {
        return gson.fromJson(Files.newBufferedReader(file.toPath()), clazz);
    }

    public <T> T readConfig(Type type) throws IOException {
        return gson.fromJson(Files.newBufferedReader(file.toPath()), type);
    }

    public void writeConfig(Object json) throws IOException {
        writeConfig(json, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void writeConfig(Object json, OpenOption... options) throws IOException {
        Files.write(file.toPath(), gson.toJson(json).getBytes(), options);
    }

    public File getFile() {
        return file;
    }

    public Gson getGson() {
        return gson;
    }
}
