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


import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.logging.Logger;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Server {
	Logger log = Logger.getLogger("Caminus.API");
    private String m_url;

    public ValidationResponse validatePlayer(String name) throws IOException {
        ValidationResponse resp = new ValidationResponse();
        URL authServer = new URL(m_url+"validate/"+name);
        log.info("Authing "+name+" against "+authServer);
        HttpURLConnection conn = (HttpURLConnection)authServer.openConnection();
        BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
        String jsonStr;
        try {
            jsonStr = new java.util.Scanner(in).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            jsonStr = "";
        }
        in.close();
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            resp.valid = jsonObj.optBoolean("valid");
            if (!resp.valid)
                return resp;
            JSONArray perms = jsonObj.getJSONArray("permissions");
            resp.permissions = new String[perms.length()];
            for (int i = 0;i<perms.length();i++) {
                resp.permissions[i] = perms.optString(i);
            }
        } catch (JSONException e) {
            throw new IOException("JSON parse error", e);
        }
        return resp;
    }

    public Server(String url) {
        m_url = url;
    }

    public void setURL(String url) {
        m_url = url;
    }

    public String[] fetchMOTD(String user) throws IOException {
        URL motdService = new URL(m_url+"motd/"+user);
        log.info("Fetching MOTD for "+user+" from "+motdService);
        HttpURLConnection conn = (HttpURLConnection)motdService.openConnection();
        BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
        String jsonStr;
        // Stupid scanner trick. \A means "beginning of input boundary".
        try {
            jsonStr = new java.util.Scanner(in).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            jsonStr = "";
        }
        in.close();
        String[] ret = new String[0];
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray motd = jsonObj.getJSONArray("motd");
            ret = new String[motd.length()];
            for (int i = 0;i<motd.length();i++) {
                ret[i] = motd.optString(i);
            }
        } catch (JSONException e) {
            throw new IOException("JSON parse error", e);
        }
        return ret;
    }
}
