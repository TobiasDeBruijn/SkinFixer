package dev.array21.skinfixer.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "reset", description = "Reset your skin, if you have a premium account your real skin will be applied when you log in again.", permission = "skinfixer.reset")
public class ResetCommand implements Subcommand {

	@Override
	public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.commandPlayerOnly);
			return;
		}
		
		plugin.getStorageHandler().getManifest().deleteForPlayer(((Player) sender).getUniqueId());
		plugin.getStorageHandler().saveConfig();
		sender.sendMessage(SkinFixer.getPrefix() + ChatColor.GOLD + LangHandler.model.skinReset);
	}

	@Override
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
		return new String[0];
	}
}