package com.paipianwang.mq.consumer.resource.model;

import com.paipianwang.mq.consumer.domain.BaseObject;

/**
 * 文件处理类
 * @author Jack
 *
 */
public class FileParam extends BaseObject {

	private static final long serialVersionUID = 8174680028566156784L;

	private long productId = 0l; // 产品唯一编号
	
	private String convertFileId = null; // 待转换视频文件ID
	
	private String targetFileId = null; // 待转换视频文件ID

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public String getConvertFileId() {
		return convertFileId;
	}

	public void setConvertFileId(String convertFileId) {
		this.convertFileId = convertFileId;
	}

	public String getTargetFileId() {
		return targetFileId;
	}

	public void setTargetFileId(String targetFileId) {
		this.targetFileId = targetFileId;
	}
	
}
