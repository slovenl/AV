package com.phuket.tour.video_player;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.changba.songstudio.video.player.ELPlayerController;
import com.changba.songstudio.video.player.OnStoppedCallback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 提高首屏时间的测试Activity
 */
public class SpeedUpFirstScreenPlayerActivity extends Activity implements OnSeekBarChangeListener {

	/** GL surface view instance. */
	private SurfaceView surfaceView;
	private ELPlayerController playerController;
	private SeekBar playerSeekBar;
	private TextView current_time_label;
	/** Test... **/
	private TextView end_time_label;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surfaceview_player);
		playerSeekBar = (SeekBar) findViewById(R.id.music_seek_bar);
		playerSeekBar.setOnSeekBarChangeListener(this);
		surfaceView = (SurfaceView) findViewById(R.id.gl_surface_view);
		current_time_label = (TextView) findViewById(R.id.current_time_label);
		end_time_label = (TextView) findViewById(R.id.end_time_label);
		playerController = ELLivePlayerController.getInstance().getPlayerController();
		surfaceView.getLayoutParams().height = getWindowManager().getDefaultDisplay().getHeight();
		SurfaceHolder mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(previewCallback);
		findViewById(R.id.pause_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playerController.pause();
			}
		});
		findViewById(R.id.continue_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				timer = new Timer();
				timerTask = new TimerTask() {
					@Override
					public void run() {
						playTimeSeconds = playerController.getPlayProgress();
						bufferedTimeSeconds = playerController.getBufferedProgress();
						handler.sendEmptyMessage(UPDATE_PLAY_VIEDO_TIME_FLAG);
					}
				};
				timer.schedule(timerTask, 500, 100);

				playerController.play();
				timerStart();
			}
		});
		timerStart();
	}

	protected void timerStop() {
		timerTask.cancel();
		timer.cancel();
	}

	protected void timerStart() {
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				if(null != playerController) {
					playTimeSeconds = playerController.getPlayProgress();
					bufferedTimeSeconds = playerController.getBufferedProgress();
					handler.sendEmptyMessage(UPDATE_PLAY_VIEDO_TIME_FLAG);
				}
			}
		};
		timer.schedule(timerTask, 500, 100);
	}

	float playTimeSeconds = 0.0f;
	float bufferedTimeSeconds = 0.0f;
	float totalDuration = 0.0f;
	public static final int UPDATE_PLAY_VIEDO_TIME_FLAG = 1201;
	Timer timer;
	TimerTask timerTask;
	SurfaceHolder surfaceHolder = null;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Free the native renderer
		Log.i("problem", "playerController.stop()...");
		stopPlayer(new OnStoppedCallback() {
			@Override
			public void onStopped() {
				 String buriedPoints = playerController.getBuriedPoints();
				 Log.d("problem", "getBuriedPoints "+ buriedPoints);
				
				if (null != timerTask) {
					timerTask.cancel();
					timerTask = null;
				}
				if (null != timer) {
					timer.cancel();
					timer = null;
				}
			}

			@Override
			public void getstaticsData(long beginOpen, float successOpen, float firstScreenTimeMills, float failOpen,
					int failOpenType, float duration, List<Double> retryOpen, List<Double> videoQueueFull,
					List<Double> videoQueueEmpty) {
				Log.i("problem", String.format("statisticsCallbackFromNative[%d,%.2f,%.2f,%.2f,%d,%.2f,%d, %d, %d]", 
						beginOpen, successOpen, firstScreenTimeMills, failOpen, failOpenType, duration, retryOpen.size(), 
						videoQueueFull.size(), videoQueueEmpty.size()));
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void stopPlayer(OnStoppedCallback callback){
		Log.i("problem", "playerController.stop()...");
		playerController.stopPlay(callback);
	}

	private static final int PLAY_END_FLAG = 12330;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_PLAY_VIEDO_TIME_FLAG:
				if (!isDragging) {
					String curtime = String.format("%02d:%02d", (int) playTimeSeconds / 60, (int) playTimeSeconds % 60);
					String totalTime = String.format("%02d:%02d", (int) totalDuration / 60, (int) totalDuration % 60);
					current_time_label.setText(curtime);
					end_time_label.setText(totalTime);
					int progress = totalDuration == 0.0f ? 0 : (int) (playTimeSeconds * 100 / totalDuration);
					int secondProgress = totalDuration == 0.0f ? 0 : (int) (bufferedTimeSeconds * 100 / totalDuration);
					playerSeekBar.setProgress(progress);
					playerSeekBar.setSecondaryProgress(secondProgress);
				}
				break;
			case PLAY_END_FLAG:
				Toast.makeText(SpeedUpFirstScreenPlayerActivity.this, "播放结束了！", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

	};

	private Callback previewCallback = new Callback() {

		public void surfaceCreated(SurfaceHolder holder) {
			surfaceHolder = holder;

			int duration = (int)(System.currentTimeMillis() - ELLivePlayerController.getInstance().getStartTimeMills());
			Log.i("problem", "提前启动播放器的时间是 【" + duration + "】毫秒");
			int width = getWindowManager().getDefaultDisplay().getWidth();
			int height = getWindowManager().getDefaultDisplay().getHeight();
			playerController.onSurfaceCreated(holder.getSurface(), width, height);

//			playerController.resetRenderSize(0, 0, width, height);
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//			playerController.resetRenderSize(0, 0, width, height);
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			playerController.onSurfaceDestroyed(holder.getSurface());
		}
	};

	/** 是否正在拖动，如果正在拖动的话，就不要在改变seekbar的位置 **/
	private boolean isDragging = false;

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		isDragging = true;

		Log.i("problem", "onStartTrackingTouch");
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// Log.i("problem", "onProgressChanged "+progress);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		isDragging = false;
		float pro = seekBar.getProgress();
		float num = seekBar.getMax();
		float result = pro / num;
		seekToPosition(result * totalDuration);

		Log.i("problem", "onStopTrackingTouch");
	}

	public void seekToPosition(float position) {
		Log.i("problem", "position:" + position);
		playerController.seekToPosition(position);
	}

}
