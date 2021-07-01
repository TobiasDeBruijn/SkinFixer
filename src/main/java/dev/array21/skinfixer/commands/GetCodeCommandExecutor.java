package dev.array21.skinfixer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.common.AddNewSkin;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

public class GetCodeCommandExecutor implements CommandExecutor {

	private SkinFixer plugin;

	public GetCodeCommandExecutor(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandPlayerOnly);
			return true;
		}
 		
		if(!sender.hasPermission("skinfixer.getcode") ) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandNoPermission);
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.getCodeUrlRequired);
			return true;
		}
		
		int code = new AddNewSkin(this.plugin).add(args[0]);
		sender.sendMessage(ChatColor.GOLD + LangHandler.model.getCodeSkinAdded.replaceAll("%CODE%", ChatColor.RED + String.valueOf(code) + ChatColor.GOLD));
		return true;
	}
}
