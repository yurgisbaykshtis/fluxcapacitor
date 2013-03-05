<%@ page import="java.util.concurrent.Future" %>
<%@ page import="com.fluxcapacitor.edge.hystrix.BasicFallbackMiddleTierCommand" %>
<%@ page import="com.netflix.hystrix.HystrixCommand" %>

<%
	HystrixCommand<String> getCommand = new BasicFallbackMiddleTierCommand();
	Future<String> future = getCommand.queue();
	String responseString = future.get();

	if (getCommand.isResponseFromFallback()) {
		responseString += " fallback=true";
	}

	if (getCommand.isResponseTimedOut()) {
		responseString += " timeout=true";
	}

	if (getCommand.isFailedExecution()) {
		responseString += " failed=true";
	}
%>

<%= responseString %>