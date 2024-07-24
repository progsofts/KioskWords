package com.progsoft.device_owner.iflytek;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.progsoft.device_owner.R;

@SuppressLint("DefaultLocale")
public class TtsDemo {
	private static final String TAG = TtsDemo.class.getSimpleName();
	// 语音合成对象
	private SpeechSynthesizer mTts;
	private boolean inited = false;

	// 默认云端发音人
	public static String voicerCloud="xiaoyan";
	// 默认本地发音人
	public static String voicerLocal="xiaoyan"; //xiaofeng
	public static String voicerXtts="xiaoyan";
	//缓冲进度
	private int mPercentForBuffering = 0;	
	//播放进度
	private int mPercentForPlaying = 0;
	
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	@SuppressLint("StaticFieldLeak")
	private static volatile TtsDemo sInstance;
	private final Context mContext;
	//private static Activity mActivity;
	private HandlerThread mHandlerThread = new HandlerThread("TestHandleThread");
	private Handler mHandler = null;
	private static final Object SERVICE_LOCK = new Object();

	private TtsDemo(Context context /*, Activity activity*/) {
		mContext = context;
		//mActivity = activity;

		mHandlerThread.start();
		mHandler = new InnerHandler(mHandlerThread.getLooper());
	}

	public static TtsDemo getInstance(Context context /*, Activity activity*/) {
		synchronized (SERVICE_LOCK) {
			if (sInstance == null) {
				sInstance = new TtsDemo(context /*, activity*/);
				sInstance.onStart();
			}
            return sInstance;
		}
	}

	private class InnerHandler extends Handler {
		public InnerHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			Log.d(TAG, "handleMessage msg=" + message.what);
			super.handleMessage(message);
			if (message.what == 0) {
				Bundle bundle = message.getData();
				String rstr = bundle.getString("result");
				Log.e(TAG,"Result " + rstr);
			} else {
				Log.e(TAG,"Unexpected value: " + String.valueOf(message.what));
			}
		}
	}

	
	public void onStart() {
        //param.append(",");
		// 设置使用v5+
		//param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.ENGINE_MODE);
		SpeechUtility.createUtility(mContext, "appid=" + mContext.getString(R.string.app_id)
                //param.append(",");
                // 设置使用v5+
                //param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.ENGINE_MODE);
        );

		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener);
		if( null == mTts ){
			// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
			this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化" );
			Log.e(TAG,"创建对象失败，请确认 libmsc.so");
			return;
		}
		mEngineType =  SpeechConstant.TYPE_LOCAL;
		inited = true;
	}

	public void playText(String text) {
		new Thread((new Runnable() {
			@Override
			public void run() {
				try {
					int max = 0;
					while (!inited) {
						max++;
						Thread.sleep(500);
						if (max > 10)
							return;
					}
					setParam();
					Log.e(TAG,"准备点击： "+ System.currentTimeMillis());
					int code = mTts.startSpeaking(text, mTtsListener);
					/* *
					   * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
					   * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
					 */
					// String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
					// int code = mTts.synthesizeToUri(text, path, mTtsListener);

					if (code != ErrorCode.SUCCESS) {
						showTip("语音合成失败,错误码: " + code+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
					}
				} catch (Exception ignored) {
				}
			}
		}), "").start();

	}


	/**
	 * 初始化监听。
	 */
	private final InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("初始化失败,错误码："+code+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
			}
			// 初始化成功，之后可以调用startSpeaking方法
			// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
			// 正确的做法是将onCreate中的startSpeaking调用移至这里
		}
	};

	/**
	 * 合成回调监听。
	 */
	private final SynthesizerListener mTtsListener = new SynthesizerListener() {
		
		@Override
		public void onSpeakBegin() {
			//showTip("开始播放");
			Log.d(TAG,"开始播放："+ System.currentTimeMillis());
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
			mPercentForBuffering = percent;
			showTip(String.format("缓冲进度为%d%%，播放进度为%d%%",
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
			showTip(String.format("缓冲进度为%d%%，播放进度为%d%%",
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				showTip("播放完成");
			} else {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
				// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
				// 若使用本地能力，会话id为null
				if (SpeechEvent.EVENT_SESSION_ID == eventType) {
					String sid = obj.getString(SpeechEvent.KEY_EVENT_AUDIO_URL);
					Log.d(TAG, "session id =" + sid);
				}

				//实时音频流输出参考
				/*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
					byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
					Log.e("MscSpeechLog", "buf is =" + buf);
				}*/
		}
	};

	private void showTip(final String str){
		/*
		mContext.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
		 */
	}

	private void setParam(){
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		//设置合成
		if(mEngineType.equals(SpeechConstant.TYPE_CLOUD))
		{
			//设置使用云端引擎
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			//设置发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicerCloud);

		}else if(mEngineType.equals(SpeechConstant.TYPE_LOCAL)){
			//设置使用本地引擎
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			//设置发音人资源路径
			mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath());
			//设置发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicerLocal);
		}else{
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_XTTS);
			//设置发音人资源路径
			mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath());
			//设置发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicerXtts);
		}
		//mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
		//设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, "50" /* mSharedPreferences.getString("speed_preference", "50") */);
		//设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, "50" /* mSharedPreferences.getString("pitch_preference", "50") */);
		//设置合成音量 合成音量调高一点对手机而言
		mTts.setParameter(SpeechConstant.VOLUME, "95" /* mSharedPreferences.getString("volume_preference", "50") */);
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3" /* mSharedPreferences.getString("stream_preference", "3") */);
	//	mTts.setParameter(SpeechConstant.STREAM_TYPE, AudioManager.STREAM_MUSIC+"");

		// 设置播放合成音频打断音乐播放，默认为true ， 测试不打断
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");

		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
	}
	
	//获取发音人资源路径
	private String getResourcePath(){
		StringBuilder tempBuffer = new StringBuilder();
		String type= "tts";
		if(mEngineType.equals(SpeechConstant.TYPE_XTTS)){
			type="xtts";
		}
		//合成通用资源
		tempBuffer.append(ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, type+"/common.jet"));
		tempBuffer.append(";");
		//发音人资源
		if(mEngineType.equals(SpeechConstant.TYPE_XTTS)){
			tempBuffer.append(ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, type+"/"+TtsDemo.voicerXtts+".jet"));
		}else {
			tempBuffer.append(ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, type + "/" + TtsDemo.voicerLocal + ".jet"));
		}

		return tempBuffer.toString();
	}
	
	protected void onDestroy() {
		if( null != mTts ){
			mTts.stopSpeaking();
			// 退出时释放连接
			mTts.destroy();
		}
	}
}
