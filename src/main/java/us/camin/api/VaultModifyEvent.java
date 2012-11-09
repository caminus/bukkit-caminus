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

public class VaultModifyEvent extends ServerEvent {
    public PlayerVaultSlot[] contents;
    public String player;
    public static VaultModifyEvent fromJSON(JSONObject obj, int id) {
        VaultModifyEvent ret = new VaultModifyEvent();
        ret.player = obj.optString("player");
        JSONArray items;
        try {
            items = obj.getJSONArray("items");
        } catch (JSONException e) {
            return null;
        }
        ret.contents = new PlayerVaultSlot[items.length()];
        for(int i = 0;i<items.length();i++) {
            JSONObject slot;
            try {
                slot = items.getJSONObject(i);
            } catch (JSONException e) {
                return null;
            }
            ret.contents[i] = new PlayerVaultSlot();
            ret.contents[i].item = slot.optInt("item");
            ret.contents[i].quantity = slot.optInt("quantity");
            ret.contents[i].damage = (short)slot.optInt("damage");
            ret.contents[i].data = (byte)slot.optInt("data");
            ret.contents[i].position = slot.optInt("position");
        }
        return ret;
    }
}

