package com.example.pullcity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class CityReader {
	private static final String ENCODE = "UTF-8";
	private static final String primary_area = "http://weather.livedoor.com/forecast/rss/primary_area.xml";
	private static final String primary_area2 = "http://weather.livedoor.com/forecast/webservice/json/v1?city=200010";
	private CityReader() {
		
	}
	static List<CityItem> getCityList() {
		List<CityItem> cityItems = new ArrayList<CityItem>();
		HttpClient client = null;
		InputStream in = null;
		int httpStatus = 0;

		/*HttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();  
	    HttpConnectionParams.setConnectionTimeout(params, 1000); //接続のタイムアウト  
	    HttpConnectionParams.setSoTimeout(params, 1000); //データ取得のタイムアウト
	    
		StringBuilder uri = new StringBuilder("https://api.github.com/repos/sekaiya/iena/commits");
		HttpGet request = new HttpGet(uri.toString());
		HttpResponse httpResponse = null;
		 
		try {
		    httpResponse = httpClient.execute(request);
		} catch (Exception e) {
		   
		}
		 
		int status = httpResponse.getStatusLine().getStatusCode();
		 
		if (HttpStatus.SC_OK == status) {
		    try {
		        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		        httpResponse.getEntity().writeTo(outputStream);
		        String s1 = null;
				String s2 = null;
				s1 = outputStream.toString(); // JSONデータ
				s2 = outputStream.toString(); // JSONデータ
				cityItems.add(new CityItem(s1, s2));
		       
		        
		        
		    } catch (Exception e) {
		          Log.d("JSONSampleActivity", "Error");
		    }
		} else {
		    Log.d("JSONSampleActivity", "Status" + status);
		    return cityItems;
		}*/
		
		
		try {
			client = new DefaultHttpClient();
			HttpParams httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
			HttpConnectionParams.setSoTimeout(httpParams, 10000);
			HttpGet get = new HttpGet();
			get.setURI(new URI(primary_area));
			HttpResponse res = client.execute(get);
			Log.d("check", "check =" + res);
			httpStatus = res.getStatusLine().getStatusCode();
			
			if (HttpStatus.SC_OK >= httpStatus) {
				in = res.getEntity().getContent();
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser xmlPullParser = factory.newPullParser();
				xmlPullParser.setInput(in, ENCODE);
				
				int eventType = xmlPullParser.next();
				boolean findCityTag = false;
				
				String city = null;
				String code = null;
				
				while (XmlPullParser.END_DOCUMENT != eventType) {
					switch (eventType) {
					case XmlPullParser.START_TAG:
						String tagName = xmlPullParser.getName();
						if (!findCityTag) {
							findCityTag = "city".equals(tagName);
						}
						if ("city".equals(tagName)) {
							city = xmlPullParser.getAttributeValue(null, "title");
							code = xmlPullParser.getAttributeValue(null, "id");
							cityItems.add(new CityItem(city, code));
						}
						break;
					default:
						break;
					}
					eventType = xmlPullParser.next();
				}
				for (int i = 0; i < cityItems.size(); i++) {
					Log.d("city", "city[" + i + "] =" + cityItems.get(i).getCity());
					Log.d("code", "code[" + i + "] =" + cityItems.get(i).getCode());
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();		
		} catch (XmlPullParserException e) {
			Log.w(CityReader.class.getSimpleName(), "XMLフィードの解析に失敗しました。", e);
		} catch (IOException e) {
			Log.w(CityReader.class.getSimpleName(), "XMLフィードの解析に失敗しました。", e);
		} finally {
			try {
				if(client != null)
					client.getConnectionManager().shutdown();
				if(in != null)
					in.close();
			} catch (Exception e) {}
		}
		return cityItems;
	}
}
