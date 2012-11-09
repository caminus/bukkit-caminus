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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.entity.Villager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.World;

import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import java.util.List;
import java.util.logging.Level;
import java.io.IOException;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import us.camin.api.Server;
import us.camin.api.ServerEvent;
import us.camin.api.BroadcastEvent;
import us.camin.api.PlayerMessageEvent;
import us.camin.api.ClientEvent;
import us.camin.api.VaultModifyEvent;
import us.camin.api.PlayerVaultSlot;
import org.json.JSONException;

public class Plugin extends JavaPlugin {

	Logger log = Logger.getLogger("Caminus");//Define your logger
    private Server m_api;
    private JoinListener m_listener;
    private MOTDCommand m_motdCommand;
    private VomitCommand m_vomitCommand;
    private VaultCommand m_vaultCommand;
    private ServerEventPoller m_eventPoll;
    private ArrayList<Villager> m_vaultmasters;

    public Server api() {
        return m_api;
    }

	public void onDisable() {
		log.info("[Caminus] Plugin disabled");
    m_eventPoll.stop();
    m_api = null;
    saveVaultmasters();
    for (Villager v : m_vaultmasters) {
      v.remove();
    }
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
        } else if (e instanceof PlayerMessageEvent) {
            final PlayerMessageEvent evt = (PlayerMessageEvent)(e);
            getServer().getScheduler().callSyncMethod(this, new Callable<Void>() {
                public Void call() {
                    getServer().getPlayer(evt.player).sendMessage(evt.message);
                    return null;
                }
            });
        } else if (e instanceof VaultModifyEvent) {
            final VaultModifyEvent evt = (VaultModifyEvent)(e);
            getServer().getScheduler().callSyncMethod(this, new Callable<Void>() {
                public Void call() {
                    Inventory inv;
                    try {
                        inv = vaultInventory(getServer().getPlayer(evt.player));
                    } catch (IOException e) {
                        return null;
                    }
                    for (PlayerVaultSlot s : evt.contents) {
                        if (s.quantity == -1) {
                            inv.clear(s.position);
                        } else {
                            inv.setItem(s.position, new ItemStack(s.item, s.quantity, s.damage, s.data));
                        }
                    }
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

  public void sendEvent(ClientEvent event) {
        ClientEvent[] events = new ClientEvent[1];
        events[0] = event;
        try {
          api().sendEvents(events);
        } catch (JSONException e) {
          log.log(Level.SEVERE, "Could not encode event", e);
        } catch (IOException e) {
          log.log(Level.SEVERE, "Could not submit event, it is lost forever!", e);
        }
  }

  public void addVaultmaster(Villager v) {
      m_vaultmasters.add(v);
  }

  public ArrayList<Villager> vaultmasters() {
      return m_vaultmasters;
  }

  public void reloadVaultmasters() {
    Configuration config = getConfig();
    List<Map<?, ?>> npcConfigs = config.getMapList("npcs/vaultmasters");
    for(Map<?, ?> c : npcConfigs) {
      double x = (Integer)c.get("x");
      double y = (Integer)c.get("y");
      double z = (Integer)c.get("z");
      String worldName = (String)c.get("world");
      World world = getServer().getWorld(worldName);
      Location loc = new Location(world, x, y, z);
      Villager vaultmaster = (Villager)world.spawnCreature(loc, EntityType.VILLAGER);
      addVaultmaster(vaultmaster);
    }
  }

  public void saveVaultmasters() {
    Configuration config = getConfig();
    ArrayList<Map<String, Object>> npcConfigs = new ArrayList<Map<String, Object>>();
    int i = 0;
    for (Villager v : m_vaultmasters) {
      Location loc = v.getLocation();
      Map<String, Object> vaultConfig = new HashMap<String, Object>();
      vaultConfig.put("x", loc.getBlockX());
      vaultConfig.put("y", loc.getBlockY());
      vaultConfig.put("z", loc.getBlockZ());
      vaultConfig.put("world", loc.getWorld().getName());
      npcConfigs.add(vaultConfig);
      i++;
    }
    config.set("npcs/vaultmasters", npcConfigs);
    saveConfig();
  }

	public void onEnable() {
        m_vaultmasters = new ArrayList<Villager>();
        m_vaultInventories = new HashMap<String, Inventory>();
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

        m_vaultCommand = new VaultCommand(this);
        getCommand("vaultmaster").setExecutor(m_vaultCommand);

        CommandExecutor economyCommand = new EconomyCommand(this);
        getCommand("balance").setExecutor(economyCommand);

        log.info("[Caminus] Registering economy API");
        Economy econAPI = new EconomyAPI(this);
        ServicesManager sm = getServer().getServicesManager();
        sm.register(Economy.class, econAPI, this, ServicePriority.High);

        reloadVaultmasters();

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

    private HashMap<String, Inventory> m_vaultInventories;

    public void saveVault(Player p, Inventory inv) throws IOException {
        ListIterator<ItemStack> items = inv.iterator();
        PlayerVaultSlot[] vault = new PlayerVaultSlot[inv.getSize()];
        int i = 0;
        while(items.hasNext()) {
            ItemStack item = items.next();
            PlayerVaultSlot slot = new PlayerVaultSlot();
            slot.position = i;
            slot.quantity = -1;
            if (item != null) {
              slot.item = item.getTypeId();
              slot.quantity = item.getAmount();
              slot.damage = item.getDurability();
              slot.data = item.getData().getData();
            }
            vault[i] = slot;
            i++;
        }
        log.info("Saving "+vault.length+" items to vault for "+p.getName());
        try {
            m_api.saveVault(p.getName(), vault);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public void forgetVaultInventory(Player p) {
        if (m_vaultInventories.containsKey(p.getName())) {
            m_vaultInventories.remove(p.getName());
        }
    }

    public Inventory vaultInventory(Player p) throws IOException {
        if (!m_vaultInventories.containsKey(p.getName())) {
            Inventory inv = p.getServer().createInventory(p, InventoryType.CHEST);
            m_vaultInventories.put(p.getName(), inv);
            PlayerVaultSlot[] vault = m_api.loadVault(p.getName());
            for(int i = 0;i<vault.length;i++) {
                inv.setItem(vault[i].position, new ItemStack(vault[i].item, vault[i].quantity, vault[i].damage, vault[i].data));
            }
        }
        return m_vaultInventories.get(p.getName());
    }
}
