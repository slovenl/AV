package videochat.ju.com.videochat;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class VideoEncoder {
	private static final String TAG="VideoEncoder";
	private MediaCodec mediaCodec;
	private boolean isSourceDataOver = false;
	private ByteBuffer[] outputBuffers;
	private ByteBuffer[] inputBuffers;
	private int lastOutputBufferIndex =-1;
	private LinkedList<Long> ptsList= new LinkedList<Long>();
    public Surface initForSuface(int width,int height,int fps) throws IOException {
        Log.d(TAG, "java encoder initForSuface "+width+" "+height);
        mediaCodec = MediaCodec.createEncoderByType("video/avc");//
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_BIT_RATE, (int) (width * height * fps * 0.065));
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
        //format.setInteger(MediaFormat.KEY_PROFILE,MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
        //添加sps那一帧的pts记录
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Surface surface = mediaCodec.createInputSurface();
        mediaCodec.start();
        outputBuffers = mediaCodec.getOutputBuffers();
        return surface;
    }
	private boolean mHighBitrate=false;
	public void testBitrate(){
        Bundle bundle = new Bundle();
		if(mHighBitrate){
			mHighBitrate=false;
			bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, (int) (1920*1080*30*0.065));
		}else{
			mHighBitrate=true;
			bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, (int) (1920*1080*30*1));
		}

		mediaCodec.setParameters(bundle);
	}
	public void init(int width,int height,int colorFormat,int fps) throws IOException {
		Log.d(TAG, "java encoder init "+width+" "+height+" "+colorFormat);
		mediaCodec = MediaCodec.createEncoderByType("video/avc");//
	    MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
	    format.setInteger(MediaFormat.KEY_BIT_RATE, (int) (width * height * fps * 0.065));
	    format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
	    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
	    format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
	    //添加sps那一帧的pts记录
	    ptsList.add(0L);
	    mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	    mediaCodec.start();
	    outputBuffers = mediaCodec.getOutputBuffers();
	    inputBuffers = mediaCodec.getInputBuffers();
	    /**
	     * nexus 5
	     * 遇到一个bug,当先开始解码,再开始编码,编码器会报错.
	     * 这里丢个假数据进去,让编码先开始.
	     * 三星s4测试无需这样做
	     */
	    if("Nexus 5".equals(Build.MODEL)){
	    	Buffer b = new Buffer();
	    	encode(ByteBuffer.allocateDirect(0), 1, true,b);
	    }
	}
	public int encode(ByteBuffer input,long pts,boolean isIFrame,Buffer output){
		//Log.d(TAG, "java encoder "+input+" "+pts+" "+isIFrame+" "+output);
		int result = 0;
		if(!isSourceDataOver){
			if(input != null){
				int inputBufferIndex = mediaCodec.dequeueInputBuffer(10*1000*1000);
				//Log.d(TAG, "java encoder inputBufferIndex "+inputBufferIndex);
				if (inputBufferIndex >= 0) {
					ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
					inputBuffer.clear();
					inputBuffer.put(input);
					result = input.limit();
					int flag =0;
					if(isIFrame){
						flag |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
					}
					mediaCodec.queueInputBuffer(inputBufferIndex, 0, result, pts, flag);
					if(result ==0){
						return 0;
					}
					ptsList.add(pts);
				}else{
					return -1;
				}
			}else{
				isSourceDataOver = true;
				int inputBufferIndex = mediaCodec.dequeueInputBuffer(10*1000*1000);
				if (inputBufferIndex >= 0) {
					int flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
					mediaCodec.queueInputBuffer(inputBufferIndex,0,0,pts*10,flag);
				}
			}
		}
		if(lastOutputBufferIndex != -1){
			mediaCodec.releaseOutputBuffer(lastOutputBufferIndex, false);
			lastOutputBufferIndex = -1;
		}
		long waitTime = 0;
		if(isSourceDataOver){
			waitTime = -1;
		}
		BufferInfo info = new BufferInfo();
		int outputBufferIndex = mediaCodec.dequeueOutputBuffer(info,waitTime);
		//Log.d(TAG, "java encoder dequeueOutputBuffer "+outputBufferIndex);
		while(waitTime==-1 && outputBufferIndex<0){
			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
		    	outputBuffers = mediaCodec.getOutputBuffers();
		    }
			outputBufferIndex = mediaCodec.dequeueOutputBuffer(info,waitTime);
		}
		if (outputBufferIndex >= 0) {
			// outputBuffer is ready to be processed or rendered.
			if((info.flags &  MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
				//end
				return result;
			}
			ByteBuffer buffer = outputBuffers[outputBufferIndex];
			buffer.clear();
			output.data=buffer;
			output.length = info.size;
			output.isIFrame = (info.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME)!=0;
			Long temp = ptsList.pollFirst();
			long outputPts =  temp == null?info.presentationTimeUs/10:temp;
			output.pts = outputPts;
			lastOutputBufferIndex = outputBufferIndex;
			//Log.d(TAG, "java encoder output "+output);
	    }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
	    	outputBuffers = mediaCodec.getOutputBuffers();
	    }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			
			//Log.d(TAG, "java encoder dequeueOutputBuffer2 "+outputBufferIndex);
		}
		return result;
		
	}

    public int encode(Buffer output){
        Log.d(TAG, "java encoder ");
        int result = 0;
        if(lastOutputBufferIndex != -1){
            mediaCodec.releaseOutputBuffer(lastOutputBufferIndex, false);
            lastOutputBufferIndex = -1;
        }
        long waitTime = 0;
        BufferInfo info = new BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(info,waitTime);
        Log.d(TAG, "java encoder dequeueOutputBuffer "+outputBufferIndex);

        if (outputBufferIndex >= 0) {
            // outputBuffer is ready to be processed or rendered.
            if((info.flags &  MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                //end
                return result;
            }
            ByteBuffer buffer = outputBuffers[outputBufferIndex];
            buffer.clear();
            output.data=buffer;
            output.length = info.size;
            output.isIFrame = (info.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME)!=0;
            Long temp = ptsList.pollFirst();
            long outputPts =  temp == null?info.presentationTimeUs/10:temp;
            output.pts = outputPts;
            lastOutputBufferIndex = outputBufferIndex;
            Log.d(TAG, "java encoder output "+output);
        }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            outputBuffers = mediaCodec.getOutputBuffers();
        }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            Log.d(TAG, "java encoder dequeueOutputBuffer2 "+outputBufferIndex);
        }
        return result;

    }

	public void close(){
		Log.d(TAG, "java encoder close");
		if(lastOutputBufferIndex != -1){
			mediaCodec.releaseOutputBuffer(lastOutputBufferIndex, false);
			lastOutputBufferIndex = -1;
		}
		mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
	}
}
