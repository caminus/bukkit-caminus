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


import java.util.logging.Logger;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import java.io.IOException;
import java.util.ArrayList;

import us.camin.api.ValidationResponse;
import us.camin.api.ClientEvent;
import us.camin.api.BlockEvent;
import us.camin.api.ChatEvent;
import us.camin.api.DeathEvent;
import us.camin.api.MurderEvent;
import us.camin.api.WeatherEvent;

public class JoinListener implements Listener {
	Logger log = Logger.getLogger("Caminus.Join");
    private String m_url;
    private Plugin m_plugin;

    public JoinListener(Plugin p) {
        m_plugin = p;
    }

    private void closePlayerSession(Player p) throws IOException {
        m_plugin.api().closeSession(p.getName());
    }

    private ValidationResponse openPlayerSession(Player p) throws IOException {
        ValidationResponse resp = null;
        resp = m_plugin.api().openSession(p.getName(), p.getAddress());
        if (!resp.valid) {
            return resp;
        }
        log.info("Session "+resp.sessionId+" opened for "+p.getName());
        PermissionAttachment att = p.addAttachment(m_plugin);
        for(String perm : resp.permissions) {
            log.info("Granting "+perm);
            att.setPermission(perm, true);
        }
        p.recalculatePermissions();
        return resp;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            closePlayerSession(event.getPlayer());
        } catch (IOException e) {
        }
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        try {
            closePlayerSession(event.getPlayer());
        } catch (IOException e) {
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        ValidationResponse resp;
        try {
            resp = openPlayerSession(event.getPlayer());
        } catch (IOException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Camin.us auth server seems down.");
            return;
        }
        if (!resp.valid) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, resp.errorMessage);
            return;
        }
        m_plugin.forgetVaultInventory(event.getPlayer());
    }

    static public final String SESSION_METADATA_KEY = "caminus-session-id";

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        m_plugin.sendMOTD(event.getPlayer());
        m_plugin.checkFreeHalfDoorDay(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        ChatEvent evt = new ChatEvent(event.getPlayer().getName(), event.getMessage());
        m_plugin.sendEvent(evt);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        BlockEvent evt = new BlockEvent(event.getPlayer().getName(), event.getBlock(), BlockEvent.Type.BREAK);
        m_plugin.sendEvent(evt);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockEvent evt = new BlockEvent(event.getPlayer().getName(), event.getBlock(), BlockEvent.Type.PLACE);
        m_plugin.sendEvent(evt);
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        WeatherEvent evt = new WeatherEvent(event.getWorld().getName(), event.toWeatherState());
        m_plugin.sendEvent(evt);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player target = event.getEntity();
        Entity source = target.getKiller();
        ClientEvent evt = new DeathEvent(target.getName(), event.getDeathMessage());
        m_plugin.sendEvent(evt);
        if (source instanceof Player) {
          Player killer = (Player)source;
          evt = new MurderEvent(target.getName(), killer.getName());
          m_plugin.sendEvent(evt);
        }
    }

    @EventHandler
    public void onVaultmasterInteract(PlayerInteractEntityEvent event) {
        System.out.println("Interacted with someone!");
        ArrayList<Villager> vaultmasters = m_plugin.vaultmasters();
        Villager vaultmaster = null;
        boolean found = false;
        for(Villager v : vaultmasters) {
            if (event.getRightClicked() == v) {
                System.out.println("It was one of ours!");
                event.setCancelled(true);
                found = true;
                vaultmaster = v;
                break;
            }
        }
        if (!found)
          return;
        Inventory inv;
        try {
            inv = m_plugin.vaultInventory(event.getPlayer());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Couldn't fetch user's vault!", e);
            event.getPlayer().sendMessage("Couldn't fetch your vault! api.camin.us might be down.");
            return;
        }
        event.getPlayer().openInventory(inv);
    }

    @EventHandler
    public void onVaultmasterInventoryChange(InventoryCloseEvent event) {
        Player p = (Player)event.getPlayer();
        try {
            m_plugin.saveVault(p, m_plugin.vaultInventory(p));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Couldn't save user's vault!", e);
            p.sendMessage("Your vault couldn't be saved to the server!");
            p.sendMessage("Your items are still held by the vaultmaster, but might vanish.");
        }
    }

    @EventHandler
    public void onVaultmasterAttacked(EntityDamageEvent event) {
        Entity target = event.getEntity();
        if (target instanceof Villager) {
            Villager v = (Villager)target;
            if (m_plugin.vaultmasters().contains(target)) {
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
                    if (e.getDamager() instanceof LivingEntity) {
                        v.setTarget((LivingEntity)e.getDamager());
                        //Projectile weapon = v.launchProjectile(Arrow.class);
                    }
                }
                v.setHealth(v.getMaxHealth());
            }
        }
    }
}
