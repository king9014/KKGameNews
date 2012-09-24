package cn.dreamfield.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.stream.JsonReader;
import com.kk.activity.PcGameNewsActivity;

public class DreamFieldReader {
	
	public static PageShow getPageFromJSON(String url) throws ClientProtocolException, IOException {
		String content = getRsFromDreamField(url);
		PageShow ps = null;
		JsonReader jsonReader = new JsonReader(new StringReader(content));
		try {
			jsonReader.beginArray();
			while(jsonReader.hasNext()) {
				jsonReader.beginObject();
				ps = new PageShow();
				while(jsonReader.hasNext()) {
					String tagName = jsonReader.nextName();
					if("id".equals(tagName)) {
						ps.setId(jsonReader.nextInt());
					} else if("pid".equals(tagName)) {
						try {
							ps.setPid(jsonReader.nextInt());
						} catch (Exception e) {
							jsonReader.nextNull();
							ps.setPid(0);
						}
					} else if("page_corrent".equals(tagName)) {
						ps.setPageCurrent(jsonReader.nextInt());
					} else if("html_url".equals(tagName)) {
						ps.setHtmlUrl(jsonReader.nextString());
					}
				}
				jsonReader.endObject();
			}
			jsonReader.endArray();
		} catch(Exception e) {
			ps = null;
		}
		return ps;
	}
	
	public static void getNewResFromJSON(String url, ArrayList<NewsRes> newsRess) throws IOException {
		String content = getRsFromDreamField(url);
		JsonReader jsonReader = new JsonReader(new StringReader(content));
		jsonReader.beginObject();
		while(jsonReader.hasNext()) {
			String mainName = jsonReader.nextName();
			if("total".equals(mainName)) {
				PcGameNewsActivity.NEWS_TOTAL = jsonReader.nextInt();
			} else if("rows".equals(mainName)) {
				jsonReader.beginArray();
				while(jsonReader.hasNext()) {
					jsonReader.beginObject();
					NewsRes newsRes = new NewsRes();
					while(jsonReader.hasNext()) {
						String tagName = jsonReader.nextName();
						if("id".equals(tagName)) {
							newsRes.setId(jsonReader.nextString());
						} else if("name".equals(tagName)) {
							newsRes.setTitle(jsonReader.nextString());
						} else if("date".equals(tagName)) {
							newsRes.setDate(jsonReader.nextString());
						} else if("page_total".equals(tagName)) {
							newsRes.setPageTotal(jsonReader.nextInt());
						} else if("intro".equals(tagName)) {
							try {
								newsRes.setIntro(jsonReader.nextString());
							} catch (Exception e) {
								jsonReader.nextNull();
								newsRes.setIntro("作者好像没有留下导读...");
							}
						} else if("html_url".equals(tagName)) {
							newsRes.setUrl(jsonReader.nextString());
						} else if("img_url_s".equals(tagName)) {
							try {
								newsRes.setImgUrl(jsonReader.nextString());
							} catch (Exception e) {
								jsonReader.nextNull();
								newsRes.setImgUrl(null);
							}
						}
					}
					jsonReader.endObject();
					newsRess.add(newsRes);
				}
				jsonReader.endArray();
			}
		}
		jsonReader.endObject();
	}

	public static void getNewResFromDreamField(String url, ArrayList<NewsRes> newsRess) throws ClientProtocolException, IOException {
		String content = getRsFromDreamField(url);
        Pattern p = Pattern.compile("<news>([\\w[\\W]]+?)</news>");
        Matcher m = p.matcher(content);//匹配news
        while(m.find()) {
        	String news = m.group(1);
        	NewsRes newsRes = new NewsRes();
        	Pattern pa = Pattern.compile("<id>([\\w[\\W]]+?)</id>");
            Matcher ma = pa.matcher(news);
            if(ma.find()) {	newsRes.setId(ma.group(1));}
            pa = Pattern.compile("<title>([\\w[\\W]]+?)</title>");
            ma = pa.matcher(news);
            if(ma.find()) {	newsRes.setTitle(ma.group(1));}
            pa = Pattern.compile("<date>([\\w[\\W]]+?)</date>");
            ma = pa.matcher(news);
            if(ma.find()) {	newsRes.setDate(ma.group(1));}
            pa = Pattern.compile("<intro>([\\w[\\W]]+?)</intro>");
            ma = pa.matcher(news);
            if(ma.find()) {	newsRes.setIntro(ma.group(1));}
            pa = Pattern.compile("<url>([\\w[\\W]]+?)</url>");
            ma = pa.matcher(news);
            if(ma.find()) {	newsRes.setUrl(ma.group(1));}
            pa = Pattern.compile("<imgurl>([\\w[\\W]]+?)</imgurl>");
            ma = pa.matcher(news);
            if(ma.find()) {	newsRes.setImgUrl(ma.group(1));}
            newsRess.add(newsRes);
        }
	}
	
	public static String getRsFromDreamField(String url) throws ClientProtocolException, IOException {
		StringBuffer content = new StringBuffer();
		HttpGet httpGet = new HttpGet(url);
System.out.println("1--->"+content);
        HttpClient httpClient = new DefaultHttpClient();
System.out.println("2--->"+content);
		InputStream inputStream = null;

        HttpResponse httpResponse = httpClient.execute(httpGet);
System.out.println("3--->"+content);
        HttpEntity httpEntity = httpResponse.getEntity();
System.out.println("4--->"+content);
        inputStream = httpEntity.getContent();
System.out.println("5--->"+content);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        String line = "";
System.out.println("6--->"+content);
        while((line = bufferedReader.readLine()) != null) {
        	content.append(line);
        }

    	if(null != inputStream)
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

//System.out.println("content--->"+content);
		return content.toString();
	}
}
