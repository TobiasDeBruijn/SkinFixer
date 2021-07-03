package dev.array21.skinfixer.commands.subcommands;

import java.math.BigInteger;

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
		
		int code = Integer.valueOf(args[0]);
		if(!plugin.getSkinCodeMap().containsKey(code)) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.RED + LangHandler.model.setSkinCodeUnknown);
			return;
		}
		
		String url = plugin.getSkinCodeMap().remove(code);
		Player p = (Player) sender;
		SkinChangeHandler sck = new SkinChangeHandler(plugin);
		
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
				
		return;
		
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
		if(!s.matches("(\\d*.)")) {
			return false;
		}
				
		BigInteger bi = new BigInteger(s);
		if(bi.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) > 0) {
			return false;
		}
		
		return true;
	}
}
