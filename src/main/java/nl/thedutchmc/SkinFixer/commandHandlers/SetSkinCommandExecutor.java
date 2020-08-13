package nl.thedutchmc.SkinFixer.commandHandlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.changeSkin.SkinChangeOrchestrator;
import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;

public class SetSkinCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only!");
			return true;
		}
 		
		if(!sender.hasPermission("skinfixer.setskin") ) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "You need to provide a code!");
			return true;
		}
		
		int code = 0;
		try {
			code = getIntFromString(args[0]);
		} catch(NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + "The code you entered is not a number!");
			return true;
		}
		
		if(!StorageHandler.pendingLinks.containsKey(code)) {
			sender.sendMessage(ChatColor.RED + "Unkown code!");
			return true;
		}
		
		String url = StorageHandler.pendingLinks.get(code);
		
		Player p = (Player) sender;
		
		//Check if the user has given an option for if it should be a slim skin model
		if(args.length == 2) {
			if(args[1].equals("true")) {
				//Slim model
				SkinChangeOrchestrator.changeSkinJson(url, p.getUniqueId(), null, true, false);
			} else {
				//Regular model
				SkinChangeOrchestrator.changeSkinJson(url, p.getUniqueId(), null, false, false);
			}
		} else {
			//Regular model
			SkinChangeOrchestrator.changeSkinJson(url, p.getUniqueId(), null, false, false);
		}
		
		SkinFixer.STORAGE.updateConfig();
		
		return true;
	}
	
	private int getIntFromString(String str) throws NumberFormatException {
		return Integer.valueOf(str);
	}
}
