package nl.thedutchmc.SkinFixer;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import nl.thedutchmc.SkinFixer.discordEvents.MessageReceivedEventListener;
import nl.thedutchmc.SkinFixer.fileHandlers.ConfigurationHandler;

public class JdaHandler {

	private static JDA jda;
	private static MessageChannel channel;
	
	public void setupJda() {
		
		List<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_MESSAGES);
		
		try {
			jda = JDABuilder.createDefault(ConfigurationHandler.token)
					.enableIntents(intents)
					.build();
			
			jda.awaitReady();
			
		} catch (LoginException e) {
			SkinFixer.logWarn("Unable to connecto the Discord API. Is your token correct?");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Register event listeners
		jda.addEventListener(new MessageReceivedEventListener());
		
		channel = jda.getTextChannelById(ConfigurationHandler.channel);
	}
	
	public static JDA getJda() {
		return jda;
	}
	
	public static MessageChannel getChannel() {
		return channel;
	}
	
	public static void shutdownJda() throws Exception {
		jda.shutdownNow();
	}
}
