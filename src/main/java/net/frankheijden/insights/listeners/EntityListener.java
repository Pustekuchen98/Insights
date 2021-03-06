package net.frankheijden.insights.listeners;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.events.PlayerEntityDestroyEvent;
import net.frankheijden.insights.events.PlayerEntityPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class EntityListener implements Listener {

    private final MainListener listener;

    public EntityListener(MainListener listener) {
        this.listener = listener;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        handleEntityPlaceEvent(event, player, entity);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        Player player = listener.getInteractListener().getPlayerWithinRadius(entity.getLocation());

        if (player != null) {
            handleEntityPlaceEvent(event, player, entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (!(remover instanceof Player)) return;

        handleEntityDestroyEvent(event, (Player) remover, event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Entity remover = event.getAttacker();
        if (!(remover instanceof Player)) return;

        handleEntityDestroyEvent(event, (Player) remover, event.getVehicle());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        EntityDamageEvent entityDamageEvent = entity.getLastDamageCause();
        if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            Entity damager = entityDamageByEntityEvent.getDamager();

            if (damager instanceof Player) {
                // On some minecraft versions this statement is false
                if (event instanceof Cancellable) {
                    handleEntityDestroyEvent(event, (Player) damager, entity);
                } else {
                    handleEntityDestroyEvent(entityDamageByEntityEvent, (Player) damager, entity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Some fallback method for armorstands <= MC 1.10.2
        // because Armorstand destroy isn't called on EntityDeath
        if (PaperLib.getMinecraftVersion() >= 11) return;
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (!(damager instanceof Player)) return;
        if (!(entity instanceof ArmorStand)) return;
        handleEntityDestroyEvent(event, (Player) damager, entity);
    }

    public static void handleEntityPlaceEvent(Cancellable cancellable, Player player, Entity entity) {
        PlayerEntityPlaceEvent entityPlaceEvent = new PlayerEntityPlaceEvent(player, entity);
        Bukkit.getPluginManager().callEvent(entityPlaceEvent);
        cancellable.setCancelled(entityPlaceEvent.isCancelled());
    }

    public static void handleEntityDestroyEvent(Cancellable cancellable, Player player, Entity entity) {
        PlayerEntityDestroyEvent entityDestroyEvent = new PlayerEntityDestroyEvent(player, entity);
        Bukkit.getPluginManager().callEvent(entityDestroyEvent);
        cancellable.setCancelled(entityDestroyEvent.isCancelled());
    }
}
