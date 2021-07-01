package dev.array21.skinfixer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

public class SkinFixerCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { 
	
		final ChatColor cg = ChatColor.GOLD;
		final ChatColor cw = ChatColor.WHITE;
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.GOLD + LangHandler.model.skinFixerNoOptionProvided);
			return true;
		}
		
		if(args[0].equals("help")) {
			sender.sendMessage(cg + "SkinFixer help");
			sender.sendMessage(cg + "------------");
			sender.sendMessage("- " + cg + "/setskin <code> [slim true/false]" + cw + " " + LangHandler.model.skinFixerSetSkinHelp);
			sender.sendMessage("- " + cg + "/getcode <url>" + cw + " " + LangHandler.model.skinFixerGetCodeHelp);
			sender.sendMessage("- " + cg + "/skinfixer help" + cw + " " + LangHandler.model.skinFixerShowHelp);
			sender.sendMessage("- " + cg + "/skinfixer version" + cw + " " + LangHandler.model.skinFixerVersionHelp);
			sender.sendMessage("- " + cg + "/resetskin" + cw + " " + LangHandler.model.skinFixerVersionHelp);
			
			return true;
		}
		
		if(args[0].equals("version")) {
			sender.sendMessage(cg + LangHandler.model.skinFixerVersion.replaceAll("%VERSION%", ChatColor.RED + SkinFixer.PLUGIN_VERSION + ChatColor.GOLD));
			
			return true;
		}
		
		return false;
	}
}
