package dev.array21.skinfixer.commands.subcommands;

import java.util.HashMap;

import org.bukkit.command.CommandSender;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "help", description = "Get information about the commands SkinFixer has and how to use them")
public class HelpCommand implements Subcommand {

	private final HashMap<String, String> helpMessages;
	public HelpCommand(HashMap<String, String> helpMessages) {
		this.helpMessages = helpMessages;
	}
	
	@Override
	public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {		
		String p = SkinFixer.getPrefix();
		ChatColor cg = ChatColor.GOLD;
		ChatColor cw = ChatColor.WHITE;
		
		sender.sendMessage(p + cg + "SkinFixer Help");
		helpMessages.forEach((command, help) -> {
			sender.sendMessage(p + "- " + cg + command + cw + " " + help);
		});
	}

	@Override
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
		return new String[0];
	}
}
