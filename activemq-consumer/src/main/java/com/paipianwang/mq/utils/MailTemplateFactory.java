package com.paipianwang.mq.utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
/**
 * 提供mail模板
 * 2016-10-12 15:27:38
 */
public class MailTemplateFactory {

	public static String getRegistMailTpl(){
		String str = " "+
		"<!DOCTYPE html>"+
		"<html lang='en'>"+
		"<head>"+
		"    <meta charset='utf-8'>"+
		"    <title>Swiper demo</title>"+
		"    <style>"+
		"    body {"+
		"        background: #eee;"+
		"        font-family: Helvetica Neue, Helvetica, Arial, sans-serif;"+
		"        font-size: 14px;"+
		"        color:#000;"+
		"        margin: 0;"+
		"        padding: 0;"+
		"    }"+
		"    img{"+
		"    	height: 200px;"+
		"    	width: 200px;"+
		"    }"+
		"    </style>"+
		"</head>"+
		"<body>"+
		"   <a href='http://www.baidu.com'><img src='http://apaipian.com/product/img/product659-20160818142295282.jpg'></a>"+
		"   <a href='http://www.baidu.com'><img src='http://apaipian.com/product/img/product560-201608181355288132.jpg'></a>"+
		"   <a href='http://www.baidu.com'><img src='http://apaipian.com/product/img/product1785-201609081853488952.JPG'></a>"+
		"</body>"+
		"</html>";
		return str;
	}
	
	/*public static String addHtml(String tmp) {
		String str = " "+
				"<html>"+
				"<head>"+
				"</head>"+
				"<body>"+
				tmp+
				"</body>"+
				"</html>";
			return str;
	}*/

	public static String addHtml(String tmp) {
		String str = " "+
				"<html>																																																																																																									"+					
				"<head>                                                                                                                                                                                                                                                                                                                                                                                                                                 "+
				"    <meta charset='utf-8'>                                                                                                                                                                                                                                                                                                                                                                                                             "+
				"    <meta http-equiv='X-UA-Compatible' content='IE=9,chrome=1'>                                                                                                                                                                                                                                                                                                                                                                        "+
				"    <meta name='viewport' content='width=device-width, initial-scale=1.0'>                                                                                                                                                                                                                                                                                                                                                             "+
				"    <meta name='keywords' content='拍片网'>                                                                                                                                                                                                                                                                                                                                                                                            "+
				"    <meta name='description' content=''>                                                                                                                                                                                                                                                                                                                                                                                               "+
				"    <meta name='baidu-site-verification' content='dMz6jZpIwd' />                                                                                                                                                                                                                                                                                                                                                                       "+
				"    <title>拍片网－pMail</title>                                                                                                                                                                                                                                                                                                                                                                                                       "+
				"</head>                                                                                                                                                                                                                                                                                                                                                                                                                                "+
				"<body style='font-family: 'Microsoft YaHei', '微软雅黑', 'Helvetica Neue', Helvetica, Arial, sans-serif !important; font-weight:500; background:#f0f2f5;'>																																																																					"+										
				"    <div class='page' style='position:relative;width: 860px;margin: 0 auto;'>                                                                                                                                                                                                                                                                                                                                                              "+
				"        <div class='contentArea' style='-moz-box-shadow: 0px 0px 10px #d1d1d1;-webkit-box-shadow: 0px 0px 10px #d1d1d1;-o-box-shadow: 0px 0px 10px #d1d1d1;box-shadow: 0px 0px 10px #d1d1d1;position: relative;z-index:5;width:752px;margin: 0 auto;'>                                                                                                                                                                                     "+
				"            <div class='contentTop' style='height: 100px;width: 100%;background:#fe5453;font-size: 0px;'>                                                                                                                                                                                                                                                                                                                                  "+
				"                <a href='http://www.apaipian.com/'><img class='logo' style='display: inline-block;color: white;vertical-align: top; margin-top: 33px;margin-left: 40px;padding-right:16px;width: 110px;height: 36px;border-right: 1px solid white;' src='http://resource.apaipian.com/resource/group1/M00/00/37/Cgpw7FhGOBeARnmZAAAJAO6IpKs757.png'></a>                                                                                   "+
				"                <div class='title' style='display: inline-block;color: white;vertical-align:top;font-size: 22px;line-height: 103px;margin-left: 16px;'>专业商业视频服务平台</div>                                                                                                                                                                                                                                                         "+
				"                <a href='http://www.apaipian.com/' style='text-decoration:none;display: inline-block;color: white;vertical-align: top;font-size:14px;line-height: 103px;position: relative;margin-left:230px;'>进入官网>></a>                                                                                                                                                                                                                     "+
				"            </div>                                                                                                                                                                                                                                                                                                                                                                                                                         "+
				"            <div class='contentTag' style='background:#f0f2f5;'>                                                                                                                                                                                                                                                                                                                                                                           "+
				"                <ul style='height: 50px;font-size: 0; margin: auto;padding: 0px;'>                                                                                                                                                                                                                                                                                                                                                         "+
				"                    <li style='display: inline-block; margin: 0;padding: 0;line-height: 48px;list-style-type:none;margin-left: 160px; margin-right: 60px;'><a href='http://www.apaipian.com/about-us.html' style='text-decoration:none;'><span style='font-size: 14px;color: #666;border-bottom: 1px solid #999;'  onMouseOver='this.style.color='#fe5453'' onMouseOut='this.style.color='#666''>了解我们</span></a></li>                  "+
				"                    <li style='display: inline-block; margin: 0;padding: 0;line-height: 48px;list-style-type:none;margin-right: 60px;'><a href='http://www.apaipian.com/order-flow.html' style='text-decoration:none;'>                                                                                                                                                                                                               "+
				"                    <span style='font-size: 14px;color: #666;border-bottom: 1px solid #999;'  onMouseOver='this.style.color='#fe5453'' onMouseOut='this.style.color='#666''>服务流程</span></a></li>                                                                                                                                                                                                                                       "+
				"                    <li style='display: inline-block; margin: 0;padding: 0;line-height: 48px;list-style-type:none;'><a href='http://www.apaipian.com/company-service.html' style='text-decoration:none;'><span style='font-size: 14px;color: #666;border-bottom: 1px solid #999;'  onMouseOver='this.style.color='#fe5453'' onMouseOut='this.style.color='#666''>服务协议</span></a></li>                                                 "+
				"                    <li style='display: inline-block; margin: 0;padding: 0;line-height: 48px;list-style-type:none; margin-left: 60px;'><a href='http://www.apaipian.com/member.html' style='text-decoration:none;' ><span style='font-size: 14px;color: #666;border-bottom: 1px solid #999;' onMouseOver='this.style.color='#fe5453''; onMouseOut='this.style.color='#666''>在线联系我们</span></a></li>                                  "+
				"                </ul>                                                                                                                                                                                                                                                                                                                                                                                                                      "+
				"            </div>                                                                                                                                                                                                                                                                                                                                                                                                                         "+
				"            <div class='content' style='padding: 40px 20px 50px 20px; height: auto;background: white;width:712px'>                                                                                                                                                                                                                                                                                                                        "+
				tmp+
				"            </div>                                                                                                                                                                                                                                                                                                                                                                                                                         "+
				"        </div>                                                                                                                                                                                                                                                                                                                                                                                                                             "+
				"         <a href='http://www.apaipian.com' style='cursor: pointer'>                                                                                                                                                                                                                                                                                                                                                                        "+
				"             <img class='foot' style='height:450px;width:861px;z-index:5;position: relative; bottom: 0px;left:0px;'  src='http://resource.apaipian.com/resource/group1/M00/00/38/Cgpw7FhJUG6AE7TUAAQro7B50-g717.png'/>                                                                                                                                                                                                                                   "+
				"         </a>                                                                                                                                                                                                                                                                                                                                                                                                                              "+
				"    </div>                                                                                                                                                                                                                                                                                                                                                                                                                                 "+
				"</body>     "+                                                                                                                                                                                                                                                                                                                                                                                                                              
				"</html> ";
				return str;
	}
	/**
	 * 1.转码
	 * 2.添加首尾html
	 */
	public static <T> String decorate(String content) {
			try {
				//1.转码
				content = new String(Base64.getDecoder().decode(content),"utf-8");
				//2.添加首尾模板
				content = addHtml(content);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		return content;
	}
	
	
	
	public static  String decorate(String[] str, String content) {
		for(int i=1;i<=str.length;i++){
			content = content.replaceAll("\\{"+ i +"\\}", str[i-1]==null?"":str[i-1]);
		}
		return content;
	}
	/*
	public static <T> String decorate(T t, String content) {
		try {
			//1.替换信息
			Field[] fs = t.getClass().getDeclaredFields();
			for(Field f : fs){
				f.setAccessible(true); //设置属性是可以访问的
				String name = f.getName();//获取属性
				Object val = f.get(t);//获取属性值
				content = content.replaceAll("\\$\\{"+name+"\\}", null == val?"":String.valueOf(val));
			};
		} catch (SecurityException | IllegalArgumentException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		return content;
	}*/

	public static String addImgHost(String content) {
		//return content.replaceAll("@.@", "http://resource.apaipian.com/resource/");
		return content.replaceAll("@.@", "http://123.59.86.252:8888/");
	}
	
	
}