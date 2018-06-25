package yly.crawl.springboot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.Session;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import yly.crawl.springboot.dao.GalleryDAO;
import yly.crawl.springboot.dao.ImageDAO;
import yly.crawl.springboot.pojo.Gallery;
import yly.crawl.springboot.pojo.Image;

/**

 * @author Administrator
 *
 */

public class Ecrawler {
	
	private  GalleryDAO galleryDAO;
	
	private  ImageDAO imageDAO;
	

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

	public Ecrawler(GalleryDAO galleryDAO, ImageDAO imageDAO, Session session) {
		super();
		this.galleryDAO = galleryDAO;
		this.imageDAO = imageDAO;
		this.session = session;
	}
	public Ecrawler() {
	}
	/**
	 * 发送消息
	 * @param message
	 */

    public void sendMessage(String message)  {
        try {
			this.session.getBasicRemote().sendText(message+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //this.session.getAsyncRemote().sendText(message);
    }


//    /**
//     * 群发自定义消息
//     * */
//    public static void sendInfo(String message) throws IOException {
//        for (MyWebSocket item : webSocketSet) {
//            try {
//                item.sendMessage(message);
//            } catch (IOException e) {
//                continue;
//            }
//        }
//    }


    
    /**
     * 方法区
     */
    /**
	 * 需要的方法...
	 * 
	 */
	/**
	 * 我的cookie
	 */
	private static  String COOKIE = "ipb_member_id=2798962; ipb_pass_hash=24576fff1f871b0feff9c4fbfeba4d92; yay=louder;  igneous=a5854c5a1; lv=1527562527-1527562527; s=e3a51d202; sk=qkpbkq4ifredzvkfr9a5ubuizu6";
	private static final String CODING = "gzip, deflate";
	private static final String LANGUAGE = "zh-CN,zh;q=0.9";
	private static final String CONNECTION = "Keep-Alive";
	private static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";
	private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
	private static final int DEFAULT_CONNECT_TIMEOUT = 10000;//10s建立连接的超时时间
	private static final int DEFAULT_SOCKET_TIMEOUT = 60000;//60s的传输超时时间
	private static final String ROOT_PATH = "images/";
	private static final String ZIP_PATH = ROOT_PATH + "zips/";
	
	@SuppressWarnings("static-access")
	public  CloseableHttpResponse getResponse(String url, int refreshTime, int connectTimeout, int socketTimeout, int sleep) {
		
		int sleepMills = (int)(Math.random()*sleep+1000);
		try {
			Thread.currentThread().sleep(sleepMills);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//int refreshTime = 3; //3次重连机会
		int total = refreshTime;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet  = getRequest(url);
		if(!url.contains("exh")){
			httpGet.removeHeaders("COOKIE");
		}
		//ConnectTimeout为建立连接until 的超时时间，SocketTimeout为传输数据的超时时间
		/**
		 * getConnectTimeout() 
		*  Determines the timeout in milliseconds until a connection is established.
		*	getSocketTimeout() 
		*	Defines the socket timeout (SO_TIMEOUT) in milliseconds, 
		*	which is the timeout for waiting for data or, put differently, 
		*	a maximum period inactivity between two consecutive data packets).
		 */
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();
		httpGet.setConfig(requestConfig);
		//InputStream in = null;
		CloseableHttpResponse response = null;
		while(refreshTime>0 && response==null){
			try {
				response= client.execute(httpGet);	
				break;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//超时等情况
				sendMessage("网络连接超时，开始尝试第 "+ (total + 1 - refreshTime)+"次重连");
				//e.printStackTrace();
				CloseUtil.close(response);
			}

			refreshTime--;
		}
		return response;
	}
	/**
	 * 获取网页内容的输入流，最后没关response..,有时间改成返回response
	 * In order to ensure correct deallocation of system resources
     * the user MUST call CloseableHttpResponse#close() from a finally clause.
	 * @param url
	 * @return
	 * @
	 */
	public  CloseableHttpResponse getResponse(String url) {
		final int times = 3;
		final int sleepMs = 2000;
		return getResponse(url, times, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT, sleepMs);
		
	}
	/**
	 * 默认utf8了
	 * @param url
	 * @return
	 * @
	 */
	public  String getHtml(String url) {
		CloseableHttpResponse response = getResponse(url);
		int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode==403){
			sendMessage("严重错误！服务器禁止访问！");
			return null;
		}
		if(statusCode==404){
			sendMessage("不存在的错误的地址！");
			return null;
		}
		HttpEntity entity = response.getEntity();
		InputStream in = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			in = entity.getContent();
			br = new BufferedReader(new InputStreamReader(in, "utf8"));
			String line = null;
			while(null!=(line = br.readLine())){
				sb.append(line+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			CloseUtil.close(br);
			CloseUtil.close(response);
		}
		return sb.toString();
	}
	/**
	 * 获得标题
	 * @param html
	 * @return
	 */
	public  String getOriginalTitle(String html){
		String pattern = "<title>([\\d\\D]*)</title>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(html);
		String title = "";
		if(m.find()){
			title = m.group(1);
		}
		return title;
	}
	/**
	 * 根据原始标题，获得windows下的标题
	 * @param title
	 * @return
	 */
	public  String getWindowsTitle(String title){
		//9种字符不能出现在windows文件命名中
		String[] limit = {"<", ">", "/", "\\", "|", "\"", "*", "?", ":"};
		for(String s:limit){
			if(title.contains(s)){
				title = title.replace(s, "");
			}
		}
		if(title.length()>200){
			title = title.substring(0, 200);
		}
		return title;
		
	}
	/**
	 * 从pageHtml里获得长度
	 * @param pageHtml
	 * @return
	 */
	public  int getLenth(String pageHtml){
		int lenth = 0;
		String pattern = "(\\d*) pages";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(pageHtml);
		if(m.find()){
			lenth = Integer.parseInt(m.group(1));
		}
		return lenth;
		
	}
	/**
	 * 从imageHtml里获得图片Url
	 * @param imageHtml
	 */
	public  String getImageUrl(String imageHtml){
		String url = "";
		String pattern = "img id=\"img\" src=\"([\\d\\D]*?)\"";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(imageHtml);
		
		if(m.find()){
			url = m.group(1);
		}
		return url;
	}
	/**
	 * 下载图片
	 * @param url
	 * @param file
	 * @
	 */
	public  boolean downloadImage(String url, String file) {
		CloseableHttpResponse response = getResponse(url);
		if(null == response){
			return false;
		}
		HttpEntity entity = response.getEntity();
		InputStream in = null;
		FileOutputStream fos = null;
		try {
			in = entity.getContent();
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[10240];
			int lenth = 0;
			while(-1!=(lenth=in.read(buffer))){
				fos.write(buffer, 0, lenth);
			}
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			//e.printStackTrace(); //超时，读图片的时间超过了指定的时间阈值
			sendMessage("网速慢或者图片资源问题导致的超时！");
			return false;
		}finally{

			CloseUtil.close(fos);
			CloseUtil.close(in);
			CloseUtil.close(response);
		}
		return true;
	}
	/**
	 * 根据图片uri，获得图片的类型（后缀）
	 * @param imageUrl
	 * @return
	 */
	public  String getSuffix(String imageUrl){
		String suffix = "";

		String pattern = "[\\d\\D]+\\.([\\d\\D]*)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(imageUrl);
		
		if(m.find()){
			suffix = m.group(1);
		}
		return suffix;
	}
	/**
	 * 从内部的html（即imageHtmll）获得
	 * 0：当前页码
	 * 1：总大小
	 * @param innerHtml
	 * @return
	 */
	public  int[] getInnerInfo(String innerHtml){
		int[] info = new int[2];
		String pattern = "<div><span>([\\d]*)</span> / <span>([\\d]*)</span></div>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(innerHtml);
		if(m.find()){
			info[0] = Integer.parseInt(m.group(1));
			info[1] = Integer.parseInt(m.group(2));
		}
		return info;
	}
	/**
	 * 获得当前innerHtml的后继，如果没有，返回空串
	 * @param innerHtml
	 * @return
	 */
	public  String getNextUrl(String innerHtml){
		String nextUrl = "";
		String pattern = "<a id=\"next\"[\\d\\D]*?href=\"([\\d\\D]*?)\">";
		
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(innerHtml);
		if(m.find()){
			nextUrl = m.group(1);
		}
		return nextUrl;
	}
	/**
	 * 获得outerHtml的第指定个innner的url
	 * @param outerHtml
	 * @return
	 */
	private  String getFocusInner(String outerHtml, int focus ){
		String url = "";
		String pattern = "a href=\"(https://[A-Za-z0-9/.]+?-"+focus+")";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(outerHtml);
		
		if(m.find()){
			url = m.group(1);
		}
		return url;	
	}
	/**
	 * 从PageUrl下面下载一定数量的图
	 * @param url PageUrl
	 * @param num 数量
	 * @ 
	 */
	public  Map<String, Object> exDownloadOuter(String url, int begin, int end) {
		//String outerHtml = getHtml(url);
		//String firstInnerUrl = getFirstInner(outerHtml);
		String startUrl = getStartUrl(url, begin);
		int num = end - begin + 1;
		return exDownloadInner(startUrl, num);//进入内部
	}
	/**
	 * 从ImageUrl下面下载一定数量
	 * @param url
	 * @param num
	 * @ 
	 */
	public Map<String, Object> exDownloadInner(String url, int num) {
		Gallery gallery = new Gallery();
		
		String startInnerHtml = getHtml(url);
		String title = getOriginalTitle(startInnerHtml);
		//sendMessage("开始下载："+title+"\n保存路径"+ROOT_PATH);
		//String dirName = ROOT_PATH + getWindowsTitle(title);
		
//		File destDir = new File(dirName);
//		if(!destDir.isDirectory()){
//			destDir.mkdirs();
//		}
		int[] pageInfo = getInnerInfo(startInnerHtml);
		int currentPage = pageInfo[0];
		int totalPage = pageInfo[1];
		int lenth = totalPage - currentPage + 1;
		if(lenth < num || num<=0){
			num = lenth;
		}
		
		/**
		 * set区
		 */
		gallery.setUrl(getOuterUrl(startInnerHtml));
		gallery.setTitle(title);
		gallery.setLenth(totalPage);
		String serialId = getSerialId(url);
		gallery.setSerialId(serialId);
		gallery.setGmtCreate(new Date());
		gallery.setGmtModified(new Date());
		this.galleryDAO.save(gallery);
		
		
		
		Map<Integer, String> imageUrlMap = new LinkedHashMap<>();
		Map<Integer, String> innerUrlMap = new LinkedHashMap<>();
		String currentUrl = url;
		String currentHtml = null;
		String currentImageUrl = null;
		String nextUrl = null;
		sendMessage("开始遍历要下载的图片的信息...");
		//num++;//名字从一开始
		for(int i=currentPage;i<num+currentPage;i++){
			sendMessage("正在遍历第"+i+"张！");
			currentHtml = getHtml(currentUrl);
			currentImageUrl = getImageUrl(currentHtml);
			innerUrlMap.put(i, currentUrl);
			imageUrlMap.put(i, currentImageUrl);
			nextUrl = getNextUrl(currentHtml);
			if(nextUrl.equals("")){
				break;
			}
			currentUrl = nextUrl;
			
		}
		List<Image> imageList = new ArrayList<>();
		for(Entry<Integer, String> entry:imageUrlMap.entrySet()){
			int key = entry.getKey();
			Image image = new Image();
			String imageUrl = entry.getValue();
			String suffix = getSuffix(imageUrl);
			//String fileName = dirName + "\\"+ key + "." + suffix;
			/**
			 * set区
			 */
			image.setInnerUrl(innerUrlMap.get(key));
			image.setGalleryId(serialId);
			image.setSerialNum(key);
			image.setSuffix(suffix);
			image.setUrl(imageUrl);
			image.setGmtCreate(new Date());
			image.setGmtModified(new Date());
			this.imageDAO.save(image);
			imageList.add(image);
			
//			if(downloadImage(imageUrl, fileName)){
//				sendMessage("系列第"+key+"张下载成功！");
//			}else{
//				sendMessage("系列第"+key+"张下载失败！");
//			}
		}
		Map<String, Object> infoMap = new Hashtable<>();
		infoMap.put("gallery", gallery);
		infoMap.put("imageList", imageList);
		return infoMap;
	}
	
	public void exDownloadWithDatabase(String url, int begin, int end, String cookie){
		sendMessage("初始化.......");
		if(cookie!=null){
			COOKIE = cookie;
		}
		Map<String, Object> infoMap = getInfoMap(url, begin, end);
		Gallery gallery = (Gallery) infoMap.get("gallery");
		@SuppressWarnings("unchecked")
		List<Image> imageList = (List<Image>) infoMap.get("imageList");
		/**
		 * infoMap中直接下载
		 */
		String title = gallery.getTitle();
		
		sendMessage("开始下载:"+title);
		title = getWindowsTitle(title);
		String zipFileName = gallery.getSerialId();
		sendMessage("zipFileName,"+zipFileName);
		sendMessage("下载总长度："+imageList.size());
		
		String dirName = ROOT_PATH + title;
		
		File destDir = new File(dirName);
		System.out.println(destDir.getAbsolutePath());
		if(!destDir.isDirectory()){
			destDir.mkdirs();
		}
		int reDownload = 3;
		
	
		List<Image> failedList = imageList; //将第一次视为第0次失败
		while( !(failedList.size() == 0 || reDownload < 0) ){
			failedList = downloadFailed(dirName, failedList);
			if(failedList!=null && failedList.size()!=0){
				sendMessage("失败的集合："+failedList.toString()+"\n开始第"+(4-reDownload)+"次下载失败列表：");
			}
			reDownload --;
		}
		
		
		
		
		sendMessage("下载结束！\n开始生成压缩包...");
		File zipDir = new File(ZIP_PATH);
		if(!zipDir.exists()){
			zipDir.mkdirs();
		}
		File zipFile = new File(ZIP_PATH + zipFileName +".zip");
		if(zipFile.exists()){
			zipFile.delete();
		}
		try {
			ZipUtil.toZip(dirName, new FileOutputStream(zipFile), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			sendMessage("生成失败！");
			return;
		} 
		sendMessage("生成成功！");
		
	}
	

	
	/**
	 * 私有方法
	 */
	private  List<Image> downloadFailed(String dirName, List<Image> failedList){
		String fileName = "";
		String imageUrl = "";
		List<Image> failedList2 = new ArrayList<>();
		for(Image image:failedList){
			fileName = dirName+"/"+image.getSerialNum()+"."+image.getSuffix();
			imageUrl = image.getUrl();
			CloseableHttpResponse res = getResponse(imageUrl, 1, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT, 2000);
			if(null == res || res.getStatusLine().getStatusCode()==403){
				//imageDAO.deleteById(imageUrl);
				sendMessage("更新......");
				String innerHtml = getHtml(image.getInnerUrl());
				imageUrl = getImageUrl(innerHtml);
				image.setUrl(imageUrl);
				image.setGmtModified(new Date());
				imageDAO.save(image);
			}
			if(downloadImage(imageUrl, fileName)){
				sendMessage("系列第"+image.getSerialNum()+"下载成功！");
			}else{
				sendMessage("系列第"+image.getSerialNum()+"下载失败！");
				failedList2.add(image);
			}
		}
		return failedList2;
	}
	/**
	 * 获取信息
	 * @param url
	 * @param begin
	 * @param end
	 * @return
	 */
	private Map<String, Object> getInfoMap(String url, int begin, int end){
		String serialId = getSerialId(url);
		 
		List<Gallery> galleryList = galleryDAO.findBySerialId(serialId);
		if(galleryList==null || galleryList.size()>1){
			System.out.println("数据库错误了！");
		}
		Gallery gallery = null;
		if(galleryList.size()==1){
			gallery = galleryList.get(0);
		}
		if(gallery == null){
			
			return exDownloadOuter(url, begin, end);
		}
		int lenth = gallery.getLenth();
		if(begin<=0 || begin>lenth){
			begin = 1;
		}
		if(end<=0 || end>lenth){
			end = lenth;
		}
		if(end - begin < 0){
			begin = 1;
			end = lenth;
		}
		
		int realCount = end - begin;
		List<Image> inImage = imageDAO.inImage(serialId, begin, end);
		int inCount  = inImage.size();
		if(inCount < realCount){
			
			return exDownloadOuter(url, begin, end);
		}
		Map<String, Object> infoMap = new Hashtable<>();
		infoMap.put("gallery", gallery);
		infoMap.put("imageList", inImage);
		return infoMap;
	}
	
	/**
	 * 获取第start个图的innerUrl
	 * @param outerUrl
	 * @param start
	 */
	private String getStartUrl(String outerUrl, int start){
		String outerHtml = getHtml(outerUrl);
		int lenth = getLenth(outerHtml);
		String startUrl ="";
		if(start <= 0 || start > lenth){
			return getFocusInner(outerHtml, 1);
		}
		int page = getLocationPage(start, lenth)-1; //实际页面从0计数
		String locationHtml = "";
		if(page == 0){
			locationHtml = outerHtml;
			
		}else{
			String pageUrl = outerUrl + "/?p=" + page;
			locationHtml = getHtml(pageUrl);
		}
		startUrl = getFocusInner(locationHtml, start);
		return startUrl;
		
	}
	/**
	 * 按照每页40个，获得所在的页数（从1计数）
	 * 0<start<=lenth
	 * @param start
	 * @param lenth
	 * @return
	 */
	private int getLocationPage(int start, int lenth){
		int pageItemCount = 40;
		return (int) Math.ceil(start*1.0/pageItemCount);
		
	}
	/**
	 * 获得Outer的url
	 * @param innerHtml
	 * @return
	 */
	private String getOuterUrl(String innerHtml){
		//<div class="sb"><a href="*/">
		String pattern = "<div class=\"sb\"><a href=\"([a-z0-9:/]*/)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(innerHtml);
		String outUrl = "";
		if(m.find()){
			outUrl = m.group(1);
		}
		return outUrl;
	}
	
	
	
	/**
	 * 获得request对象
	 * @param url
	 * @return
	 * @
	 */
	private  HttpGet getRequest(String url) {
		String host = null;
		try {
			URL uri = new URL(url);
			int port = uri.getPort();
			if(port == 80 || port == -1){
				host = uri.getHost();
			}else{
				host = uri.getHost()+":"+port;
			}
			
		} catch (MalformedURLException e) {
			System.out.println(url);
			e.printStackTrace();
		}
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("Accept", ACCEPT);
		httpGet.addHeader("Accept-Encoding", CODING);
		httpGet.addHeader("Accept-Language", LANGUAGE);
		httpGet.addHeader("Connection", CONNECTION);
		httpGet.addHeader("Cookie", COOKIE);
		httpGet.addHeader("Host", host);
		httpGet.addHeader("User-Agent", AGENT);
	
		return httpGet;
	}
	
	/**
	 *根据url获取序列号
	 */
	private  String getSerialId(String url){
		String pattern ="";
		if(url.contains("-")){
			pattern = "[\\d\\D]*/(\\d*)-";
		}else{
			
			pattern = "/(\\d+)/";
		}
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(url);
		String SerialId = "";
		if(m.find()){
			SerialId = m.group(1);
		}
		return SerialId;
	}
	/**
	 * 获取url标准的title
	 * @param title
	 * @return
	 */
//	private String getStandardUrlTitle(String title){
//		//保留字符 ! * ’ ( ) ; : @ & = + $ , / ? # [ ]
//		String regex="[^a-zA-Z0-9-_.~]";
//		return title.replaceAll(regex, "");
//	}
}
