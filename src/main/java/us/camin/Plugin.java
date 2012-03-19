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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.logging.Logger;
import java.io.IOException;

import us.camin.api.Server;

public class Plugin extends JavaPlugin {

	Logger log = Logger.getLogger("Caminus");//Define your logger
    private Server m_api;
    private JoinListener m_listener;
    private MOTDCommand m_motdCommand;

    public Server api() {
        return m_api;
    }

	public void onDisable() {
		log.info("[Caminus] Plugin disabled");
        m_api = null;
	}

	public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();
        m_motdCommand = new MOTDCommand(this);
        m_listener = new JoinListener(this);
        Configuration conf = getConfig();
        conf.addDefault("url", "http://camin.us/api/");
        String url = conf.getString("url");
        m_api = new Server(url);
        pm.registerEvents(m_listener, this);

        m_motdCommand = new MOTDCommand(this);
        getCommand("motd").setExecutor(m_motdCommand);

        log.info("[Caminus] Plugin enabled");
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
                sender.sendMessage(msg);
            }
        }
    }
}
