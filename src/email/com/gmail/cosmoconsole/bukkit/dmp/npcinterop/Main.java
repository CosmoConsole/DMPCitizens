
package email.com.gmail.cosmoconsole.bukkit.dmp.npcinterop;

import org.bukkit.plugin.java.*;

import email.com.gmail.cosmoconsole.bukkit.deathmsg.DMPReloadEvent;
import email.com.gmail.cosmoconsole.bukkit.deathmsg.DeathMessageCustomEvent;
import email.com.gmail.cosmoconsole.bukkit.deathmsg.DeathMessagesPrime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.*;
import org.bukkit.event.*;

public class Main extends JavaPlugin implements Listener
{
    public final int CONFIG_VERSION = 1;
    DeathMessagesPrime dmp;
    FileConfiguration config;
    boolean generalShow; // show-npc-messages
    ConfigurationSection overrides;

    public Main() {
        this.config = null;
        this.generalShow = false;
    }
    
    public void onEnable() {
        dmp = (DeathMessagesPrime) getServer().getPluginManager().getPlugin("DeathMessagesPrime");
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.loadConfig();
    }

    @EventHandler
    public void reloadConfig(DMPReloadEvent e) {
        this.loadConfig();
    }
    
    @EventHandler
    public void preBroadcast(DeathMessageCustomEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) {
            String w = e.getWorld().getName();
            boolean display = generalShow;
            if (overrides != null && overrides.contains(w)) {
                display = overrides.getBoolean(w);
            }
            if (!display) {
                e.setCancelled(true);
            }
        }
    }
    
    private void loadConfig() {
        this.config = this.getConfig();
        try {
            this.config.load(new File(this.getDataFolder(), "config.yml"));
            if (!this.config.contains("config-version")) {
                throw new Exception();
            }
            if (this.config.getInt("config-version") < CONFIG_VERSION) {
                throw new ConfigTooOldException();
            }
        }
        catch (FileNotFoundException e6) {
            this.getLogger().info("Extracting default config.");
            this.saveResource("config.yml", true);
            try {
                this.config.load(new File(this.getDataFolder(), "config.yml"));
            }
            catch (IOException | InvalidConfigurationException ex3) {
                ex3.printStackTrace();
                this.getLogger().severe("The JAR config is broken, disabling");
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                this.setEnabled(false);
            }
        }
        catch (ConfigTooOldException e3) {
            this.getLogger().warning("!!! WARNING !!! Your configuration is old. There may be new features or some config behavior might have changed, so it is advised to regenerate your config when possible!");
        }
        catch (Exception e4) {
            e4.printStackTrace();
            this.getLogger().severe("Configuration is invalid. Re-extracting it.");
            final boolean success = !new File(this.getDataFolder(), "config.yml").isFile() || new File(this.getDataFolder(), "config.yml").renameTo(new File(this.getDataFolder(), "config.yml.broken" + new Date().getTime()));
            if (!success) {
                this.getLogger().severe("Cannot rename the broken config, disabling");
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                this.setEnabled(false);
            }
            this.saveResource("config.yml", true);
            try {
                this.config.load(new File(this.getDataFolder(), "config.yml"));
            }
            catch (IOException | InvalidConfigurationException ex4) {
                ex4.printStackTrace();
                this.getLogger().severe("The JAR config is broken, disabling");
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                this.setEnabled(false);
            }
        }
        this.generalShow = config.getBoolean("show-npc-messages", false);
        this.overrides = config.getConfigurationSection("world-overrides");
    }
}

