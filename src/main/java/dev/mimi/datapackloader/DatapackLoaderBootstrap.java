package dev.mimi.datapackloader;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Registers every datapack found in {@code plugins/DatapackLoader/datapacks/}
 * (folders and .zip files) so they are loaded without touching the world directory.
 */
public final class DatapackLoaderBootstrap implements PluginBootstrap {

    public static final String DIR_NAME = "datapacks";

    /** Pack ids registered by the most recent discovery run. */
    static final Set<String> LOADED_IDS = ConcurrentHashMap.newKeySet();

    @Override
    public void bootstrap(BootstrapContext context) {
        Path packsDir = context.getDataDirectory().resolve(DIR_NAME);
        try {
            Files.createDirectories(packsDir);
        } catch (IOException e) {
            context.getLogger().error("Could not create {}", packsDir, e);
            return;
        }

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, event -> {
            var registrar = event.registrar();
            LOADED_IDS.clear();
            try (Stream<Path> files = Files.list(packsDir)) {
                files.sorted().forEach(path -> {
                    String fileName = path.getFileName().toString();
                    boolean isDir = Files.isDirectory(path);
                    boolean isZip = Files.isRegularFile(path) && fileName.toLowerCase(Locale.ROOT).endsWith(".zip");
                    if (!isDir && !isZip) {
                        return;
                    }
                    String name = isZip ? fileName.substring(0, fileName.length() - 4) : fileName;
                    // pack ids may only contain [a-z0-9_.-]
                    String id = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
                    try {
                        var pack = registrar.discoverPack(path, id, configurer -> {
                            configurer.title(Component.text(name));
                            configurer.autoEnableOnServerStart(true);
                        });
                        if (pack == null) {
                            context.getLogger().warn("Failed to load datapack '{}' (missing pack.mcmeta?)", fileName);
                        } else {
                            LOADED_IDS.add(id);
                            context.getLogger().info("Discovered datapack '{}' as '{}'", fileName, id);
                        }
                    } catch (Exception e) {
                        context.getLogger().error("Error loading datapack '{}'", fileName, e);
                    }
                });
            } catch (IOException e) {
                context.getLogger().error("Could not list {}", packsDir, e);
            }
        });
    }
}
