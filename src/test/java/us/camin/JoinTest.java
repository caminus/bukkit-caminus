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

import us.camin.JoinListener;

public class JoinTest {
    private JoinListener listener;
    private APIServer server;
    @Before public void setup() throws Exception {
        server = new APIServer();
        server.start();
        listener = new JoinListener();
        listener.setURL("http://localhost:8001/api/");
    }

    @After public void teardown() throws Exception {
        server.stop();
    }

    @Test public void validUser() throws IOException {
        assertTrue(listener.isUserAuthed("TestUser"));
    }

    @Test public void invaliduser() throws IOException {
        assertFalse(listener.isUserAuthed("InvalidUser"));
    }

    @Test public void motd() throws IOException, JSONException {
        String[] goodMOTD = {"Test MOTD"};
        String[] motd = listener.fetchMOTD("TestUser");
        assertArrayEquals(null, goodMOTD, motd);
    }
}
