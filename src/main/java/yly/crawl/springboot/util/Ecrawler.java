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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.Session;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
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
	/**
	 * 我的cookie
	 */
	private String COOKIE = "ipb_member_id=2798962; ipb_pass_hash=24576fff1f871b0feff9c4fbfeba4d92";
	private static final String DEFAULT_CHARSET = "utf8";
	private static final String STR404 = "Key missing, or incorrect key provided.";
	private static final String STR4042 = "No gallery specified.";
	private static final String CODING = "gzip, deflate";
	private static final String LANGUAGE = "zh-CN,zh;q=0.9";
	private static final String CONNECTION = "Keep-Alive";
	private static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";
	private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
	private static final int DEFAULT_CONNECT_TIMEOUT = 10000;//10s建立连接的超时时间
	private static final int DEFAULT_SOCKET_TIMEOUT = 60000;//60s的传输超时时间
	private static final String ROOT_PATH = "images/";
	private static final String ZIP_PATH = "zips/";
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
    	if(session!=null && session.isOpen()){
	    	synchronized(this.session){
		        try {
					this.session.getBasicRemote().sendText(message+"\n");
				} catch (IOException e) {
					System.out.println("浏览器强制断开！");
				}
	    	}
    	}else{
    		System.out.println(message);
    	}
    }





    
    /**
     * 方法区
     */
    /**
	 * 需要的方法...
	 * 
	 */
	
	
	/**
	 * 获取response, response 必须关闭
	 * @param url
	 * @param refreshTime
	 * @param connectTimeout
	 * @param socketTimeout
	 * @param sleep
	 * @return
	 */
	public CloseableHttpResponse getResponse(String url, int refreshTime, int connectTimeout, int socketTimeout, int sleep) {
		
		int sleepMills = (int)(Math.random()*sleep+1000);
		try {
			Thread.sleep(sleepMills);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet  = getRequest(url);

		//ConnectTimeout为建立连接until 的超时时间，SocketTimeout为传输数据的超时时间
		/**
		 * getConnectTimeout() 
		*  Determines the timeout in milliseconds until a connection is established.
		*	getSocketTimeout() 
		*	Defines the socket timeout (SO_TIMEOUT) in milliseconds, 
		*	which is the timeout for waiting for data or, put differently, 
		*	a maximum period inactivity between two consecutive data packets).
		 */
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();
		httpGet.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		while(refreshTime>0 && response==null){
			try {
				response= client.execute(httpGet);	
				break;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				CloseUtil.close(response);
			}

			refreshTime--;
		}
		return response;
	}
	/**
	 * 默认获取response
	 * In order to ensure correct deallocation of system resources
     * the user MUST call CloseableHttpResponse#close() from a finally clause.
	 * @param url
	 * @return
	 * @
	 */
	public  CloseableHttpResponse getResponse(String url) {
		final int times = 3;
		final int sleepMs = 0;
		return getResponse(url, times, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT, sleepMs);
		
	}
	/**
	 * 获取html，默认utf8了
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
		
		String charset = getCharset(entity.getContentType().getValue());
		if(charset==null){
			charset = DEFAULT_CHARSET;
		}
		InputStream in = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			in = entity.getContent();
			br = new BufferedReader(new InputStreamReader(in, charset));
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
	private  String getOriginalTitle(String html){
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
	private  String getWindowsTitle(String title){
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
	private  int getLenth(String pageHtml){
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
	private  String getImageUrl(String imageHtml){
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
	private  boolean downloadImage(String url, String file) {
		CloseableHttpResponse response = getResponse(url);
		if(null == response){
			return false;
		}
		if(response.getStatusLine().getStatusCode()!=200){
			CloseUtil.close(response);
			return false;
		}
		
		HttpEntity entity = response.getEntity();
		if(!entity.getContentType().getValue().contains("image")){
			CloseUtil.close(response);
			return false;
		}
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
			 //超时，读图片的时间超过了指定的时间阈值
			//网速慢或者图片资源问题导致的超时！
			return false;
		}finally{

			CloseUtil.close(fos);
			CloseUtil.close(in);
			CloseUtil.close(response);
		}
		return true;
	}
	/**
	 * 根据图片url，获得图片的类型（后缀）
	 * @param imageUrl
	 * @return
	 */
	private  String getSuffix(String imageUrl){
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
	 * 从内部的html（即inner）获得
	 * 0：当前页码
	 * 1：总大小
	 * @param innerHtml
	 * @return
	 */
	/*
	private  int[] getInnerInfo(String innerHtml){
		int[] info = new int[2];
		String pattern = "<div><span>([\\d]*)</span> / <span>([\\d]*)</span></div>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(innerHtml);
		if(m.find()){
			info[0] = Integer.parseInt(m.group(1));
			info[1] = Integer.parseInt(m.group(2));
		}
		return info;
	}*/
	/**
	 * 获得当前innerHtml的后继，如果没有，返回空串
	 * @param innerHtml
	 * @return
	 */
	private  String getNextUrl(String innerHtml){
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
	 * 并行扫描
	 * @param url PageUrl
	 * @param num 数量
	 * @ 
	 */
	private  List<Image> parallelScan(Gallery gallery, int begin, int end, List<Image> inImage, int degree) {
		sendMessage("开始遍历要下载的图片的信息...");
		String url = gallery.getUrl();
		List<Integer> serialNumList = new ArrayList<>();
		int size = inImage.size();
		int cur = 0;
		for(int i=begin;i<=end;i++){
			if(cur<size && 
					inImage.get(cur).getSerialNum().equals(i)){
				cur++;
				continue;
			}
			serialNumList.add(i);
		}
		
		int length = serialNumList.size();
		//length must greater than 0, so it's not necessary
		if(length>0){
			begin = serialNumList.get(0);
		}
		if(length<20){
			String startUrl = getStartUrl(url, begin);
			return scan(gallery, serialNumList, startUrl);
		}
		
		List<Future<List<Image>>> futureList = new ArrayList<>(degree);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		int scale = length/degree;
		int from = 0;
		int to = 0;
		
		for(int i=0;i<degree;i++){
			String startUrl = getStartUrl(url, serialNumList.get(from));
			to = from + scale;
			if(i == (degree-1)){
				to = length;
			}
			futureList.add(executor.submit(getScanThread(gallery, serialNumList, from, to, startUrl)));
			from = to;
		}
		executor.shutdown();
		try {
			executor.awaitTermination(2, TimeUnit.HOURS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		
		List<Image> image = new ArrayList<>();
		for(Future<List<Image>> future:futureList){
			try {
				image.addAll(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return image;

	}
	/**
	 * 扫描列表中的图
	 * @param url
	 * @param num
	 * @ 
	 */
	private List<Image> scan(Gallery gallery, List<Integer> serialNumList, String startUrl ) {
		
		
		String galleryId = gallery.getSerialId();

		List<Image> imageList = new ArrayList<>();
		Image image = null;
		for(int i : serialNumList){
			sendMessage("正在遍历第"+i+"张！");
			//only out
			Image preImage = imageDAO.findByUkImage(galleryId, i-1).orElse(null);
			String innerUrl = null;
			if(preImage == null){
				innerUrl = startUrl;
			}else{
				innerUrl = preImage.getNextInnerUrl();
			}
			String innerHtml = getHtml(innerUrl);
			String imageUrl = getImageUrl(innerHtml);
			String nextInnerUrl = getNextUrl(innerHtml);
			image = setImage(imageUrl, innerUrl, nextInnerUrl, galleryId, i);
			
			imageList.add(image);
			
		}
		return imageList;
	}
	
	/**
	 * 入口
	 * @param url
	 * @param begin
	 * @param end
	 * @param cookie
	 */
	public void exDownload(String url, int begin, int end, String cookie) throws Exception{
		sendMessage("初始化.......");
		if(cookie!=null){
			COOKIE = cookie;
		}
		
		Gallery gallery = getGallery(url);
		if(gallery==null){
			sendMessage("请检查地址的有效性！");
			throw new Exception("地址无效");
			
		}
		
		List<Image> imageList = getImageList(gallery, begin, end);
		String title = gallery.getTitle();
		sendMessage("开始下载:"+title);
		title = getWindowsTitle(title);
		String zipFileName = gallery.getSerialId();
		sendMessage("zipFileName,"+zipFileName);
		String dirName = ROOT_PATH + title;	
		File destDir = new File(dirName);
		if(!destDir.isDirectory()){
			destDir.mkdirs();
		}
		List<Image> failed = parallelDownload(imageList, dirName);
		
		
		sendMessage("下载结束！\n开始生成压缩包...");
		sendMessage(pack(zipFileName, dirName));
		overView(gallery, imageList, failed);
	}
	/**
	 * 下载总览
	 * @param gallery
	 * @param imageList
	 * @param failed
	 */
	private void overView(Gallery gallery, List<Image> imageList, List<Image> failed){
		imageList.sort(new Comparator<Image>() {

			@Override
			public int compare(Image o1, Image o2) {
				return o1.getSerialNum()-o2.getSerialNum();
			}
		});
		int length = imageList.size();
		int failedCount = failed.size();
		sendMessage("\n--------------------------\n"
				   + "下载总览\n"
				   + "-------------------------\n");
		sendMessage("图集："+gallery.getTitle());
		sendMessage("范围："+imageList.get(0).getSerialNum()+"-"+imageList.get(length-1).getSerialNum());
		sendMessage("长度："+length);
		sendMessage("成功数："+(length-failedCount));
		sendMessage("失败数："+failedCount);
		if(failedCount>0){
			sendMessage("失败列表及地址信息：");
			for(Image i:failed){
				sendMessage("第"+i.getSerialNum()+"张："+i.getInnerUrl());
			}
		}
	}
	
	
	/**
	 * 并行下载
	 * @param list 图片列表
	 * @param dirName 保存路径
	 * @param degree 并行度
	 * @return
	 */
	private List<Image> parallelDownload(List<Image> imageList, String dirName){
		final int degree = 4;
		
		List<Image> needDownload = new ArrayList<>();
		String fileName = null;
		for(Image image:imageList){
			fileName = dirName+"/"+image.getSerialNum()+"."+image.getSuffix();
			if(!(new File(fileName).exists())){
				needDownload.add(image);
			}
		}
		List<Image> list = needDownload;
		int length = list.size();
		sendMessage("下载总长度："+length);
		
		
		if(length<12){
			return download(list, dirName);
		}
		ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		int scale = list.size() / degree;
		int end = 0;
		int begin = 0;
		List<Future<List<Image>>> futureList = new ArrayList<>();
		for(int i=0;i<degree;i++){
			end = begin + scale;
			//end = Math.min(end, length);
			if(i == (degree-1)){
				end = length;
			}
			futureList.add(executor.submit(getDownloadThread(list, begin, end, dirName)));
			begin = end;
		}
		executor.shutdown();
		try {
			executor.awaitTermination(2, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<Image> failedList = new ArrayList<>();
		for(Future<List<Image>> future:futureList){
			try {
				failedList.addAll(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return failedList;
	}
	/**
	 * 获取并行下载的线程
	 * @param list
	 * @param begin
	 * @param end
	 * @param dirName
	 * @return
	 */
	private Callable<List<Image>> getDownloadThread(List<Image> list, int begin, int end, String dirName){
		List<Image> subList = list.subList(begin, end);//一个视图，会影响父子
		return new Callable<List<Image>>() {

			public List<Image> call() throws Exception {
				return download(subList, dirName);
			}
		};
	}
	/**
	 * 获取并行扫描的线程
	 * @param gallery
	 * @param begin
	 * @param num
	 * @param startUrl
	 * @return
	 */
	private Callable<List<Image>> getScanThread(Gallery gallery, List<Integer>serialNumList, int from, int to, String startUrl){
		List<Integer> subList = serialNumList.subList(from, to);
		return new Callable<List<Image>>() {

			@Override
			public List<Image> call() throws Exception {
				return scan(gallery, subList, startUrl);
			}
			
		};
	}
	/**
	 * 将list中的图片下载到dirName
	 * @param imageList
	 * @param dirName
	 * @return
	 */
	private List<Image> download(List<Image> imageList, String dirName){
		int reDownload = 3;
		//String failedMessage = null;
		List<Image> failedList = imageList; //将第一次视为第0次失败
		while( !(failedList.size() == 0 || reDownload < 0) ){
			failedList = downloadFailed(dirName, failedList);
//			if(failedList!=null && failedList.size()!=0){
//				if(reDownload==0){
//					failedMessage = "";
//				}else{
//					failedMessage = "\n开始第"+(4-reDownload)+"次下载失败列表：";
//				}
//				sendMessage("失败的集合："+failedList.toString() + failedMessage);
//			}
			reDownload --;
		}
		return failedList;
	}
	/**
	 * 将dirName打包成zipFileName
	 * @param zipFileName
	 * @param dirName
	 * @return
	 */
	private String pack(String zipFileName, String dirName){
		String failed = "生成失败！";
		String success = "生成成功！";
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
			
			return failed;
		} 
		return success;
	}
	/**
	 * 下载失败的列表
	 * @param dirName
	 * @param failedList
	 * @return
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
				
				String innerHtml = getHtml(image.getInnerUrl());
				imageUrl = getImageUrl(innerHtml);
				image.setUrl(imageUrl);
				image.setGmtModified(new Date());
				imageDAO.save(image);
				
			}
			CloseUtil.close(res);
			//开启failed loading
			if(downloadImage(imageUrl, fileName) || failedLoding(fileName, image)){
				sendMessage( "系列第"+image.getSerialNum()+"下载成功！");
			}else{
				sendMessage("系列第"+image.getSerialNum()+"下载失败！");
				failedList2.add(image);
			}
		}
		return failedList2;
	}
	/**
	 * 利用failed loading机制来加载有问题的图片
	 * @param fileName
	 * @param image
	 * @return
	 */
	private boolean failedLoding(String fileName, Image image){
		String oldInnerUrl = image.getInnerUrl();
		String oldInnerHtml = getHtml(oldInnerUrl);
		String newInnerUrl = oldInnerUrl + newInnerUrlSuffix(oldInnerHtml);
		String newInnerHtml = getHtml(newInnerUrl);
		String imageUrl = getImageUrl(newInnerHtml);
		return downloadImage(imageUrl, fileName);
	}
	
	/**
	 * 获取图片列表
	 * @param url
	 * @param begin
	 * @param end
	 * @return
	 */
	private List<Image> getImageList(Gallery gallery, int begin, int end){
		String serialId = gallery.getSerialId();
		int degree = 4;
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
		sendMessage(gallery.getTitle()+"--"+lenth);
		int realCount = end - begin + 1;
		List<Image> inImage = imageDAO.inImage(serialId, begin, end);
		int inCount  = inImage.size();
		if(inCount < realCount){
			
			List<Image> outImage = parallelScan(gallery, begin, end, inImage , degree);
			inImage.addAll(outImage);
		}
		return inImage;
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
	
//	private String getOuterUrl(String innerHtml){
//		String pattern = "<div class=\"sb\"><a href=\"([a-z0-9:/]*/)";
//		Pattern r = Pattern.compile(pattern);
//		Matcher m = r.matcher(innerHtml);
//		String outUrl = "";
//		if(m.find()){
//			outUrl = m.group(1);
//		}
//		return outUrl;
//	}
	
	
	
	/**
	 * 获得request对象
	 * @param url
	 * @return
	 * @
	 */
	public  HttpGet getRequest(String url) {
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
			return null;
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
	 * 设置gallery
	 * @param url
	 * @return
	 */
	private Gallery setGallery(String url){
		Gallery gallery = new Gallery();
		String outerHtml = getHtml(url);
		
		if(outerHtml == null || outerHtml.contains(STR404)|| outerHtml.contains(STR4042)){
			
			return null;
		}
		String title = getOriginalTitle(outerHtml);
		int length = getLenth(outerHtml);
		String serialId = getSerialId(url);
		/**
		 * set区
		 */
		gallery.setUrl(url);
		gallery.setTitle(title);
		gallery.setLenth(length);
		gallery.setSerialId(serialId);
		gallery.setGmtCreate(new Date());
		gallery.setGmtModified(new Date());
		this.galleryDAO.save(gallery);
		return gallery;
	}
	/**
	 * 利用failed loading机制，获取新的innerUrl后缀
	 * @param innerHtml
	 * @return
	 */
	private String newInnerUrlSuffix(String innerHtml){
		String innerUrlSuffix = null;
		Pattern p = Pattern.compile("'(\\d+-\\d+)'");
		Matcher m = p.matcher(innerHtml);
		if(m.find()){
			innerUrlSuffix = "?nl="+m.group(1);
		}
		return innerUrlSuffix;
	}
	/**
	 * 保存image
	 * @param imageUrl
	 * @param innerUrl
	 * @param galleryId
	 * @param serialNum
	 * @return
	 */
	
	private Image setImage(String imageUrl, String innerUrl, String nextInnerUrl, String galleryId, int serialNum){
		Image image = new Image();		
		String suffix = getSuffix(imageUrl);
		image.setInnerUrl(innerUrl);
		image.setNextInnerUrl(nextInnerUrl);
		image.setGalleryId(galleryId);
		image.setSerialNum(serialNum);
		image.setSuffix(suffix);
		image.setUrl(imageUrl);
		image.setGmtCreate(new Date());
		image.setGmtModified(new Date());
		this.imageDAO.save(image);
		return image;
	}
	/**
	 * 更新gallery信息
	 * @param gallery
	 */
	private void updateGallery(Gallery gallery){
		String url = gallery.getUrl();
		String html = getHtml(url);
		String title = getOriginalTitle(html);
		Integer length = getLenth(html);
		if(length.equals(gallery.getLenth()) && title.equals(gallery.getTitle())){
			return;
		}
		gallery.setTitle(title);
		gallery.setLenth(length);
		gallery.setGmtModified(new Date());
		galleryDAO.save(gallery);
	}
	/**
	 * 根据contentType获取charset
	 * @param contentType
	 * @return
	 */
	private String getCharset(String contentType){
		String charset = null;
		Pattern p = Pattern.compile("charset=([a-zA-Z0-9\\-]+)");
		Matcher m = p.matcher(contentType);
		if(m.find()){
			charset = m.group(1);
		}
		return charset;
	}
	/**
	 * 根据输入的outer URL获取gallery
	 * @param url
	 */
	private Gallery getGallery(String url){
		String serialId = getSerialId(url);
		Optional<Gallery> option = galleryDAO.findBySerialId(serialId);
		Gallery gallery = option.orElse(null);
		if(gallery == null){
			gallery = setGallery(url);
		}else{
			updateGallery(gallery);
		}
		return gallery;
	}
}
