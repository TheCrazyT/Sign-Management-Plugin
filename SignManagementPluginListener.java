
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Logger;


public class SignManagementPluginListener extends PluginListener {
	private PropertiesFile properties;
	private String lang_sign_message="";
	private String lang_ref_sign_message;
	private String lang_ref_long_sign_add;
	public static String lang_config;
	public static String lang_config_done;
	public static String lang_config_error;
	public static String lang_api;
	public static String lang_api_invalid;
	
	public static String ServerUrl;

	private String long_txt_sign;
	private String ref_txt_sign;
	public static String apikey;
	public static ConfigServer ConfServer;
	private Timer timer = new Timer();
	private HashMap<Click,Long> Clicks;
	public static HashMap<XYZ,SignInfo> SignTexts;

    public SignManagementPluginListener()
    {
        Logger log = Logger.getLogger("Minecraft");
    	properties = new PropertiesFile("SignManagementPlugin.properties");
        ServerUrl = properties.getString("ServerUrl", "http://localhost/");
        
    	/*lang_sign_message = properties.getString("lang_sign_message", "Nachricht des Schildes: ");
        lang_ref_sign_message = properties.getString("lang_ref_sign_message", "Schild kann nun über die Seite: ... verwaltet werden!");
        lang_ref_long_sign_add = properties.getString("lang_ref_long_sign_add", "Schild mit langem Beschreibungstext hinzugefügt!");
        lang_config = properties.getString("lang_config", "Gehe zu folgender Seite um deine Schilder zu verwalten: ");
        lang_config_done = properties.getString("lang_config_done", "Schildaktualisierungen abgeschlossen!");
        lang_config_error = properties.getString("lang_config_error", "Ein oder mehrere Schilder nicht gefunden!");
        long_txt_sign = properties.getString("long_txt", "Klick mich!");
        ref_txt_sign = properties.getString("ref_txt", "DynSchild");*/
        
        lang_api = properties.getString("lang_api", "API page can't be executed directly!");
        lang_api_invalid = properties.getString("lang_api_invalid", "Your API key is empty or invalid.");
    	lang_sign_message = properties.getString("lang_sign_message", "Message of the Sign: ");
        lang_ref_sign_message = properties.getString("lang_ref_sign_message", "Sign can now be changed through the url you get by the command /signconfig !");
        lang_ref_long_sign_add = properties.getString("lang_ref_long_sign_add", "Sign with long description added!");
        lang_config = properties.getString("lang_config", "Go to the following page to change your signs: ");
        lang_config_done = properties.getString("lang_config_done", "Changed sign descriptions!");
        lang_config_error = properties.getString("lang_config_error", "One or more Signs not found!");
        long_txt_sign = properties.getString("long_txt", "Click me!");
        ref_txt_sign = properties.getString("ref_txt", "DynSign");
    	SecureRandom s= new SecureRandom();
		
    	Integer[] b=new Integer[3];
		for(int i=0;i<3;i++)
			b[i]=s.nextInt();
		apikey="";
		
		for(Integer by:b)
			apikey+=Integer.toHexString(by);
		
        apikey = properties.getString("apikey", apikey);
        
        
        boolean start_config_server = (properties.getString("start_config_server", "yes").equals("yes"));
        int timerperiod=Integer.parseInt(properties.getString("timerperiod", "10000"));
        
        log.info("Sign Management Plugin");
        log.info("Sign texts: \""+long_txt_sign+"\",\""+ref_txt_sign+"\"");
        log.info("Server url:"+ServerUrl);
        log.info("API-Key: "+apikey);
        log.info("API-Commands: \n"+ServerUrl+"api/setSignText\n"+ServerUrl+"api/getSigns\n");
        
        SignTexts = new HashMap<XYZ,SignInfo>();
        Clicks = new HashMap<Click,Long>();
        if(!LoadSignsTexts())SaveSignsTexts();
        timer.schedule( new UpdateSigns(), 0 ,timerperiod);
        
        if(start_config_server)
        {
	        int port=Integer.parseInt(properties.getString("server_port", "80"));
	        try
	        {
	        	log.info("Starting http server.");
	        	ConfServer=new ConfigServer(port);
	        	log.info("Started http server.");
	        }
	        catch(IOException e)
	        {
	        	log.info(e.getMessage());
	        }
        }
    }
    public static boolean LoadSignsTexts()
    {
    	File f=new File("signs.txt");
		String txt="";
    	if(!f.exists())return false;
    	try{
    		FileReader fr = new FileReader(f);
    		char cbuf[]=new char[1024];
    		int len;
    		while((len=fr.read(cbuf, 0, 1024))!=-1)
    		{
    			txt+=String.copyValueOf(cbuf,0,len);
    		}
    		fr.close();
    	}
    	catch(FileNotFoundException e)
    	{
    		return false;
    	}
       	catch(IOException e)
    	{
    		return false;
    	}
       	txt=txt.replace("\r\n","\n");
    	String lines[]=txt.split("\\|\n");
    	for(String line:lines)
    	{
    		String[] parts=(line+" ").split("\\|");
    		if(parts.length!=7)continue;
			SignInfo si=new SignInfo();
			si.Text="";
			si.owner=parts[0];
			si.islong=(Integer.parseInt(parts[1])==1);
			si.isref=(Integer.parseInt(parts[2])==1);
			si.Text=parts[6];
			SignTexts.put(new XYZ(Integer.parseInt(parts[3]),Integer.parseInt(parts[4]),Integer.parseInt(parts[5])), si);
			
			Sign s=(Sign)etc.getServer().getComplexBlock(Integer.parseInt(parts[3]),Integer.parseInt(parts[4]),Integer.parseInt(parts[5]));
			if(s!=null)
			if(si.isref)
			{
				String[] k= si.Text.split("\n");
				int i=0;
				for(String e:k)
				{
					if(e.length()>15)
						s.setText(i++, e.substring(0,15));
					else
						s.setText(i++, e);
					if(i>3)break;
				}
				s.update();
			}
    	}
    	return true;
    }
	public static void SaveSignsTexts()
	{
		File f=new File("signs.txt");
		try
		{
			FileWriter fw=new FileWriter(f);
			for(Map.Entry<XYZ,SignInfo> h:SignTexts.entrySet())
			{
				fw.write(h.getValue().owner);
				fw.write("|");
				fw.write(h.getValue().islong?"1":"0");
				fw.write("|");
				fw.write(h.getValue().isref?"1":"0");
				fw.write("|");
				
				String s=h.getKey().x.toString();
				fw.write(s);
				fw.write("|");
				s=h.getKey().y.toString();
				fw.write(s);
				fw.write("|");
				s=h.getKey().z.toString();
				fw.write(s);
				fw.write("|");
				fw.write(h.getValue().Text.replace('|', ' '));
				fw.write("|\n");
			}
			fw.close();
		}
    	catch(FileNotFoundException e)
    	{
    		return;
    	}
       	catch(IOException e)
    	{
    		return;
    	}
	
	}
	public void UpdateSignsTexts(Integer x,Integer y,Integer z,SignInfo si)
	{
		File f=new File("signs.txt");
		try
		{
			
			FileWriter fw=new FileWriter(f,true);
			
			fw.write(si.owner);
			fw.write("|");
			fw.write(si.islong?"1":"0");
			fw.write("|");
			fw.write(si.isref?"1":"0");
			fw.write("|");
			String s=x.toString();
			fw.write(s);
			fw.write("|");
			s=y.toString();
			fw.write(s);
			fw.write("|");
			s=z.toString();
			fw.write(s);
			fw.write("|");
			fw.write(si.Text.replace('|', ' '));
			fw.write("|\n");
			fw.close();
		}
    	catch(FileNotFoundException e)
    	{
    		return;
    	}
       	catch(IOException e)
    	{
    		return;
    	}
	}
	
    public boolean onComplexBlockChange(Player player, ComplexBlock block) {
		if (block instanceof Sign)
		{
			Sign s=(Sign)block;
			if(s.getText(0).equalsIgnoreCase(long_txt_sign))
			{
				player.sendMessage(lang_ref_long_sign_add);
				SignInfo si=new SignInfo();
				si.islong=true;
				si.Text="";
				si.owner=player.getName();
				
				XYZ xyz=new XYZ(block.getX(),block.getY(),block.getZ());
				SignTexts.put(xyz, si);
				
				UpdateSignsTexts(block.getX(),block.getY(),block.getZ(),si);
			}
			if(s.getText(0).equalsIgnoreCase(ref_txt_sign))
			{
				SignInfo si=new SignInfo();
				si.isref=true;
				si.Text="";
				si.owner=player.getName();
				player.sendMessage(lang_ref_sign_message);
				s.setText(0,"");
				s.setText(1,"");
				s.setText(2,"");
				s.setText(3,"");
				
				SignTexts.put(new XYZ(block.getX(),block.getY(),block.getZ()), si);
				
				UpdateSignsTexts(block.getX(),block.getY(),block.getZ(),si);
			}
		}
        return false;
    }
	public boolean onBlockCreate(Player player, Block bp, Block bc, int iih) {
		if(bc.getType() == 63)
		{
			Click c=new Click(bc.getX(),bc.getY(),bc.getZ(),player.getName());
			if(Clicks.containsKey(c))
			{
				long secondsleft=10-(System.currentTimeMillis()-Clicks.get(c))/1000;
				if(secondsleft<=0)
					Clicks.remove(c);
				else
					return false;
			}
			Clicks.put(c,System.currentTimeMillis());
			XYZ xyz=new XYZ(bc.getX(),bc.getY(),bc.getZ());
			SignInfo si=SignTexts.get(xyz);
			if(si!=null)
				player.sendMessage(lang_sign_message+si.Text);

		}
		
		
		return false;
	}
    public boolean onBlockDestroy(Player player, Block block) {
    	if(block.getType() == 63)
		{
			Sign s=(Sign)etc.getServer().getComplexBlock(block.getX(),block.getY(),block.getZ());
			if(s!=null)
			{
				Click c=new Click(block.getX(),block.getY(),block.getZ(),player.getName());
				if(Clicks.containsKey(c))
				{
					long secondsleft=10-(System.currentTimeMillis()-Clicks.get(c))/1000;
					if(secondsleft<=0)
						Clicks.remove(c);
					else
						return false;
				}
				
				Clicks.put(c,System.currentTimeMillis());
				XYZ xyz=new XYZ(block.getX(),block.getY(),block.getZ());
				SignInfo si=SignTexts.get(xyz);
				if(si!=null)
					player.sendMessage(lang_sign_message+si.Text);
			}
		}

        return false;
    }
    public boolean onBlockBreak(Player player, Block block) {
        return false;
    }
    public boolean onCommand(Player player, String[] split) {
    	if(split.length>0)
    	{
    		if(split[0].equalsIgnoreCase("signconfig"))
    		{
    			ConfServer.SignConfig(false,player);
    			return true;
    		}
    	}
        return false;
    }
    public boolean onConsoleCommand(String[] split) {
    	if(split.length>0)
    	{
    		if(split[0].equalsIgnoreCase("signconfig"))
    		{
    			ConfServer.SignConfig(true,null);
    			return true;
    		}
    		if(split[0].equalsIgnoreCase("reloadsigns"))
    		{
    			SignManagementPluginListener.LoadSignsTexts();
    			return true;
    		}
    	}
        return false;
    }
}