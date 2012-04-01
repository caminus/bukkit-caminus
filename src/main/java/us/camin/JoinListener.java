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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;
import java.io.IOException;

import us.camin.api.ValidationResponse;

public class JoinListener implements Listener {
	Logger log = Logger.getLogger("Caminus.Join");
    private String m_url;
    private Plugin m_plugin;

    public JoinListener(Plugin p) {
        m_plugin = p;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player p = event.getPlayer();
        ValidationResponse resp = null;
        try {
            resp = m_plugin.api().validatePlayer(p.getName());
            if (!resp.valid) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "An active camin.us account is required.");
                return;
            }
        } catch (IOException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Camin.us auth server seems down.");
            return;
        }
        PermissionAttachment att = p.addAttachment(m_plugin);
        for(String perm : resp.permissions) {
            log.info("Granting "+perm);
            att.setPermission(perm, true);
        }
        p.recalculatePermissions();
        log.info(p.hasPermission("caminus.whitelisted")+" "+p.hasPermission("bukkit.command.op.give"));
        log.info(p.hasPermission("permissions.build")+"");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        m_plugin.sendMOTD(event.getPlayer());
        m_plugin.checkFreeHalfDoorDay(event.getPlayer());
    }
}
