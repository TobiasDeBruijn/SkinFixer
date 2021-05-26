package nl.thedutchmc.SkinFixer.commandexecutors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.common.AddNewSkin;
import nl.thedutchmc.SkinFixer.language.LangHandler;

public class GetCodeCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandPlayerOnly);
			return true;
		}
 		
		if(!sender.hasPermission("skinfixer.getcode") ) {
			//sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
			sender.sendMessage(ChatColor.RED + LangHandler.model.commandNoPermission);
			return true;
		}
		
		if(args.length == 0) {
			//sender.sendMessage(ChatColor.RED + "You need to provide a URL to your skin!");
			sender.sendMessage(ChatColor.RED + LangHandler.model.getCodeUrlRequired);
			return true;
		}
		
		int code = AddNewSkin.add(args[0]);
		//sender.sendMessage(ChatColor.GOLD + "Your skin has been added! You can apply it with /setskin " + code);
		sender.sendMessage(ChatColor.GOLD + LangHandler.model.getCodeSkinAdded.replaceAll("%CODE%", ChatColor.RED + String.valueOf(code) + ChatColor.GOLD));
		return true;
	}
}
