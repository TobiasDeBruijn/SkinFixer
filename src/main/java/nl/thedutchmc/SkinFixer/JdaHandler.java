package nl.thedutchmc.SkinFixer;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import nl.thedutchmc.SkinFixer.discordEvents.MessageReceivedEventListener;

public class JdaHandler {

	private static JDA jda;
	private static MessageChannel channel;
	
	public void setupJda() {
		JDABuilder jdaBuilder = JDABuilder.createDefault(ConfigurationHandler.token);
		
		try {
			jda = jdaBuilder.build();
			jda.awaitReady();
			
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
}
