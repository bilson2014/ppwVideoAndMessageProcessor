package com.paipianwang.mq.video.domain;

import java.io.Serializable;

/**
 * 全局常量
 * 用来加载服务中用到的可配置常量
 * @author Jack
 *
 */
public class GlobalConstants implements Serializable {

	private static final long serialVersionUID = -3744474459765644412L;

	public static String INPUT_FILE_DIR; // 原视频存储路径
	
	public static String CONVERT_FILE_DIR; // 转换后视频存储路径

	public static String CHECK_INPUT_DIR; // 视频文件检测路径
	
	public static String REPORT_DIR; // 报告产生路径
	
	private String inputFileDir = "/data/temp/upload/";
	
	private String convertFileDir = "/data/temp/convert/";
	
	private String checkInputDir = "/data/temp/check/";
	
	private String reportDir = "/data";

	public GlobalConstants(String inputFileDir, String convertFileDir, String checkInputDir, String reportDir) {
		INPUT_FILE_DIR = inputFileDir;
		CONVERT_FILE_DIR = convertFileDir;
		CHECK_INPUT_DIR = checkInputDir;
		REPORT_DIR = reportDir;
	}

	public String getInputFileDir() {
		return inputFileDir;
	}

	public void setInputFileDir(String inputFileDir) {
		this.inputFileDir = inputFileDir;
	}

	public String getConvertFileDir() {
		return convertFileDir;
	}

	public void setConvertFileDir(String convertFileDir) {
		this.convertFileDir = convertFileDir;
	}

	public String getCheckInputDir() {
		return checkInputDir;
	}

	public void setCheckInputDir(String checkInputDir) {
		this.checkInputDir = checkInputDir;
	}

	public String getReportDir() {
		return reportDir;
	}

	public void setReportDir(String reportDir) {
		this.reportDir = reportDir;
	}
	
}
