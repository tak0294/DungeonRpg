package all.jp.Game.Dungeon;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.opengl.GLUtils;

public class MiniMap
{
	 private static final int FONT_MAX=256;
	 
	 public class TextureStruct{
		 int width;
		 int height;
		 int id;
		 Bitmap image;
	 };
	 
	 private TextureStruct m_texture;
	 private Canvas m_canvas;
	 private Paint m_paint;

	 //メモリを確保する
	 public void createTextBuffer(GL10 gl)
	 {	
		 	//一枚の巨大なテクスチャを定義する
	 		m_texture=new TextureStruct();
	 		m_texture.width=128;
	 		m_texture.height=128;
			m_texture.image = Bitmap.createBitmap(m_texture.width, m_texture.height, Config.ARGB_8888);  
			m_texture.id=0;
			
			//BMPへの描画コンテキストを取得する
			m_canvas=new Canvas(m_texture.image);
			m_paint =new Paint();
			
			//フォント定義
		 	m_paint.setAntiAlias(false);
		 	m_paint.setARGB(0xff, 0xff, 0xff, 0xff);

		 	//テクスチャを生成する
		 	alocTexture(gl);
	 }
	 
	 private void alocTexture(GL10 gl)
	 {
			//テクスチャを生成する
		 	int textures[]=new int[1];
		 	gl.glGenTextures(1, textures, 0);
		 	gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		 	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, m_texture.image, 0);
			m_texture.id=textures[0];
	 }

	 private void releaseTexture(GL10 gl)
	 {
		 	if(m_texture.id!=0){
		 		int []id=new int[1];
		 		id[0]=m_texture.id;
		 		gl.glDeleteTextures(1, id, 0);
		 		m_texture.id=0;
		 	}
	 }
	 
	 //テクスチャを復元する
	 public void onResume(GL10 gl)
	 {
		 releaseTexture(gl);
		 alocTexture(gl);		 
	 }

	 //メモリを開放する
	 public void onDestroy(GL10 gl)
	 {
		 releaseTexture(gl);
	 }

	 //1フレームで必要な文字を全て先行して描画しておく
	 public void preDrawBegin()
	 {
	 	//とりあえず初期化しておく
	 	m_texture.image.eraseColor(0);
	 }
	 
	 public void resetPreDrawCount()
	 {
	 }
	 
	 public void drawMap(GL10 gl, int[][][] view)
	 {	
		 m_paint.setAlpha(170);
		 for(int ii=0;ii<7;ii++)
		 {
			 m_canvas.drawLine(0.0f, ii*16.0f, 80.0f, ii*16.0f, m_paint);
			 m_canvas.drawLine(ii*16.0f, 0.0f, ii*16.0f, 96.0f, m_paint);
		 }
		 m_paint.setAlpha(255);
		 
		 m_paint.setStrokeWidth(2.0f);
		 m_paint.setColor(Color.WHITE);
		 
		 for(int ii=0;ii<6;ii++)
		 {
			 for(int jj=0;jj<5;jj++)
			 {
				 if(view[0][ii][jj] != 0)
				 {
					 m_canvas.drawLine(jj*16.0f, ii*16.0f, jj*16.0f, ii*16.0f+16.0f, m_paint);
				 }
				 
				 if(view[1][ii][jj] != 0)
				 {
					 m_canvas.drawLine(jj*16.0f, ii*16.0f+16.0f, jj*16.0f+16.0f, ii*16.0f+16.0f, m_paint);
				 }
			 }
		 }
		 m_paint.setColor(Color.WHITE);
		 m_paint.setStrokeWidth(1.0f);
	 }

	 public void preDrawEnd(GL10 gl)
	 {
		 //テクスチャを更新する
		 int textures[]=new int[1];
		 textures[0]=m_texture.id;
		 gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);		 
//		 long start = System.currentTimeMillis();
		 GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, m_texture.image);
//		 long end = System.currentTimeMillis();
//		 Log.i("",""+(end-start)+"ms");
	 }

	 //2パス目に順番に文字を取得してUV座標を変更しながら文字を描画する
	 public int getTexture(){
	 	return m_texture.id;
	 }
}
