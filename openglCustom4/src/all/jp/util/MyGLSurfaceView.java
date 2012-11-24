package all.jp.util;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class MyGLSurfaceView extends GLSurfaceView {

	//画面サイズ
	private float mWidth;
	private float mHeight;
	
	//MyRendererを保持する
	private MyRenderer mMyRenderer;
 
	public MyGLSurfaceView(Context context) {
		super(context);
		setFocusable(true);//タッチイベントが取得できるようにする
	}
	
	@Override
	public void setRenderer(Renderer renderer) {
		super.setRenderer(renderer);
		this.mMyRenderer = (MyRenderer)renderer;
	}
 
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		this.mWidth = w;
		this.mHeight = h;
	}
 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = (event.getX() / (float)mWidth) * 3.0f - 1.5f;
		float y = (event.getY() / (float)mHeight) * -2.0f + 1.0f;
		
		switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	    		mMyRenderer.touched(event.getX(), event.getY(),x,y);
	        break;
	    case MotionEvent.ACTION_UP:
	    		mMyRenderer.unTouched();
	        break;
	    case MotionEvent.ACTION_MOVE:
	        
	        break;
	    case MotionEvent.ACTION_CANCEL:
	    		mMyRenderer.unTouched();
	        break;
	    }

		
		return true;
	}
}
