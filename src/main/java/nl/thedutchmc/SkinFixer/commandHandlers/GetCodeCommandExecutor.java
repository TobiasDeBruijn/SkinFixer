package nl.thedutchmc.SkinFixer.commandHandlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.commonEventMethods.AddNewSkin;

public class GetCodeCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only!");
			return true;
		}
 		
		if(!sender.hasPermission("skinfixer.getcode") ) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "You need to provide a URL to your skin!");
			return true;
		}
		
		int code = AddNewSkin.add(args[0]);
		sender.sendMessage(ChatColor.GOLD + "Your skin has been added! You can apply it with /setskin " + code);
		
		return true;
	}
}
