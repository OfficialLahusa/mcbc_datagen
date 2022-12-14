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

    public MetaDataWriter() {
        gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    }

    public void writeToFile(MetaData data, String filename) throws IOException {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path metaDir = gameDir.resolve("screenshots/meta");
        Path metaFile = metaDir.resolve(filename);
        String content = gson.toJson(data);
        Files.writeString(
                metaFile,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
