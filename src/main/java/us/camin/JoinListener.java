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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
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
    }

    static public final String SESSION_METADATA_KEY = "caminus-session-id";

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        m_plugin.sendMOTD(event.getPlayer());
        m_plugin.checkFreeHalfDoorDay(event.getPlayer());
    }
}
