package com.paipianwang.mq.video.utils;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.Fraction;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.job.FFmpegJob.State;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.probe.FFmpegStream.CodecType;

/**
 * 
 * @author Jack
 *
 */
public class PPWFFmpegExecutor {

	private final Logger logger = LoggerFactory.getLogger(PPWFFmpegExecutor.class);
	
	public long timeoutParam = 300000000;
	public long bitRate = 2000000; // 比特率
	public String ffmpegParam = "ffmpeg";
	public String ffprobeParam = "ffmpeg";
	static float MAX_WIDTH = 1920;
	static float MAX_HEIGHT = 1080;
	static float DEFAULT_RATIO;
	static int AUTOSET = -1;
	
	public static FFmpeg ffmpeg = null; // 视频处理程序
	public static FFprobe ffprobe = null; // 媒体信息读取程序
	public static FFmpegExecutor ffExecutor = null; // ffmpeg 处理程序
	public static ExecutorService executor = null; // 构造线程池
	public Timeout timeout = null;
	
	
	public static PPWFFmpegExecutor ppw = null;

	public PPWFFmpegExecutor(final long timeoutParam, final String ffmpegParam, final String ffprobeParam, final long bitRate) {
		try {
			String logId = UUID.randomUUID().toString();  
	        logger.info("[ffmpeg构造方法(PPWFFmpegExecutor)][" + logId  
	                + "][默认参数：ffmpeg=" + ffmpegParam + ",ffprobe="  
	                + ffprobeParam + ",timeout=" + timeoutParam + ",bitRate=" + bitRate + "]");  
	        
	        this.timeoutParam = timeoutParam;
			this.ffmpegParam = ffmpegParam;
			this.ffprobeParam = ffprobeParam;
			this.bitRate = bitRate;
			
			ffmpeg = new FFmpeg(ffmpegParam);
			ffprobe = new FFprobe(ffprobeParam);
			ffExecutor = new FFmpegExecutor(ffmpeg, ffprobe); // ffmpeg 处理程序
			executor = Executors.newSingleThreadExecutor(); // 构造线程池
			timeout = new Timeout(timeoutParam, TimeUnit.SECONDS);
		} catch (IOException e) {
			logger.error("[ffmpeg构造方法(PPWFFmpegExecutor)] initial error ...");
			e.printStackTrace();
		}
	}
	
	public PPWFFmpegExecutor() {
		
	}

	public void init() {
		ppw = new PPWFFmpegExecutor(timeoutParam, ffmpegParam, ffprobeParam, bitRate);
	}

	public State adjustBitRate(String inputFile, String outputFile)
			throws IOException, InterruptedException, ExecutionException {
		
		FFmpegProbeResult inFFmpegProbeResult = ffprobe.probe(inputFile);
		assertFalse(inFFmpegProbeResult.hasError());
		FFmpegFormat format = inFFmpegProbeResult.getFormat();
		int videoStatus = -1; // -1 不存在流，0复合标准的流，1不符合标准的流
		int audioStatus = -1;
		List<FFmpegStream> streams = inFFmpegProbeResult.getStreams();
		VideoSize adjustSize = adjustSize(inFFmpegProbeResult);
		if (streams != null) {
			if (streams != null && !streams.isEmpty()) {
				for (FFmpegStream fFmpegStream : streams) {
					if (fFmpegStream.codec_type == CodecType.VIDEO) {
						if ("h264".equals(fFmpegStream.codec_name)) {
							videoStatus = 0;
						} else {
							videoStatus = 1;
						}
					} else if (fFmpegStream.codec_type == CodecType.AUDIO) {
						if ("aac".equals(fFmpegStream.codec_name)) {
							audioStatus = 0;
						} else {
							audioStatus = 1;
						}
					}
				}
			}
		}

		if (format != null && !adjustSize.conversion) {
			long bit_rate = format.bit_rate;
			if (bit_rate <= bitRate) {
				if (format.format_name.contains("mp4")) {
					String fileName = format.filename;
					if (fileName.lastIndexOf(".mp4") > -1) {
						if ((videoStatus == -1 || videoStatus == 0) && (audioStatus == -1 || audioStatus == 0)) {
							File tmpInFile = new File(inputFile);
							File tmpOutFile = new File(outputFile);
							tmpInFile.renameTo(tmpOutFile);
							return State.FINISHED;
						}
					}
				}
			}
		}

		FFmpegBuilder builder = new FFmpegBuilder().overrideOutputFiles(true).setInput(inFFmpegProbeResult);
		FFmpegOutputBuilder ffmpegOutput = builder.addOutput(outputFile);
		ffmpegOutput.setFormat("mp4").setVideoBitRate(bitRate);
		if (videoStatus != -1 && videoStatus == 1) {
			ffmpegOutput.setVideoCodec("h264");
		}
		if (audioStatus != -1 && audioStatus == 1) {
			ffmpegOutput.setAudioCodec("aac");
		}
		if (adjustSize.conversion) {
			// 使用过滤器修改视频，可以用crop、scale。前者是裁剪后者是缩放再次用scale
			String filter = "scale=";
			filter += adjustSize.width;
			filter += ":";
			filter += adjustSize.height;
			ffmpegOutput.setVideoFilter(filter);
		}

		builder = ffmpegOutput.done();
		State state = execute(builder);
		return state;
	}
	
	public static State interceptionFirstFrame(String inputFile, String outputFile)
			throws InterruptedException, ExecutionException {
		FFmpegBuilder builder = new FFmpegBuilder().setInput(inputFile).overrideOutputFiles(true).addOutput(outputFile)
				.setFrames(1).setStartOffset(2, TimeUnit.SECONDS).done();
		State state = execute(builder);
		return state;
	}

	private static State execute(FFmpegBuilder builder) throws InterruptedException, ExecutionException {
		FFmpegJob job = ffExecutor.createJob(builder);
		executor.submit(job).get();
		return job.getState();
	}
	
	private static VideoSize adjustSize(FFmpegProbeResult fFmpegProbeResult) {
		VideoSize videoSize = new VideoSize();
		if (fFmpegProbeResult != null) {

			List<FFmpegStream> streams = fFmpegProbeResult.getStreams();
			if (streams != null && !streams.isEmpty()) {
				FFmpegStream videoStream = null;
				for (FFmpegStream fFmpegStream : streams) {
					if (CodecType.VIDEO.equals(fFmpegStream.codec_type)) {
						videoStream = fFmpegStream;
						break;
					}
				}
				// 调整视频尺寸
				float width = 0;
				float height = 0;
				if (videoStream != null) {
					width = videoStream.width;
					height = videoStream.height;
					BigDecimal b = new BigDecimal(width / height);
					float inRatio = b.setScale(5, BigDecimal.ROUND_HALF_UP).floatValue();
					// 将视频转换为16:9
					if (inRatio > DEFAULT_RATIO) {
						// 宽大,检测调整宽。高自适应。
						if (width > MAX_WIDTH) {
							videoSize.width = MAX_WIDTH;
							videoSize.height = AUTOSET;
							videoSize.setConversion(true);
						} else {
							// 不需要强制降低宽度，调整高度即可
							videoSize.setConversion(false);
						}
					} else if (inRatio == DEFAULT_RATIO) {
						if (width > MAX_WIDTH || height > MAX_HEIGHT) {
							videoSize.width = MAX_WIDTH;
							videoSize.height = MAX_HEIGHT;
							videoSize.setConversion(true);
						} else {
							videoSize.setConversion(false);
						}
					} else {
						// 高大,因为以16/9为参照，所以1:1视频也属于高达，自动调整宽
						if (height > MAX_HEIGHT) {
							videoSize.width = AUTOSET;
							videoSize.height = MAX_HEIGHT;
							videoSize.setConversion(true);
						} else {
							videoSize.setConversion(false);
						}
					}
				}
			}
		}
		return videoSize;
	}
	
	static class VideoSize {
		float width = 0;
		float height = 0;
		boolean conversion = false;

		public float getWidth() {
			return width;
		}

		public void setWidth(float width) {
			this.width = width;
		}

		public float getHeight() {
			return height;
		}

		public void setHeight(float height) {
			this.height = height;
		}

		public boolean isConversion() {
			return conversion;
		}

		public void setConversion(boolean conversion) {
			this.conversion = conversion;
		}

	}
	
	public static Video videoAnalysis(String inputFilePath,String fileName) throws IOException {
		File inputFile = new File(inputFilePath);
		FFmpegProbeResult inFFmpegProbeResult = ffprobe.probe(inputFile.getAbsolutePath());
		if (inFFmpegProbeResult.hasError()) {
			return null;
		}
		FFmpegFormat format = inFFmpegProbeResult.getFormat();
		List<FFmpegStream> streams = inFFmpegProbeResult.getStreams();
		// 分辨率，比特率 bps，音频编码，视频编码，每秒帧数
		Video video = new Video();
		if (format != null) {
			video.setBit_rate(format.bit_rate);
		}
		if (streams != null && streams.size() > 0) {
			for (FFmpegStream fFmpegStream : streams) {
				if (fFmpegStream != null) {
					if (CodecType.VIDEO.equals(fFmpegStream.codec_type)) {
						video.setWidth(fFmpegStream.width);
						video.setHeight(fFmpegStream.height);
						video.setVideoEncode(fFmpegStream.codec_name);
						Fraction avg = fFmpegStream.avg_frame_rate;
						video.setAvg_frame_rate(avg.intValue() + "");
					} else if (CodecType.AUDIO.equals(fFmpegStream.codec_type)) {
						video.setAudioEncode(fFmpegStream.codec_name);
					} else {
						System.out.println("没有用到的流！");
					}
				}
			}
		}
		video.setFileName(fileName);
		return video;
	}
	
	public static class Video {
		long bit_rate;
		int width;
		int height;
		String audioEncode;
		String videoEncode;
		String avg_frame_rate;
		String fileName;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public long getBit_rate() {
			return bit_rate;
		}

		public void setBit_rate(long bit_rate) {
			this.bit_rate = bit_rate;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public String getAudioEncode() {
			return audioEncode;
		}

		public void setAudioEncode(String audioEncode) {
			this.audioEncode = audioEncode;
		}

		public String getVideoEncode() {
			return videoEncode;
		}

		public void setVideoEncode(String videoEncode) {
			this.videoEncode = videoEncode;
		}

		public String getAvg_frame_rate() {
			return avg_frame_rate;
		}

		public void setAvg_frame_rate(String avg_frame_rate) {
			this.avg_frame_rate = avg_frame_rate;
		}

		public String toString() {
			try {
				return new String(fileName.getBytes(), "utf-8") + "," + bit_rate + "bps," + width + "," + height + ","
						+ audioEncode + "," + videoEncode + "," + avg_frame_rate;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	public long getTimeoutParam() {
		return timeoutParam;
	}

	public void setTimeoutParam(long timeoutParam) {
		if(timeoutParam != 0) 
			this.timeoutParam = timeoutParam;
	}

	public String getFfmpegParam() {
		return ffmpegParam;
	}

	public void setFfmpegParam(String ffmpegParam) {
		if(StringUtils.isNotBlank(ffmpegParam))
			this.ffmpegParam = ffmpegParam;
	}

	public String getFfprobeParam() {
		return ffprobeParam;
	}

	public void setFfprobeParam(String ffprobeParam) {
		if(StringUtils.isNotBlank(ffprobeParam))
			this.ffprobeParam = ffprobeParam;
	}

	public long getBitRate() {
		return bitRate;
	}

	public void setBitRate(long bitRate) {
		if(bitRate != 0)
			this.bitRate = bitRate;
	}
	
}
