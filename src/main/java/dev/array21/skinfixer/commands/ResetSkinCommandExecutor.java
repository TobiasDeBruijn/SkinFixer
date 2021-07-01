package dev.array21.skinfixer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

public class ResetSkinCommandExecutor implements CommandExecutor {

	private SkinFixer plugin;
	
	public ResetSkinCommandExecutor(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandPlayerOnly);
			return true;
		}
 		
		if(!sender.hasPermission("skinfixer.resetskin") ) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandNoPermission);
			return true;
		}
		
		this.plugin.getStorageHandler().getManifest().deleteForPlayer(((Player) sender).getUniqueId());
		this.plugin.getStorageHandler().saveConfig();
		sender.sendMessage(ChatColor.GOLD + LangHandler.model.skinReset);
		
		return true;
	}
}