package dev.array21.skinfixer.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.common.AddNewSkin;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "get", description = "Generate a code from a Skin url. URL must be the Skin image itself.", permission = "skinfixer.get")
public class GetCommand implements Subcommand {

	@Override
	public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.commandPlayerOnly);
			return;
		}
		
		if(args.length == 0) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.getCodeUrlRequired);
			return;
		}
		
		//Ignore any query parameters
		String url = args[0].substring(0, args[0].indexOf('?'));
		
		int code = new AddNewSkin(plugin).add(url);
		sender.sendMessage(ChatColor.GOLD + LangHandler.model.getCodeSkinAdded.replaceAll("%CODE%", ChatColor.RED + String.valueOf(code) + ChatColor.GOLD));
		return;
	}

	@Override
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
		return new String[0];
	}
}
