package all.jp.opengl.particle;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import all.jp.util.Global;
import all.jp.util.GraphicUtil;

public class ParticleSystem
{
	public Particle[] particle;
	public int capacity;
	
	//--------------------------------------------------------------
	//	�R���X�g���N�^.
	//--------------------------------------------------------------
	public ParticleSystem(int capacity, int lifeSpan)
	{
		this.capacity = capacity;
		
		//�p�[�e�B�N���Ǘ��z��.
		this.particle = new Particle[capacity];
		
		//�p�[�e�B�N���Ǘ��z���������.
		for(int ii=0;ii<capacity;ii++)
		{
			particle[ii] = new Particle();
			particle[ii].lifeSpan = lifeSpan;
		}
	}
	

	//----------------------------------------------
	// �l�������ɔ�юU��p�[�e�B�N����ǉ�����
	//----------------------------------------------
	public void addSparks(float x, float y, float z, float size, float maxSpeed, int num, int startWait)
	{
		Random rand = Global.rand;
		for (int i = 0; i < num; i++) {
			float moveX = (rand.nextFloat() * 2.0f - 1.0f) * maxSpeed;
			float moveY = (rand.nextFloat() * 2.0f - 1.0f) * maxSpeed;
			float moveZ = (rand.nextFloat() * 2.0f - 1.0f) * maxSpeed;
			this.add(x, y, z, size, moveX, moveY, moveZ,startWait);
		}
	}


	//----------------------------------------------
	// �w����W�Ƀp�[�e�B�N����ǉ�����
	//----------------------------------------------
	public void addParticle(float x, float y, float z, float size, float moveX, float moveY, float moveZ, int startWait)
	{
		this.add(x, y, z, size, moveX, moveY, moveZ, startWait);
	}

	
	//--------------------------------------------------------------
	//	�p�[�e�B�N���̒ǉ�.
	//--------------------------------------------------------------
	public void add(float x, float y, float z, float size, float moveX, float moveY, float moveZ, int startWait)
	{
	    //��Ԃ��A�N�e�B�u�łȂ��p�[�e�B�N����T��.
	    for(int ii=0;ii<capacity;ii++)
	    {
	        if(particle[ii].activeFlag == false)
	        {
	            particle[ii].activeFlag = true;
	            particle[ii].x = x;
	            particle[ii].y = y;
	            particle[ii].z = z;
	            particle[ii].size = size;
	            particle[ii].moveX = moveX;
	            particle[ii].moveY = moveY;
	            particle[ii].moveZ = moveZ;
	            particle[ii].frame = 0;
	            particle[ii].startWait = startWait;
	            break;
	        }
	    }
	}
	
	
	public void draw(GL11 gl, int texture)
	{
	    //���_�̔z��.
	    //�P�̃p�[�e�B�N��������U���_���Q�v�f�i���C���j���ő�̃p�[�e�B�N����.
	    float[] vertices = new float[6 * 3 * capacity];
	    
	    //�F�̔z��.
	    //�P�̃p�[�e�B�N��������U���_���S�v�f�i���C���C���Ca�j���ő�̃p�[�e�B�N����.
	    float[] colors = new float[6 * 4 * capacity];
	    
	    //�e�N�X�`���}�b�s���O�̔z��.
	    //�P�̃p�[�e�B�N��������U���_���Q�v�f�i���C���j���ő�̃p�[�e�B�N����.
	    float[] texCoords = new float[6 * 2 * capacity];
	    
	    //�A�N�e�B�u�ȃp�[�e�B�N���̃J�E���g.
	    int vertexIndex     = 0;
	    int colorIndex      = 0;
	    int texCoordsIndex  = 0;
	    
	    int activeParticleCount = 0;
	    
	    for(int ii=0;ii<capacity;ii++)
	    {
	        //��Ԃ��A�N�e�B�u�ȃp�[�e�B�N���̂ݕ`�悷��.
	        if(particle[ii].activeFlag == true && particle[ii].isStarted)
	        {
	            //���_���W��ǉ�.
	            float centerX   = particle[ii].x;
	            float centerY   = particle[ii].y;
	            float centerZ   = particle[ii].z;
	            float size      = particle[ii].size;
	            float vLeft     = -0.5f * size + centerX;
	            float vRight    =  0.5f * size + centerX;
	            float vTop      =  0.5f * size + centerY;
	            float vBottom   = -0.5f * size + centerY;
	            
	            //�|���S���P.
	            vertices[vertexIndex++] = vLeft;
	            vertices[vertexIndex++] = vTop;     //����.
	            vertices[vertexIndex++] = centerZ;
	            vertices[vertexIndex++] = vRight;
	            vertices[vertexIndex++] = vTop;     //�E��.
	            vertices[vertexIndex++] = centerZ;
	            vertices[vertexIndex++] = vLeft;
	            vertices[vertexIndex++] = vBottom;  //����.
	            vertices[vertexIndex++] = centerZ;
	            
	            //�|���S���Q.
	            vertices[vertexIndex++] = vRight;
	            vertices[vertexIndex++] = vTop;     //�E��.
	            vertices[vertexIndex++] = centerZ;            
	            vertices[vertexIndex++] = vLeft;
	            vertices[vertexIndex++] = vBottom;  //����.
	            vertices[vertexIndex++] = centerZ;            
	            vertices[vertexIndex++] = vRight;
	            vertices[vertexIndex++] = vBottom;  //�E��.
	            vertices[vertexIndex++] = centerZ;            
	            
	            //�F.
	            //���݂̃t���[�����Ǝ�������alpha���v�Z.
	            float lifePercentage = (float)particle[ii].frame / (float)particle[ii].lifeSpan;
	            float alpha;
	            if(lifePercentage <= 0.5f)
	                alpha = lifePercentage * 2.0f;
	            else
	                alpha = 1.0f - lifePercentage;

	            
	            for(int jj=0;jj<6;jj++)
	            {
	                colors[colorIndex++] = 1.0f;
	                colors[colorIndex++] = 1.0f;
	                colors[colorIndex++] = 1.0f;
	                colors[colorIndex++] = alpha;
	            }
	            
	            //�}�b�s���O���W.
	            //�|���S���P.
	            texCoords[texCoordsIndex++] = 0.0f;
	            texCoords[texCoordsIndex++] = 0.0f; //����.
	            texCoords[texCoordsIndex++] = 1.0f;
	            texCoords[texCoordsIndex++] = 0.0f; //�E��.
	            texCoords[texCoordsIndex++] = 0.0f;
	            texCoords[texCoordsIndex++] = 1.0f;
	            
	            //�|���S���Q.
	            texCoords[texCoordsIndex++] = 1.0f;
	            texCoords[texCoordsIndex++] = 0.0f; //�E��.
	            texCoords[texCoordsIndex++] = 0.0f;
	            texCoords[texCoordsIndex++] = 1.0f; //����.
	            texCoords[texCoordsIndex++] = 1.0f;
	            texCoords[texCoordsIndex++] = 1.0f; //�E��.
	            
	            //�A�N�e�B�u�p�[�e�B�N���̐��𑝂₷.
	            activeParticleCount++;
	        }
	    }
	    
		FloatBuffer verticesBuffer = GraphicUtil.makeVerticesBuffer(vertices);
		FloatBuffer colorBuffer = GraphicUtil.makeColorsBuffer(colors);
		FloatBuffer coordBuffer = GraphicUtil.makeTexCoordsBuffer(texCoords);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, coordBuffer);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, activeParticleCount * 6);
		
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);

	}

	public void update()
	{
		for(int ii=0;ii<capacity;ii++)
		{
			if(particle[ii].activeFlag == true)
			{
				particle[ii].update();
			}
		}
	}
}
