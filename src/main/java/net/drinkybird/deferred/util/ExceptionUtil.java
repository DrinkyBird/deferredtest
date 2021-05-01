package net.drinkybird.deferred.util;

public class ExceptionUtil {
	private ExceptionUtil() {
		
	}
	
	private static void format(Throwable t, StringBuffer buffer) {
		buffer.append(t.getClass().getName());
		buffer.append(": ");
		buffer.append(t.getMessage());
		buffer.append('\n');
		
		for (var element : t.getStackTrace()) {
			buffer.append("\tat ");
			buffer.append(element.toString());
			buffer.append('\n');
		}
		
		if (t.getCause() != null) {
			buffer.append("Caused by: ");
			format(t.getCause(), buffer);
		}
	}
	
	public static String formatThrowable(Throwable t) {
		StringBuffer buffer = new StringBuffer();
		format(t, buffer);
		return buffer.toString();
	}
}
