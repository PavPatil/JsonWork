package json_modify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class test_JSON {
	public static void main(String args[]){
		test();
	}
	public static void test(){

		//System.out.print("App");
		BufferedReader in = null;
		try{
		Base64.Encoder encoder = Base64.getEncoder();
		String str = "vcastell@tibco.com" + ":" + "L!tmein1now2";
		String authorizationCode = encoder.encodeToString(str.getBytes());	
		String metadataUrlString = "https://fa-evxg-test-saasfaprod1.fa.ocs.oraclecloud.com//fscmRestApi/resources/11.13.18.05/bankAccountUserRules/describe";
		//String murl1="https://api.sap.com/odata/1.0/catalog.svc/$metadata";
		//URL metadataUrl = new URL(metadataUrlString);
		URL metadataUrl = new URL(metadataUrlString);
		HttpURLConnection metadataConnection = (HttpURLConnection) metadataUrl.openConnection();
		metadataConnection.setRequestMethod("GET");
		metadataConnection.setRequestProperty("Content-Type", "application/json");
		metadataConnection.setRequestProperty("Accept", "application/json");
		metadataConnection.setRequestProperty("Authorization", "Basic " + authorizationCode);
		metadataConnection.setDoInput(true);
		
		metadataConnection.connect();
		int responseCode = metadataConnection.getResponseCode();
		

		in = new BufferedReader(new InputStreamReader(metadataConnection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		if (responseCode == 200) {
			
			String content = response.toString();
			String apiName = getApiName(content);
			try{
				JSONObject obj = new JSONObject(content);
				JSONArray arr =  obj.getJSONObject("Resources").getJSONObject(apiName).getJSONArray("links");
				String link = arr.getJSONObject(0).getString("href");
				String required = getRequiredLink(link);
				
				JSONArray arr1 =  obj.getJSONObject("Resources").getJSONObject(apiName).getJSONObject("item").getJSONArray("actions");
				String[] methods = getMethods(arr1);
				//String ans = arr1.get
			
			}catch(JSONException e){
				System.out.println(e);
			}
		}
		
		
		}
		catch(Exception e){
			
		}
	
	}
	public static String getApiName(String content){
		String ans="";
		String test = content.replaceAll("\\s", "");
		int idx0 = test.indexOf(":", 0);
		int idx1 = test.indexOf("\"", idx0);
		int idx2 = test.indexOf("\"", idx1+1);
		ans = test.substring(idx1+1,idx2);
		//System.out.print(ans);
		return ans;
	}
	public static String getRequiredLink(String content){
		String ans="";
		String test = content.replaceAll("\\s", "");
		int idx0 = test.indexOf("fscmRestApi", 0);
		int idx1 = test.indexOf("describe", 0);
		
		ans = test.substring(idx0-1,idx1-1);
		System.out.print(ans);
		//int idx1 = test.indexOf("\"", idx0);
		//int idx2 = test.indexOf("\"", idx1+1);
		//ans = test.substring(idx1+1,idx2);
		//System.out.print(ans);
		return ans;
	}
	public static String[] getMethods(JSONArray content){
		String[] ans= new String[content.length()];
		int idx=0;
		try{
			for(int i=0;i<content.length();i++){
				JSONObject objectInArray = content.getJSONObject(i);
				String method = objectInArray.getString("method");
				System.out.print(method+"==");
				ans[idx++]=method;
			}
		}
		catch(JSONException e){
			System.out.println(e);
		}
		return ans;
	}
}
