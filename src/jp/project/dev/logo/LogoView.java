package jp.project.dev.logo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import jaghama.sound.R;
/**
 * ロゴ表示View
 * @author TAKA@はままつ
 *
 */
public class LogoView extends SurfaceView implements Callback, Runnable{
	private Bitmap logoBmp = null;
	private int posX = 0;
	private int posY = 0;
	private int waitCnt = 0;
	private int sceneFlg = 0;
	private int alpha = 0;
	
	private static final int SCENE_LOGO_IN = 0;
	private static final int SCENE_LOGO_KEEP = 1;
	private static final int SCENE_LOGO_FADE = 2;
	private OnLogoEnd onLogoEndFunc = null;
	private Thread thread = null;

	/**
	 * 
	 * @param context
	 */
	public LogoView(Context context) {
		super(context);
	}

	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public LogoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public LogoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 
	 */
	public void init() {
		Log.d("LogicLogoView", "init");
		BitmapDrawable bmp = (BitmapDrawable)getContext().getResources().getDrawable(R.drawable.logo);
		logoBmp = bmp.getBitmap();
		waitCnt = 0;
		alpha = 0;
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * 
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	/**
	 * 
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	/**
	 * 
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		SurfaceHolder holder = getHolder();
		Canvas canvas = null;
		while(thread != null){
			canvas = holder.lockCanvas();
			if(canvas == null){
				try {
					Thread.sleep(66);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			// ↓↓ここの処理を変更すればいろんなログのパターンを作れる↓↓
			defaultDraw(canvas);
			// ↑↑ここの処理を変更すればいろんなログのパターンを作れる↑↑

			holder.unlockCanvasAndPost(canvas);
			try {
				Thread.sleep(66);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param canvas
	 */
	private void defaultDraw(Canvas canvas){
		Rect rect = new Rect(0, 0, getWidth(), getHeight());
		posX = (rect.width() - logoBmp.getWidth()) / 2;
		posY = (rect.height() - logoBmp.getHeight()) / 2;
		Log.d("LogoScene:init", "posX = " + Integer.toString(posX) + " / posY = " + Integer.toString(posY));

		Paint paint = null;
		paint = new Paint();
		// 背景クリア
		paint.setColor(Color.WHITE);
		canvas.drawRect(rect, paint);
		switch(sceneFlg){
		case SCENE_LOGO_IN:
			alpha += 15;
			if(alpha >= 255){
				alpha = 255;
				sceneFlg = SCENE_LOGO_KEEP;
			}
			break;
		case SCENE_LOGO_KEEP:
			waitCnt++;
			if(waitCnt >= (1000 / 66) * 1){
				sceneFlg = SCENE_LOGO_FADE;
			}
			break;
		case SCENE_LOGO_FADE:
			alpha -= 15;
			if(alpha <= 0){
				alpha = 0;
				thread = null;
				onLogoEndFunc.onLogoEnd();
			}
			break;
		}
		paint.setColor(Color.BLACK);
		paint.setAlpha(alpha);
		canvas.drawBitmap(logoBmp, posX, posY, paint);
	}

	/**
	 * 
	 * @param onLogoEndFunc
	 */
	public void setOnLogoEndFunc(OnLogoEnd onLogoEndFunc) {
		this.onLogoEndFunc = onLogoEndFunc;
	}
}
