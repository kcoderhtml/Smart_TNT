package events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import thirtyvirus.uber.helpers.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class explode implements Listener {

    private static Map<Location, BlockState> savedExplodedBlocks = new HashMap<>();
    private int ticksLimit = 20 * 15;

    @EventHandler
    private void onTNTExplode(EntityExplodeEvent event) {
        // make sure that the explosion is caused by an UberItem TNT
        if (event.getEntityType() != EntityType.PRIMED_TNT) return;
        if (!event.getEntity().hasMetadata("ubertnt")) {
            for (Block block : event.blockList()) saveBlockState(block);
            return;
        }

        // process repair TNT
        if (event.getEntity().hasMetadata("repair")) {
            for (Block block : getNearbyBlocks(event.getEntity().getLocation(), 6)) restoreBlockState(block);
            event.getLocation().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3, 0.5f);
            event.setCancelled(true);
        }

        if (event.getEntity().hasMetadata("mining")) {
            for (Block block : getNearbyBlocks(event.getEntity().getLocation(), 4)) {
                if (block.getType().getBlastResistance() <= 6) {
                    saveBlockState(block);
                    block.breakNaturally();
                }
            }
            event.getLocation().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3, 0.5f);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void saveBrokenBlocks(BlockBreakEvent event) {
        saveBlockState(event.getBlock());
    }

    private void saveBlockState(Block block) {
        if (block.getType() == Material.AIR || block.getType() == Material.WATER) return;

        savedExplodedBlocks.put(block.getLocation(), block.getState());
        Utilities.scheduleTask(() -> savedExplodedBlocks.remove(block.getLocation()) , ticksLimit);
    }

    private void restoreBlockState(Block block) {
        if (!savedExplodedBlocks.containsKey(block.getLocation())) return;
        BlockState backup = savedExplodedBlocks.get(block.getLocation());

        if (backup.getType() == Material.FIRE || backup.getType() == Material.TNT) return;

        block.setType(backup.getBlock().getType());
        block.setBlockData(backup.getBlockData());
        block.setBiome(backup.getBlock().getBiome());
    }

    public static List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = new ArrayList<Block>();
        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

}
