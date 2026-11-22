package dev.mimi.datapackloader;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackSource;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Datapack registration happens in {@link DatapackLoaderBootstrap}, because
 * the discovery lifecycle event fires before plugins are enabled.
 * This class adds {@code /datapackloader reload} to pick up changes at runtime.
 */
public final class DatapackLoaderPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Datapacks are loaded from " + getDataPath().resolve(DatapackLoaderBootstrap.DIR_NAME));

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register(
                Commands.literal("datapackloader")
                    .requires(src -> src.getSender().hasPermission("datapackloader.reload"))
                    .then(Commands.literal("reload").executes(ctx -> {
                        int enabled = reloadPacks();
                        ctx.getSource().getSender().sendMessage(Component.text(
                            "Datapacks reloaded (" + DatapackLoaderBootstrap.LOADED_IDS.size()
                                + " found, " + enabled + " newly enabled).", NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    }))
                    .build(),
                "Reload datapacks from the plugin folder",
                java.util.List.of("dpl")));
    }

    /** Rescans the folder, enables newly found packs and reloads server data. Returns newly enabled count. */
    private int reloadPacks() {
        var manager = Bukkit.getDatapackManager();
        manager.refreshPacks(); // re-fires DATAPACK_DISCOVERY -> rescans the plugin folder

        int enabled = 0;
        for (Datapack pack : manager.getPacks()) {
            if (pack.getSource() != DatapackSource.PLUGIN || pack.isEnabled()) continue;
            String name = pack.getName();
            boolean ours = DatapackLoaderBootstrap.LOADED_IDS.stream()
                .anyMatch(id -> name.equals(id) || name.endsWith("/" + id) || name.endsWith(":" + id));
            if (ours) {
                pack.setEnabled(true);
                enabled++;
            }
        }
        Bukkit.reloadData(); // same as /minecraft:reload
        return enabled;
    }
}
