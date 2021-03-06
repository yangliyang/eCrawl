package yly.crawl.springboot.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DownloadController {
	private static final String PATH = "zips/";
	@RequestMapping("/download")
	public void download(HttpServletResponse res, String title){
		String trueFileName = title + ".zip";
		File f = new File(PATH+trueFileName);
		if(!f.exists()){
			try {
				res.sendRedirect("/index");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    res.setContentType("application/octet-stream");
	    res.setHeader("Content-Disposition", "attachment;filename=" + trueFileName);
	    res.setContentLengthLong(f.length());
	    byte[] buff = new byte[2048];
	    BufferedInputStream bis = null;
	    OutputStream os = null;
	    try {
	      os = res.getOutputStream();
	      bis = new BufferedInputStream(new FileInputStream(f));
	      int i = bis.read(buff);
	      while (i != -1) {
	        os.write(buff, 0, buff.length);
	        os.flush();
	        i = bis.read(buff);
	      }
	    } catch (IOException e) {
	    	System.out.println("打开了下载工具！");
	    } finally {
	      if (bis != null) {
	        try {
	          bis.close();
	        } catch (IOException e) {
	          e.printStackTrace();
	        }
	      }
	    }
	    
	  }
	
	
}

