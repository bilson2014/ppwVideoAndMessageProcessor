package com.paipianwang.mq.video.biz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paipianwang.mq.video.domain.GlobalConstants;
import com.paipianwang.mq.video.fastdfs.utils.FileUtil;
import com.paipianwang.mq.video.utils.CSVUtils;
import com.paipianwang.mq.video.utils.ConvertFileUtils;
import com.paipianwang.mq.video.utils.PPWFFmpegExecutor;
import com.paipianwang.mq.video.utils.PPWFFmpegExecutor.Video;
import com.paipianwang.pat.common.web.file.FastDFSClient;
import com.paipianwang.pat.facade.product.entity.PmsProduct;
/*import com.paipianwang.service.fastdfs.biz.FastDFSBiz;
import com.paipianwang.service.fastdfs.utils.FileUtil;
import com.paipianwang.service.video.domian.GlobalConstants;
import com.paipianwang.service.video.utils.CSVUtils;
import com.paipianwang.service.video.utils.ConvertFileUtils;
import com.paipianwang.service.video.utils.PPWFFmpegExecutor;
import com.paipianwang.service.video.utils.PPWFFmpegExecutor.Video;*/

import net.bramp.ffmpeg.job.FFmpegJob.State;

/**
 * 视频处理业务类
 * 
 * @author Jack
 *
 */
@Service("videoHandlerBiz")
public class VideoHandlerBiz {

	 private final Logger logger = LoggerFactory.getLogger(VideoHandlerBiz.class);
	
	@Autowired
	private PPWFFmpegExecutor ppwFFmpegExecutor = null;

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
		final State state = ppwFFmpegExecutor.adjustBitRate(inputFile, outputFile);
		if (State.FINISHED.equals(state)) {
			return true;
		} else {
			File out = new File(outputFile);
			out.delete();
		}
		return false;
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
		for (int i = 0; i < 3; i++) {
			final State state = ppwFFmpegExecutor.interceptionFirstFrame(inputFile, outputFile);
			if (State.FINISHED.equals(state)) {
				return true;
			} else {
				// 尝试转换3次
				File out = new File(outputFile);
				out.delete();
			}
		}
		return false;
	}

	/**
	 * 检查转换完的视频 分辨率、比特率、视频编码、音频编码、每秒帧数
	 * 
	 * @return csv文件路径
	 */
	public boolean checkConvertedFile(final List<PmsProduct> list) {
		// FastDFS 获取文件
		if (!list.isEmpty()) {
			List<String> analysisList = new ArrayList<String>();
			File csv = new File(GlobalConstants.REPORT_DIR + File.separator + "videoAnalysis.csv");
			for (final PmsProduct product : list) {
				final String productName = product.getProductName();
				final String fileId = product.getVideoUrl();
				System.out.println(productName + fileId);
				if (StringUtils.isNotEmpty(fileId) && StringUtils.isNotEmpty(productName)) {
					// 下载文件
					final InputStream ins = FastDFSClient.downloadFile(fileId);
					if (ins != null) {
						// 将文件流 转化为 文件并临时存储成文件
						final String prepareFilePath = GlobalConstants.INPUT_FILE_DIR + File.separator
								+ FileUtil.getFileName(fileId);
						File prepareFile = new File(prepareFilePath);
						ConvertFileUtils.inputstreamtofile(ins, prepareFile);
						try {
							final Video video = ppwFFmpegExecutor.videoAnalysis(prepareFilePath, productName);
							analysisList.add(video.toString());
							prepareFile.delete();
						} catch (IOException e) {
							logger.error(e.toString());
							e.printStackTrace();
						}
					}
				}
			}
			CSVUtils.exportCsv(csv, analysisList);
			return true;
		} else {
			System.out.println("LIST IS NULL!");
		}
		return false;
	}

}
