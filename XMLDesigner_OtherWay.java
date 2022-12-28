package com.tibco.bw.sharedresource.oerpconnection.design.section;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tibco.bw.sharedresource.oerpconnection.design.IOERPConnectionConstants;
import com.tibco.bw.sharedresource.oerpconnection.design.service.Parameter;
import com.tibco.bw.sharedresource.oerpconnection.design.service.Request;
import com.tibco.bw.sharedresource.oerpconnection.design.service.Response;
import com.tibco.bw.sharedresource.oerpconnection.design.service.XMLHelper;
import com.tibco.bw.sharedresource.oerpconnection.design.util.OERPAPIUtil;

public class XMLDesigner implements IOERPConnectionConstants {
	public HashMap<String, String> mapDesc = new HashMap<>();
	public HashMap<String, ArrayList<String>> mapMethod = new HashMap<>();
	public HashMap<String, ArrayList<XMLHelper>> mapMaster = new HashMap<>();
	public JSONObject parent = null;
	public HashSet<String> track = new HashSet<>();
	public void setParent(JSONObject obj) {
		parent = obj;
	}

	public String getServerURl() {
		JSONObject jsonObject = parent;
		String url = BLANK;
		try {
			if (jsonObject.has(SERVERS)) {
				JSONArray serverarray = jsonObject.getJSONArray(SERVERS);
				JSONObject objectInArray = serverarray.getJSONObject(0);

				if (objectInArray.has(URL))
					url = objectInArray.getString(URL);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return url;
	}

	public JSONObject setJsonObject() {
		JSONObject jsonObject = null;
		try {
			String loc = new String(OERPAPIUtil.swaggerFileHome + OERPAPIUtil.filename);
			File file = new File(loc);
			// file.getAbsolutePath();
			String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
			jsonObject = new JSONObject(content);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parent = jsonObject;
		return jsonObject;
	}

	public void fillTags() {
		try {
			JSONObject jsonObject = parent;
			JSONArray Tags = (JSONArray) jsonObject.get(TAGS);
			for (int i = 0; i < Tags.length(); i++) {
				JSONObject objectInArray = Tags.getJSONObject(i);
				String tagDesc = BLANK;
				String tagName = BLANK;
				if (objectInArray.has(NAME))
					tagName = objectInArray.getString(NAME);
				if (objectInArray.has(DESCRIPTION))
					tagDesc = objectInArray.getString(DESCRIPTION);
				mapDesc.put(tagName, tagDesc);
				ArrayList<XMLHelper> h = new ArrayList<>();
				mapMaster.put(tagName, h);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean isMethod(String key) {
		if(!key.equals(SERVERS) && !key.equals(JSON_PARAMETERS))return true;
		return false;
				
		/*if (key.equals(HTTP_METHOD_KEY_GET) || key.equals(HTTP_METHOD_KEY_POST) || key.equals(HTTP_METHOD_KEY_DELETE)
				|| key.equals(HTTP_METHOD_KEY_PATCH) || key.equals(HTTP_METHOD_KEY_PUT) || key.equals(HTTP_METHOD_KEY_OPTIONS))
			return true;
		return false;*/
	}

	public boolean isRequestBody(String key) {
		if (key.equals(REQUEST_BODY))
			return true;
		return false;
	}

	public boolean isResponseBody(String key) {
		if (key.equals(JSON_RESPONSES))
			return true;
		return false;
	}

	public boolean isParameter(String key) {
		if (key.equals(JSON_PARAMETERS))
			return true;
		return false;
	}

	public ArrayList<Parameter> extractParameters(JSONArray parameterArray) {
		ArrayList<Parameter> arr = new ArrayList<>();
		boolean hasQuery = false;
		for (int i = 0; i < parameterArray.length(); i++) {
			try {
				JSONObject parameter = parameterArray.getJSONObject(i);
				
				if( !parameter.has(IN) && parameter.has(DOLLAR + REF)) {
					String refPath = parameter.getString(DOLLAR + REF);
					JSONObject parameterRoot = parent.getJSONObject(JSON_PARAMETERS);
					int idx = refPath.lastIndexOf(FORWARD_SLASH);
					refPath = refPath.substring(idx + 1);
					if(parameterRoot.has(refPath)) {
						parameter = parameterRoot.getJSONObject(refPath);
					}
				}
				
				if(parameter.has(IN)) {
					String type = parameter.getString(IN);
					if (type.equals(JSON_PATH)) {
						Parameter p = new Parameter();
						if (parameter.has(NAME))
							p.setName(parameter.getString(NAME));
						if (parameter.has(REQUIRED))
							p.setRequired(parameter.getBoolean(REQUIRED));
						if (parameter.has(SCHEMA)) {
							JSONObject fields = parameter.getJSONObject(SCHEMA);
							if (fields.has(TYPE))
								p.setType(fields.getString(TYPE));
							if (fields.has(READONLY))
								p.setReadOnly(fields.getBoolean(READONLY));
						}
						arr.add(p);
					}else if(type.equals(JSON_QUERY)){
						hasQuery = true;
					}
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(hasQuery){
			Parameter p = new Parameter();
			p.setName(QUERY);
			p.setType(DATATYPESTRING);
			p.setRequired(false);
			arr.add(p);
		}
		return arr;
	}

	public ArrayList<Request> extractRequest(JSONObject reqObj) {
		ArrayList<Request> arr = new ArrayList<>();
		try {
			if (reqObj.has(DOLLAR + REF)) {
				String url = reqObj.getString(DOLLAR + REF);
				int idx = url.lastIndexOf(FORWARD_SLASH);
				String reqKey = url.substring(idx + 1);

				JSONObject jsonObject = parent;
				JSONObject requestBody = null;

				if (parent.getJSONObject(JSON_COMPONENTS).getJSONObject(JSON_SCHEMAS).getJSONObject(reqKey)
						.has(JSON_PROPERTIES)) {
					requestBody = parent.getJSONObject(JSON_COMPONENTS).getJSONObject(JSON_SCHEMAS)
							.getJSONObject(reqKey).getJSONObject(JSON_PROPERTIES);

					Iterator<String> reqkeys = requestBody.keys();

					while (reqkeys.hasNext()) {
						String key = reqkeys.next();
						JSONObject req = requestBody.getJSONObject(key);

						Request r = new Request();

						r.setName(key);

						if (req.has(TYPE)) {
							r.setType(req.getString(TYPE));
						}
						if (req.has(NULLABLE)) {
							r.setNullable(req.getBoolean(NULLABLE));
						}

						arr.add(r);
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return arr;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Response> extractResponse(JSONObject obj) {
		ArrayList<Response> response = new ArrayList<>();
		String openApiVersion = null;
		try {
			JSONObject contentObj = null;
			String schemaPath = BLANK;
			if (obj.has(JSON_RESPONSES)) {
				contentObj = obj.getJSONObject(JSON_RESPONSES);
				if (contentObj.has(JSON_DEFAULT)) {
					contentObj = contentObj.getJSONObject(JSON_DEFAULT);
					if (contentObj.has(JSON_CONTENT)) {
						contentObj = contentObj.getJSONObject(JSON_CONTENT);
						if (contentObj.has(APPLICATION_JSON)) {
							contentObj = contentObj.getJSONObject(APPLICATION_JSON);
							if (contentObj.has(SCHEMA)) {
								contentObj = contentObj.getJSONObject(SCHEMA);
								if (contentObj.has(DOLLAR + REF))
									schemaPath = contentObj.getString(DOLLAR + REF);
							}
						}
						
					}else if (contentObj.has(SCHEMA)) {
						contentObj = contentObj.getJSONObject(SCHEMA);
						if (contentObj.has(DOLLAR + REF))
							schemaPath = contentObj.getString(DOLLAR + REF);
					}else if (contentObj.has(DOLLAR + REF))
						schemaPath = contentObj.getString(DOLLAR + REF);
					//pptest for swagger
					/*else if (contentObj.has(SCHEMA)) {
						contentObj = contentObj.getJSONObject(SCHEMA);
						if (contentObj.has(DOLLAR + REF))
							schemaPath = contentObj.getString(DOLLAR + REF);
					}*/
					
				}else if(contentObj.has(_200)) {
					contentObj = contentObj.getJSONObject(_200);
					if (contentObj.has(SCHEMA)) {
						contentObj = contentObj.getJSONObject(SCHEMA);
						if (contentObj.has(DOLLAR + REF))
							schemaPath = contentObj.getString(DOLLAR + REF);
					}
				}
			}
			
			openApiVersion = getOpenApiVersion();
			
			if(openApiVersion.startsWith(_2_0)) {
				
				if(schemaPath.contains(DEFINITIONS)) {
					prepareResponseFromDefinitions(response, schemaPath,new Response(),true);
				}
				else {
					int idx = schemaPath.lastIndexOf(FORWARD_SLASH);
					schemaPath = schemaPath.substring(idx + 1, schemaPath.length());
					response = getResponseForOpenApiV2(response,schemaPath);
				}
				return response;
			}
			
			int idx = schemaPath.lastIndexOf(FORWARD_SLASH);
			schemaPath = schemaPath.substring(idx + 1, schemaPath.length());
			
			JSONObject componentObj = null;
			JSONObject schemaObj = null;
			JSONObject propObj = null;
			JSONObject props = null;
			if (parent.has(JSON_COMPONENTS)) {
				componentObj = parent.getJSONObject(JSON_COMPONENTS);
				if (componentObj.has(JSON_SCHEMAS)) {
					schemaObj = componentObj.getJSONObject(JSON_SCHEMAS);
					if (schemaObj.has(schemaPath)) {
						propObj = schemaObj.getJSONObject(schemaPath);
						Iterator<String> itr = propObj.keys();
						while (itr.hasNext()) {
							String key = itr.next();
							if (key.equals(JSON_PROPERTIES)) {
								props = propObj.getJSONObject(key);
								Iterator<String> citr = props.keys();
								while (citr.hasNext()) {
									String ckey = citr.next();
									Response res = new Response();
									res.setName(ckey);
									JSONObject propsType = props.getJSONObject(ckey);
									if(propsType.has(TYPE)){
										String type = propsType.getString(TYPE);
										res.setType(type);
									}
									response.add(res);
								}
							}
						}
					}
				}
			}
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;
	}

	private String getOpenApiVersion() throws JSONException {
		String openApiVersion = null;
		if(parent.has(OPENAPI)) {
			openApiVersion = parent.getString(OPENAPI);
		}else if(parent.has(SWAGGER)){
			openApiVersion = parent.getString(SWAGGER);
		}
		return openApiVersion;
	}
	private ArrayList<Response> getResponseForOpenApiV2(ArrayList<Response> response, String schemaPath) throws JSONException {
		JSONObject responsesObj = null;
		JSONObject responseObject = null;
		JSONObject schemaObject = null;
		
		if (parent.has(JSON_RESPONSES)) {
			responsesObj = parent.getJSONObject(JSON_RESPONSES);
			if (responsesObj.has(schemaPath)) {
				responseObject = responsesObj.getJSONObject(schemaPath);
				if (responseObject.has(SCHEMA)) {
					schemaObject = responseObject.getJSONObject(SCHEMA);
					if (schemaObject.has(DOLLAR + REF))
						schemaPath = schemaObject.getString(DOLLAR + REF);
				}
			}
		}
		
		response = prepareResponseFromDefinitions(response,schemaPath,new Response(),true);
		
		return response;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Response> prepareResponseFromDefinitions(ArrayList<Response> responseList,String schemaPath, Response response, boolean addToParent) throws JSONException {
		JSONObject defObject = null;
		JSONObject schemaObject = null;
		JSONArray responseObject = null;
		if(schemaPath.contains(DEFINITIONS)) {
			schemaPath = schemaPath.substring(schemaPath.lastIndexOf(FORWARD_SLASH)+1);
			if(parent.has(DEFINITIONS)) {
				defObject = parent.getJSONObject(DEFINITIONS);
				if(defObject.has(schemaPath)) {
					schemaObject = defObject.getJSONObject(schemaPath);
					if(schemaObject.has(ALL_OF)) {
						responseObject = (JSONArray) schemaObject.get(ALL_OF);
						int i = 0;
						while(i<responseObject.length()) {
							JSONObject resObj = responseObject.getJSONObject(i);
								if(resObj.has(DOLLAR + REF)) {
									String schemaPathTemp = resObj.getString(DOLLAR + REF);
									Response newRes = new Response();
									prepareResponseFromDefinitions(responseList, schemaPathTemp,newRes,addToParent);
									response.getChild().add(newRes);
								}else if(resObj.has(PROPERTIES)) {
									if (response.getName() != null) {
										Response childRes = new Response();
										childRes.setName(schemaPath + HYPHEN + ALL_OF + OPEN_BRKT + i + CLOSE_BRKT);
										childRes.setType(OBJECT);
										getResponseFromProperties(responseList, resObj, childRes, addToParent);
										response.getChild().add(childRes);
									} else {
										response.setName(schemaPath + HYPHEN + ALL_OF + OPEN_BRKT + i + CLOSE_BRKT);
										getResponseFromProperties(responseList, resObj, response, addToParent);
									}
								}
						i++;	
						}
					}else if(schemaObject.has(PROPERTIES)) {
						response.setName(schemaPath);
						response.setType(OBJECT);
						getResponseFromProperties(responseList, schemaObject, response,addToParent);
					}
				}
			}
		}
		return responseList;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Response> getResponseFromProperties(ArrayList<Response> responseList, JSONObject resObj, Response response, boolean addToParent) throws JSONException {
		JSONObject props = resObj.getJSONObject(PROPERTIES);
		Iterator<String> propItr = props.keys();
		ArrayList<Response> childrens = new ArrayList<>();
		while (propItr.hasNext()) {
			String propkey = propItr.next();
			Response res = new Response();
			res.setName(propkey);
			JSONObject propsType = props.getJSONObject(propkey);
			if(propsType.has(TYPE)){
				String type = propsType.getString(TYPE);
				if (type.equals(JSON_ARRAY)) {
					JSONObject itemsObj = propsType.getJSONObject(ITEMS);
					if (itemsObj.has(DOLLAR + REF)) {
						Response itemChild = new Response();
						res.getChild().add(itemChild);
						String refPath = itemsObj.getString(DOLLAR + REF);
						prepareResponseFromDefinitions(responseList, refPath,itemChild,false);
					}
				}else if (type.equals(OBJECT)) {
					if (propsType.has(PROPERTIES)) {
						getResponseFromProperties(responseList, propsType, res,false);
					}
				}
				
				res.setType(type);
			}
			childrens.add(res);
		}
		response.setChild(childrens);
		if(addToParent) {
			responseList.add(response);
		}
		return responseList;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, ArrayList<XMLHelper>> getPathsData() {
		try {
			JSONObject jsonObject = parent;
			JSONObject pathObject = jsonObject.getJSONObject(JSON_PATHS);
			Iterator<String> pathkeys = pathObject.keys();
			while (pathkeys.hasNext()) {
				String key = pathkeys.next();
				ArrayList<XMLHelper> a = new ArrayList<>();
				if (pathObject.has(key)) {
					JSONObject singlePathObject = pathObject.getJSONObject(key);
					
					ArrayList<Request> commonParameters = new ArrayList<>();
					
					if (singlePathObject.has(JSON_PARAMETERS)) {
						JSONArray parameterArray = singlePathObject.getJSONArray(JSON_PARAMETERS);
						commonParameters = getParameters(parameterArray);
					}
					Iterator<String> singlepathkeys = singlePathObject.keys();
					ArrayList<String> arr = new ArrayList<>();
					while (singlepathkeys.hasNext()) {
						String ckey = singlepathkeys.next();
						if (singlePathObject.has(ckey)) {
							if (isMethod(ckey)) {
								JSONArray methodArray = singlePathObject.getJSONObject(ckey).getJSONArray(TAGS);
								String tagName = methodArray.getString(0);
								XMLHelper xml = new XMLHelper();
								xml.setPath(key);
								xml.setMethod(ckey);
								if (singlePathObject.getJSONObject(ckey).has(JSON_PARAMETERS)) {
									JSONArray parameterArray = singlePathObject.getJSONObject(ckey)
											.getJSONArray(JSON_PARAMETERS);
									ArrayList<Parameter> pm = extractParameters(parameterArray);
									xml.setParameters(pm);
									
									String apiVersion = getOpenApiVersion();
									if(apiVersion.startsWith(_2_0)) {
										ArrayList<Request> req = getParameters(parameterArray);
										xml.setRequest(req);
									}
								}
								if(commonParameters.size()>0) {
									ArrayList<Request> currentReq = (xml.getRequest() == null) ? new ArrayList<>() : xml.getRequest();
									ArrayList<Request> result = new ArrayList<>();
									result.addAll(commonParameters);
									result.addAll(currentReq);
									
									xml.setRequest(result);
								}
								
								if (singlePathObject.getJSONObject(ckey).has(REQUEST_BODY)) {
								

									JSONObject requestBody = singlePathObject.getJSONObject(ckey)
											.getJSONObject(REQUEST_BODY);
									ArrayList<Request> rb = null;
									rb = extractRequest(requestBody);
									Request req = extractRequestForOpenApiV3(requestBody);
									xml.setSingleRequest(req);
									xml.setRequest(rb);
								}
								if (singlePathObject.getJSONObject(ckey).has(JSON_RESPONSES)) {
									JSONObject parameterArray = singlePathObject.getJSONObject(ckey)
											.getJSONObject(JSON_RESPONSES);
									Response res = null;
									String apiVersion = getOpenApiVersion();
									if(apiVersion.startsWith(_2_0)) {
										track = new HashSet<>();
										res = getResponse(singlePathObject.getJSONObject(ckey).getJSONObject(JSON_RESPONSES));
									}
									else{
										res = extractResponseForOpenApiV3(singlePathObject.getJSONObject(ckey));
									}
									xml.setSingleResponse(res);
								}
								if (mapMaster.containsKey(tagName)) {
									mapMaster.get(tagName).add(xml);
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mapMaster;
	}

	private ArrayList<Request> extractRequestForOpenApiV2(JSONArray parameterArray) {
		ArrayList<Request> requestBody = new ArrayList<>();
		for (int i = 0; i < parameterArray.length(); i++) {
			try {
				JSONObject parameter = parameterArray.getJSONObject(i);
				if (parameter.has(IN)) {
					prepareBodyRequest(requestBody, parameter);
				} else if (parameter.has(DOLLAR + REF)) {
					String path = parameter.getString(DOLLAR + REF);
					if (path.contains(JSON_PARAMETERS)) {
						if (parent.has(JSON_PARAMETERS)) {
							int idx = path.lastIndexOf(FORWARD_SLASH);
							path = path.substring(idx + 1, path.length());

							JSONObject paramRoot = parent.getJSONObject(JSON_PARAMETERS);
							if (paramRoot.has(path)) {
								paramRoot = paramRoot.getJSONObject(path);
								if (paramRoot.has(IN)) {
									prepareBodyRequest(requestBody, paramRoot);
								}
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return requestBody;
	}

	private void prepareBodyRequest(ArrayList<Request> requestList, JSONObject parameter) throws JSONException {
		String type = parameter.getString(IN);
		if (type.equals(BODY)) {
			if (parameter.has(SCHEMA)) {
				parameter = parameter.getJSONObject(SCHEMA);
				if (parameter.has(DOLLAR + REF)) {
					String refPath = parameter.getString(DOLLAR + REF);
					prepareRequestFromDefinitions(requestList, refPath, new Request(), true);
				}
			}
		}
	}

	private void prepareRequestFromDefinitions(ArrayList<Request> requestList, String refPath,Request request, boolean isParent) throws JSONException {
		if (refPath.contains(DEFINITIONS)) {
			int idx = refPath.lastIndexOf(FORWARD_SLASH);
			refPath = refPath.substring(idx + 1, refPath.length());

			JSONObject defObject = parent.getJSONObject(DEFINITIONS);
			if (defObject.has(refPath)) {
				defObject = defObject.getJSONObject(refPath);

				if (defObject.has(ALL_OF)) {
					JSONArray reqArray = (JSONArray) defObject.get(ALL_OF);
					int i = 0;
					while (i < reqArray.length()) {
						JSONObject reqObj = reqArray.getJSONObject(i);
						if (reqObj.has(DOLLAR + REF)) {
							String schemaPathTemp = reqObj.getString(DOLLAR + REF);
							Request newReq = new Request();
							if (reqObj.has(TYPE)) {
								newReq.setType(reqObj.getString(TYPE));
							}
							if (reqObj.has(NULLABLE)) {
								newReq.setNullable(reqObj.getBoolean(NULLABLE));
							}

							prepareRequestFromDefinitions(requestList, schemaPathTemp, newReq, isParent);
						} else if (reqObj.has(PROPERTIES)) {
							request.setName(refPath + HYPHEN + ALL_OF + OPEN_BRKT + i + CLOSE_BRKT);
							if (reqObj.has(TYPE)) {
								request.setType(reqObj.getString(TYPE));
							}
							if (reqObj.has(NULLABLE)) {
								request.setNullable(reqObj.getBoolean(NULLABLE));
							}
							prepareRequestFromProperties(requestList, reqObj, request, isParent);
						}
						i++;
					}
				} else if (defObject.has(PROPERTIES)) {
					request.setName(refPath);
					if (defObject.has(TYPE)) {
						request.setType(defObject.getString(TYPE));
					}
					if (defObject.has(NULLABLE)) {
						request.setNullable(defObject.getBoolean(NULLABLE));
					}
					prepareRequestFromProperties(requestList, defObject, request, isParent);
				} else if (defObject.has(DOLLAR + REF)) {
					String refPath1 = defObject.getString(DOLLAR + REF);
					prepareRequestFromDefinitions(requestList, refPath1, request, isParent);
				}
			}
		}
	}

	private ArrayList<Request> prepareRequestFromProperties(ArrayList<Request> reqList, JSONObject reqObj,
			Request request, boolean isParent) throws JSONException {
		JSONObject props = reqObj.getJSONObject(PROPERTIES);
		Iterator<String> propItr = props.keys();
		ArrayList<Request> childrens = new ArrayList<>();
		while (propItr.hasNext()) {
			String propkey = propItr.next();
			Request req = new Request();
			req.setName(propkey);
			JSONObject propsType = props.getJSONObject(propkey);
			if (propsType.has(TYPE)) {
				String type = propsType.getString(TYPE);

				if (type.equals(JSON_ARRAY)) {
					JSONObject itemsObj = propsType.getJSONObject(ITEMS);
					if (itemsObj.has(DOLLAR + REF)) {
						String refPath = itemsObj.getString(DOLLAR + REF);
						Request itemChild = new Request();
						req.getChild().add(itemChild);
						prepareRequestFromDefinitions(reqList, refPath, itemChild, false);
					}else if(itemsObj.has(TYPE) && itemsObj.getString(TYPE).equals(JSON_STRING)) {
						Request newReq = new Request();
						newReq.setName(ITEMS);
						newReq.setType(JSON_STRING);
						req.getChild().add(newReq);
					}
				} else if (type.equals(OBJECT)) {
					if (propsType.has(PROPERTIES)) {
						prepareRequestFromProperties(reqList, propsType, req, false);
					}
				}
				req.setType(type);
			}
			childrens.add(req);
		}
		request.setChild(childrens);
		if (isParent) {
			reqList.add(request);
		}
		return reqList;
	}
	
	/**Below are newly added methods for constructing Response**/
	
	public Response extractResponseForOpenApiV3(JSONObject obj) {
		Response res = null;
		try {
			JSONObject contentObj = null;
			String schemaPath = BLANK;
			if (obj.has(JSON_RESPONSES)) {
				contentObj = obj.getJSONObject(JSON_RESPONSES);
				if (contentObj.has(JSON_DEFAULT)) {
					contentObj = contentObj.getJSONObject(JSON_DEFAULT);
					if (contentObj.has(JSON_CONTENT)) {
						contentObj = contentObj.getJSONObject(JSON_CONTENT);
						if (contentObj.has(APPLICATION_JSON)) {
							contentObj = contentObj.getJSONObject(APPLICATION_JSON);
							if (contentObj.has(SCHEMA)) {
								contentObj = contentObj.getJSONObject(SCHEMA);
								if (contentObj.has(DOLLAR + REF))
									schemaPath = contentObj.getString(DOLLAR + REF);
							}
						}
					}
				}
			}

			int idx = schemaPath.lastIndexOf(FORWARD_SLASH);
			schemaPath = schemaPath.substring(idx + 1, schemaPath.length());

			JSONObject componentObj = null;
			JSONObject schemaObj = null;
			JSONObject propObj = null;
			JSONObject props = null;
			if (parent.has(JSON_COMPONENTS)) {
				componentObj = parent.getJSONObject(JSON_COMPONENTS);

				if (componentObj.has(JSON_SCHEMAS)) {
					schemaObj = componentObj.getJSONObject(JSON_SCHEMAS);

					if (schemaObj.has(schemaPath)) {
						propObj = schemaObj.getJSONObject(schemaPath);
						res = Addschema(propObj, schemaObj, schemaPath, false, null);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return res;
	}

	public Response Addschema(JSONObject singleEle, JSONObject schema, String name, boolean isObjectType, Response par)
			throws JSONException {

		Response parent;
		if (isObjectType == false) {
			parent = new Response();
			parent.name = name;
			if (singleEle.has(TYPE))
				parent.type = singleEle.getString(TYPE);
			else
				parent.type = ANYTYPE;
		} else {
			parent = par;
		}

		if (singleEle.has(JSON_PROPERTIES)) {
			JSONObject props = singleEle.getJSONObject(JSON_PROPERTIES);
			Iterator<String> singlepProp = props.keys();
			while (singlepProp.hasNext()) {
				String ckey = singlepProp.next();
				if (props.has(ckey)) {
					JSONObject ccprops = props.getJSONObject(ckey);
					String pType = ccprops.getString(TYPE);

					Response child = new Response();
					child.name = ckey;
					child.type = pType;
					if (pType.equals(JSON_ARRAY)) {

						if (ccprops.has(JSON_ITEMS)) {
							JSONObject Citem = ccprops.getJSONObject(JSON_ITEMS);
							if (Citem.has(DOLLAR + REF)) {
								String url = Citem.getString(DOLLAR + REF);
								String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
								JSONObject childSchema = schema.getJSONObject(childSchemaName);

								Response c = Addschema(childSchema, schema, childSchemaName, false, null);
								child.childs.add(c);

							}

						}
					} else if (pType.equals(OBJECT)) {
						JSONObject childSchema = props.getJSONObject(ckey);
						Response c = Addschema(childSchema, schema, null, true, child);
					}
					parent.childs.add(child);

				}
			}
		}
		else{

			if(singleEle.has(JSON_DISCRIMINATOR)){
				JSONObject disc = singleEle.getJSONObject(JSON_DISCRIMINATOR);
				
				
				Response child = new Response();
				child.name = JSON_DISCRIMINATOR;
				child.type = ANYTYPE;
				
				JSONObject mapping = null;
				if(disc.has(JSON_MAPPING)){
					mapping = disc.getJSONObject(JSON_MAPPING);

					Iterator<String> pathkeys = mapping.keys();
					
					while (pathkeys.hasNext()) {
						String key = pathkeys.next();
						String url = mapping.getString(key);
						String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
						JSONObject childSchema = schema.getJSONObject(childSchemaName);
						Response c = Addschema(childSchema, schema, childSchemaName,false,null);
						child.childs.add(c);
					}
				
				}
				parent.childs.add(child);
			}
			if(singleEle.has(JSON_ONEOF)){
				JSONArray oneOF = singleEle.getJSONArray(JSON_ONEOF);
				Response child = new Response();
				child.name = JSON_ONEOF;
				child.type = ANYTYPE;
				for (int i = 0; i < oneOF.length(); i++) {
					JSONObject objectInArray = oneOF.getJSONObject(i);
					if(objectInArray.has(DOLLAR + REF)){
						String url = objectInArray.getString(DOLLAR + REF);
						String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
						JSONObject childSchema = schema.getJSONObject(childSchemaName);
						Response c = Addschema(childSchema, schema, childSchemaName,false,null);
						child.childs.add(c);
					}
					
				}
				parent.childs.add(child);
			}
			if(parent.name.equals(JSON_MAPPING)){
				Iterator<String> pathkeys = singleEle.keys();
				
				while (pathkeys.hasNext()) {
					String key = pathkeys.next();
					Response child = new Response();
					child.name = key;
					child.type = ANYTYPE;
					String url = singleEle.getString(key);
					String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
					JSONObject childSchema = schema.getJSONObject(childSchemaName);
					Response c = Addschema(childSchema, schema, childSchemaName,false,null);
					child.childs.add(c);
					parent.childs.add(child);
				}
			}
			if (singleEle.has(DOLLAR + REF)) {
				
				String url = singleEle.getString(DOLLAR + REF);
				String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
				JSONObject childSchema = schema.getJSONObject(childSchemaName);
				Response c = Addschema(childSchema, schema, childSchemaName,false,null);
				parent.childs.add(c);
			}
			
		}
		return parent;

	}
	
	public Request extractRequestForOpenApiV3(JSONObject reqObj) {
		Request req = new Request();
		try {
			if (reqObj.has(DOLLAR + REF)) {
				String url = reqObj.getString(DOLLAR + REF);
				int idx = url.lastIndexOf(FORWARD_SLASH);
				String reqKey = url.substring(idx + 1);

				JSONObject jsonObject = parent;

				JSONObject componentObj = null;
				JSONObject schemaObj = null;
				JSONObject propObj = null;
				JSONObject props = null;
				JSONObject requestBody = null;
				if (parent.has(JSON_COMPONENTS)) {
					componentObj = parent.getJSONObject(JSON_COMPONENTS);

					if (componentObj.has(JSON_SCHEMAS)) {
						schemaObj = componentObj.getJSONObject(JSON_SCHEMAS);

						if (schemaObj.has(reqKey)) {
							propObj = schemaObj.getJSONObject(reqKey);
							req = AddschemaReq(propObj, schemaObj, reqKey, false, null);
						}
					}
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return req;
	}
	public static Request AddschemaReq(JSONObject singleEle, JSONObject schema, String name, boolean isObjectType,
			Request par) throws JSONException {
		
		Request parent;
		if (isObjectType == false) {
			parent = new Request();
			parent.name = name;
			if (singleEle.has(TYPE))
				parent.type = singleEle.getString(TYPE);
			else
				parent.type = ANYTYPE;
		} else {
			parent = par;
		}

		if (singleEle.has(JSON_PROPERTIES)) {
			JSONObject props = singleEle.getJSONObject(JSON_PROPERTIES);
			Iterator<String> singlepProp = props.keys();
			while (singlepProp.hasNext()) {
				String ckey = singlepProp.next();
				if (props.has(ckey)) {
					JSONObject ccprops = props.getJSONObject(ckey);
					String pType = ccprops.getString(TYPE);

					Request child = new Request();
					child.name = ckey;
					child.type = pType;
					if (pType.equals(JSON_ARRAY)) {
						if (ccprops.has(JSON_ITEMS)) {
							JSONObject Citem = ccprops.getJSONObject(JSON_ITEMS);
							if (Citem.has(DOLLAR + REF)) {
								String url = Citem.getString(DOLLAR + REF);
								String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
								JSONObject childSchema = schema.getJSONObject(childSchemaName);
								
								Request c = AddschemaReq(childSchema, schema, childSchemaName, false, null);
								child.childs.add(c);
							}
						}
					} else if (pType.equals(OBJECT)) {
						JSONObject childSchema = props.getJSONObject(ckey);
						Request c = AddschemaReq(childSchema, schema, null, true, child);
					}
					parent.childs.add(child);

				}
			}
		}
		else{
			if(singleEle.has(JSON_DISCRIMINATOR)){
				JSONObject disc = singleEle.getJSONObject(JSON_DISCRIMINATOR);	
				Request child = new Request();
				child.name = JSON_DISCRIMINATOR;
				child.type = ANYTYPE;
				
				JSONObject mapping = null;
				if(disc.has(JSON_MAPPING)){
					
					mapping = disc.getJSONObject(JSON_MAPPING);
					Iterator<String> pathkeys = mapping.keys();
					
					while (pathkeys.hasNext()) {
						String key = pathkeys.next();
						String url = mapping.getString(key);
						String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
						JSONObject childSchema = schema.getJSONObject(childSchemaName);
						Request c = AddschemaReq(childSchema, schema, childSchemaName,false,null);
						child.childs.add(c);
					}
				}
				parent.childs.add(child);
			}
			if(singleEle.has(JSON_ONEOF)){
				JSONArray oneOF = singleEle.getJSONArray(JSON_ONEOF);
				Request child = new Request();
				child.name = JSON_ONEOF;
				child.type = ANYTYPE;
				for (int i = 0; i < oneOF.length(); i++) {
					JSONObject objectInArray = oneOF.getJSONObject(i);
					if(objectInArray.has(DOLLAR + REF)){
						String url = objectInArray.getString(DOLLAR + REF);
						String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
						JSONObject childSchema = schema.getJSONObject(childSchemaName);
						Request c = AddschemaReq(childSchema, schema, childSchemaName,false,null);
						child.childs.add(c);
					}
				}
				parent.childs.add(child);
			}
			if(parent.name.equals(JSON_MAPPING)){
				Iterator<String> pathkeys = singleEle.keys();
				
				while (pathkeys.hasNext()) {
					String key = pathkeys.next();
					Request child = new Request();
					child.name = key;
					child.type = ANYTYPE;
					String url = singleEle.getString(key);
					String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
					JSONObject childSchema = schema.getJSONObject(childSchemaName);
					Request c = AddschemaReq(childSchema, schema, childSchemaName,false,null);
					child.childs.add(c);
					parent.childs.add(child);
				}
			}
			if (singleEle.has(DOLLAR + REF)) {
				
				String url = singleEle.getString(DOLLAR + REF);
				String childSchemaName = url.substring(url.lastIndexOf(FORWARD_SLASH) + 1);
				JSONObject childSchema = schema.getJSONObject(childSchemaName);
				Request c = AddschemaReq(childSchema, schema, childSchemaName,false,null);
				parent.childs.add(c);
			}
		}
		return parent;
	}
	//Definitions
	public Request fillDefinitions(JSONObject defObj, String name, JSONObject defParent) throws JSONException {

		Request parent = new Request();
		parent.setName(name);
		
		String type = defObj.has(TYPE) ? defObj.getString(TYPE) : OBJECT;

		parent.setType(type);
		if (defObj.has(PROPERTIES)) {
			JSONObject props = defObj.getJSONObject(PROPERTIES);
			Iterator<String> singlepProp = props.keys();
			while (singlepProp.hasNext()) {
				String ckey = singlepProp.next();
				if (props.has(ckey)) {
					JSONObject ccprops = props.getJSONObject(ckey);

					String pType = BLANK;
					if (ccprops.has(TYPE)) {
						pType = ccprops.getString(TYPE);
					} else {
						pType = OBJECT;
						
					}
					Request child = new Request();
					child.setName(ckey);
					child.setType(pType);
					if (pType.equals(OBJECT) || pType.equals(JSON_ARRAY)) {
						if (ccprops.has(ITEMS)) {

							Object aObj = ccprops.get(ITEMS);
							if (aObj instanceof JSONArray) {
								JSONArray items = ccprops.getJSONArray(ITEMS);
								for (int i = 0; i < items.length(); i++) {
									JSONObject objectInArray = items.getJSONObject(i);
									Request cc = fillDefinitions(objectInArray, child.name + ITEMS + i, defParent);
									child.childs.add(cc);
								}
							} else if (aObj instanceof JSONObject) {
								JSONObject items = ccprops.getJSONObject(ITEMS);
								if (items.has(DOLLAR + REF)) {

									String schemaPath = items.getString(DOLLAR + REF);
									String schema = getSchema(schemaPath);
									JSONObject cc = defParent.getJSONObject(schema);
									Request c = fillDefinitions(cc, schema, defParent);
									child.childs.add(c);
								}
							}
						}
						
					}
					parent.childs.add(child);
				}
			}
		} else if (defObj.has(DOLLAR + REF)) {
			Request child = new Request();
			String schemaPath = defObj.getString(DOLLAR + REF);
			String schema = getSchema(schemaPath);
			JSONObject cc = defParent.getJSONObject(schema);
			child = fillDefinitions(cc, schema, defParent);
			parent.childs.add(child);
		} else if (defObj.has(ALL_OF)) {
			JSONArray allOf = defObj.getJSONArray(ALL_OF);
			String childname = ALL_OF;
			/*Request child = new Request();
			child.name = ALL_OF;
			child.type = JSON_ARRAY;*/

			for (int i = 0; i < allOf.length(); i++) {
				JSONObject objectInArray = allOf.getJSONObject(i);
				if (objectInArray.has(DOLLAR + REF)) {
					Request cchild = new Request();
					String schemaPath = objectInArray.getString(DOLLAR + REF);
					String schema = getSchema(schemaPath);
					JSONObject cc = defParent.getJSONObject(schema);
					cchild = fillDefinitions(cc, schema, defParent);
					parent.childs.add(cchild);

				} else if (objectInArray.has(PROPERTIES)) {
					JSONObject props = objectInArray.getJSONObject(PROPERTIES);
					Request allOfchild = new Request();
					allOfchild.setName(parent.name + childname + i);
					String dtype = objectInArray.has(TYPE) ? objectInArray.getString(TYPE) : OBJECT;
					allOfchild.setType(dtype);

					Iterator<String> singlepProp = props.keys();
					while (singlepProp.hasNext()) {
						String ckey = singlepProp.next();
						if (props.has(ckey)) {
							JSONObject ccprops = props.getJSONObject(ckey);

							String pType = ccprops.has(TYPE) ? ccprops.getString(TYPE) : OBJECT;
							Request cchild = new Request();
							cchild.setName(ckey);
							cchild.setType(pType);
							if (pType.equals(OBJECT) || pType.equals(JSON_ARRAY)) {
								if (ccprops.has(ITEMS)) {
									JSONObject items = ccprops.getJSONObject(ITEMS);
									if (items.has(DOLLAR + REF)) {
										String schemaPath = items.getString(DOLLAR + REF);
										String schema = getSchema(schemaPath);
										JSONObject cc = defParent.getJSONObject(schema);
										Request c = fillDefinitions(cc, schema, defParent);
										cchild.childs.add(c);
									}
								}
							}

							allOfchild.childs.add(cchild);

						}
					}
					parent.childs.add(allOfchild);

				}

			}

		}
		return parent;
	}
	//Adding Parameters for Swagger 2.0
	public boolean isQueryAdded = false;
	public ArrayList<Request> getParameters(JSONArray parameterArray) throws JSONException {
		isQueryAdded = false;
		ArrayList<Request> arr = new ArrayList<>();
		for (int i = 0; i < parameterArray.length(); i++) {
			JSONObject parObject = parameterArray.getJSONObject(i);
			if (parObject.has(SCHEMA)) {
				int si = 1;
				parObject = parObject.getJSONObject(SCHEMA);
				if (parObject.has(DOLLAR + REF)) {
					String schema = parObject.getString(DOLLAR + REF);
					if (schema.contains(HASH+FORWARD_SLASH+DEFINITIONS)) {
						schema = getSchema(schema);
						JSONObject jsonObject = parent;
						JSONObject defObject = jsonObject.getJSONObject(DEFINITIONS);
						JSONObject target = defObject.getJSONObject(schema);

						Request ele = fillDefinitions(target, schema, defObject);
						arr.add(ele);
					}
				}
			} else if (parObject.has(DOLLAR + REF)) {
				String schema = parObject.getString(DOLLAR + REF);
				if (schema.contains(HASH+FORWARD_SLASH+JSON_PARAMETERS)) {
					schema = getSchema(schema);
					JSONObject jsonObject = parent;
					JSONObject paramObject = jsonObject.getJSONObject(JSON_PARAMETERS);
					JSONObject target = paramObject.getJSONObject(schema);

					Request ele = fillParametersHelperSpecific(target, schema, paramObject);
					if(!ele.name.equals(BLANK))
					arr.add(ele);
				}
			} else {
				String val = parObject.getString(IN);
				if (val.equals(PATH)) {
					Request simpleDef = new Request();
					if (parObject.has(NAME)) {
						simpleDef.setName(parObject.getString(NAME));
					}
					if (parObject.has(TYPE)) {
						simpleDef.setType(parObject.getString(TYPE));
					}
					arr.add(simpleDef);
				} else if (val.equals(JSON_QUERY)) {
					if (isQueryAdded == false) {
						Request simpleDef = new Request();
						simpleDef.setName(QUERY);
						simpleDef.setType(TYPESTRING);
						arr.add(simpleDef);
						isQueryAdded = true;
					}
				}

			}
		}
		return arr;
	}
	public Request fillParametersHelperSpecific(JSONObject paramObj, String name, JSONObject paramParent)
			throws JSONException {
		JSONObject jsonObject = parent;
		JSONObject singleDefObject = paramObj;
		Request parele = null;
		if (singleDefObject.has(IN)) {
			String val = singleDefObject.getString(IN);
			parele = fillParameters(singleDefObject, name, paramParent);
		}
		return parele;

	}
	
	public Request fillParameters(JSONObject parObj, String name, JSONObject parParent) throws JSONException {
		JSONObject defParent = parent.getJSONObject(DEFINITIONS);
		Request parent = new Request();
		if (parObj.has(IN)) {

			String val = parObj.getString(IN);
			if (val.equals(PATH) || val.equals(BODY)) {
				if (parObj.has(SCHEMA)) {
					JSONObject schemaObj = parObj.getJSONObject(SCHEMA);
					if (schemaObj.has(DOLLAR + REF)) {
						Request child = new Request();
						String schemaPath = schemaObj.getString(DOLLAR + REF);
						String schema = getSchema(schemaPath);
						JSONObject cc = defParent.getJSONObject(schema);
						parent = fillDefinitions(cc, schema, defParent);
					}
				}
			} else if (val.equals(JSON_QUERY) && isQueryAdded == false) {
				parent = new Request();
				parent.setName(QUERY);
				parent.setType(TYPESTRING);
				isQueryAdded = true;
			}
		}
		return parent;
	}
	
	//Adding Definitions for Response
	 public Response fillDefinitionsRes(JSONObject defObj, String name, JSONObject defParent,HashSet<String> cycleCheck) throws JSONException {

			Response parent = new Response();
			parent.setName(name);
			
			String type = defObj.has(TYPE) ? defObj.getString(TYPE) : OBJECT;

			parent.setType(type);
			if (defObj.has(PROPERTIES)) {
				JSONObject props = defObj.getJSONObject(PROPERTIES);
				Iterator<String> singlepProp = props.keys();
				while (singlepProp.hasNext()) {
					String ckey = singlepProp.next();
					if (props.has(ckey)) {
						JSONObject ccprops = props.getJSONObject(ckey);

						String pType = BLANK;
						if (ccprops.has(TYPE)) {
							pType = ccprops.getString(TYPE);
						} else {
							pType = OBJECT;
							
						}
						Response child = new Response();
						child.setName(ckey);
						child.setType(pType);
						if (pType.equals(OBJECT) || pType.equals(JSON_ARRAY)) {
							if (ccprops.has(ITEMS)) {

								Object aObj = ccprops.get(ITEMS);
								if (aObj instanceof JSONArray) {
									JSONArray items = ccprops.getJSONArray(ITEMS);
									for (int i = 0; i < items.length(); i++) {
										JSONObject objectInArray = items.getJSONObject(i);
										Response cc = fillDefinitionsRes(objectInArray, child.name + ITEMS + i, defParent);
										child.childs.add(cc);
									}
								} else if (aObj instanceof JSONObject) {
									JSONObject items = ccprops.getJSONObject(ITEMS);
									if (items.has(DOLLAR + REF)) {

										String schemaPath = items.getString(DOLLAR + REF);
										String schema = getSchema(schemaPath);
										JSONObject cc = defParent.getJSONObject(schema);
										Response c = fillDefinitionsRes(cc, schema, defParent);
										child.childs.add(c);
									}
								}
							}
							else if(ccprops.has("$ref")){
								
								String schemaPath =ccprops.getString("$ref");
								if(schemaPath.contains("#/definitions")){
								String schema = getSchema(schemaPath);
								if(!schema.equals("Related")){
									//track.add(schema);
									JSONObject cc = defParent.getJSONObject(schema);
									Response c = fillDefinitionsRes(cc, schema, defParent);
									child.childs.add(c);
								}
								else if(schema.equals("Related") && !track.contains("Related")){
									track.add(schema);
									JSONObject cc = defParent.getJSONObject(schema);
									Response c = fillDefinitionsRes(cc, schema, defParent);
									child.childs.add(c);
								}
								else{
									Response c = new Response();
									c.name="Related";
									c.type="object";
									child.childs.add(c);
								}
								}
								
							}
							
						}
						parent.childs.add(child);
					}
				}
			} else if (defObj.has(DOLLAR + REF)) {
				Response child = new Response();
				String schemaPath = defObj.getString(DOLLAR + REF);
				String schema = getSchema(schemaPath);
				JSONObject cc = defParent.getJSONObject(schema);
				child = fillDefinitionsRes(cc, schema, defParent);
				parent.childs.add(child);
			} else if (defObj.has(ALL_OF)) {
				JSONArray allOf = defObj.getJSONArray(ALL_OF);
				String childname = ALL_OF;
				for (int i = 0; i < allOf.length(); i++) {
					JSONObject objectInArray = allOf.getJSONObject(i);
					if (objectInArray.has(DOLLAR + REF)) {
						Response cchild = new Response();
						String schemaPath = objectInArray.getString(DOLLAR + REF);
						String schema = getSchema(schemaPath);
						JSONObject cc = defParent.getJSONObject(schema);
						cchild = fillDefinitionsRes(cc, schema, defParent);
						parent.childs.add(cchild);

					} else if (objectInArray.has(PROPERTIES)) {
						JSONObject props = objectInArray.getJSONObject(PROPERTIES);

						Response allOfchild = new Response();
						allOfchild.setName(parent.name + childname + i);
						String dtype = objectInArray.has(TYPE) ? objectInArray.getString(TYPE) : OBJECT;
						allOfchild.setType(dtype);

						Iterator<String> singlepProp = props.keys();
						while (singlepProp.hasNext()) {
							String ckey = singlepProp.next();
							if (props.has(ckey)) {
								JSONObject ccprops = props.getJSONObject(ckey);
								String pType = ccprops.has(TYPE) ? ccprops.getString(TYPE) : OBJECT;
								Response cchild = new Response();
								cchild.setName(ckey);
								cchild.setType(pType);
								if (pType.equals(OBJECT) || pType.equals(JSON_ARRAY)) {
									if (ccprops.has(ITEMS)) {
										JSONObject items = ccprops.getJSONObject(ITEMS);
										if (items.has(DOLLAR + REF)) {
											String schemaPath = items.getString(DOLLAR + REF);
											String schema = getSchema(schemaPath);
											JSONObject cc = defParent.getJSONObject(schema);
											Response c = fillDefinitionsRes(cc, schema, defParent);
											cchild.childs.add(c);
										}
									}
								}

								allOfchild.childs.add(cchild);

							}
						}
						parent.childs.add(allOfchild);

					}

				}

			}
			return parent;
		}
	 public Response getResponse(JSONObject responseObj) throws JSONException {
	 		Response response = null;
	 		if (responseObj.has(JSON_DEFAULT)) {
	 			responseObj = responseObj.getJSONObject(JSON_DEFAULT);
	 			if (responseObj.has(SCHEMA)) {
	 				responseObj = responseObj.getJSONObject(SCHEMA);
	 			}
	 			if (responseObj.has(DOLLAR + REF)) {
	 				String schemaPath = responseObj.getString(DOLLAR + REF);

	 				if (schemaPath.contains(HASH+FORWARD_SLASH+JSON_RESPONSES)) {
	 					String schema = getSchema(schemaPath);
	 					JSONObject jsonObject = parent;
	 					JSONObject resObject = jsonObject.getJSONObject(JSON_RESPONSES);
	 					JSONObject target = resObject.getJSONObject(schema);

	 					response = fillResponseHelperSpecific(target, schema, resObject);
	 				} else if (schemaPath.contains(HASH+FORWARD_SLASH+DEFINITIONS)) {
	 					String schema = getSchema(schemaPath);
	 					JSONObject jsonObject = parent;
	 					JSONObject resObject = jsonObject.getJSONObject(DEFINITIONS);
	 					JSONObject target = resObject.getJSONObject(schema);

	 					response = fillDefinitionsRes(target, schema, resObject);
	 				}

	 			}
	 		}

	 		else if (responseObj.has("200") || responseObj.has("201") || responseObj.has("202") || responseObj.has("203")
	 				|| responseObj.has("204")) {
	 			if(responseObj.has("200"))
	 			responseObj = responseObj.getJSONObject("200");
	 			if(responseObj.has("201"))
	 				responseObj = responseObj.getJSONObject("201");
	 			if(responseObj.has("202"))
	 				responseObj = responseObj.getJSONObject("202");
	 			if(responseObj.has("203"))
	 				responseObj = responseObj.getJSONObject("203");
	 			if(responseObj.has("204"))
	 				responseObj = responseObj.getJSONObject("204");
	 			if (responseObj.has(SCHEMA)) {
	 				responseObj = responseObj.getJSONObject(SCHEMA);
	 			}
	 			if (responseObj.has(DOLLAR + REF)) {
	 				String schemaPath = responseObj.getString(DOLLAR + REF);
	 				String schema = getSchema(schemaPath);
	 				JSONObject jsonObject = parent;
	 				JSONObject resObject = jsonObject.getJSONObject(DEFINITIONS);
	 				JSONObject target = resObject.getJSONObject(schema);

	 				response = fillDefinitionsRes(target, schema, resObject);
	 			}
	 		}

	 		return response;
	 	}
	 public Response fillResponseHelperSpecific(JSONObject target, String schema, JSONObject resObj)
				throws JSONException {
			JSONObject jsonObject = parent;
			Response parentObj=null;
			JSONObject resObject = jsonObject.getJSONObject(JSON_RESPONSES);
			JSONObject defObject = jsonObject.getJSONObject(DEFINITIONS);
			if(target.has(SCHEMA)){
				JSONObject schemaObj = target.getJSONObject(SCHEMA);
				if (schemaObj.has(DOLLAR + REF)) {
					Request child = new Request();
					String schemaPath = schemaObj.getString(DOLLAR + REF);
					String schemaName = getSchema(schemaPath);
					JSONObject cc = defObject.getJSONObject(schemaName);
					parentObj = fillDefinitionsRes(cc, schema, defObject);
				}
			}
			return parentObj;
	    }
	 public static String getSchema(String path) {
		int idx = path.lastIndexOf(FORWARD_SLASH);
		return path.substring(idx + 1);

	}
}
