
package com.changba.songstudio.video.player;

import java.util.List;

import com.changba.songstudio.video.decoder.MediaCodecDecoderLifeCycle;
import com.changba.songstudio.video.player.OnInitializedCallback.OnInitialStatus;

import android.util.Log;
import android.view.Surface;

public class ELPlayerController extends MediaCodecDecoderLifeCycle {
	private volatile boolean isInitializing = false;
	private volatile boolean isStopping = false;
	private volatile boolean isPlaying = false;
	private int index = -1;
	
	public void init(final String srcFilenameParam, final String rtmpCurl, final int[] max_analyze_duration,
			final int probesize, final boolean fpsProbeSizeConfigured, final float minBufferedDuration, final float maxBufferedDuration,
			final OnInitializedCallback onInitializedCallback) {
		Log.i("ChangbaPlayer_JNI_Layer", "ELPlayerController init isPlaying : " + isPlaying);
		if(!isInitializing && !isPlaying){
			isInitializing = true;
			initializedCallback = onInitializedCallback;
			
			index = prepare(srcFilenameParam,(rtmpCurl == null)?"":rtmpCurl, max_analyze_duration, max_analyze_duration.length, probesize, fpsProbeSizeConfigured, minBufferedDuration,
					maxBufferedDuration);
			Log.i("ChangbaPlayer_JNI_Layer", "ELPlayerController init  Instantial An Instance index : " + index);
		}
	}

	/** 当init失败, 要进行重试 **/
	public void retry(final String srcFilenameParam, final String rtmpCurl, final int[] max_analyze_duration,
			final int probesize, final boolean fpsProbeSizeConfigured, final float minBufferedDuration, final float maxBufferedDuration,
			final OnInitializedCallback onInitializedCallback) {
		Log.i("ChangbaPlayer_JNI_Layer", "ELPlayerController init isPlaying : " + isPlaying);
		if(!isInitializing && !isPlaying){
			isInitializing = true;
			initializedCallback = onInitializedCallback;
			index = retry(index, srcFilenameParam, max_analyze_duration, max_analyze_duration.length, probesize, fpsProbeSizeConfigured, minBufferedDuration,
					maxBufferedDuration);
			Log.i("ChangbaPlayer_JNI_Layer", "ELPlayerController init  Instantial An Instance index : " + index);
		}
	}
	
	/* OnInitializedCallback */
	private OnInitializedCallback initializedCallback;

	public void onInitializedFromNative(boolean initCode) {
		isInitializing = false;
		isPlaying = initCode;
		if (initializedCallback != null){
			OnInitialStatus onInitialStatus = initCode ? OnInitialStatus.CONNECT_SUCESS : OnInitialStatus.CONNECT_FAILED;
			if(isStopping){
				onInitialStatus = OnInitialStatus.CLINET_CANCEL;
			}
			initializedCallback.onInitialized(onInitialStatus);
		}
	}

	public boolean isInitializing(){
		return isInitializing;
	}
	
	public boolean isPlaying(){
		return isPlaying;
	}
	
	public void onSurfaceCreated(final Surface surface, int width, int height){
		Log.i("ChangbaPlayer_JNI_Layer", "ELPlayerController onSurfaceCreated index : " + index);
		this.onSurfaceCreated(index, surface, width, height);
	}
	
	private native void onSurfaceCreated(int index, final Surface surface, int widht, int height);

	public void onSurfaceDestroyed(final Surface surface){
		this.onSurfaceDestroyed(index, surface);
	}
	private native void onSurfaceDestroyed(int index, final Surface surface);

	/**
	 * 初始化
	 *
	 * @param srcFilenameParam
	 *            文件路径或者直播地址
	 * @return 是否正确初始化
	 */
	public native int prepare(String srcFilenameParam, String rtmpUrl,  int[] max_analyze_durations, int size, int probesize, boolean fpsProbeSizeConfigured,
			float minBufferedDuration, float maxBufferedDuration);
	/**
	 * 初始化失败的话, 重试一次
	 *
	 * @param srcFilenameParam
	 *            文件路径或者直播地址
	 * @return 是否正确初始化
	 */
	public native int retry(int index, String srcFilenameParam, int[] max_analyze_durations, int size, int probesize, boolean fpsProbeSizeConfigured,
			float minBufferedDuration, float maxBufferedDuration);

	/**
	 * 暂停播放
	 */
	public void pause(){
		this.pause(index);
	}
	private native void pause(int index);

	/**
	 * 继续播放
	 */
	public void play(){
		this.play(index);
	}
	private native void play(int index);

	private OnStoppedCallback mStoppedCallback;
	
	/**
	 * 停止播放
	 */
	public void stopPlay(final OnStoppedCallback callback) {
		if(!isStopping){
			mStoppedCallback = callback;
			isStopping = true;
			new Thread() {
				public void run() {
					ELPlayerController.this.stop(index);

					isInitializing = false;
					isPlaying = false;
					isStopping = false;

					if (mStoppedCallback != null) {
						mStoppedCallback.onStopped();
					}
				}
			}.start();
		}
	}
	
	/**
	 * @param beginOpen 开始试图去打开一个直播流
	 * @param successOpen 成功打开流
	 * @param firstScreenTimeMills 首屏时间
	 * @param failOpen 流打开失败
	 * @param failOpenType 流打开失败类型
	 * @param duration 时长
	 * @param retryOpen 重试
	 * @param videoQueueFull 解码缓冲区满
	 * @param videoQueueEmpty 解码缓冲区空
	 */
	public void statisticsCallbackFromNative(long beginOpen, float successOpen, float firstScreenTimeMills, float failOpen, int failOpenType, float duration,
			List<Double> retryOpen, List<Double> videoQueueFull, List<Double> videoQueueEmpty){
		if (mStoppedCallback != null){
			mStoppedCallback.getstaticsData(beginOpen, successOpen, firstScreenTimeMills, failOpen, failOpenType, 
					duration, retryOpen, videoQueueFull, videoQueueEmpty);
		}
	}
	
	private native void stop(int index);

	/**
	 * 获得缓冲进度 返回秒数（单位秒 但是小数点后有3位 精确到毫秒）
	 */
	public float getBufferedProgress(){
		return this.getBufferedProgress(index);
	}
	private native float getBufferedProgress(int index);

	/**
	 * 获得播放进度（单位秒 但是小数点后有3位 精确到毫秒）
	 */
	public float getPlayProgress(){
		return this.getPlayProgress(index);
	}
	private native float getPlayProgress(int index);

	/**
	 * 跳转到某一个位置
	 */
	public void seekToPosition(float position){
		this.seekToPosition(index, position);
	}
	private native void seekToPosition(int index, float position);

	public void resetRenderSize(int left, int top, int width, int height){
		this.resetRenderSize(index, left, top, width, height);
	}
	private native void resetRenderSize(int index, int left, int top, int width, int height);

	public void setRTMPCurl(String url){
		this.setRTMPCurl(index, url);
	}
	private native void setRTMPCurl(int index, String url);
	
	// 得到埋点，目前的形式是返回字符串，"B_0.000, O_1.056, W1_1.056, R_1.056, F_1.202, F_2.202, E_45.557, E_46.557"
	// 这样的形式，然后加载中正在缓冲和缓冲结束，开始播放以及结束没做没埋点，不想把太多业务逻辑代码做到底层，可以由java端通过回调得
	@Deprecated
	public native String getBuriedPoints();
	
	public void showLoadingDialog() {
		Log.i("problem", "showLoadingDialog...");
	}

	public void hideLoadingDialog() {
		Log.i("problem", "hideLoadingDialog...");
	}

	public void onCompletion() {
		Log.i("problem", "onCompletion...");
	}

	public void videoDecodeException() {
		Log.i("problem", "videoDecodeException...");
	}

	public void viewStreamMetaCallback(int width, int height, float duration) {
		Log.i("problem", "width is : " + width + ";height is : " + height + ";duration is : " + duration);
	}
}
