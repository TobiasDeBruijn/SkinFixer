package dev.array21.skinfixer.discord.events;

import java.util.List;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.common.AddNewSkin;
import dev.array21.skinfixer.discord.JdaHandler;
import dev.array21.skinfixer.language.LangHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceivedEventListener extends ListenerAdapter {

	private SkinFixer plugin;
	
	public MessageReceivedEventListener(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		final MessageChannel msgChannel = event.getChannel();
		
		if(!msgChannel.equals(JdaHandler.getChannel())) return;
		
		Message msg = event.getMessage();
		
		if(msg.getAttachments().size() == 0) return;
		List<Attachment> attachments = msg.getAttachments();
		
		AddNewSkin ans = new AddNewSkin(this.plugin);
		for(Attachment a : attachments) {
			int n = ans.add(a.getUrl());
			msgChannel.sendMessage(LangHandler.model.discordSetSkin.replaceAll("%CODE%", String.valueOf(n))).queue();
		}
	}
}
