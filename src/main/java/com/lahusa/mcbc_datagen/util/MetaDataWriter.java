package com.lahusa.mcbc_datagen.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MetaDataWriter {
    private final Gson gson;
    private final Path metaDir;

    public MetaDataWriter() {
        gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        Path gameDir = FabricLoader.getInstance().getGameDir();
        metaDir = gameDir.resolve("screenshots/meta");
        // Create metadata directory
        try {
            Files.createDirectories(metaDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToFile(MetaData data, String filename) throws IOException {
        Path metaFile = metaDir.resolve(filename);
        String content = gson.toJson(data);
        Files.writeString(
                metaFile,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE
        );
    }
}
