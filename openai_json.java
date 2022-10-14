package json_modify;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
//import org.json.simple.*;
//import org.json.simple.parser.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class openapi {
	public static HashMap<tags_info, ArrayList<String>> map = new HashMap<>();

	public static void main(String[] args) {
		//test();
		getPaths();
	}

	public static JSONObject getJsonObject() {
		JSONObject jsonObject = null;
		try {
			String loc = new String("/C:/Users/pavpatil/Desktop/API/openapi.json");
			File file = new File(loc);
			String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
			jsonObject = new JSONObject(content);
		} catch (JSONException e) {
			System.out.print(e);
		} catch (IOException e) {
			System.out.print(e);
		}

		return jsonObject;
	}

	public static void test() {
		try {
			/*
			 * String loc = new
			 * String("/C:/Users/pavpatil/Desktop/API/openapi.json"); File file
			 * = new File(loc); String content = new
			 * String(Files.readAllBytes(Paths.get(file.toURI())));
			 */
			JSONObject jsonObject = getJsonObject();
			// Object obj = parser.parse(new
			// FileReader("/C:/Users/pavpatil/Desktop/API/openapi.json"));
			// JSONObject jsonObject = (JSONObject)obj;
			// String name = (String)jsonObject.get("tags");
			// String course = (String)jsonObject.get("Course");
			JSONArray Tags = (JSONArray) jsonObject.get("tags");

			for (int i = 0; i < Tags.length(); i++) {
				JSONObject objectInArray = Tags.getJSONObject(i);
				String tagDesc = "";
				String tagName = "";

				if (objectInArray.has("name"))
					tagName = objectInArray.getString("name");

				if (objectInArray.has("description"))
					tagDesc = objectInArray.getString("description");

				tags_info t = new tags_info();
				t.setTagName(tagName);
				t.setTagDesc(tagDesc);
				map.put(t, new ArrayList<String>());
			}

		} catch (JSONException e) {
			System.out.print(e);
		}

		System.out.print(map.size());
	}

	public static void getPaths() {
		try {
			JSONObject jsonObject = getJsonObject();
			JSONObject pathObject = jsonObject.getJSONObject("paths");
			Iterator<String> keys = pathObject.keys();
			int cnt=0;
			while(keys.hasNext()) {
				JSONObject methodObject = null;
				String key = keys.next();
				JSONObject currentObject = pathObject.getJSONObject(key);
				Iterator<String> ckeys = currentObject.keys();
				
				while(ckeys.hasNext()) {
					String keyr = ckeys.next();
					//System.out.println(keyr);
					
				}cnt++;
				
				
				/*JSONObject childObject = currentObject.getJSONObject(key);
				Iterator<String> ckeys = childObject.keys();
				int cnt = 0;
				String fkey = "";
				while(ckeys.hasNext()) {
					String ckey = ckeys.next();
					if(ckey!="servers"){
						cnt++;
						fkey = ckey;
						break;
					}
				}*/
				//methodObject = pathObject.getJSONObject(fkey);
				
				
				
				
				
				
				
				
				
				
				/*if (childObject.has("get"))
				
				else if((childObject.has("post")))
				methodObject = pathObject.getJSONObject(key);
				else if((childObject.has("patch")))
				methodObject = pathObject.getJSONObject(key);
				else if((childObject.has("patch")))
				methodObject = pathObject.getJSONObject(key);
				else if((childObject.has("update")))
				methodObject = pathObject.getJSONObject(key);
				else if((childObject.has("delete")))
				methodObject = pathObject.getJSONObject(key);
			    System.out.print(key);*/
			   
			}
			
			System.out.println(cnt);
			JSONArray Tags = (JSONArray) jsonObject.get("tags");
		} catch (Exception e) {
			System.out.print(e);
		}
	}

}
