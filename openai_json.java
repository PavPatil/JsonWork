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
	public static HashMap<String, String> mapDesc = new HashMap<>();
	public static HashMap<String, ArrayList<String>> mapMethod = new HashMap<>();
	public static void main(String[] args) {
		test();
		System.out.println("Hi");
		getPaths();
		System.out.println(mapMethod);
		System.out.println(mapDesc);
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
				mapDesc.put(tagName, tagDesc);
				mapMethod.put(tagName,new ArrayList<String>());
			}

		} catch (JSONException e) {
			System.out.print(e);
		}

		System.out.print(mapMethod.size());
	}
	public static boolean isMethod(String key){
		if(key.equals("get") || key.equals("post") || key.equals("delete") || key.equals("patch") || key.equals("update"))
			return true;
		return false;
	}
	public static void getPaths() {
		try {

			JSONObject jsonObject = getJsonObject();
			// if (jsonObject.has(key)) {
			JSONObject pathObject = jsonObject.getJSONObject("paths");
			Iterator<String> pathkeys = pathObject.keys();

			int cnt = 0;
			while (pathkeys.hasNext()) {
				String key = pathkeys.next();

				if (pathObject.has(key)) {
					JSONObject singlePathObject = pathObject.getJSONObject(key);
					Iterator<String> singlepathkeys = singlePathObject.keys();
					ArrayList<String> arr = new ArrayList<>();
					
					while (singlepathkeys.hasNext()) {
						String ckey = singlepathkeys.next();
						if (singlePathObject.has(ckey)) {
							if(isMethod(ckey)){
								JSONArray methodArray = singlePathObject.getJSONObject(ckey).getJSONArray("tags");
								String tagName = methodArray.getString(0);
								
								if(mapMethod.containsKey(tagName)){
									mapMethod.get(tagName).add(key);
								}
								//map.containsKey(ckey)
								//mapMethod.containsKey()
							}
						}
					}
				}
				cnt++;

			}
			// System.out.println(pathObject);

			System.out.println(cnt);
			// JSONArray Tags = (JSONArray) jsonObject.get("tags");
		} catch (JSONException e) {
			System.out.print(e);
		}
	}
}
