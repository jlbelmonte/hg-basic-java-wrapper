package wrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;

import siena.Json;
import utils.HGConstants;
import utils.HGUtilities;


public class HGConnector {
	public static Logger logger = Logger.getLogger(HGConnector.class);	
	private String command = "/usr/bin/hg";
	private String uri;
	private String path;
	private String revFrom = "";
	private String revTo = "";

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
		File dir = new File(path);

		CommandLine cl = new CommandLine(command);
		
		if (HGConstants.LOG.equals(action) || "incoming".equals(action)){
			cl.addArgument(action);
			cl.addArgument("--rev");
			cl.addArgument(revFrom+":"+revTo);
			cl.addArgument("--template");
			cl.addArgument(HGConstants.TEMPLATE);
		} else if ("cloneRev".equals(action)) {
			if (!dir.exists()) dir.mkdir();
			cl.addArgument(HGConstants.CLONE);
			cl.addArgument("--rev");
			cl.addArgument(revFrom);
			cl.addArgument(uri);
			cl.addArgument(path);
		} else if (HGConstants.CLONE.equals(action)) {
			if (!dir.exists()) dir.mkdir();
			cl.addArgument(HGConstants.CLONE);
			cl.addArgument(uri);
			cl.addArgument(path);
		} else if (HGConstants.PULL.equals(action)){
			if(!dir.exists()) return callHG(HGConstants.CLONE);
			cl.addArgument(HGConstants.PULL);
			cl.addArgument("-u");
		}
		Json result = Json.map();

		try{
			File file = File.createTempFile("hgkit", "tmp");
			FileOutputStream fOS = new FileOutputStream(file);
			PumpStreamHandler streamHandler = new PumpStreamHandler();
			streamHandler = new PumpStreamHandler(fOS);
			Executor executor = new DefaultExecutor();
			executor.setStreamHandler(streamHandler);
			executor.setWorkingDirectory(dir);

			int statusCode = executor.execute(cl);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String stdErr = System.err.toString();
			result = HGUtilities.parseData(br, stdErr, statusCode, action);
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.error("HG Exception: "+ uri, e);
		}
		return result;
	}
	
	// public methods
	//clones
	public Json clone(){
		return callHG("clone");
	}
	
	public Json cloneRev(String revision){
		this.revFrom = revision;
		return callHG("cloneRev");
	}
	
	// logs
	public Json log(){
		return callHG("log");
	}
	public Json log(String revFrom, String revTo){
		this.revFrom = revFrom;
		this.revTo = revTo;
		return  callHG("log");		
	}
	
	public Json log(String revFrom){
		this.revFrom = revFrom;
		this.revTo="tip";
		return callHG("log");
	}
	
	public Json hgRemoteLog(){
		return callHG("incoming");
	}
	
	//update
	public Json pull(){
		return callHG("pull");
	}
	
}
