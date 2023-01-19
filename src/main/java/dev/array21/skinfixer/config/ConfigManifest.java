package dev.array21.skinfixer.config;

import dev.array21.classvalidator.annotations.Required;

public class ConfigManifest {
	
	@Required
	public Integer configVersion;
	
	@Required
	public DatabaseType databaseType;
	
	public SqlSettings sqlSettings;
	
	@Required
	public Boolean useDiscord;
	
	public DiscordSettings discordSettings;
	
	@Required
	public String language;
	
	@Required
	public Boolean updateCheck;

	@Required
	public Boolean disableSkinApplyOnLoginMessage;

	public Boolean applySkinOnJoin;
}
