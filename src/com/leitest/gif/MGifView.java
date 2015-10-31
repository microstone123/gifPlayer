package com.leitest.gif;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * @author Administrator
 * 
 *         Movie不能使用网络图片的流，也就是不可以使用Movie.decodeStream
 * 
 */
@SuppressLint("HandlerLeak")
public class MGifView extends View {
	private OnStartPlayListening onStartPlayListening;

	private long movieStart;

	private Movie movie;

	// 此处必须重写该构造方法

	public MGifView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public MGifView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setResource(int id) {
		movie = Movie.decodeStream(getResources().openRawResource(id));
		refresh();
	}

	public void setResource(final String urlstr) {
		DownloadImageTask task = new DownloadImageTask();
		task.execute(urlstr);
	}

	public void setResource(byte[] data) {
		movie = Movie.decodeByteArray(data, 0, data.length);
		Log.e("width", movie.width() + "");
		Log.e("height", movie.height() + "");
		refresh();
	}

	private final void refresh() {
		reMeasure();
		invalidate();
	}

	private final void reMeasure() {
		if (movie != null) {
			measure(MeasureSpec.makeMeasureSpec(movie.width(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(movie.height(), MeasureSpec.EXACTLY));
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.e("look", "onMeasure is called");

		LayoutParams lp = (LayoutParams) getLayoutParams();
		boolean isWidMat = lp.width == LayoutParams.MATCH_PARENT;
		boolean isHeiMat = lp.height == LayoutParams.MATCH_PARENT;
		boolean isWidWra = lp.width == LayoutParams.WRAP_CONTENT;
		boolean isHeiWra = lp.height == LayoutParams.WRAP_CONTENT;
		if (isWidMat && isHeiMat) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		if (movie != null) {
			requestLayout();
			setMeasuredDimension(
					isWidMat ? MeasureSpec.getSize(widthMeasureSpec) : isWidWra ? movie.width() : lp.width,
					isHeiMat ? MeasureSpec.getSize(heightMeasureSpec) : isHeiWra ? movie.height() : lp.height);
		} else {
			setMeasuredDimension(isWidMat ? MeasureSpec.getSize(widthMeasureSpec) : isWidWra ? 0 : lp.width,
					isHeiMat ? MeasureSpec.getSize(heightMeasureSpec) : isHeiWra ? 0 : lp.height);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		long curTime = android.os.SystemClock.uptimeMillis();

		// 第一次播放

		if (movieStart == 0) {
			movieStart = curTime;
		}

		if (movie != null) {
			int duraction = movie.duration();

			if (duraction == 0) {
				duraction = 1000;
			}

			int relTime = (int) ((curTime - movieStart) % duraction);

			movie.setTime(relTime);

			movie.draw(canvas, 0, 0);

			// 强制重绘
			invalidate();
		}

		super.onDraw(canvas);
	}

	private final static byte[] getByte(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int len = 0;
		byte[] buffer = new byte[1024];
		try {
			while ((len = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
			}
			return outputStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public OnStartPlayListening getOnStartPlayListening() {
		return onStartPlayListening;
	}

	public void setOnStartPlayListening(OnStartPlayListening onStartPlayListening) {
		this.onStartPlayListening = onStartPlayListening;
	}

	private class DownloadImageTask extends AsyncTask<String, Integer, byte[]> {
		protected void onPostExecute(byte[] buffer) {
			onStartPlayListening.onStartPlay(buffer);
		}

		@Override
		protected byte[] doInBackground(String... params) {
			byte[] buffer =null;
			try {
				URL url = new URL(params[0]);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				connection.setRequestMethod("GET");
				if (connection.getResponseCode() == 200) {
					InputStream is = connection.getInputStream();
					buffer = getByte(is);
				} else {
					Log.e("getResponseCode", connection.getResponseCode() + ":");
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			return buffer;
		}
	}

	public interface OnStartPlayListening {
		void onStartPlay(byte[] buffer);
	}

}
