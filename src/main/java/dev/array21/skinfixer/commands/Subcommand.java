package dev.array21.skinfixer.commands;

import org.bukkit.command.CommandSender;

import dev.array21.skinfixer.SkinFixer;

public interface Subcommand {
	public void onSubcommand(SkinFixer plugin, CommandSender sender, String[] args);
	public String[] onSubcommandComplete(SkinFixer plugin, CommandSender sender, String[] args);
}