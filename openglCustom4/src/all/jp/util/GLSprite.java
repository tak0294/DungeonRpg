package all.jp.util;

import javax.microedition.khronos.opengles.GL11;

public class GLSprite
{
	public MyVbo vbo;
	public Vector3D position;
	public Vector3D relativePosition;	//相対的な座標（描画にのみ使用）.
	public float width, height, depth;	//サイズ.
	public float angle;					//角度.
	public float rotationX, rotationY, rotationZ;
	public float alpha;
	public int texture = -1;
	
	//--------------------------------------------------------------------
	//	コンストラクタ.
	//--------------------------------------------------------------------
	public GLSprite()
	{
		position 			= new Vector3D();
		relativePosition	= new Vector3D();
		rotationX = 0.0f;
		rotationY = 0.0f;
		rotationZ = 0.0f;
		width 	= 1.0f;
		height 	= 1.0f;
		depth	= 1.0f;
		angle   = 0.0f;
		alpha	= 1.0f;
	}
	
	public GLSprite(float x, float y, float z, float width, float height, float depth)
	{
		position 			= new Vector3D(x,y,z);
		relativePosition	= new Vector3D();
		rotationX = 0.0f;
		rotationY = 0.0f;
		rotationZ = 0.0f;
		this.width 	= width;
		this.height = height;
		this.depth	= depth;
		angle   = 0.0f;
		alpha	= 1.0f;
		vbo		= new MyVbo();
	}
	
	public void setTexture(int texture)
	{
		this.texture = texture;
	}
	
	public void draw_texture(GL11 gl)
	{
		gl.glPushMatrix();
	    gl.glTranslatef(position.x + relativePosition.x, position.y + relativePosition.y, position.z + relativePosition.z);
	    gl.glRotatef(rotationX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(rotationZ, 0.0f, 0.0f, 1.0f);

	    GraphicUtil.drawTexture(gl, 0.0f, 0.0f, width, height, texture, 1.0f, 1.0f, 1.0f, alpha);
		gl.glPopMatrix();		
	}
	
	public void draw(GL11 gl, int tex)
	{
		gl.glPushMatrix();
	    gl.glTranslatef(position.x + relativePosition.x, position.y + relativePosition.y, position.z + relativePosition.z);
	    gl.glRotatef(rotationX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(rotationZ, 0.0f, 0.0f, 1.0f);
	    gl.glScalef(this.width, this.height, this.depth);
	    
	    vbo.draw(gl, tex);
	    
		gl.glPopMatrix();
	}
}
