package all.jp.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

public class MyVbo
{
	public int vCount = 0;			//���_�̐�.
	public int vbo;			//���_�o�b�t�@ID.
	public int ibo;			//�C���f�b�N�X�o�b�t�@ID.
	public int nbo;			//�@���o�b�t�@ID.
	public int tbo;			//�e�N�X�`��UV ID.
	public MyVbo()
	{
		
	}
	
	public void draw(GL11 gl)
	{
		this.draw(gl, -1);
	}
	
	public void draw(GL11 gl, int texture)
	{
		//���_�f�[�^��ݒ�.
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		
		
		//���_�o�b�t�@.
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, this.vbo);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		
		//�@���o�b�t�@.
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, this.nbo);
		gl.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		
		//�C���f�b�N�X�o�b�t�@.
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, this.ibo);
		
		//�e�N�X�`��.
		
		if(texture != -1)
		{
	        //�e�N�X�`����UV�̎w��
			gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);//UV�o�b�t�@
			gl.glEnable(GL11.GL_TEXTURE_2D);                    //�e�N�X�`��
	        gl.glBindTexture(GL11.GL_TEXTURE_2D,texture);
	        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER,this.tbo);
	        gl.glTexCoordPointer(2,GL11.GL_FLOAT,0,0);
			
		}
		
		gl.glDrawElements(GL11.GL_TRIANGLES, this.vCount, GL11.GL_UNSIGNED_BYTE, 0);
		
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		
		//bind����.
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
