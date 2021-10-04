package events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import thirtyvirus.uber.UberItems;
import thirtyvirus.uber.helpers.Utilities;

import java.util.*;

public class explode implements Listener {

    private static Map<Location, BlockState> savedExplodedBlocks = new HashMap<>();
    private int ticksLimit = 20 * 15;

    private static final List<Material> EXCLUDE = Arrays.asList(Material.TNT, Material.GRASS, Material.TALL_GRASS,
            Material.AIR, Material.DANDELION, Material.POPPY, Material.SPRUCE_LOG, Material.SPRUCE_LEAVES,
            Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE, Material.COAL_BLOCK, Material.CLAY);

    @EventHandler
    private void onTNTExplode(EntityExplodeEvent event) {
        // make sure that the explosion is caused by an UberItem TNT
        if (event.getEntityType() != EntityType.PRIMED_TNT) return;
        if (!event.getEntity().hasMetadata("ubertnt")) {
            for (Block block : event.blockList()) saveBlockState(block);
            return;
        }

        // process tele TNT
        if (event.getEntity().hasMetadata("tele")) {
            event.getLocation().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3, 0.5f);
            event.setCancelled(true);
            for (Block block : event.blockList()) {

                if (block.getType().isSolid()) {
                    Block newBlock = block.getWorld().getBlockAt(block.getLocation().add(0,20,0));
                    newBlock.setType(block.getType());
                    newBlock.setBlockData(block.getBlockData());
                }

                block.setType(Material.AIR);
            }
        }

        // process winter TNT
        if (event.getEntity().hasMetadata("winter")) {
            event.getLocation().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3, 0.5f);
            event.setCancelled(true);

            for (Block block : event.blockList()) {
                block.setBiome(Biome.TAIGA);
                if (EXCLUDE.contains(block.getType())) continue;
                replaceBlockWithWinter(block);
            }
        }

        // process mining TNT
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

        // process matroska TNT
        if (event.getEntity().hasMetadata("nested")) {
            int nestLevel = event.getEntity().getMetadata("nested").get(0).asInt();
            if (nestLevel > 0) {
                for (Block block : event.blockList()) saveBlockState(block);
                nestedExplosion((TNTPrimed)event.getEntity(), 3, nestLevel);
            }
        }

        // process reverse TNT
        if (event.getEntity().hasMetadata("repair")) {
            Bukkit.getLogger().info("saved exploded blocks: " + savedExplodedBlocks.size());
            for (Block block : getNearbyBlocks(event.getEntity().getLocation(), 6)) restoreBlockState(block);
            event.getLocation().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3, 0.5f);
            event.setCancelled(true);
        }

        // process big boy
        if (event.getEntity().hasMetadata("bigboy")) {
            event.setCancelled(true);
            event.getLocation().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 3, 0.5f);
            nineExplosions(event.getLocation(), 24, 50);
            if (event.getLocation().getY() > 9) nineExplosions(event.getLocation().clone().add(0, -8, 0), 17, 50);
            if (event.getLocation().getY() > 18) nineExplosions(event.getLocation().clone().add(0, -17, 0), 15, 50);
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

    private static List<Block> getNearbyBlocks(Location location, int radius) {
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

    private static void nestedExplosion(TNTPrimed tntPrime, int counter, int amount) {

        tntPrime.getWorld().playSound(tntPrime.getLocation(), Sound.ENTITY_EGG_THROW, 4, 2f);

        TNTPrimed tnt = tntPrime.getWorld().spawn(tntPrime.getLocation(), TNTPrimed.class);
        tnt.setMetadata("ubertnt", new FixedMetadataValue(UberItems.getInstance(), "a"));
        tnt.setMetadata("nested", new FixedMetadataValue(UberItems.getInstance(), amount - 1));
        tnt.setVelocity(getRandomDirection());

        if (counter > 0 && amount > 0) Utilities.scheduleTask(()-> nestedExplosion(tntPrime, counter - 1, amount), 1);
    }

    private static Vector getRandomDirection() {
        Vector direction = new Vector();
        direction.setX(Math.random()*2-1d);
        direction.setY(Math.random());
        direction.setZ(Math.random()*2-1d);
        return direction.normalize().multiply(0.6);
    }

    private static void nineExplosions(Location location, int factor, int power) {
        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(factor * -1, 0, factor * -1), power), 1);
        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(0, 0, factor * -1), power), 2);
        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(factor, 0, factor * -1), power), 3);

        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(factor * -1, 0, 0), power), 4);
        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(0, 0, 0), power), 5);
        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(factor, 0, 0), power), 6);

        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(factor * -1, 0, factor), power), 7);
        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(0, 0, factor), power), 8);
        Utilities.scheduleTask(() -> location.getWorld().createExplosion(location.clone().add(factor, 0, factor), power), 9);
    }

    private static void replaceBlockWithWinter(Block block) {
        switch(block.getType()) {
            case OAK_LOG:
            case ACACIA_LOG:
            case BIRCH_LOG:
            case DARK_OAK_LOG:
            case JUNGLE_LOG:
                block.setType(Material.SPRUCE_LOG);
                break;
            case OAK_LEAVES:
            case ACACIA_LEAVES:
            case BIRCH_LEAVES:
            case DARK_OAK_LEAVES:
            case JUNGLE_LEAVES:
                block.setType(Material.SPRUCE_LEAVES);
                break;
            case STONE:
                block.setType(Material.PACKED_ICE);
                break;
            case GRANITE:
            case ANDESITE:
            case DIORITE:
            case GRAVEL:
                block.setType(Material.BLUE_ICE);
                break;
            case DIAMOND_ORE:
            case COAL_ORE:
            case EMERALD_ORE:
            case GOLD_ORE:
            case IRON_ORE:
            case REDSTONE_ORE:
                block.setType(Material.COAL_BLOCK);
                break;
            case SAND:
                block.setType(Material.CLAY);
                break;
            default:
                block.setType(Material.SNOW_BLOCK);
                break;
        }
    }
}
