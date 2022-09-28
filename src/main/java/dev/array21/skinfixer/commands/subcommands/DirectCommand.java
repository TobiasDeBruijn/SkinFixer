package dev.array21.skinfixer.commands.subcommands;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinChangeHandler;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.common.AddNewSkin;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "direct", description = "Fetches a skin code and immediately applies skin to sender. URL must be Skin image. Only accepts URLs.", permission = "skinfixer.set", globalCommand = "setskin")
public class DirectCommand implements Subcommand {

    @Override
    public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.commandPlayerOnly);
            return;
        }

        // User must provide a URL
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + LangHandler.model.missingUrlArgument);
            return;
        }

        // Check that the user actually supplied a URL
        if (!(args[0].contains("https://") || args[0].contains("http://"))) {
            sender.sendMessage(ChatColor.RED + LangHandler.model.invalidUrlArgument);
        }

        // Filter off the query parameters
        int indexOfQuery = args[0].indexOf('?');
        int endIndex = indexOfQuery > 0 ? indexOfQuery : args[0].length();
        String url = args[0].substring(0, endIndex);

        // Check if the skin is a slim model
        boolean slim = args.length >= 2 && args[1].equalsIgnoreCase("true");

        // Apply the skin
        new SkinChangeHandler(plugin).changeSkinJson(url, ((Player) sender).getUniqueId(), null, slim, false, false);
    }

    @Override
    public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
        return new String[0];
    }
}
