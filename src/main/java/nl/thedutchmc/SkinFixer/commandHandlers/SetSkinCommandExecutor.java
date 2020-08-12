package nl.thedutchmc.SkinFixer.commandHandlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.StorageHandler;
import nl.thedutchmc.SkinFixer.changeSkin.SkinChangeOrchestrator;

public class SetSkinCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only!");
			return true;
		}
 		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "You need to provide a code!");
			return true;
		}
		
		String code = args[0];
		
		if(!StorageHandler.pendingLinks.containsKey(code)) {
			sender.sendMessage(ChatColor.RED + "Unkown code!");
			return true;
		}
		
		String url = StorageHandler.pendingLinks.get(code);
		
		Player p = (Player) sender;
		SkinChangeOrchestrator.changeSkin(url, p.getUniqueId());
		
		SkinFixer.STORAGE.updateConfig();
		
		return false;
	}
}
