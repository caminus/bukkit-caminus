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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Villager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class VaultCommand implements CommandExecutor {
    private Plugin m_plugin;

    public VaultCommand(Plugin p) {
        m_plugin = p;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (sender.hasPermission("caminus.vaultmaster")) {
            if (sender instanceof Player) {
                Player p = (Player)sender;
                Location loc = p.getLocation();
                Villager vaultmaster = (Villager)p.getWorld().spawnCreature(loc, EntityType.VILLAGER);
                m_plugin.addVaultmaster(vaultmaster);
            }
        }
        return true;
    }
}
