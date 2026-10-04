package club.bagel.autototem;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoTotem extends JavaPlugin implements Listener {

    // Players who currently have auto totem switched on.
    private final Set<UUID> enabled = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Every 2 ticks, make sure each enabled player has a totem in their offhand.
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (UUID id : enabled) {
                Player p = getServer().getPlayer(id);
                if (p != null && p.isOnline()) {
                    refill(p);
                }
            }
        }, 2L, 2L);
    }

    private void refill(Player p) {
        PlayerInventory inv = p.getInventory();
        ItemStack off = inv.getItemInOffHand();
        if (off.getType() == Material.TOTEM_OF_UNDYING) {
            return;
        }

        // Look for a totem in the main inventory and hotbar (slots 0-35).
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                slot = i;
                break;
            }
        }
        if (slot == -1) {
            return;
        }

        ItemStack totem = inv.getItem(slot);
        // Whatever was in the offhand goes to the slot the totem came from.
        inv.setItem(slot, off.getType() == Material.AIR ? null : off);
        inv.setItemInOffHand(totem);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("autototem.use")) {
            p.sendMessage("You don't have permission to do that.");
            return true;
        }

        if (enabled.remove(p.getUniqueId())) {
            p.sendMessage("Auto totem: OFF");
        } else {
            enabled.add(p.getUniqueId());
            p.sendMessage("Auto totem: ON");
        }
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        enabled.remove(e.getPlayer().getUniqueId());
    }
}
