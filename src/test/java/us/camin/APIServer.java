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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.Server;

import java.io.IOException;

public class APIServer {
    private Server m_server;

    public APIServer() {
        m_server = new Server(8001);
        Context api = new Context(m_server, "/api");
        api.addServlet(new ServletHolder(new ValidateServlet()), "/validate/*");
        api.addServlet(new ServletHolder(new MOTDServlet()), "/motd/*");
    }

    public void start() throws Exception {
        m_server.start();
    }

    public void stop() throws Exception {
        m_server.stop();
    }

    private class ValidateServlet extends HttpServlet {
        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
            resp.setContentType("application/json");
            if (req.getPathInfo().equals("/TestUser"))
                resp.sendError(200);
            else
                resp.sendError(404);
        }
    }

    private class MOTDServlet extends HttpServlet {
        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
            resp.setContentType("application/json");
            ServletOutputStream out = resp.getOutputStream();
            out.println("{\"motd\": [\"Test MOTD\"]}");
        }
    }
}
