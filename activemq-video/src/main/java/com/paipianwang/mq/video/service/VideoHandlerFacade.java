package com.paipianwang.mq.video.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.paipianwang.pat.facade.product.entity.PmsProduct;

/**
 * 该服务提供视频的转码、压缩服务
 * @author Jack
 *
 */
public interface VideoHandlerFacade {

	/**
	 * 该方法用来将视频文件转换成H264编码，mp4格式
	 * 并且会压缩视频比特率，使视频文件播放更流畅
	 * @param fileId
	 * 			FastDFS 上文件的ID
	 * @return 上传在FastDFS上的文件 ID
	 */
	public String converFile2Normal(final String fileId);
	
	/**
	 * 该方法用来将视频文件转换成H264编码，mp4格式
	 * 并且会压缩视频比特率，使视频文件播放更流畅
	 * @param inputFile
	 * @param outputFile
	 * @return boolean 是否成功
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public boolean converFile2Normal(final String inputFile, final String outputFile)
			throws IOException, InterruptedException, ExecutionException;
	
	/**
	 * 该方法用来提取视频第2帧的图片
	 * @param inputFile
	 * @param outputFile
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public boolean interceptionFirstFrame(final String inputFile, final String outputFile)
			throws InterruptedException, ExecutionException;
	
	/**
	 * 检查转换完的视频 分辨率、比特率、视频编码、音频编码、每秒帧数
	 * @return csv文件路径
	 */
	public boolean checkConvertedFile(final List<PmsProduct> list);
}
