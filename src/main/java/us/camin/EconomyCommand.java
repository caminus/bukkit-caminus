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

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconomyCommand implements CommandExecutor {
    private Plugin m_plugin;

    public EconomyCommand(Plugin p) {
        m_plugin = p;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        String name = sender.getName();
        double balance;
        try {
            balance = m_plugin.api().getBalance(name);
            sender.sendMessage("You have "+balance+" grist");
        } catch (IOException e) {
            sender.sendMessage("Error: "+e.getMessage());
        }
        return true;
    }
}
