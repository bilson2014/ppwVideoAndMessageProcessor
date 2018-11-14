package com.paipianwang.mq.video.fastdfs.utils;

import org.apache.commons.lang3.StringUtils;

public class FileUtil {

	/**
	 * 获取文件后缀名（不带点）.
	 * 
	 * @return 如："jpg" or "".
	 */
	public static String getFileExt(final String fileName) {
		if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
			return "";
		} else {
			return fileName.substring(fileName.lastIndexOf(".") + 1); // 不带最后的点
		}
	}
	
	/**
	 * 根据FastDFS的fileId 截取文件名称
	 * @param fileId FastDFS返回的fileId   eg:M00/00/00/wKgxgk5HbLvfP86RAAAAChd9X1Y736.jpg
	 * @return 文件名称 eg:wKgxgk5HbLvfP86RAAAAChd9X1Y736.jpg
	 */
	public static String getFileName(final String fileId) {
		if(StringUtils.isBlank(fileId) || !fileId.contains("/")) {
			return null;
		} else {
			return fileId.substring(fileId.lastIndexOf("/") + 1);
		}
	}
}
