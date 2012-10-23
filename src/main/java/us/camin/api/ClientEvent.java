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
import org.json.JSONStringer;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class ClientEvent {
    public JSONObject toJSON() throws JSONException {
        JSONWriter s = new JSONStringer();
        s.object();
        s.key("type");
        s.value(jsonName());
        s.key("payload");
        s.object();
        s = toJSON(s);
        s.endObject();
        s.endObject();
        return new JSONObject(((JSONStringer)s).toString());
    }

    public abstract String jsonName();

    public abstract JSONWriter toJSON(JSONWriter writer) throws JSONException;
}
