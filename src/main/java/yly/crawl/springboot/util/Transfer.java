package yly.crawl.springboot.util;

public class Transfer {
	
	public static String trans(String src){
		String dest = null;
		byte[] srcBytes = src.getBytes();
		int length = srcBytes.length;
		byte[] destBytes = new byte[length];
		for(int i=0;i<length;i++){
			destBytes[i] = (byte) (srcBytes[i]^1);
		}
		dest = new String(destBytes);
		
		return dest;
	}
}
