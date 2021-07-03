package dev.array21.skinfixer.language;

import dev.array21.classvalidator.annotations.Required;

public class LanguageModel {

	// SkinChangeHandler
	@Required
	public String skinFetching;
	@Required
	//Variable: %ERROR%
	public String skinApplyFailed;
	@Required
	public String skinApplying;
	@Required
	public String skinApplied;
	
	//General commands
	@Required
	public String commandPlayerOnly;
	@Required
	public String commandNoPermission;
	
	//GetCodeCommandExecutor
	@Required
	public String getCodeUrlRequired;
	@Required
	//Variable: %CODE%
	public String getCodeSkinAdded;
	
	//SetSkinCommandExecutor
	@Required
	public String setSkinCodeRequired;
	@Required
	public String setSkinCodeNotANumber;
	@Required
	public String setSkinCodeUnknown;
	
	@Required
	//Variable: %VERSION%
	public String skinFixerVersion;
	
	//MessageReceivedEventListener
	@Required
	//Variable: %CODE%
	public String discordSetSkin;
	
	//ResetSkinCommandExecutor
	@Required
	public String skinReset;
	
	@Required
	public String unknownCommand;
}
