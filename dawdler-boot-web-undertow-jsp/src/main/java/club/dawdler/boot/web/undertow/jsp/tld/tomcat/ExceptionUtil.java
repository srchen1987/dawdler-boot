package club.dawdler.boot.web.undertow.jsp.tld.tomcat;

public class ExceptionUtil {
	public static void handleThrowable(Throwable t) {
		if (t instanceof ThreadDeath) {
			throw (ThreadDeath) t;
		}
		if (t instanceof StackOverflowError) {
			// Swallow silently - it should be recoverable
			return;
		}
		if (t instanceof VirtualMachineError) {
			throw (VirtualMachineError) t;
		}
		// All other instances of Throwable will be silently swallowed
	}

}
