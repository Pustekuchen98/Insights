package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.managers.HookManager;
import net.frankheijden.insights.managers.WorldGuardManager;
import net.frankheijden.insights.utils.MessageUtils;
import net.frankheijden.insights.utils.PlayerUtils;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandInsights implements CommandExecutor, TabExecutor {

    private static final Insights plugin = Insights.getInstance();
    public static final int DEFAULT_BLOCK_RANGE = 100;
    public static final int DEFAULT_ENTITY_RANGE = 25;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new String[]{
                    MessageUtils.color("&8&l&m---------------=&r&8[ &b&lInsights&8 ]&l&m=----------------"),
                    MessageUtils.color("&b Plugin version: &7" + plugin.getDescription().getVersion()),
                    MessageUtils.color("&b Plugin author: &7https://www.spigotmc.org/members/213966/"),
                    MessageUtils.color("&b Plugin link: &7https://www.spigotmc.org/resources/56489/"),
                    MessageUtils.color("&8&m-------------------------------------------------")
            });
            return true;
        } else if (args[0].equalsIgnoreCase("block") || args[0].equalsIgnoreCase("entity")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command cannot be executed from console!");
                return true;
            }
            Player player = (Player) sender;

            boolean isBlock = args[0].equalsIgnoreCase("block");
            int range = -1;
            if (args.length == 1) {
                if (isBlock) {
                    range = DEFAULT_BLOCK_RANGE;
                } else {
                    range = DEFAULT_ENTITY_RANGE;
                }
            } else if (args.length == 2) {
                try {
                    range = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    MessageUtils.sendMessage(player, "messages.insights.invalid_number");
                    return true;
                }
            }

            if (range < 0) {
                return false;
            }

            if (player.hasPermission("insights." + args[0].toLowerCase())) {
                if (isBlock) {
                    Block target = PlayerUtils.getTargetBlock(player, range);
                    if (target != null) {
                        MessageUtils.sendMessage(player, "messages.insights.block",
                                "%block%", target.getType().name());
                    } else {
                        MessageUtils.sendMessage(player, "messages.insights.invalid_block");
                    }
                } else {
                    Entity target = PlayerUtils.getTargetEntity(player, range);
                    if (target != null) {
                        MessageUtils.sendMessage(player, "messages.insights.entity",
                                "%entity%", target.getType().name());
                    } else {
                        MessageUtils.sendMessage(player, "messages.insights.invalid_entity");
                    }
                }
            } else {
                MessageUtils.sendMessage(player, "messages.no_permission");
            }
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("insights.reload")) {
                    try {
                        plugin.reload();
                    } catch (Exception ex) {
                        MessageUtils.sendMessage(sender, "messages.insights.reload_failed");
                        return true;
                    }

                    MessageUtils.sendMessage(sender, "messages.insights.reload");
                } else {
                    MessageUtils.sendMessage(sender, "messages.no_permission");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("hooks")) {
                if (sender.hasPermission("insights.hooks")) {
                    List<String> plugins = new ArrayList<>();
                    HookManager.getInstance().getHooks().forEach(hook -> plugins.add(hook.getPlugin().getName()));

                    if (plugin.hasPlaceholderAPIHook()) {
                        plugins.add("PlaceHolderAPI");
                    }
                    if (WorldGuardManager.getInstance() != null) {
                        plugins.add("WorldGuard");
                    }
                    Collections.sort(plugins);

                    if (plugins.size() > 0) {
                        MessageUtils.sendMessage(sender, "messages.insights.hooks.header");
                        plugins.forEach(pl -> MessageUtils.sendMessage(sender, "messages.insights.hooks.format", "%plugin%", pl));
                        MessageUtils.sendMessage(sender, "messages.insights.hooks.footer");
                    } else {
                        MessageUtils.sendMessage(sender, "messages.insights.hooks.none");
                    }
                } else {
                    MessageUtils.sendMessage(sender, "messages.no_permission");
                }
                return true;
            }
        }

        MessageUtils.sendMessage(sender, "messages.insights.help");
        return true;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Collections.singletonList("help"));
            if (sender.hasPermission("insights.block")) {
                list.add("block");
            }
            if (sender.hasPermission("insights.entity")) {
                list.add("entity");
            }
            if (sender.hasPermission("insights.hooks")) {
                list.add("hooks");
            }
            if (sender.hasPermission("insights.reload")) {
                list.add("reload");
            }
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
