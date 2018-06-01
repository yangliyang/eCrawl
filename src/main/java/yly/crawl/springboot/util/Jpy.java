package yly.crawl.springboot.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Jpy {
	public static void main(String[] args) {
		String[] args1 = {"python", "C:\\Users\\yjx\\Desktop\\2.py"};
		String a= "a";
		System.out.println(execPy(args1, a));
		
	}
	public static InputStream execPy(String[] args, String str){
		InputStream in = null;
		try {
			Process pr = Runtime.getRuntime().exec(args);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
			bw.write(str);
			bw.close();
			in = pr.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return in;
	}
	
}
