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


import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.logging.Logger;
import java.util.Scanner;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class JoinListener extends PlayerListener {
	Logger log = Logger.getLogger("Caminus.Join");
    private String m_url;

    public JoinListener() {
    }

    public void setURL(String url) {
        m_url = url;
    }

    public static void main(String[] args) throws IOException, MalformedURLException {
        JoinListener listener = new JoinListener();
        if (listener.isUserAuthed(args[0]))
            System.out.println("Yes!");
        else
            System.out.println("No!");
    }

    public void onPlayerLogin(PlayerLoginEvent event) {
        Player p = event.getPlayer();
        try {
            if (!isUserAuthed(p.getName())) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "An active camin.us account is required.");
                return;
            }
        } catch (MalformedURLException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Auth URL is invalid!");
            return;
        } catch (IOException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Camin.us auth server seems down.");
            return;
        }
        String[] motd = null;
        try {
            motd = fetchMOTD(p.getName());
        } catch (MalformedURLException e) {
            p.chat("Could not fetch MOTD: Bad URL");
        } catch (IOException e) {
            p.chat("Could not fetch MOTD: Communication error");
        } catch (JSONException e) {
            p.chat("Could not fetch MOTD: Bad JSON");
        }
        if (motd != null) {
            for(String msg : motd) {
                p.chat(msg);
            }
        }
    }

    public String[] fetchMOTD(String user) throws IOException, MalformedURLException, JSONException {
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
        JSONObject jsonObj = new JSONObject(jsonStr);
        JSONArray motd = jsonObj.getJSONArray("motd");
        String[] ret = new String[motd.length()];
        for (int i = 0;i<motd.length();i++) {
            ret[i] = motd.optString(i);
        }
        return ret;
    }

    public boolean isUserAuthed(String user) throws IOException, MalformedURLException {
        URL authServer = new URL(m_url+"validate/"+user);
        log.info("Authing "+user+" against "+authServer);
        HttpURLConnection conn = (HttpURLConnection)authServer.openConnection();
        int code = conn.getResponseCode();
        if (code >= 200 && code < 300)
            return true;
        return false;
    }
}
