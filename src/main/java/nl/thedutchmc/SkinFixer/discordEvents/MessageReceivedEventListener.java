package nl.thedutchmc.SkinFixer.discordEvents;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.thedutchmc.SkinFixer.JdaHandler;
import nl.thedutchmc.SkinFixer.common.AddNewSkin;
import nl.thedutchmc.SkinFixer.language.LangHandler;

public class MessageReceivedEventListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		final MessageChannel msgChannel = event.getChannel();
		
		if(!msgChannel.equals(JdaHandler.getChannel())) return;
		
		Message msg = event.getMessage();
		
		if(msg.getAttachments().size() == 0) return;
		List<Attachment> attachments = msg.getAttachments();
		for(Attachment a : attachments) {
			int n = AddNewSkin.add(a.getUrl());
			
			msgChannel.sendMessage(LangHandler.model.discordSetSkin.replaceAll("%CODE%", String.valueOf(n))).queue();
		}
	}
}
