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
import org.bukkit.block.Block;
import org.bukkit.Location;

public class BlockEvent extends ClientEvent {
    public String sender;
    public Block block;
    public Type type;

    public enum Type {
        BREAK, PLACE
    }

    public BlockEvent(String player, Block block, Type type) {
        this.sender = player;
        this.block = block;
        this.type = type;
    }

    public JSONWriter toJSON(JSONWriter writer) throws JSONException {
      writer.key("sender").value(sender);
      String evtType;
      switch(type) {
        case BREAK:
          evtType = "break";
          break;
        case PLACE:
          evtType = "place";
          break;
        default:
          evtType = "unknown";
      }
      writer.key("type").value(evtType);
      writer.key("location");
      writer.object();
      Location loc = block.getLocation();
      writer.key("x").value(loc.getBlockX());
      writer.key("y").value(loc.getBlockY());
      writer.key("z").value(loc.getBlockZ());
      writer.endObject();
      return writer;
    }

    public String jsonName() {
      return "block";
    }
}

