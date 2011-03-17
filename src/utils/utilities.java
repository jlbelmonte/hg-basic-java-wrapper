package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import siena.Json;

public class utilities {
	public static String getString(BufferedReader bR) {
		String s = null;
		String out = "";
		try {
			while ((s = bR.readLine()) != null) {
				out += s;
			}
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
public static Json parseData(BufferedReader stdOutput, BufferedReader stdError, int statusCode, String action){
		
		String stdErrorString = getString(stdError);
		
		if (statusCode != 0)
			return Json.map().put("status", "NOK").put("action", action).put("error", stdErrorString);


		if (Constants.CLONE.equals(action))
			return Json.map().put("status", "OK" );

		Pattern p = Pattern.compile("^'(\\{.*?\\})'$");
		String s = null;
		
		try{
			while((s = stdOutput.readLine()) != null){
				Matcher log = p.matcher(s);
				if(log.find()){
					return Json.loads(log.group(1).trim()).put("status", "OK");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Json.map().put("status", "NOK").put("action", action).put("error", "exception").put("message", e.getMessage());
		}
		return null;
	}
}
