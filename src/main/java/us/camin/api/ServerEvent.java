package us.camin.api;

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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerEvent {
    public int id;
    static Logger log = Logger.getLogger("Caminus.api");
    public static ServerEvent fromJSON(JSONObject obj) {
        int id = obj.optInt("id");
        JSONObject event;
        try {
          event = obj.getJSONObject("event");
        } catch (JSONException e) {
          log.log(Level.SEVERE, "Bad JSON", e);
          return null;
        }
        String type = event.optString("type");
        JSONObject payload;
        try {
          payload = event.getJSONObject("payload");
        } catch (JSONException e) {
          log.log(Level.SEVERE, "Bad JSON ", e);
          return null;
        }
        if (type.equals("broadcast")) {
            return BroadcastEvent.fromJSON(payload, id);
        } else if (type.equals("player-message")) {
            return PlayerMessageEvent.fromJSON(payload, id);
        } else {
            log.log(Level.SEVERE, "Unhandled event type: "+type);
            return null;
        }
    }
}
