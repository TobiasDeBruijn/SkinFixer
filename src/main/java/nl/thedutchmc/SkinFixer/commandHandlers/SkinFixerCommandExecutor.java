package nl.thedutchmc.SkinFixer.commandHandlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.SkinFixer;

public class SkinFixerCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { 
	
		final ChatColor cg = ChatColor.GOLD;
		final ChatColor cw = ChatColor.WHITE;
		
		if(args.length == 0) {
			sender.sendMessage(cg + "No option provided. See " + ChatColor.RED + "/skinfixer help " + cg + "for help!");
			return true;
		}
		
		if(args[0].equals("help")) {
			sender.sendMessage(cg + "SkinFixer help");
			sender.sendMessage(cg + "------------");
			sender.sendMessage("- " + cg + "/setskin <code> [slim true/false]" + cw + " Set your skin from a code.");
			sender.sendMessage("- " + cg + "/getcode <url>" + cw + " Generate a code from a Skin url. URL must be the skinfie itself.");
			sender.sendMessage("- " + cg + "/skinfixer help" + cw + " Shows this page");
			sender.sendMessage("- " + cg + "/skinfixer version" + cw + " Returns the version of SkinFixer you are using");
		
			return true;
		}
		
		if(args[0].equals("version")) {
			sender.sendMessage(cg + "You are using SkinFixer version " + ChatColor.RED + SkinFixer.PLUGIN_VERSION);
			
			return true;
		}
		
		return false;
	}
}
