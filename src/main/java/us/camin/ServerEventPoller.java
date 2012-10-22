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

import java.lang.Runnable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import us.camin.api.ServerEvent;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.scheduler.BukkitScheduler;

public class ServerEventPoller implements Runnable {
    private Plugin m_plugin;
    Logger log = Logger.getLogger("CaminusEventPoll");

    public ServerEventPoller(Plugin plugin) {
        m_plugin = plugin;
    }

    public void run() {
      log.info("Poll events");
      ServerEvent[] events = new ServerEvent[0];
      try {
        events = m_plugin.api().pollEventQueue();
      } catch (IOException e) {
      }
      for(ServerEvent e : events) {
          m_plugin.handleEvent(e);
      }
      final BukkitScheduler scheduler = m_plugin.getServer().getScheduler();
      scheduler.scheduleAsyncDelayedTask(m_plugin, this);
      log.info("Events handled.");
    }
}
