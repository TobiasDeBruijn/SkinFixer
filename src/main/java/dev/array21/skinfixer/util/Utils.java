package dev.array21.skinfixer.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
	
	public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
	}
	
	public static String insertDashUUID(String uuid) {
		StringBuilder sb = new StringBuilder(uuid);
		sb.insert(8, "-");
		sb = new StringBuilder(sb.toString());
		sb.insert(13, "-");
		sb = new StringBuilder(sb.toString());
		sb.insert(18, "-");
		sb = new StringBuilder(sb.toString());
		sb.insert(23, "-");
		
		return sb.toString();
	}
}
