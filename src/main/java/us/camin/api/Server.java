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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.Random;
import org.apache.commons.codec.binary.Hex;

public class Server {
	Logger log = Logger.getLogger("Caminus.API");
    private String m_url;
    private String m_name;
    private String m_secret;

    private String genToken() {
        Random r = new Random();
        int salt = r.nextInt();
        MessageDigest crypt;
        try {
            crypt = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            log.warning("Could not find SHA-1 algorithm");
            return "";
        }
        crypt.reset();
        String token = m_name+salt+m_secret;
        crypt.update(token.getBytes());
        token = m_name+"$"+salt+"$"+Hex.encodeHexString(crypt.digest());
        log.info("Generated token "+token+" from "+m_name+salt+m_secret);
        return token;
    }

    public JSONObject exec(String path, String method, HashMap<String, String> params) throws MalformedURLException, ProtocolException, IOException {
        HttpURLConnection conn = open(path);
        conn.setRequestMethod(method);
        if (params.size() > 0) {
            conn.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            Set<Map.Entry<String, String>> values = params.entrySet();
            Iterator<Map.Entry<String, String>> it = values.iterator();
            StringBuilder sb = new StringBuilder();
            while(it.hasNext()) {
                Map.Entry<String, String> param = it.next();
                sb.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                sb.append("=");
                sb.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                sb.append("&");
            }
            String postData = sb.substring(0, sb.length()-1);
            conn.setFixedLengthStreamingMode(postData.length());
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            out.writeBytes(postData);
        }
        return readJSON(conn);
    }

    public HttpURLConnection open(String path) throws MalformedURLException, IOException {
        URL authServer = new URL(m_url+path);
        HttpURLConnection conn = (HttpURLConnection)authServer.openConnection();
        conn.setRequestProperty("Authorization", "X-Caminus "+genToken());
        return conn;
    }

    public JSONObject readJSON(HttpURLConnection conn) throws IOException {
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
            return jsonObj;
        } catch (JSONException e) {
            throw new IOException("JSON parse error", e);
        }
    }

    // Convienence methods
    public JSONObject exec(String path, String method) throws MalformedURLException, ProtocolException, IOException {
        return exec(path, method, new HashMap<String, String>());
    }

    public JSONObject get(String path) throws IOException {
        return exec(path, "GET");
    }

    public JSONObject put(String path, HashMap<String, String> params) throws MalformedURLException, IOException {
        try {
            return exec(path, "PUT", params);
        } catch (ProtocolException e) {
            return null;
        }
    }

    public ValidationResponse validatePlayer(String name) throws IOException {
        log.info("Validating "+name+" against "+m_url);
        ValidationResponse resp = new ValidationResponse();
        JSONObject jsonObj = get("validate/"+name);
        resp.valid = jsonObj.optBoolean("valid");
        if (!resp.valid)
            return resp;

        try {
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

    public void setServerName(String name) {
        m_name = name;
    }

    public void setServerSecret(String secret) {
        m_secret = secret;
    }

    public String[] fetchMOTD(String user) throws IOException {
        log.info("Fetching motd for "+user);
        String[] ret = new String[0];
        JSONObject jsonObj = get("motd/"+user);
        try {
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

    public double getBalance(String player) throws IOException {
        log.info("Fetching balance for "+player);
        JSONObject jsonObj = get("server/economy/"+player);
        return jsonObj.optDouble("balance", 0);
    }

    public BalanceAdjustResponse adjustBalance(String player, double delta) throws IOException {
        log.info("Adjusting balance for "+player+" by "+delta);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("delta", ""+delta);
        JSONObject jsonObj = put("server/economy/"+player, params);
        BalanceAdjustResponse resp = new BalanceAdjustResponse();
        resp.success = jsonObj.optBoolean("success", false);
        resp.newBalance = jsonObj.optDouble("balance", 0);
        resp.message = jsonObj.optString("message", "");
        return resp;
    }

    public boolean pingAPI() {
        log.info("Pinging API server to verify credentials");
        try {
            get("server/whoami");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not ping API server.", e);
            return false;
        }
        return true;
    }
}
