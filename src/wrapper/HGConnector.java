package wrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import siena.Json;
import utils.Constants;
import utils.utilities;


public class HGConnector {
	public static Logger logger = Logger.getLogger(HGConnector.class);	
	private String command = "/usr/bin/hg";
	private int revision = 0;
	private String uri;
	private String path;

	public HGConnector(String uri, String path, String command){
		this.command = command;
		this.uri = uri;
		this.path = path;
	}

	public HGConnector(String uri, String path){
		this.uri = uri;
		this.path = path;
	}
	
	
	// main call to system
	private Json callHG (String action){
		String[] cmd = null;
		
		if (Constants.LOG.equals(action) || "incoming".equals(action)){
			cmd = new String[] {command ,action,"--template",Constants.TEMPLATE};
		} else {
			cmd = new String[] {command, action,"--rev", ""+revision, uri, path};
		}
		Json result = Json.map();
		File dir = new File(path);
		if (!dir.exists()) dir.mkdir();
		try{
			Process p = Runtime.getRuntime().exec(cmd, null,dir);

			BufferedReader stdOutput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));

			int statusCode = p.waitFor();
			result = utilities.parseData(stdOutput, stdError, statusCode, action);
		}
		catch (IOException e) {
			logger.error("HG Exception: "+ uri, e);
		} catch (InterruptedException e) {
			logger.error("HG Error executing the command",e);
		}
		
		return result;
	}
	
	// public methods
	public Json hgClone(int revision){
		this.revision = revision;
		return callHG("clone");
	}
	
	public Json hgCloneFirstRev(){
		return callHG("clone");
	}
	
	public Json hgLog(){
		return callHG("log");
	}
	
	public Json hgRemoteLog(){
		return callHG("incoming");
	}
	
	public static void main(String args[]) {
		String uri = "https://bitbucket.org/rhangelxs/django-impersonate";
		String path = "/Users/jlbelmonte/rev3";
		HGConnector hg = new HGConnector(uri, path, "/usr/local/bin/hg");
		//Json result = hg.hgClone(9);
		Json result = hg.hgRemoteLog();
	}
}
