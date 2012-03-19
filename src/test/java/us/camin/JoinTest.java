package us.camin.tests;

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

import org.json.JSONException;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

import us.camin.api.Server;
import us.camin.api.ValidationResponse;

public class JoinTest {
    private Server api;
    private APIServer server;
    @Before public void setup() throws Exception {
        server = new APIServer();
        server.start();
        api = new Server("http://localhost:8001/api/");
    }

    @After public void teardown() throws Exception {
        server.stop();
    }

    @Test public void validUser() throws IOException, JSONException {
        ValidationResponse resp = api.validatePlayer("TestUser");
        assertTrue(resp.valid);
        assertNotNull(resp.permissions);
        assertTrue(resp.permissions.length>0);
    }

    @Test public void invaliduser() throws IOException, JSONException {
        ValidationResponse resp = api.validatePlayer("InvalidUser");
        assertFalse(resp.valid);
        assertNotNull(resp.permissions);
        assertEquals(resp.permissions.length, 0);
    }

    @Test public void motd() throws IOException, JSONException {
        String[] goodMOTD = {"Test MOTD"};
        String[] motd = api.fetchMOTD("TestUser");
        assertArrayEquals(null, goodMOTD, motd);
    }
}
