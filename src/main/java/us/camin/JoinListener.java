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
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinListener extends PlayerListener {
    public JoinListener() {
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
        if (!isUserAuthed(p.getName()))
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "An active camin.us account is required.");
        } catch (MalformedURLException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Auth URL is invalid!");
        } catch (IOException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Camin.us auth server seems down.");
        }
    }

    public boolean isUserAuthed(String user) throws IOException, MalformedURLException {
        URL authServer = new URL("http://dev.camin.us/api/validate/"+user);
        URLConnection conn = authServer.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        boolean ret;
        if (in.readLine().equals("false"))
            ret = false;
        ret = true;
        in.close();
        return ret;
    }
}
