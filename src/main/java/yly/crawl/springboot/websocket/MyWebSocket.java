package yly.crawl.springboot.websocket;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import yly.crawl.springboot.dao.GalleryDAO;
import yly.crawl.springboot.dao.ImageDAO;
import yly.crawl.springboot.util.Ecrawler;
import yly.crawl.springboot.util.Transfer;

/**
 * 默认的实现，AutoWired注入失败，猜测是因为@ServerEndpoint管理了，不归spring了
 * 解决方法1：定义一个Config,将其给spring管理
 * 缺点：不是正常的管理。。。
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
    private String cookie;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();
    private static final String HOST="dyidou`h/nsf";
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
    	long start = System.currentTimeMillis();
    	String cookie =null;
    	String[] info = null;
    	if(message.contains("cookie")){
    		//System.out.println(message);
    		String[] cookieArry = message.split(",");
    		//System.out.println(cookieArry.length);
    		if(cookieArry.length ==2 ){
    			cookie = cookieArry[1];
    		}
    		this.cookie = cookie;
    	}else{
    		info = message.split(",");
    		String url = info[0];
    		if(!URLCheck(url)){
    		   sendMessage("URL格式不正确！",session);
 	  		   sendMessage("ERROR",session);
 	  		   return;
    		}
	  		 int begin = 0;
	  		 int end = 0;
	  		 try{
	  	  	   begin = Integer.parseInt(info[1]);
	  	  	   end = Integer.parseInt(info[2]);
	  	     }catch(NumberFormatException e){
	  	  	   
	  	  	   sendMessage("不是整数或者太大！",session);
	  	  	   sendMessage("ERROR",session);
	  	  	   return;
	  	     }
	  		 Ecrawler crawler = new Ecrawler(galleryDAO, imageDAO, session);
	  		 try{
	  			 crawler.exDownload(url, begin, end, this.cookie);
	  			 
	  		}catch(Throwable e){
	  			e.printStackTrace();
	  			sendMessage("发生异常错误！", session);
	  			sendMessage("ERROR",session);
	  			return;
	  		}
	  		sendMessage("complete", session);
	  		long finish = System.currentTimeMillis();
	  		int seconds = (int) ((finish-start)/1000);
	  		int minutes = seconds / 60;
	  		int rest = seconds % 60;
	  		sendMessage("总耗时："+minutes+"分"+rest+"秒",session);
	  		
    	}
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        System.out.println(error.getMessage());
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
    	if(session !=null && session.isOpen()){
	        try {
				session.getBasicRemote().sendText(message+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else{
    		System.out.println(message);
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
    private boolean URLCheck(String url){
    	try {
			new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
    	String s = url;
    	String host = Transfer.trans(HOST);		
    	Pattern p = Pattern.compile("^https://"+host+"/g/[0-9]+/[a-z0-9]+/?");
    	Matcher m = p.matcher(s);
    	if(m.matches()){
    		return true;
    	}
    	return false;
    }
    
	
}
