package nl.thedutchmc.SkinFixer.commandexecutors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.changeSkin.SkinChangeHandler;
import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;
import nl.thedutchmc.SkinFixer.language.LangHandler;

public class SetSkinCommandExecutor implements CommandExecutor {

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
		
		if(!StorageHandler.pendingLinks.containsKey(code)) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.setSkinCodeUnknown);
			return true;
		}
		
		String url = StorageHandler.pendingLinks.get(code);
		
		Player p = (Player) sender;
		
		//Check if the user has given an option for if it should be a slim skin model
		if(args.length == 2) {
			if(args[1].equals("true")) {
				//Slim model
				SkinChangeHandler.changeSkinJson(url, p.getUniqueId(), null, true, false);
			} else {
				//Regular model
				SkinChangeHandler.changeSkinJson(url, p.getUniqueId(), null, false, false);
			}
		} else {
			//Regular model
			SkinChangeHandler.changeSkinJson(url, p.getUniqueId(), null, false, false);
		}
		
		SkinFixer.STORAGE.updateConfig();
		
		return true;
	}
	
	private int getIntFromString(String str) throws NumberFormatException {
		return Integer.valueOf(str);
	}
}
