package yly.crawl.springboot.websocket;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArraySet;

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
		 Ecrawler crawler = new Ecrawler(galleryDAO, imageDAO, session);
		 try{
			 crawler.exDownloadWithDatabase(url, begin, end);
		}catch(Throwable e){
			e.printStackTrace();
			sendMessage("发生异常错误！", session);
			
			return;
		}
		sendMessage("complete", session);
		
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
    
 
	
}
