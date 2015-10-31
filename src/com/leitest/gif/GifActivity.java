package com.leitest.gif;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.leitest.gif.GifView.OnStartPlayListening;
import com.terry.gif.R;

@SuppressLint("HandlerLeak")
public class GifActivity extends Activity implements OnStartPlayListening {
	/** Called when the activity is first created. */

	private GifView mgv;

	private ACache mACache;

	private String url = "http://img2.100bt.com/upload/ttq/20130914/1379127831456_middle.gif";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mACache = ACache.get(this);
		mgv = (GifView) findViewById(R.id.mgv);
		mgv.setOnStartPlayListening(this);
		if (mACache.getAsBinary(url).length != 0) {
			mgv.setGifDecoderImage(mACache.getAsBinary(url));
		} else {
			mgv.setResource(url);
		}
	}

	@Override
	public void onStartPlay(byte[] buffer) {
		mACache.put(url, buffer);
		mgv.setGifDecoderImage(buffer);
	}
}
