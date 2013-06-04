<%@ page import="java.util.concurrent.Future" %>
<%@ page import="com.fluxcapacitor.edge.hystrix.GetLogsCommand" %>
<%@ page import="com.netflix.hystrix.HystrixCommand" %>

<%
	HystrixCommand<String> getCommand = new GetLogsCommand("1234");
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