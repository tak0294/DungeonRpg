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
	//	コンストラクタ.
	//--------------------------------------------------------------
	public ParticleSystem(int capacity, int lifeSpan)
	{
		this.capacity = capacity;
		
		//パーティクル管理配列.
		this.particle = new Particle[capacity];
		
		//パーティクル管理配列を初期化.
		for(int ii=0;ii<capacity;ii++)
		{
			particle[ii] = new Particle();
			particle[ii].lifeSpan = lifeSpan;
		}
	}
	

	//----------------------------------------------
	// 四方八方に飛び散るパーティクルを追加する
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
	// 指定座標にパーティクルを追加する
	//----------------------------------------------
	public void addParticle(float x, float y, float z, float size, float moveX, float moveY, float moveZ, int startWait)
	{
		this.add(x, y, z, size, moveX, moveY, moveZ, startWait);
	}

	
	//--------------------------------------------------------------
	//	パーティクルの追加.
	//--------------------------------------------------------------
	public void add(float x, float y, float z, float size, float moveX, float moveY, float moveZ, int startWait)
	{
	    //状態がアクティブでないパーティクルを探す.
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
	    //頂点の配列.
	    //１つのパーティクルあたり６頂点ｘ２要素（ｘ，ｙ）ｘ最大のパーティクル数.
	    float[] vertices = new float[6 * 3 * capacity];
	    
	    //色の配列.
	    //１つのパーティクルあたり６頂点ｘ４要素（ｒ，ｇ，ｂ，a）ｘ最大のパーティクル数.
	    float[] colors = new float[6 * 4 * capacity];
	    
	    //テクスチャマッピングの配列.
	    //１つのパーティクルあたり６頂点ｘ２要素（ｘ，ｙ）ｘ最大のパーティクル数.
	    float[] texCoords = new float[6 * 2 * capacity];
	    
	    //アクティブなパーティクルのカウント.
	    int vertexIndex     = 0;
	    int colorIndex      = 0;
	    int texCoordsIndex  = 0;
	    
	    int activeParticleCount = 0;
	    
	    for(int ii=0;ii<capacity;ii++)
	    {
	        //状態がアクティブなパーティクルのみ描画する.
	        if(particle[ii].activeFlag == true && particle[ii].isStarted)
	        {
	            //頂点座標を追加.
	            float centerX   = particle[ii].x;
	            float centerY   = particle[ii].y;
	            float centerZ   = particle[ii].z;
	            float size      = particle[ii].size;
	            float vLeft     = -0.5f * size + centerX;
	            float vRight    =  0.5f * size + centerX;
	            float vTop      =  0.5f * size + centerY;
	            float vBottom   = -0.5f * size + centerY;
	            
	            //ポリゴン１.
	            vertices[vertexIndex++] = vLeft;
	            vertices[vertexIndex++] = vTop;     //左上.
	            vertices[vertexIndex++] = centerZ;
	            vertices[vertexIndex++] = vRight;
	            vertices[vertexIndex++] = vTop;     //右上.
	            vertices[vertexIndex++] = centerZ;
	            vertices[vertexIndex++] = vLeft;
	            vertices[vertexIndex++] = vBottom;  //左下.
	            vertices[vertexIndex++] = centerZ;
	            
	            //ポリゴン２.
	            vertices[vertexIndex++] = vRight;
	            vertices[vertexIndex++] = vTop;     //右上.
	            vertices[vertexIndex++] = centerZ;            
	            vertices[vertexIndex++] = vLeft;
	            vertices[vertexIndex++] = vBottom;  //左下.
	            vertices[vertexIndex++] = centerZ;            
	            vertices[vertexIndex++] = vRight;
	            vertices[vertexIndex++] = vBottom;  //右下.
	            vertices[vertexIndex++] = centerZ;            
	            
	            //色.
	            //現在のフレーム数と寿命からalphaを計算.
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
	            
	            //マッピング座標.
	            //ポリゴン１.
	            texCoords[texCoordsIndex++] = 0.0f;
	            texCoords[texCoordsIndex++] = 0.0f; //左上.
	            texCoords[texCoordsIndex++] = 1.0f;
	            texCoords[texCoordsIndex++] = 0.0f; //右上.
	            texCoords[texCoordsIndex++] = 0.0f;
	            texCoords[texCoordsIndex++] = 1.0f;
	            
	            //ポリゴン２.
	            texCoords[texCoordsIndex++] = 1.0f;
	            texCoords[texCoordsIndex++] = 0.0f; //右上.
	            texCoords[texCoordsIndex++] = 0.0f;
	            texCoords[texCoordsIndex++] = 1.0f; //左下.
	            texCoords[texCoordsIndex++] = 1.0f;
	            texCoords[texCoordsIndex++] = 1.0f; //右下.
	            
	            //アクティブパーティクルの数を増やす.
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
