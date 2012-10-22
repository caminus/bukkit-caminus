package us.camin;

/*
    This file is part of Caminus

    Caminus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Caminus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Caminus.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.util.concurrent.Callable;

import us.camin.api.Server;
import us.camin.api.ServerEvent;
import us.camin.api.BroadcastEvent;

public class Plugin extends JavaPlugin {

	Logger log = Logger.getLogger("Caminus");//Define your logger
    private Server m_api;
    private JoinListener m_listener;
    private MOTDCommand m_motdCommand;
    private VomitCommand m_vomitCommand;
    private ServerEventPoller m_eventPoll;

    public Server api() {
        return m_api;
    }

	public void onDisable() {
		log.info("[Caminus] Plugin disabled");
        m_api = null;
	}

  public void handleEvent(ServerEvent e) {
        if (e instanceof BroadcastEvent) {
            final BroadcastEvent evt = (BroadcastEvent)(e);
            getServer().getScheduler().callSyncMethod(this, new Callable<Void>() {
                public Void call() {
                    getServer().broadcastMessage(evt.message);
                    return null;
                }
            });
        }
        try {
          m_api.notifyEventHandled(e);
        } catch (IOException ex) {
          log.severe("Could not close out event. Duplicates will happen!!!");
        }
  }

	public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();
        m_listener = new JoinListener(this);
        Configuration conf = getConfig();
        conf.addDefault("url", "http://camin.us/api/");
        conf.addDefault("name", "localhost");
        conf.addDefault("secret", "");
        String url = conf.getString("url");
        m_api = new Server(url);
        m_api.setServerName(conf.getString("name"));
        m_api.setServerSecret(conf.getString("secret"));

        if (!m_api.pingAPI()) {
            log.log(Level.SEVERE, "Could not ping API server. Certain features may be disabled.");
        }

        pm.registerEvents(m_listener, this);

        m_eventPoll = new ServerEventPoller(this);

        m_motdCommand = new MOTDCommand(this);
        getCommand("motd").setExecutor(m_motdCommand);
        m_vomitCommand = new VomitCommand(this);
        getCommand("vomit").setExecutor(m_vomitCommand);

        CommandExecutor economyCommand = new EconomyCommand(this);
        getCommand("balance").setExecutor(economyCommand);

        log.info("[Caminus] Registering economy API");
        Economy econAPI = new EconomyAPI(this);
        ServicesManager sm = getServer().getServicesManager();
        sm.register(Economy.class, econAPI, this, ServicePriority.High);

        log.info("[Caminus] Plugin enabled");
        getServer().getScheduler().scheduleAsyncDelayedTask(this, m_eventPoll);
	}

    public void checkFreeHalfDoorDay(Player sender) {
        if (sender.hasPermission("caminus.freedoorday")) {
            ItemStack stack = new ItemStack(64); // Wooden door block
            sender.getInventory().addItem(stack);
            sender.sendMessage("Happy mandatory free half door day!");
        }
    }

    public void sendMOTD(CommandSender sender) {
        String[] motd = null;
        try {
            motd = m_api.fetchMOTD(sender.getName());
        } catch (IOException e) {
            sender.sendMessage("Could not fetch MOTD: Communication error");
        }
        if (motd != null) {
            for(String msg : motd) {
                sender.sendMessage(msg.replace('&', ChatColor.COLOR_CHAR));
            }
        }
    }
}
