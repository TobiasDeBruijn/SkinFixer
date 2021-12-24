package dev.array21.skinfixer.commands.subcommands;

import java.math.BigInteger;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.skinfixer.SkinChangeHandler;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "set", description = "Set your skin from a code.", permission = "skinfixer.set")
public class SetCommand implements Subcommand {

	@Override
	public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.commandPlayerOnly);
			return;
		}
		
		if(args.length == 0) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.setSkinCodeRequired);
			return;
		}
		
		if(!isValidInt(args[0])) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.setSkinCodeNotANumber);
			return;
		}
		
		
		boolean slim = false;
		if(args.length == 2 && args[1].equalsIgnoreCase("true")) {
			slim = true;
		}
		
		int code = Integer.valueOf(args[0]);
		Player p = (Player) sender;
		SkinChangeHandler sck = new SkinChangeHandler(plugin);

		if(plugin.getSkinCodeUrlMap().containsKey(code)) {
			String url = plugin.getSkinCodeUrlMap().remove(code);			
			sck.changeSkinJson(url, p.getUniqueId(), null, slim, false, false);
			
		} else if(plugin.getSkinCodeUuidMap().containsKey(code)) {
			String externalUuid = plugin.getSkinCodeUuidMap().remove(code);
			sck.changeSkinFromUuid(UUID.fromString(externalUuid), p.getUniqueId(), slim);
		
		} else {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.setSkinCodeUnknown);
			return;
		}
	}

	@Override
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
		return new String[0];
	}
	
	/**
	 * Check if a String is a valid Integer:
	 * - It matches a Regex
	 * - It is less than Integer.MAX_VALUE
	 * @param s The String to check
	 * @return True if it is valid, false if it is not
	 */
	private boolean isValidInt(String s) {
		if(!s.matches("^\\d*$")) {
			return false;
		}
				
		BigInteger bi = new BigInteger(s);
		if(bi.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) > 0) {
			return false;
		}
		
		return true;
	}
}
