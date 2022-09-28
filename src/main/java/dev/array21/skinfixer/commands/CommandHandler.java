package dev.array21.skinfixer.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.commands.subcommands.DirectCommand;
import dev.array21.skinfixer.commands.subcommands.GetCommand;
import dev.array21.skinfixer.commands.subcommands.HelpCommand;
import dev.array21.skinfixer.commands.subcommands.ReloadCommand;
import dev.array21.skinfixer.commands.subcommands.ResetCommand;
import dev.array21.skinfixer.commands.subcommands.SetCommand;
import dev.array21.skinfixer.commands.subcommands.VersionCommand;
import dev.array21.skinfixer.language.LangHandler;
import net.md_5.bungee.api.ChatColor;

public class CommandHandler implements CommandExecutor, TabCompleter {

	private final HashMap<String, Subcommand> subcommands = new HashMap<>();
	private final HashMap<String, String> helpMessages = new HashMap<>();
	private final HashMap<Subcommand, String> subcommandPermissions = new HashMap<>();
	
	public SkinFixer plugin;
	
	public CommandHandler(SkinFixer plugin) {
		this.plugin = plugin;
		
		registerCommand(new ResetCommand());
		registerCommand(new HelpCommand(this.helpMessages));
		registerCommand(new VersionCommand());
		registerCommand(new SetCommand());
		registerCommand(new GetCommand());
		registerCommand(new ReloadCommand());
		registerCommand(new DirectCommand());
	}
	
	/**
	 * Register a Subcommand. The provided Subcommand must be annotated with {@link CommandInfo}
	 * @param cmd The Subcommand to add
	 * @throws IllegalArgumentException When the provided Subcommand is not annotated with {@link CommandInfo}
	 */
	private void registerCommand(Subcommand cmd) throws IllegalArgumentException {
		if(!cmd.getClass().isAnnotationPresent(CommandInfo.class)) {
			throw new IllegalArgumentException("Provided Subcommand is not annotated with CommandInfo");
		}
		
		CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
		if(this.subcommands.containsKey(info.name())) {
			throw new IllegalStateException(String.format("Command with name '%s' is already registered.", info.name()));
		}

		this.subcommands.put(info.name(), cmd);
		this.helpMessages.put(info.name(), info.description());
		this.subcommandPermissions.put(cmd, info.permission());

		if(!info.globalCommand().equals("")) {
			if(this.plugin.getCommand(info.globalCommand()) == null) {
				throw new IllegalStateException(String.format("Subcommand '%s' called for '%s' to be registered as a global command, however the command is not registered in 'plugin.yml'", info.name(), info.globalCommand()));
			}

			this.plugin.getCommand(info.globalCommand()).setExecutor((sender, command, label, args) -> CommandHandler.this.onCommandHandler(sender, args, cmd));

			this.plugin.getCommand(info.globalCommand()).setTabCompleter((sender, command, alias, args) -> CommandHandler.this.onTabCompleteHandler(sender, args, cmd));
		}
	}

	private boolean onCommandHandler(CommandSender sender, String[] args, Subcommand subcommand) {
		if(!this.subcommandPermissions.get(subcommand).isEmpty() && !sender.hasPermission(this.subcommandPermissions.get(subcommand))) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.GOLD + LangHandler.model.commandNoPermission);
			return true;
		}

		subcommand.onSubcommand(this.plugin, sender, Arrays.copyOfRange(args, 1, args.length));
		return true;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0) {
			this.subcommands.get("help").onSubcommand(this.plugin, sender, args);
			return true;
		}
		
		Subcommand exec = this.subcommands.get(args[0]);
		if(exec == null) {
			sender.sendMessage(SkinFixer.getPrefix() + ChatColor.GOLD + LangHandler.model.unknownCommand);
			return true;
		}
		
		return this.onCommandHandler(sender, args, exec);
	}

	private List<String> onTabCompleteHandler(CommandSender sender, String[] args, Subcommand subcommand) {
		return Arrays.asList(subcommand.onSubcommandComplete(this.plugin, sender, Arrays.copyOfRange(args, 1, args.length)));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {		
		if(args.length == 1) {
			return List.copyOf(subcommands.keySet());
		}
		
		Subcommand exec = this.subcommands.get(args[0]);
		if(exec == null) {
			return null;
		}

		return this.onTabCompleteHandler(sender, args, exec);
	}
}
