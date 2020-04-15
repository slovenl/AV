package videochat.ju.com.videochat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

public class VideoDecoder {
	private static final String TAG="VideoDecoder";
    private static final int CONFIGURE_FLAG_LOWLAT = 2;
	private boolean isSourceDataOver =false;
	private MediaCodec decoder;
	private ByteBuffer[] inputBuffers;
	private ByteBuffer[] outputBuffers;
	/**
	 * 输出的颜色格式
	 */
	private int outputColorFormat;
	private boolean isInit =false;

	public void init(int width,int height,int colorFormat,Surface surface) throws IOException {

        Log.d(TAG, "java decode init "+width+" "+height+" "+colorFormat);
        decoder = MediaCodec.createDecoderByType("video/avc");
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        if("OMX.MS.AVC.Decoder".equals(decoder.getName())){
            decoder.configure(format,surface,null,CONFIGURE_FLAG_LOWLAT);
        }else{
            decoder.configure(format,surface,null,0);
        }
		decoder.start();
		inputBuffers = decoder.getInputBuffers();
		outputBuffers = decoder.getOutputBuffers();
	}
	public void checkoutOutPut(){
        long waitTime = 0;
        BufferInfo info = new BufferInfo();
        int outputBufferIndex = decoder.dequeueOutputBuffer(info,waitTime);

        //Log.d(TAG, "java decode 4 outputBufferIndex="+outputBufferIndex+" "+info.size+" "+info.offset);
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            outputColorFormat = decoder.getOutputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT);
            outputBufferIndex = decoder.dequeueOutputBuffer(info,waitTime);
            //Log.d(TAG, "java decode 5 outputBufferIndex2="+outputBufferIndex+" "+info.size+" "+info.offset);
        }


        if (outputBufferIndex >= 0) {
            if((info.flags &  MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                Log.d(TAG, "java decode end");
                return;
            }
            ByteBuffer buffer = outputBuffers[outputBufferIndex];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                decoder.releaseOutputBuffer(outputBufferIndex, System.nanoTime());
            }else{
                decoder.releaseOutputBuffer(outputBufferIndex, true);
            }
            Log.d("ttttt","remoteDecodeFrameNo="+remoteDecodeFrameNo);
            remoteDecodeFrameNo++;
        }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            outputBuffers = decoder.getOutputBuffers();
        }
        return;
    }
    public int decodeOnly(byte[] input,int length,long pts,boolean isIFrame){
        int result = 0;
        if(!isSourceDataOver){
            if(input != null){
                if(!isInit ){
                    int inputBufferIndex = decoder.dequeueInputBuffer(10*1000*1000);
                    if(inputBufferIndex < 0 ){
                        return -1;
                    }
                    ByteBuffer buffer = inputBuffers[inputBufferIndex];
                    buffer.clear();
                    int flag =MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
                    buffer.put(input);
                    decoder.queueInputBuffer(inputBufferIndex,0,length,0,flag);
                    isInit = true;
                    return 0;
                }
                int inputBufferIndex = decoder.dequeueInputBuffer(10*1000*1000);
                //Log.d(TAG, "java decode 2 inputBufferIndex="+inputBufferIndex);

                if (inputBufferIndex >= 0) {
                    // fill inputBuffers[inputBufferIndex] with valid data
                    ByteBuffer buffer = inputBuffers[inputBufferIndex];

                    buffer.clear();
                    int flag =0;
                    //buffer.put(spsPps);
                    buffer.put(input);
                    if(input[5]==0x65){
                        flag |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                    }
                    decoder.queueInputBuffer(inputBufferIndex,0,length,pts,flag);
                }else{
                    return -1;
                }
            }else{
                //Log.d(TAG, "java decode 3 sourceDataOver "+this+" "+isSourceDataOver);
                isSourceDataOver = true;
                int inputBufferIndex = decoder.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    int flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                    decoder.queueInputBuffer(inputBufferIndex,0,0,pts,flag);
                }
            }
        }
        return result;
    }
	public int decode(byte[] input,int length,long pts,boolean isIFrame){
		//Log.d(TAG, "java decode 1 "+input+" "+pts+" "+isIFrame);
		int result = 0;
		if(!isSourceDataOver){
			if(input != null){
				if(!isInit ){
					int inputBufferIndex = decoder.dequeueInputBuffer(10*1000*1000);
					if(inputBufferIndex < 0 ){
						return -1;
					}
					ByteBuffer buffer = inputBuffers[inputBufferIndex];
					buffer.clear();
					int flag =MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
					buffer.put(input);
					decoder.queueInputBuffer(inputBufferIndex,0,length,0,flag);
					isInit = true;
                    return 0;
				}
				int inputBufferIndex = decoder.dequeueInputBuffer(10*1000*1000);
				//Log.d(TAG, "java decode 2 inputBufferIndex="+inputBufferIndex);

				if (inputBufferIndex >= 0) {
					// fill inputBuffers[inputBufferIndex] with valid data
					ByteBuffer buffer = inputBuffers[inputBufferIndex];

					buffer.clear();
					int flag =0;
					//buffer.put(spsPps);
					buffer.put(input);
					if(isIFrame){
						flag |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
					}
					decoder.queueInputBuffer(inputBufferIndex,0,length,pts,flag);
				}else{
					return -1;
				}
			}else{
				//Log.d(TAG, "java decode 3 sourceDataOver "+this+" "+isSourceDataOver);
				isSourceDataOver = true;
				int inputBufferIndex = decoder.dequeueInputBuffer(-1);
				if (inputBufferIndex >= 0) {
					int flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
					decoder.queueInputBuffer(inputBufferIndex,0,0,pts,flag);
				}
			}
		}
		long waitTime = 0;
		if(isSourceDataOver){
			waitTime = -1;
		}
		BufferInfo info = new BufferInfo();
		int outputBufferIndex = decoder.dequeueOutputBuffer(info,waitTime);

		while(waitTime ==-1 && outputBufferIndex < 0){
			//一定要拿到输出
			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				outputColorFormat = decoder.getOutputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT);
			}else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
		    	outputBuffers = decoder.getOutputBuffers();
		    }
			outputBufferIndex = decoder.dequeueOutputBuffer(info,waitTime);
			//Log.d(TAG, "java decode 5 outputBufferIndex2="+outputBufferIndex+" "+info.size+" "+info.offset);
		}

		//Log.d(TAG, "java decode 4 outputBufferIndex="+outputBufferIndex+" "+info.size+" "+info.offset);
		if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			outputColorFormat = decoder.getOutputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT);
			outputBufferIndex = decoder.dequeueOutputBuffer(info,waitTime);
			//Log.d(TAG, "java decode 5 outputBufferIndex2="+outputBufferIndex+" "+info.size+" "+info.offset);
	    }


		if (outputBufferIndex >= 0) {
			if((info.flags &  MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
				Log.d(TAG, "java decode end");
				return result;
			}
			ByteBuffer buffer = outputBuffers[outputBufferIndex];
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				decoder.releaseOutputBuffer(outputBufferIndex, System.nanoTime());
			}else{
				decoder.releaseOutputBuffer(outputBufferIndex, true);
			}
			Log.d("ttttt","remoteDecodeFrameNo="+remoteDecodeFrameNo);
			remoteDecodeFrameNo++;
	    }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
	    	outputBuffers = decoder.getOutputBuffers();
	    }
		return result;
	}
	private int remoteDecodeFrameNo;
	public void close(){
		Log.d(TAG, "java decode close");
		decoder.stop();
		decoder.release();
		decoder = null;
	}
}
