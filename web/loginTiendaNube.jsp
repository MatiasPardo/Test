<%@page import="java.net.URL"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>"HTTP/1.1 200 OK"</title>
</head>
<body>
    </form>
	<%@page import="java.io.*"%>
	<%@page import="java.util.Date"%>
	<%@page import="java.lang.String"%>	
	<%@page import="javax.servlet.*"%>
	<%@page import="javax.servlet.http.*"%>
	<%@page import="java.text.*"%>
	<%@page import="java.io.BufferedReader.*"%>
	<%@page import="java.io.ByteArrayInputStream.*"%>
	<%@page import="java.io.IOException.*"%>
	<%@page import="java.io.InputStream.*"%>
	<%@page import="java.io.InputStreamReader.*"%>
	<%@page import="java.net.URL.*"%>
<%
	String inputText = new String();
	String inputLine = new String();
	StringBuffer inputBuffer = new StringBuffer();
	BufferedReader bis = null;
	//String esquema =request.getParameter("esquema");
	try {
		bis = new BufferedReader(new InputStreamReader(request.getInputStream()));
		while ((inputLine = bis.readLine()) != null) {
			inputBuffer.append(inputLine);
		}
	
		String req = new String(inputBuffer.toString());
		session.setAttribute("codeTN", request.getParameter("code"));
	}
	catch(Exception e){
		Date time = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");		
		PrintStream printStream = null;
		try{
			File strFile = new File("C:/Users/Matias/Desktop/prueba.txt");			
			printStream = new PrintStream(new FileOutputStream(inputText));
			printStream.print(e.toString());
			printStream.flush();
		}
		finally{
			if (printStream != null){
				printStream.close();
			}			
		}
		throw e;
	}
	finally {
		if (bis != null){
			bis.close();
			session.invalidate();
		}
	}
	
	
	
%> 
</body>
</html>