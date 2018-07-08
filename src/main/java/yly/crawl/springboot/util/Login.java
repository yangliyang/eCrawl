package yly.crawl.springboot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;

public class Login {

	private static final String LOGIN_URL = "iuuqr;..gnstlr/d,idou`h/nsf.hoedy/qiq>`bu<Mnfho'BNED<10";
	private static final String USER = "UserName";
	private static final String PASS = "PassWord";
	private static final String COOKIE = "CookieDate";
	private static final boolean PROXY = true;	//默认使用代理
	private static final String CODING = "utf8";	//默认编码utf8
	private static final String PROXY_HOST = "127.0.0.1"; //代理host
	private static final int PROXY_PORT = 8118; //代理端口
	private static final int CONNECT_TIMEOUT = 10000;
	private static final String SUCCESS_FLAG = "Thanks";
	private static final String COOKIE_MEMBER = "ipb_member_id";
	private static final String COOKIE_PASS = "ipb_pass_hash";
	public static String[] eLogin(String username, String password){
		String[] ret = new String[3]; //初始为null
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		//设置代理
		if(PROXY){
			HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			httpClientBuilder.setRoutePlanner(routePlanner);
		}
		//设置Cookie
		CookieStore cookieStore = new BasicCookieStore();
		httpClientBuilder.setDefaultCookieStore(cookieStore);
		//设置超时
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).build();
		CloseableHttpClient httpClient = httpClientBuilder.build();
		HttpPost httpPost = new HttpPost(Transfer.trans(LOGIN_URL));
		httpPost.setConfig(requestConfig);
		//设置参数
		List<NameValuePair> para = new ArrayList<>();
		para.add(new BasicNameValuePair(USER, username));
		para.add(new BasicNameValuePair(PASS, password));
		para.add(new BasicNameValuePair(COOKIE, "1")); //默认一直是1，表示保存
		//登录
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(para));
		} catch (UnsupportedEncodingException e) {
			System.out.println("登录设参数失败！");
			e.printStackTrace();
		}
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String html = stream2String(entity.getContent());
			List<Cookie> cookies= cookieStore.getCookies();
			//设置返回结果
			if(html.contains(SUCCESS_FLAG)){
				ret[0] = "true";
				ret[1] = getValueByName(cookies, COOKIE_MEMBER);
				ret[2] = getValueByName(cookies, COOKIE_PASS);
				return ret;
			}
		} catch (ClientProtocolException e) {
			System.out.println("连接超时！");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			CloseUtil.close(response);
			CloseUtil.close(httpClient);
		}
		ret[0] = "false";
		return ret;
	}
	public static String stream2String(InputStream in){
		
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			
			br = new BufferedReader(new InputStreamReader(in, CODING));
			String line = null;
			while(null!=(line = br.readLine())){
				sb.append(line+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			CloseUtil.close(in);
			CloseUtil.close(br);
		}
		return sb.toString();
	}
	private static String getValueByName(List<Cookie> cookies, String name){
		String value = null;
		for(Cookie c:cookies){
			if(name.equals(c.getName())){
				value = c.getValue();
				break;
			}
		}
		return value;
	}
}
