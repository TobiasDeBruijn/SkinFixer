package dev.array21.skinfixer.config;

import dev.array21.classvalidator.annotations.Required;

public class ConfigManifest {
	
	/**
	 * Should SkinFixer use Discord
	 */
	@Required
	public boolean useDiscord;
	
	/**
	 * The Discord Token SkinFixer should use
	 */
	public String token;
	
	/**
	 * The Discord channel SkinFixer should listen on
	 */
	public String channel;
	
	/**
	 * The language SkinFixer should use
	 * @since 1.4.0
	 */
	@Required
	public String language;
	
	/**
	 * The statistics UUID
	 * @since 1.4.1
	 */
	public String statUuid;
	
	/**
	 * Should statistics be disabled
	 * @since 1.4.1
	 */
	public boolean disableStat;
	
	/**
	 * Should a message be displayed informing the user that we're applying their skin when they log in. This excludes error messages
	 * @since 1.5.2
	 */
	public boolean disableSkinApplyOnLoginMessage;
}
