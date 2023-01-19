package dev.array21.skinfixer.apis.gson;

public class RemoteInfoManifest {

    public String[] servers;

    public Message[] messages;

    public static class Message {
        public String text;
    }
}
