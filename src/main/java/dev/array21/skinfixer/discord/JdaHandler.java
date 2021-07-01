package dev.array21.skinfixer.discord;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.config.ConfigManifest;
import dev.array21.skinfixer.discord.events.MessageReceivedEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class JdaHandler {

	private static JDA jda;
	private static MessageChannel channel;
	private SkinFixer plugin;
	
	public JdaHandler(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	public void setupJda() {
		ConfigManifest manifest = this.plugin.getConfigManifest();
		
		List<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_MESSAGES);
		
		try {
			jda = JDABuilder.createDefault(manifest.token)
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
		jda.addEventListener(new MessageReceivedEventListener(this.plugin));
		
		channel = jda.getTextChannelById(manifest.channel);
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
