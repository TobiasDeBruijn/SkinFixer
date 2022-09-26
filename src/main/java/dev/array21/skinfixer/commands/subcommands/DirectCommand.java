package dev.array21.skinfixer.commands.subcommands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinChangeHandler;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.common.AddNewSkin;
import dev.array21.skinfixer.language.LangHandler;
import dev.array21.skinfixer.util.Pair;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "direct", description = "Fetches a skin code and immediately applies skin to sender. URL must be Skin image. Only accepts URLs.", permission = "skinfixer.direct")
public class DirectCommand implements Subcommand {

    @Override
    public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {
        int code = 0;
        if (!(sender instanceof Player)) {
            sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.commandPlayerOnly);
            return;
        }
        // TODO: add new language option for direct command
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + LangHandler.model.getCodeArgumentRequired);
            return;
        }
        if (args[0].contains("https://") || args[0].contains("http://")) {
            int indexOfQuery = args[0].indexOf('?');
            int endIndex = indexOfQuery > 0 ? indexOfQuery : args[0].length();
            String url = args[0].substring(0, endIndex);

            code = new AddNewSkin(plugin).addByUrl(url);
            //sender.sendMessage(ChatColor.GOLD + LangHandler.model.getCodeSkinAdded.replaceAll("%CODE%", ChatColor.RED + String.valueOf(code) + ChatColor.GOLD));
        }

        Player targetPlayer = (Player) sender;
        boolean slim = args.length >= 2 && args[1].equalsIgnoreCase("true");
        SkinChangeHandler sck = new SkinChangeHandler(plugin);

        if (plugin.getSkinCodeUrlMap().containsKey(code)) {
            String url2 = plugin.getSkinCodeUrlMap().remove(code);
            sck.changeSkinJson(url2, targetPlayer.getUniqueId(), null, slim, false, false);

        } else {
            sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.setSkinCodeUnknown);
        }
    }

    @Override
    public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
        return new String[0];
    }
}
