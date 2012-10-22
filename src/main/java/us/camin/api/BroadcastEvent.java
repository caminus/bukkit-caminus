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

public class BroadcastEvent extends ServerEvent {
    public String message;
    public static BroadcastEvent fromJSON(JSONObject obj, int id) {
        BroadcastEvent ret = new BroadcastEvent();
        ret.message = obj.optString("message");
        ret.id = id;
        return ret;
    }
}
