package yly.crawl.springboot.controller;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import yly.crawl.springboot.util.CloseUtil;
import yly.crawl.springboot.util.Login;
import yly.crawl.springboot.util.Transfer;

@RestController
public class TargetController {
	private static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
			+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";
	private static final String TARGET = "iuuqr;..dyidou`h/nsf.";
	@RequestMapping("/target")
	public String target(String cookie){
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(Transfer.trans(TARGET));
		httpGet.addHeader("User-Agent", AGENT);
		httpGet.addHeader("Cookie", cookie);
		String html = "";
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			html = Login.stream2String(response.getEntity().getContent());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			CloseUtil.close(response);
			CloseUtil.close(httpclient);
		}
		return html;
	}
}
