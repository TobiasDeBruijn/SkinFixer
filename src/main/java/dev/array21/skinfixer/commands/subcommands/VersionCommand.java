package dev.array21.skinfixer.commands.subcommands;

import org.bukkit.command.CommandSender;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.CommandInfo;
import dev.array21.skinfixer.commands.Subcommand;
import dev.array21.skinfixer.updatechecker.UpdateChecker;
import dev.array21.skinfixer.util.Pair;
import net.md_5.bungee.api.ChatColor;

@CommandInfo(name = "version", description = "Check if there is a new version of SkinFixer available", permission = "skinfixer.update")
public class VersionCommand implements Subcommand {

	@Override
	public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args) {
		Pair<Boolean, String> updateResult = new UpdateChecker(plugin).checkUpdate();
		sender.sendMessage(SkinFixer.getPrefix() + ((updateResult.getA()) ? ChatColor.GOLD : ChatColor.RED) + updateResult.getB());

		if(updateResult.getA()) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.GOLD + "You are currently running SkinFixer version " + plugin.getDescription().getVersion() + "!");
		}
	}

	@Override
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args) {
		return new String[0];
	}
}