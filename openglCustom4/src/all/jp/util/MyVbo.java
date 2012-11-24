package all.jp.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

public class MyVbo
{
	public int vCount = 0;			//頂点の数.
	public int vbo;			//頂点バッファID.
	public int ibo;			//インデックスバッファID.
	public int nbo;			//法線バッファID.
	public int tbo;			//テクスチャUV ID.
	public MyVbo()
	{
		
	}
	
	public void draw(GL11 gl)
	{
		this.draw(gl, -1);
	}
	
	public void draw(GL11 gl, int texture)
	{
		//頂点データを設定.
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		
		
		//頂点バッファ.
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, this.vbo);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		
		//法線バッファ.
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, this.nbo);
		gl.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		
		//インデックスバッファ.
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, this.ibo);
		
		//テクスチャ.
		
		if(texture != -1)
		{
	        //テクスチャとUVの指定
			gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);//UVバッファ
			gl.glEnable(GL11.GL_TEXTURE_2D);                    //テクスチャ
	        gl.glBindTexture(GL11.GL_TEXTURE_2D,texture);
	        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER,this.tbo);
	        gl.glTexCoordPointer(2,GL11.GL_FLOAT,0,0);
			
		}
		
		gl.glDrawElements(GL11.GL_TRIANGLES, this.vCount, GL11.GL_UNSIGNED_BYTE, 0);
		
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		
		//bind解除.
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
