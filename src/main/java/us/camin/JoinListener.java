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
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class JoinListener implements Listener {
	Logger log = Logger.getLogger("Caminus.Join");
    private String m_url;

    public JoinListener() {
    }

    public void setURL(String url) {
        m_url = url;
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player p = event.getPlayer();
        if (p.hasPermission("caminus.whitelisted"))
            return;
        try {
            if (!isUserAuthed(p.getName())) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "An active camin.us account is required.");
            }
        } catch (MalformedURLException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Auth URL is invalid!");
        } catch (IOException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Camin.us auth server seems down.");
        } catch (JSONException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Bad auth server response.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String[] motd = null;
        try {
            motd = fetchMOTD(p.getName());
        } catch (MalformedURLException e) {
            p.sendMessage("Could not fetch MOTD: Bad URL");
        } catch (IOException e) {
            p.sendMessage("Could not fetch MOTD: Communication error");
        } catch (JSONException e) {
            p.sendMessage("Could not fetch MOTD: Bad JSON");
        }
        if (motd != null) {
            for(String msg : motd) {
                p.sendMessage(msg);
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

    public boolean isUserAuthed(String user) throws IOException, MalformedURLException, JSONException {
        URL authServer = new URL(m_url+"validate/"+user);
        log.info("Authing "+user+" against "+authServer);
        HttpURLConnection conn = (HttpURLConnection)authServer.openConnection();
        BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
        String jsonStr;
        try {
            jsonStr = new java.util.Scanner(in).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            jsonStr = "";
        }
        in.close();
        JSONObject jsonObj = new JSONObject(jsonStr);
        boolean valid = jsonObj.optBoolean("valid");
        if (valid)
            return true;
        return false;
    }
}
