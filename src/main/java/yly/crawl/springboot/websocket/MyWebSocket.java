package yly.crawl.springboot.websocket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import yly.crawl.springboot.dao.GalleryDAO;
import yly.crawl.springboot.dao.ImageDAO;
import yly.crawl.springboot.pojo.Gallery;
import yly.crawl.springboot.pojo.Image;
import yly.crawl.springboot.util.CloseUtil;

/**
 * 默认的实现，AutoWired注入失败，猜测是因为@ServerEndpoint管理了，不归spring了
 * 解决方法1：定义一个Config,将其给spring管理
 * 缺点：不是正常的管理。。。
 * @author Administrator
 *
 */
@Component
@ServerEndpoint(value = "/websocket",configurator=MyEndpointConfigure.class)

public class MyWebSocket {
	@Autowired 
	private  GalleryDAO galleryDAO;
	@Autowired 
	private  ImageDAO imageDAO;
	
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @throws IOException */
    @OnMessage
    public void onMessage(String message, Session session)  {
    	String[] info = message.split(",");
    	String url = info[0];
		  try {
		   new URL(url);
		  } catch (MalformedURLException e) {
		   sendMessage("URL格式不正确！",session);
		   e.printStackTrace();
		   return;
		  }
		 int begin = 0;
		 int end = 0;
		 try{
	  	   begin = Integer.parseInt(info[1]);
	  	   end = Integer.parseInt(info[2]);
	     }catch(NumberFormatException e){
	  	   e.printStackTrace();
	  	   sendMessage("不是数字或者太大！",session);
	  	   return;
	     }
		 exDownloadWithDatabase(url, begin, end);
//       String[] info = message.split(",");
//       String url = info[0];
//       try {
//    	   new URL(url);
//       } catch (MalformedURLException e) {
//    	   sendMessage("URL格式不正确！",session);
//    	   e.printStackTrace();
//    	   return;
//       }
//       int num = 0;
//       try{
//    	   num = Integer.parseInt(info[1]);
//       }catch(NumberFormatException e){
//    	   e.printStackTrace();
//    	   sendMessage("数字！",session);
//    	   return;
//       }
//       if(url.contains("-")){
//    	   exDownloadInner(url, num);
//       }else{
//    	   exDownloadOuter(url, num);
//       }
    	
        //群发消息
//        for (MyWebSocket item : webSocketSet) {
//            try {
//                item.sendMessage(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    public void sendMessage(String message)  {
        try {
			this.session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //this.session.getAsyncRemote().sendText(message);
    }
    public void sendMessage(String message,Session session)  {
        try {
			session.getBasicRemote().sendText(message+"\n");
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

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }
    
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
	private static final String COOKIE = "ipb_member_id=2798962; ipb_pass_hash=24576fff1f871b0feff9c4fbfeba4d92; yay=louder;  igneous=a5854c5a1; lv=1527562527-1527562527; s=e3a51d202; sk=qkpbkq4ifredzvkfr9a5ubuizu6";
	private static final String CODING = "gzip, deflate";
	private static final String LANGUAGE = "zh-Hans-CN,zh-Hans,q=0.5";
	private static final String CONNECTION = "Keep-Alive";
	private static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393";
	private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
	private static final int DEFAULT_TIMEOUT = 30000;//30s的超时检测
	private static final String ROOT_PATH = "F:\\images\\";
	/**
	 * 获取网页内容的输入流，最后没关response..,有时间改成返回response
	 * In order to ensure correct deallocation of system resources
     * the user MUST call CloseableHttpResponse#close() from a finally clause.
	 * @param url
	 * @return
	 * @
	 */
	public  CloseableHttpResponse getResponse(String url) {
		int sleep = (int)(Math.random()*2000+1000);
		try {
			Thread.currentThread().sleep(sleep);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		int refreshTime = 3; //3次重连机会
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet  = getRequest(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(DEFAULT_TIMEOUT).setConnectTimeout(DEFAULT_TIMEOUT).build();
		httpGet.setConfig(requestConfig);
		//InputStream in = null;
		CloseableHttpResponse response = null;
		while(refreshTime>0 && response==null){
			try {
				response= client.execute(httpGet);
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode==403){
					System.out.println("严重错误！服务器禁止访问！");
					return null;
				}
				if(statusCode==404){
					System.out.println("不存在的错误的地址！");
					return null;
				}
				//HttpEntity entity = response.getEntity();
				//in = entity.getContent();
				//EntityUtils.consume(entity);	
				break;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//超时等情况
				System.out.println("网络连接超时，开始尝试第 "+ (4 - refreshTime)+"次重连");
				//e.printStackTrace();
				CloseUtil.close(response);
			}
//			finally{
//				if(response!=null){
//					try {
//						response.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
			refreshTime--;
		}
		return response;
		
	}
	/**
	 * 默认utf8了
	 * @param url
	 * @return
	 * @
	 */
	public  String getHtml(String url) {
		CloseableHttpResponse response = getResponse(url);
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
				title = title.replaceAll(s, "");
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
			e.printStackTrace();
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
		String pattern = "a href=\"(https://exhentai.org/s/[\\d\\D]*?-"+focus+")";
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
	public  void exDownloadOuter(String url, int begin, int end) {
		//String outerHtml = getHtml(url);
		//String firstInnerUrl = getFirstInner(outerHtml);
		String startUrl = getStartUrl(url, begin);
		int num = end - begin + 1;
		exDownloadInner(startUrl, num);//进入内部
	}
	/**
	 * 从ImageUrl下面下载一定数量
	 * @param url
	 * @param num
	 * @ 
	 */
	public  void exDownloadInner(String url, int num) {
		Gallery gallery = new Gallery();
		Image image = new Image();
		String startInnerHtml = getHtml(url);
		String title = getOriginalTitle(startInnerHtml);
		System.out.println("开始下载："+title+"\n保存路径"+ROOT_PATH);
		String dirName = ROOT_PATH + getWindowsTitle(title);
		
		File destDir = new File(dirName);
		if(!destDir.isDirectory()){
			destDir.mkdirs();
		}
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
	
		this.galleryDAO.save(gallery);
		
		
		
		Map<Integer, String> imageUrlMap = new LinkedHashMap<>();
		Map<Integer, String> innerUrlMap = new LinkedHashMap<>();
		String currentUrl = url;
		String currentHtml = null;
		String currentImageUrl = null;
		String nextUrl = null;
		System.out.println("开始遍历要下载的图片的信息...");
		//num++;//名字从一开始
		for(int i=currentPage;i<num+currentPage;i++){
			currentHtml = getHtml(currentUrl);
			currentImageUrl = getImageUrl(currentHtml);
			innerUrlMap.put(i, currentUrl);
			imageUrlMap.put(i, currentImageUrl);
			nextUrl = getNextUrl(currentHtml);
			if(nextUrl.equals("")){
				break;
			}else{
				currentUrl = nextUrl;
			}
		}
		
		for(Entry<Integer, String> entry:imageUrlMap.entrySet()){
			int key = entry.getKey();
			String imageUrl = entry.getValue();
			String suffix = getSuffix(imageUrl);
			String fileName = dirName + "\\"+ key + "." + suffix;
			/**
			 * set区
			 */
			image.setInnerUrl(innerUrlMap.get(key));
			image.setGalleryId(serialId);
			image.setSerialNum(key);
			image.setSuffix(suffix);
			image.setUrl(imageUrl);
			image.setGmtCreate(new Date());
			this.imageDAO.save(image);
			
			
			if(downloadImage(imageUrl, fileName)){
				System.out.println("系列第"+key+"张下载成功！");
			}else{
				System.out.println("系列第"+key+"张下载失败！");
			}
		}
		System.out.println("下载结束！");
	}
	
	public void exDownloadWithDatabase(String url, int begin, int end){
		String serialId = getSerialId(url);
		Gallery gallery = galleryDAO.findOne(serialId);
		if(gallery == null){
			exDownloadOuter(url, begin, end);
			return;
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
			exDownloadOuter(url, begin, end);
			return;
		}
		/**
		 * 开始从数据库信息中直接下载
		 */
		String title = gallery.getTitle();
		System.out.println("开始下载："+title+"\n保存路径"+ROOT_PATH);
		String dirName = ROOT_PATH + getWindowsTitle(title);
		
		File destDir = new File(dirName);
		if(!destDir.isDirectory()){
			destDir.mkdirs();
		}
		String fileName = "";
		String imageUrl = "";
		for(Image image:inImage){
			fileName = dirName+"\\"+image.getSerialNum()+"."+image.getSuffix();
			imageUrl = image.getUrl();
			if(downloadImage(imageUrl, fileName)){
				System.out.println("系列第"+image.getSerialNum()+"下载成功！");
			}else{
				System.out.println("系列第"+image.getSerialNum()+"下载失败！");
			}
		}
		System.out.println("下载结束！");
		
		
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
			host = new URL(url).getHost();
		} catch (MalformedURLException e) {
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
	
}
