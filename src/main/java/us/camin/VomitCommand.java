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
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.Location;

import java.lang.Thread;
import java.lang.Runnable;

public class VomitCommand implements CommandExecutor {
    private Plugin m_plugin;

    public VomitCommand(Plugin p) {
        m_plugin = p;
    }

    private class VomitRunner implements Runnable {
        private ItemStack m_stack;
        private Player m_target;
        private int m_qty;

        public VomitRunner(ItemStack stack, Player target, int quantity) {
            m_stack = stack;
            m_target = target;
            m_qty = quantity;
        }

        public void run() {
            for(int i = 0;i<m_qty;i++) {
                Location vomitLocation = m_target.getEyeLocation();
                double speed = 0.5;
                double pitch = vomitLocation.getPitch()+90;
                double yaw = vomitLocation.getYaw()+90;
                double xSpeed = Math.cos(Math.toRadians(yaw))*Math.sin(Math.toRadians(pitch));
                double zSpeed = Math.sin(Math.toRadians(pitch))*Math.sin(Math.toRadians(yaw));
                double ySpeed = Math.cos(Math.toRadians(pitch));
                Vector vomitVelocity = new Vector(xSpeed*speed, ySpeed*speed, zSpeed*speed);
                Item item = m_target.getWorld().dropItem(vomitLocation, m_stack);
                item.setPickupDelay(3);
                item.setVelocity(vomitVelocity);
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (sender.hasPermission("caminus.vomit")) {
            if (split.length < 3) {
                sender.sendMessage("Usage: /"+command+" player quantity item");
                return true;
            }
            String target = split[0];
            Player player = sender.getServer().getPlayer(target);
            int qty;
            try {
                qty = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Not a number: "+split[1]);
                return true;
            }
            String itemName = split[2];
            ItemStack stack = new ItemStack(Material.valueOf(itemName.toUpperCase()));
            Thread runner = new Thread(new VomitRunner(stack, player, qty));
            runner.start();
        }
        return true;
    }
}

