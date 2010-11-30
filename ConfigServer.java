
import java.security.SecureRandom;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import com.sun.net.httpserver.*;

public class ConfigServer{
	HttpServer server;
	HashMap<Long,AccessEntry> Access;
	class MyHandler implements HttpHandler {
		
			//Copied this function from the internet, shame on me :-P
		    //wonder why java does not have a similar function
			public String stringToHTMLString(String string) {
			    StringBuffer sb = new StringBuffer(string.length());
			    // true if last char was blank
			    boolean lastWasBlankChar = false;
			    int len = string.length();
			    char c;
	
			    for (int i = 0; i < len; i++)
			        {
			        c = string.charAt(i);
			        if (c == ' ') {
			            // blank gets extra work,
			            // this solves the problem you get if you replace all
			            // blanks with &nbsp;, if you do that you loss 
			            // word breaking
			            if (lastWasBlankChar) {
			                lastWasBlankChar = false;
			                sb.append("&nbsp;");
			                }
			            else {
			                lastWasBlankChar = true;
			                sb.append(' ');
			                }
			            }
			        else {
			            lastWasBlankChar = false;
			            //
			            // HTML Special Chars
			            if (c == '"')
			                sb.append("&quot;");
			            else if (c == '&')
			                sb.append("&amp;");
			            else if (c == '<')
			                sb.append("&lt;");
			            else if (c == '>')
			                sb.append("&gt;");
			            else if (c == '\n')
			                // Handle Newline
			                sb.append("&lt;br/&gt;");
			            else {
			                int ci = 0xffff & c;
			                if (ci < 160 )
			                    // nothing special only 7 Bit
			                    sb.append(c);
			                else {
			                    // Not 7 Bit use the unicode system
			                    sb.append("&#");
			                    sb.append(new Integer(ci).toString());
			                    sb.append(';');
			                    }
			                }
			            }
			        }
			    return sb.toString();
			}
			
			private HashMap<String,String> getParams(String txt)throws UnsupportedEncodingException
			{
		           String[] parts=txt.split("&");
		           HashMap<String,String> params=new HashMap<String,String>();
		           for(String p:parts)
		           {
		        	   if(p.equals(""))continue;
		        	   if(p.indexOf("=")<=0)continue;
		        	   String pname=p.substring(0,p.indexOf("="));
		        	   String pval=URLDecoder.decode(p.substring(p.indexOf("=")+1),"ISO-8859-1");
		        	   
		        	   //log.info(pname+" "+pval);
		        	   params.put(pname, pval);
		           }
		           return params;
			}
			
		   private void API_setSignText(HttpExchange t)throws IOException
		   {
	           InputStream is = t.getRequestBody();
	           int b;
	           String txt="";
	           while((b=is.read())>=0)
	        	   txt+=(char)b;
	           is.close();
	           
	           HashMap<String,String> params=getParams(txt);
	           if(txt.equals("")||(params.get("apikey")==null)||(params.get("X")==null)||(params.get("Y")==null)||(params.get("Z")==null)||(params.get("Text")==null))
	           {
    			   String msg="<html><body>"+SignManagementPluginListener.lang_api+"</body></html>";
    			   t.sendResponseHeaders(404, msg.length());
               	   OutputStream os = t.getResponseBody();
    	           os.write(msg.getBytes());
    	           os.close();
		           t.close();
	        	   return;
	           }
	           if(!params.get("apikey").equals(SignManagementPluginListener.apikey))
			   {
    			   String msg="<html><body>"+SignManagementPluginListener.lang_api_invalid+"</body></html>";
    			   t.sendResponseHeaders(404, msg.length());
               	   OutputStream os = t.getResponseBody();
    	           os.write(msg.getBytes());
    	           os.close();
		           t.close();
				   return;
			   }

	           
	           int X=Integer.parseInt(params.get("X"));
    		   int Y=Integer.parseInt(params.get("Y"));
    		   int Z=Integer.parseInt(params.get("Z"));
    		   SignInfo s=SignManagementPluginListener.SignTexts.get(new XYZ(X,Y,Z));
    		   if(s!=null)
    		   {
    			   s.Text=params.get("Text");
    			   Sign sign=(Sign)etc.getServer().getComplexBlock(X,Y,Z);
    			   if(sign!=null)
    				if(s.isref)
    				{
    					String[] k= s.Text.split("\n");
    					int i=0;
    					for(String e:k)
    					{
    						if(e.length()>15)
    							sign.setText(i++, e.substring(0,15));
    						else
    							sign.setText(i++, e);
    						if(i>3)break;
    					}
    				    sign.update();
    			   }
    		   }
    		   else
    		   {
    			   String msg="<html><body>"+SignManagementPluginListener.lang_config_error+"</body></html>";
    			   t.sendResponseHeaders(404, msg.length());
               	   OutputStream os = t.getResponseBody();
    	           os.write(msg.getBytes());
    	           os.close();
		           t.close();
    	           return;
    		   }
        	   SignManagementPluginListener.SaveSignsTexts();
			   String msg="<html><body>"+SignManagementPluginListener.lang_config_done+"</body></html>";
			   t.sendResponseHeaders(202, msg.length());
           	   OutputStream os = t.getResponseBody();
	           os.write(msg.getBytes());
	           os.close();
	           t.close();
		   }
		   public void API_getSigns(HttpExchange t)throws IOException
		   {
			   InputStream is = t.getRequestBody();
			   int b;
			   String txt="";
			   while((b=is.read())>=0)
				   txt+=(char)b;
			   is.close();
			   
			   HashMap<String,String> params=getParams(txt);
			   
			   String owner=params.get("owner");
			   if((owner==null)||(params.get("apikey")==null))
			   {
    			   String msg="<html><body>"+SignManagementPluginListener.lang_api+"</body></html>";
    			   t.sendResponseHeaders(404, msg.length());
               	   OutputStream os = t.getResponseBody();
    	           os.write(msg.getBytes());
    	           os.close();
		           t.close();
				   return;
			   }
			   
			   if(!params.get("apikey").equals(SignManagementPluginListener.apikey))
			   {
    			   String msg="<html><body>"+SignManagementPluginListener.lang_api_invalid+"</body></html>";
    			   t.sendResponseHeaders(404, msg.length());
               	   OutputStream os = t.getResponseBody();
    	           os.write(msg.getBytes());
    	           os.close();
		           t.close();
				   return;
			   }
			   
			   
           	   OutputStream os = t.getResponseBody();
           	   String msg="";
           	   for(Map.Entry<XYZ,SignInfo> h:SignManagementPluginListener.SignTexts.entrySet())
			   {
           		   if(h.getValue().owner.equalsIgnoreCase(owner))
           		   {
           			   String part=h.getKey().x+" "+h.getKey().y+" "+h.getKey().z+" "+h.getValue().Text;
           		   		msg+=part.length()+" "+part+"\n";
           		   }
			   }
           	   t.sendResponseHeaders(202, msg.length());
	           os.write(msg.getBytes());
	           os.close();
	           t.close();
		   }
		   
	       public void handle(HttpExchange t) throws IOException {
	    	   try
	    	   {
		    	   AccessEntry AE;
		    	   if(t.getRequestURI().getPath().length()>5)
		    	   if(t.getRequestURI().getPath().substring(1,4).equalsIgnoreCase("api"))
    			   {
			    	   if(t.getRequestURI().getPath().substring(1).equalsIgnoreCase("api/setSignText"))
			    	   {
			    		   API_setSignText(t);
			    		   return;
			    	   }
			    	   
			    	   if(t.getRequestURI().getPath().substring(1).equalsIgnoreCase("api/getSigns"))
			    	   {
			    		   API_getSigns(t);
			    		   return;
			    	   }
	    			   String msg="<html><body>API Command: "+t.getRequestURI().getPath().substring(5)+" not found</body></html>";
	    			   t.sendResponseHeaders(404, msg.length());
 		           	   OutputStream os = t.getResponseBody();
			           os.write(msg.getBytes());
			           os.close();
			           t.close();
    			   }
		    	   
		    	   long ID=0;
		    	   try
		    	   {
		    		   ID=Integer.parseInt(t.getRequestURI().getPath().substring(1));
		    	   }
		    	   catch(NumberFormatException e)
		    	   {
	    			   String msg="<html><body>Wrong ID</body></html>";
	    			   t.sendResponseHeaders(404, msg.length());
 		           	   OutputStream os = t.getResponseBody();
			           os.write(msg.getBytes());
			           os.close();
			           t.close();
		    		   return;
		    	   }
		    	   if(Access.containsKey(ID))
		    	   {
		    		   if(Access.get(ID).time-System.currentTimeMillis()>1000*60*60)
		    		   {
		    			   String msg="<html><body>Wrong ID</body></html>";
		    			   Access.remove(ID);
		    			   t.sendResponseHeaders(404, msg.length());
		    			   OutputStream os = t.getResponseBody();
				           os.write(msg.getBytes());
				           os.close();
				           t.close();
		    			   return;
		    		   }
		    	   }
		    	   else 
	    		   {
	    			   String msg="<html><body></body></html>";
	    			   t.sendResponseHeaders(404, msg.length());
 		           	   OutputStream os = t.getResponseBody();
			           os.write(msg.getBytes());
			           os.close();
			           t.close();
			           return;
	    		   }
		    	   AE=Access.get(ID);
		    	   Logger log = Logger.getLogger("Minecraft");
		           InputStream is = t.getRequestBody();
		           int b;
		           String txt="";
		           while((b=is.read())>=0)
		        	   txt+=(char)b;
		           
		           HashMap<String,String> params=getParams(txt);
		           
		           if(params.get("do")!=null)
		           {
		        	   int j=0;
		        	   while(params.get("X"+j)!=null)
		        	   {
		        		   int X=Integer.parseInt(params.get("X"+j));
		        		   int Y=Integer.parseInt(params.get("Y"+j));
		        		   int Z=Integer.parseInt(params.get("Z"+j));
		        		   SignInfo s=SignManagementPluginListener.SignTexts.get(new XYZ(X,Y,Z));
		        		   if(s!=null)
		        		   {
		        			   s.Text=params.get("Text"+j);
		        			   Sign sign=(Sign)etc.getServer().getComplexBlock(X,Y,Z);
		        			   if(sign!=null)
		        				if(s.isref)
		        				{
		        					String[] k= s.Text.split("\n");
		        					int i=0;
		        					for(String e:k)
		        					{
		        						if(e.length()>15)
		        							sign.setText(i++, e.substring(0,15));
		        						else
		        							sign.setText(i++, e);
		        						if(i>3)break;
		        					}
		        				    sign.update();
		        			   }
		        		   }
		        		   j++;
		        	   }
		        	   SignManagementPluginListener.SaveSignsTexts();
	    			   String msg="<html><body>"+SignManagementPluginListener.lang_config_done+"</body></html>";
	    			   t.sendResponseHeaders(202, msg.length());
 		           	   OutputStream os = t.getResponseBody();
			           os.write(msg.getBytes());
			           os.close();
			           t.close();
			           Access.remove(ID);
		        	   return;
		           }
		           
		           String response = 
		        	   "<html>"+
		        	   "<body>"+
		        	   "<form method=\"POST\">";
		           
		           int i=0;
		           for(Map.Entry<XYZ,SignInfo> h:SignManagementPluginListener.SignTexts.entrySet())
		        	   if(h.getValue().owner.equals(AE.name)||AE.name.equals(""))
			           {
			        	   response += "<input name=\"X"+i+"\" type=\"hidden\" value=\""+h.getKey().x+"\" />";
			        	   response += "<input name=\"Y"+i+"\" type=\"hidden\" value=\""+h.getKey().y+"\" />";
			        	   response += "<input name=\"Z"+i+"\" type=\"hidden\" value=\""+h.getKey().z+"\" />";
			        	   response += "Text: <input style=\"width:450px\" name=\"Text"+i+"\" type=\"text\" value=\""+stringToHTMLString(h.getValue().Text)+"\" /><br />";
			           }
		        	
		           response +="<input type=\"hidden\" name=\"do\" value=\"change\" />" +
		        	   	"<br /><input type=\"submit\" />" +
		        	   	"</form>" +
		        	   	"</body>" +
		        	   	"</html>";
	           
		           
		           t.sendResponseHeaders(200, response.length());
		           OutputStream os = t.getResponseBody();
		           os.write(response.getBytes());
		           os.close();
		           t.close();
	    	   }
		       catch(Exception e)
		       {
		    	   Logger log = Logger.getLogger("Minecraft");
		    	   log.info(e.getMessage());
		    	   for(StackTraceElement ste:e.getStackTrace())
		    		   log.info(ste.toString());
		    	   t.sendResponseHeaders(200, 1);
		           OutputStream os = t.getResponseBody();
		           os.write(' ');
		           os.close();
		           t.close();
		       }
	       }
	   }
	public long GenerateID(Player p){
		SecureRandom s= new SecureRandom();
		
		long rnd=s.nextLong()%0xFFFFF;
		if(p!=null)
			Access.put(rnd,new AccessEntry(p.getName(),System.currentTimeMillis()));
		else
			Access.put(rnd,new AccessEntry("",System.currentTimeMillis()));
		return rnd;
	}
	
	public void SignConfig(boolean ServerConsole,Player p)
	{
		String txt=SignManagementPluginListener.lang_config+SignManagementPluginListener.ServerUrl+GenerateID(p);
		if(ServerConsole)
		{
			Logger log = Logger.getLogger("Minecraft");
			log.info(txt);
		}
		else
			p.sendMessage(txt);
	}
	public ConfigServer(int port) throws IOException
	{
		Access = new HashMap<Long,AccessEntry>();
		server = HttpServer.create(new InetSocketAddress(port),0);
		server.createContext("/", new MyHandler());
		server.setExecutor(null);
		server.start();
	}
}
