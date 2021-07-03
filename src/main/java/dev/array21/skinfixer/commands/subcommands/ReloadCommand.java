package dev.array21.skinfixer.commands.subcommands;

import org.bukkit.command.CommandSender;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "reload", description = "Reload the configuration from disk.", permission = "skinfixer.reload")
public class ReloadCommand implements Subcommand{

	@Override
	public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {
		plugin.reloadConfigManifest();
		plugin.reloadJda();
		
		sender.sendMessage(SkinFixer.getPrefix() + ChatColor.GOLD + LangHandler.model.reloadSuccessful);
	}

	@Override
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
		return new String[0];
	}
}
