package json_modify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class definitions {
	public static JSONObject parent = null;
	public static HashMap<tags_info, ArrayList<String>> map = new HashMap<>();
	public static HashMap<String, String> mapDesc = new HashMap<>();
	public static HashMap<String, ArrayList<String>> mapMethod = new HashMap<>();
	public static HashMap<String, ArrayList<xmlHelper>> mapMaster = new HashMap<>();

	public static HashMap<String, Integer> methodsCount = new HashMap<>();

	public static JSONObject setJsonObject() {
		JSONObject jsonObject = null;
		try {
			String loc = new String("/C:/Users/pavpatil/Desktop/OERP/d/Enterprise_Data_Management.json"); // test
			// data
			File file = new File(loc);
			String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
			jsonObject = new JSONObject(content);
		} catch (JSONException e) {
			System.out.print(e);
		} catch (IOException e) {
			System.out.print(e);
		}
		parent = jsonObject;
		return jsonObject;
	}

	public static void fillTags() {
		try {

			JSONObject jsonObject = parent;
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

				mapDesc.put(tagName, tagDesc);

				ArrayList<xmlHelper> h = new ArrayList<>();
				mapMaster.put(tagName, h);
			}

		} catch (JSONException e) {
			System.out.print(e);
		}
	}

	public static boolean isMethod(String key) {
		if (key.equals("get") || key.equals("post") || key.equals("delete") || key.equals("patch")
				|| key.equals("update"))
			return true;
		return false;
	}

	public static boolean isParameters(String key) {
		if (key.equals("parameters"))
			return true;
		return false;
	}

	public static ArrayList<defStructure> def = new ArrayList<>();
	public static ArrayList<defStructure> par = new ArrayList<>();

	public static String getSchema(String path) {
		int idx = path.lastIndexOf("/");
		return path.substring(idx + 1);

	}

	public static void fillResponseHelper() throws JSONException {
		JSONObject jsonObject = parent;

		JSONObject resObject = jsonObject.getJSONObject("responses");
		JSONObject defObject = jsonObject.getJSONObject("definitions");
		Iterator<String> resKeys = resObject.keys();
		while (resKeys.hasNext()) {
			String key = resKeys.next();
			if (resObject.has(key)) {
				JSONObject singleDefObject = resObject.getJSONObject(key);
				// System.out.println(key);
				defStructure parele = fillResponses(singleDefObject, key, resObject, defObject);
				par.add(parele);
				Print(parele, "");

			}
		}
	}

	public static defStructure fillResponses(JSONObject resObj, String name, JSONObject parParent, JSONObject defParent)
			throws JSONException {
		defStructure parent = null;
		if (resObj.has("schema")) {
			resObj = resObj.getJSONObject("schema");
			parent = fillDefinitions(resObj, name, defParent);
		}
		/*
		 * defStructure parent = new defStructure(); parent.setName(name);
		 * String type = ""; type = resObj.has("type") ?
		 * resObj.getString("type") : "object"; parent.setType(type); if
		 * (resObj.has("schema")) { JSONObject schemaObj =
		 * resObj.getJSONObject("schema");
		 * 
		 * if (schemaObj.has("$ref")) { defStructure child = new defStructure();
		 * String schemaPath = schemaObj.getString("$ref"); String schema =
		 * getSchema(schemaPath); JSONObject cc =
		 * defParent.getJSONObject(schema); child = fillDefinitions(cc, schema,
		 * parParent); parent.childs.add(child); } }
		 */
		return parent;
	}

	public static defStructure fillParameters(JSONObject parObj, String name, JSONObject parParent)
			throws JSONException {
		defStructure parent = new defStructure();
		;
		// boolean hasQuery = false;
		if (parObj.has("in")) {

			String val = parObj.getString("in");
			if (val.equals("path")) {

				parent.setName(name);

				String type = "";
				type = parObj.has("type") ? parObj.getString("type") : "object";
				parent.setType(type);
				if (parObj.has("schema")) {
					JSONObject schemaObj = parObj.getJSONObject("schema");
					if (schemaObj.has("$ref")) {
						defStructure child = new defStructure();
						String schemaPath = schemaObj.getString("$ref");
						String schema = getSchema(schemaPath);
						JSONObject cc = parParent.getJSONObject(schema);
						child = fillDefinitions(cc, schema, parParent);
						parent.childs.add(child);
					}
				}
			} else if (val.equals("query") && isQueryAdded == false) {
				// hasQuery = true;
				defStructure child = new defStructure();
				child.setName("Query");
				child.setType("String");
				parent.childs.add(child);
				isQueryAdded = true;
			}
		}
		return parent;
	}

	public static void fillParametersHelper() throws JSONException {
		JSONObject jsonObject = parent;

		JSONObject parObject = jsonObject.getJSONObject("parameters");
		Iterator<String> parKeys = parObject.keys();
		while (parKeys.hasNext()) {
			String key = parKeys.next();
			if (parObject.has(key)) {
				JSONObject singleDefObject = parObject.getJSONObject(key);
				if (singleDefObject.has("in")) {

					String val = singleDefObject.getString("in");
					if (val.equals("path") || val.equals("query")) {
						defStructure parele = fillParameters(singleDefObject, key, parObject);
						par.add(parele);
						Print(parele, "");
					}
				}

			}
		}
	}

	public static defStructure fillDefinitions(JSONObject defObj, String name, JSONObject defParent)
			throws JSONException {
		defStructure parent = new defStructure();
		parent.setName(name);

		String type = "";
		if (defObj.has("type")) {
			type = defObj.getString("type");
		} else {
			type = "object";
		}
		parent.setType(type);
		if (defObj.has("properties")) {
			JSONObject props = defObj.getJSONObject("properties");
			Iterator<String> singlepProp = props.keys();
			while (singlepProp.hasNext()) {
				String ckey = singlepProp.next();
				if (props.has(ckey)) {
					JSONObject ccprops = props.getJSONObject(ckey);

					String pType = "";
					if (ccprops.has("type"))
						pType = ccprops.getString("type");
					else
						pType = "object";
					defStructure child = new defStructure();
					child.setName(ckey);
					child.setType(pType);
					if (pType.equals("object") || pType.equals("array")) {
						if (ccprops.has("items")) {

							Object aObj = ccprops.get("items");
							if (aObj instanceof JSONArray) {
								JSONArray items = ccprops.getJSONArray("items");
								for (int i = 0; i < items.length(); i++) {
									JSONObject objectInArray = items.getJSONObject(i);
									/*
									 * JSONObject objectInArray =
									 * items.getJSONObject(i);
									 * if(objectInArray.has("properties")){
									 * JSONObject cchild =
									 * objectInArray.getJSONObject("properties")
									 * ; }
									 */
									defStructure cc = fillDefinitions(objectInArray, child.name + "items" + i,
											defParent);
									child.childs.add(cc);
								}
							} else if (aObj instanceof JSONObject) {

								JSONObject items = ccprops.getJSONObject("items");
								if (items.has("$ref")) {
									String schemaPath = items.getString("$ref");
									String schema = getSchema(schemaPath);
									JSONObject cc = defParent.getJSONObject(schema);
									defStructure c = fillDefinitions(cc, schema, defParent);
									child.childs.add(c);
								}
							}
						}
					}
					/*
					 * else{
					 * 
					 * child.setName(ckey); child.setType(pType);
					 * 
					 * }
					 */
					parent.childs.add(child);
				}
			}
		} else if (defObj.has("$ref")) {
			defStructure child = new defStructure();
			String schemaPath = defObj.getString("$ref");
			String schema = getSchema(schemaPath);
			JSONObject cc = defParent.getJSONObject(schema);
			child = fillDefinitions(cc, schema, defParent);
			parent.childs.add(child);
		} else if (defObj.has("allOf")) {
			JSONArray allOf = defObj.getJSONArray("allOf");
			defStructure child = new defStructure();
			child.name = "allOf";
			child.type = "array";

			for (int i = 0; i < allOf.length(); i++) {
				JSONObject objectInArray = allOf.getJSONObject(i);
				if (objectInArray.has("$ref")) {
					defStructure cchild = new defStructure();
					String schemaPath = objectInArray.getString("$ref");
					String schema = getSchema(schemaPath);
					JSONObject cc = defParent.getJSONObject(schema);
					cchild = fillDefinitions(cc, schema, defParent);
					child.childs.add(cchild);

				} else if (objectInArray.has("properties")) {
					JSONObject props = objectInArray.getJSONObject("properties");

					defStructure allOfchild = new defStructure();
					allOfchild.setName(parent.name + child.name + i);
					String dtype = objectInArray.has("type") ? objectInArray.getString("type") : "object";
					allOfchild.setType(dtype);

					Iterator<String> singlepProp = props.keys();
					while (singlepProp.hasNext()) {
						String ckey = singlepProp.next();
						if (props.has(ckey)) {
							JSONObject ccprops = props.getJSONObject(ckey);

							// String pType = ccprops.getString("type");
							String pType = ccprops.has("type") ? ccprops.getString("type") : "object";
							defStructure cchild = new defStructure();
							cchild.setName(ckey);
							cchild.setType(pType);
							if (pType.equals("object") || pType.equals("array")) {
								if (ccprops.has("items")) {
									JSONObject items = ccprops.getJSONObject("items");
									if (items.has("$ref")) {
										String schemaPath = items.getString("$ref");
										String schema = getSchema(schemaPath);
										JSONObject cc = defParent.getJSONObject(schema);
										defStructure c = fillDefinitions(cc, schema, defParent);
										cchild.childs.add(c);
									}
								}
							}
							/*
							 * else{
							 * 
							 * cchild.setName(ckey); cchild.setType(pType);
							 * 
							 * }
							 */
							allOfchild.childs.add(cchild);
							/* child.childs.add(allOfchild); */
						}
					}
					child.childs.add(allOfchild);
					// parent.childs.add(child);
				}

			}
			parent.childs.add(child);

		}
		return parent;
	}

	public static void fillDefinitionsHelper() throws JSONException {
		JSONObject jsonObject = parent;
		// if (jsonObject.has(key)) {
		JSONObject defObject = jsonObject.getJSONObject("definitions");
		Iterator<String> defKeys = defObject.keys();
		while (defKeys.hasNext()) {
			String key = defKeys.next();
			if (defObject.has(key)) {
				// if(key.equals("commonLookups") ||
				// key.equals("auditUpdateStatusOnError")){
				JSONObject singleDefObject = defObject.getJSONObject(key);
				if (key.equals("CommitPolicyJson")) {

					System.out.print("=-----====");

				}
				System.out.println(key + "=============");
				defStructure defele = fillDefinitions(singleDefObject, key, defObject);
				def.add(defele);
				// if(key.equals("setupTaskCSVImports-SetupTaskCSVImportProcess-SetupTaskCSVImportProcessResult-item-response")){
				Print(defele, "");
				// }
				System.out.println("=============");
				// }
			}
		}
		// Print(def.get(0),"");
	}

	public static boolean isQueryAdded = false;

	public static ArrayList<defStructure> getParameters(JSONArray parameterArray) throws JSONException {
		ArrayList<defStructure> arr = new ArrayList<>();
		for (int i = 0; i < parameterArray.length(); i++) {
			JSONObject parObject = parameterArray.getJSONObject(i);
			if (parObject.has("schema")) {
				int si = 1;
				parObject = parObject.getJSONObject("schema");
				if (parObject.has("$ref")) {
					String schema = parObject.getString("$ref");
					if (schema.contains("#/definitions")) {
						schema = getSchema(schema);
						JSONObject jsonObject = parent;
						JSONObject defObject = jsonObject.getJSONObject("definitions");
						JSONObject target = defObject.getJSONObject(schema);

						defStructure ele = fillDefinitions(target, schema, defObject);
						arr.add(ele);
					}
				}
			} else if (parObject.has("$ref")) {
				String schema = parObject.getString("$ref");
				if (schema.contains("#/parameters")) {
					schema = getSchema(schema);
					JSONObject jsonObject = parent;
					JSONObject paramObject = jsonObject.getJSONObject("parameters");
					JSONObject target = paramObject.getJSONObject(schema);

					defStructure ele = fillParametersHelperSpecific(target, schema, paramObject);
					arr.add(ele);
				}
			} else {
				String val = parObject.getString("in");
				if (val.equals("path")) {
					defStructure simpleDef = new defStructure();
					if (parObject.has("name")) {
						simpleDef.setName(parObject.getString("name"));
					}
					if (parObject.has("type")) {
						simpleDef.setType(parObject.getString("type"));
					}
					arr.add(simpleDef);
				} else if (val.equals("query")) {
					if (isQueryAdded == false) {
						defStructure simpleDef = new defStructure();
						simpleDef.setName("Query");
						simpleDef.setType("String");
						arr.add(simpleDef);
						isQueryAdded = true;
					}
				}

			}
		}
		return arr;
	}

	public static defStructure fillParametersHelperSpecific(JSONObject paramObj, String name, JSONObject paramParent)
			throws JSONException {
		JSONObject jsonObject = parent;
		JSONObject singleDefObject = paramObj;
		defStructure parele = null;
		if (singleDefObject.has("in")) {

			String val = singleDefObject.getString("in");
			// if (val.equals("path") || val.equals("query")) {
			parele = fillParameters(singleDefObject, name, paramParent);
			// par.add(parele);
			// Print(parele, "");
			// }
		}
		return parele;

	}

	// working
	public static defStructure fillResponseHelperSpecific(JSONObject target, String schema, JSONObject resObj)
			throws JSONException {
		JSONObject jsonObject = parent;

		JSONObject resObject = jsonObject.getJSONObject("responses");
		JSONObject defObject = jsonObject.getJSONObject("definitions");

		defStructure parele = fillResponses(target, schema, resObj, defObject);
		return parele;
	}

	public static defStructure getResponse(JSONObject responseObj) throws JSONException {
		defStructure response = new defStructure();
		int iy=1;
		if (responseObj.has("default")) {
			responseObj = responseObj.getJSONObject("default");
			
				if (responseObj.has("$ref")) {
					String schemaPath = responseObj.getString("$ref");
					if (schemaPath.contains("#/responses")) {
						String schema = getSchema(schemaPath);
						JSONObject jsonObject = parent;
						JSONObject resObject = jsonObject.getJSONObject("responses");
						JSONObject target = resObject.getJSONObject(schema);

						response = fillResponseHelperSpecific(target, schema, resObject);
					}
					else if (schemaPath.contains("#/definitions")) {
						String schema = getSchema(schemaPath);
						JSONObject jsonObject = parent;
						JSONObject resObject = jsonObject.getJSONObject("definitions");
						JSONObject target = resObject.getJSONObject(schema);

						response = fillDefinitions(target, schema, resObject);
					}

				}
			}
		return response;
	}

	public static void getPathsData() {
		try {
			JSONObject jsonObject = parent;
			// if (jsonObject.has(key)) {
			JSONObject pathObject = jsonObject.getJSONObject("paths");
			Iterator<String> pathkeys = pathObject.keys();

			int cnt = 0;
			while (pathkeys.hasNext()) {
				String key = pathkeys.next();

				ArrayList<xmlHelper> a = new ArrayList<>();
				if (pathObject.has(key)) {
					JSONObject singlePathObject = pathObject.getJSONObject(key);
					ArrayList<defStructure> commonParameters = new ArrayList<>();
					if (singlePathObject.has("parameters")) {
						JSONArray parameterArray = singlePathObject.getJSONArray("parameters");
						commonParameters = getParameters(parameterArray);
						/*
						 * for(defStructure ele : commonParameters){
						 * Print(ele,""); }
						 */
					}
					Iterator<String> singlepathkeys = singlePathObject.keys();
					ArrayList<String> arr = new ArrayList<>();

					while (singlepathkeys.hasNext()) {
						String ckey = singlepathkeys.next();
						if (singlePathObject.has(ckey)) {

							if (isMethod(ckey)) {
								JSONArray methodArray = singlePathObject.getJSONObject(ckey).getJSONArray("tags");
								String tagName = methodArray.getString(0);
								xmlHelper xml = new xmlHelper();
								xml.setPath(key);
								xml.setMethod(ckey);

								if (singlePathObject.getJSONObject(ckey).has("parameters")) {
									JSONArray parameterArray = singlePathObject.getJSONObject(ckey)
											.getJSONArray("parameters");
									// ArrayList<parameter> pm =
									// extractParameters(parameterArray);
									// xml.setParameters(pm);
									// if(key.equals("/fscmRestApi/resources/11.13.18.05/persons/{Personid}")){
									ArrayList<defStructure> ans = getParameters(parameterArray);
									if (commonParameters.size() > 0) {
										for (defStructure ele : commonParameters) {
											ans.add(ele);
										}
									}

									for (defStructure ele : ans) {
										Print(ele, "");

									}
									System.out
											.println("==============================================================");
									// }
								}
								if (singlePathObject.getJSONObject(ckey).has("requestBody")) {
									// System.out.print(ckey + " ");

									JSONObject requestBody = singlePathObject.getJSONObject(ckey)
											.getJSONObject("requestBody");
									ArrayList<request> rb = new ArrayList<>();
									// rb = extractRequest(requestBody);

									System.out.println();
									xml.setRequest(rb);
								}
								if (singlePathObject.getJSONObject(ckey).has("responses")) {
									JSONObject responseObject = singlePathObject.getJSONObject(ckey)
											.getJSONObject("responses");
									defStructure response = getResponse(responseObject);
									// ArrayList<response> res =
									// extractResponse(singlePathObject.getJSONObject(ckey));
									// printChild(res, 2);
									// xml.setResponse(res);
									System.out.println(key+"============"+ckey);
									Print(response, "");
								}
								// a.add(xml);
								if (mapMaster.containsKey(tagName)) {
									// mapMethod.get(tagName).add(key);

									mapMaster.get(tagName).add(xml);

								}
							}

						}
					}
				}
				System.out.println(
						"=========================------------------Method End Here-------------------------------=====================================");
				cnt++;

			}
			// System.out.println(pathObject);

			// System.out.println(cnt);
			// JSONArray Tags = (JSONArray) jsonObject.get("tags");
		} catch (JSONException e) {
			System.out.print(e);
		}
	}

	public static void Print(defStructure def, String space) {
		System.out.println(space + def.name + "  " + def.type);
		space += "\t";
		for (defStructure ele : def.childs) {
			if (ele.type.equals("array") || ele.type.equals("object")) {
				Print(ele, space + "\t");
			} else {
				System.out.println(space + ele.name + "  " + ele.type);
			}
		}
	}

	public static void main(String[] args) throws JSONException {
		setJsonObject();
		fillTags();

		getPathsData();
		// fillDefinitionsHelper();
		// fillParametersHelper();
		// fillResponseHelper();
	}
}
