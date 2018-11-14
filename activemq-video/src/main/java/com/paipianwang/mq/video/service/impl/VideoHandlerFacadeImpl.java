package com.paipianwang.mq.video.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paipianwang.mq.video.biz.VideoHandlerBiz;
import com.paipianwang.mq.video.domain.GlobalConstants;
import com.paipianwang.mq.video.fastdfs.utils.FileUtil;
import com.paipianwang.mq.video.service.VideoHandlerFacade;
import com.paipianwang.mq.video.utils.ConvertFileUtils;
import com.paipianwang.pat.common.web.file.FastDFSClient;
import com.paipianwang.pat.facade.product.entity.PmsProduct;
/*import com.paipianwang.service.fastdfs.biz.FastDFSBiz;
import com.paipianwang.service.fastdfs.utils.FileUtil;
import com.paipianwang.service.video.biz.VideoHandlerBiz;
import com.paipianwang.service.video.domian.GlobalConstants;
import com.paipianwang.service.video.utils.ConvertFileUtils;*/

@Service("videoHandlerFacade")
public class VideoHandlerFacadeImpl implements VideoHandlerFacade {

	@Autowired
	private VideoHandlerBiz videoHandlerBiz = null;

/*	@Autowired
	private FastDFSBiz fastDFSBiz = null;*/

	/**
	 * 该方法用来将视频文件转换成H264编码，mp4格式 并且会压缩视频比特率，使视频文件播放更流畅
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @return boolean 是否成功
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public boolean converFile2Normal(final String inputFile, final String outputFile)
			throws IOException, InterruptedException, ExecutionException {

		return videoHandlerBiz.converFile2Normal(inputFile, outputFile);
	}

	/**
	 * 该方法用来提取视频第2帧的图片
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public boolean interceptionFirstFrame(final String inputFile, final String outputFile)
			throws InterruptedException, ExecutionException {

		return videoHandlerBiz.interceptionFirstFrame(inputFile, outputFile);
	}

	/**
	 * 该方法用来将视频文件转换成H264编码，mp4格式 并且会压缩视频比特率，使视频文件播放更流畅
	 * 
	 * @param fileId
	 *            FastDFS 上文件的ID
	 * @return 不为空的话是上传在FastDFS上的文件 ID 为null 则转换失败
	 */
	public String converFile2Normal(final String fileId) {
		// FastDFS 获取文件
		if (StringUtils.isNotBlank(fileId)) {
			// 下载文件
			final InputStream ins = FastDFSClient.downloadFile(fileId);
			try {
				if (ins != null) {

					// 将文件流 转化为 文件并临时存储成文件
					final String prepareFilePath = GlobalConstants.INPUT_FILE_DIR + File.separator
							+ FileUtil.getFileName(fileId);
					File prepareFile = new File(prepareFilePath);
					ConvertFileUtils.inputstreamtofile(ins, prepareFile);

					// 转换完成的file存储为文件
					final String targetFilePath = GlobalConstants.CONVERT_FILE_DIR + File.separator
							+ FileUtil.getFileName(fileId);

					videoHandlerBiz.converFile2Normal(prepareFilePath, targetFilePath);
					// 获取文件，然后上传
					final File targetFile = new File(targetFilePath);
					final String path = FastDFSClient.uploadFile(targetFile, targetFile.getName());

					// 上传完成之后删除原视频
					prepareFile.delete();
					targetFile.delete();

					return path;
				}
				return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 检查转换完的视频 分辨率、比特率、视频编码、音频编码、每秒帧数
	 * @return csv文件路径
	 */
	@Override
	public boolean checkConvertedFile(final List<PmsProduct> list) {
		
		return videoHandlerBiz.checkConvertedFile(list);
	}

}
