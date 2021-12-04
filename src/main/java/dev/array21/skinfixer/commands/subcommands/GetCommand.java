package dev.array21.skinfixer.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.common.AddNewSkin;
import dev.array21.skinfixer.language.LangHandler;
import dev.array21.skinfixer.util.Pair;
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
			sender.sendMessage(ChatColor.RED + LangHandler.model.getCodeArgumentRequired);
			return;
		}
		
		if(args[0].contains("https://") || args[0].contains("http://")) {
			int indexOfQuery = args[0].indexOf('?');
			String url = args[0].substring(0, indexOfQuery > 0 ? indexOfQuery : args[0].length());

			int code = new AddNewSkin(plugin).addByUrl(url);
			sender.sendMessage(ChatColor.GOLD + LangHandler.model.getCodeSkinAdded.replaceAll("%CODE%", ChatColor.RED + String.valueOf(code) + ChatColor.GOLD));
			return;
		}
		
		Pair<Integer, String> codeResponse = new AddNewSkin(plugin).addByNickname(args[0]);
		if(codeResponse.getA() == null) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.getCodeFailedFetchingUuid.replaceAll("%ERROR%", ChatColor.GRAY + codeResponse.getB() + ChatColor.RED));
			return;
		}
		
		sender.sendMessage(ChatColor.GOLD + LangHandler.model.getCodeSkinAdded.replaceAll("%CODE%", ChatColor.RED + String.valueOf(codeResponse.getA()) + ChatColor.GOLD));
	}

	@Override
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
		return new String[0];
	}
}
