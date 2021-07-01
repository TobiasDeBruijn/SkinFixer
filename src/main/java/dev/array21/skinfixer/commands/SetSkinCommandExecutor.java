package dev.array21.skinfixer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinChangeHandler;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

public class SetSkinCommandExecutor implements CommandExecutor {

	private SkinFixer plugin;
	
	public SetSkinCommandExecutor(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandPlayerOnly);
			return true;
		}
 		
		if(!sender.hasPermission("skinfixer.setskin") ) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandNoPermission);
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.setSkinCodeRequired);
			return true;
		}
		
		int code = 0;
		try {
			code = getIntFromString(args[0]);
		} catch(NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.setSkinCodeNotANumber);
			return true;
		}
				
		if(!this.plugin.getSkinCodeMap().containsKey(code)) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.setSkinCodeUnknown);
			return true;
		}
		
		String url = this.plugin.getSkinCodeMap().remove(code);
		Player p = (Player) sender;
		SkinChangeHandler sck = new SkinChangeHandler(this.plugin);
		
		//Check if the user has given an option for if it should be a slim skin model
		if(args.length == 2) {
			if(args[1].equals("true")) {
				//Slim model
				sck.changeSkinJson(url, p.getUniqueId(), null, true, false, false);
			} else {
				//Regular model
				sck.changeSkinJson(url, p.getUniqueId(), null, false, false, false);
			}
		} else {
			//Regular model
			sck.changeSkinJson(url, p.getUniqueId(), null, false, false, false);
		}
				
		return true;
	}
	
	private int getIntFromString(String str) throws NumberFormatException {
		return Integer.valueOf(str);
	}
}
