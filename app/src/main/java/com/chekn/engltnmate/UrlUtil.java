package com.chekn.engltnmate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UrlUtil {

    public static String decode(String url, String chareset) throws UnsupportedEncodingException {
        //String keyWord = URLDecoder.decode("%C4%E3%BA%C3", chareset);
		String keyWord = URLDecoder.decode(url, chareset);
		System.out.println(keyWord);  //输出你好  
		return keyWord;
    }
	
    public static String encode(String url, String chareset) throws UnsupportedEncodingException {
		//String urlString = URLEncoder.encode("你好", "GBK");  //输出%C4%E3%BA%C3
		String urlString = URLEncoder.encode(url, chareset);
		System.out.println(urlString);  
		return urlString;
	}
}