package com.github.officialdonut.skprotobuf;

import ch.njol.skript.Skript;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

public class SkProtobuf extends JavaPlugin {

    private static SkProtobuf instance;
    private ProtoManager protoManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        protoManager = new ProtoManager(getDataFolder().toPath().resolve("descriptors"));
        protoManager.loadDescriptors();

        try {
            Skript.registerAddon(this).loadClasses("com.github.officialdonut.skprotobuf.elements");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to register Skript elements", e);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            protoManager.loadDescriptors();
            sender.sendMessage("Successfully reloaded SkProtobuf.");
            return true;
        }
        return false;
    }

    public static SkProtobuf getInstance() {
        return instance;
    }

    public ProtoManager getProtoManager() {
        return protoManager;
    }
}
