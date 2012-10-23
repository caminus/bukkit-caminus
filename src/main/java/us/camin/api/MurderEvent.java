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

import org.json.JSONWriter;
import org.json.JSONException;

public class MurderEvent extends ClientEvent {
    public String player;
    public String killer;
    public String message;

    public MurderEvent(String player, String killer) {
        this.player = player;
        this.killer = killer;
    }

    public JSONWriter toJSON(JSONWriter writer) throws JSONException {
      writer.key("player").value(player);
      writer.key("killer").value(killer);
      return writer;
    }

    public String jsonName() {
      return "player-murder";
    }
}


