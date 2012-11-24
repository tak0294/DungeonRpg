package all.jp.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.opengl.GLUtils;

public class GraphicUtil {
	
	// 配列オブジェクトを保持する
	private static Hashtable<Integer, float[]> verticesPool = new Hashtable<Integer, float[]>();
	private static Hashtable<Integer, float[]> colorsPool = new Hashtable<Integer, float[]>();
	private static Hashtable<Integer, float[]> coordsPool = new Hashtable<Integer, float[]>();
	
	public static float[] getVertices(int n) {
		if (verticesPool.containsKey(n)) {
			return verticesPool.get(n);
		}
		float[] vertices = new float[n];
		verticesPool.put(n, vertices);
		return vertices;
	}
	
	public static float[] getColors(int n) {
		if (colorsPool.containsKey(n)) {
			return colorsPool.get(n);
		}
		float[] colors = new float[n];
		colorsPool.put(n, colors);
		return colors;
	}
	
	public static float[] getCoords(int n) {
		if (coordsPool.containsKey(n)) {
			return coordsPool.get(n);
		}
		float[] coords = new float[n];
		coordsPool.put(n, coords);
		return coords;
	}
	
	// バッファオブジェクトを保持する
	private static Hashtable<Integer, FloatBuffer> squareVerticesPool = new Hashtable<Integer, FloatBuffer>();
	private static Hashtable<Integer, FloatBuffer> squareColorsPool = new Hashtable<Integer, FloatBuffer>();
	private static Hashtable<Integer, FloatBuffer> texCoordsPool = new Hashtable<Integer, FloatBuffer>();
	
	public static final FloatBuffer makeVerticesBuffer(float[] arr) {
		FloatBuffer fb = null;
		if (squareVerticesPool.containsKey(arr.length)) {
			fb = squareVerticesPool.get(arr.length);
			fb.clear();
			fb.put(arr);
			fb.position(0);
			return fb;
		}
		fb = makeFloatBuffer(arr);
		squareVerticesPool.put(arr.length, fb);
		return fb;
	}
	
	public static final FloatBuffer makeColorsBuffer(float[] arr) {
		FloatBuffer fb = null;
		if (squareColorsPool.containsKey(arr.length)) {
			fb = squareColorsPool.get(arr.length);
			fb.clear();
			fb.put(arr);
			fb.position(0);
			return fb;
		}
		fb = makeFloatBuffer(arr);
		squareColorsPool.put(arr.length, fb);
		return fb;
	}
	
	public static final FloatBuffer makeTexCoordsBuffer(float[] arr) {
		FloatBuffer fb = null;
		if (texCoordsPool.containsKey(arr.length)) {
			fb = texCoordsPool.get(arr.length);
			fb.clear();
			fb.put(arr);
			fb.position(0);
			return fb;
		}
		fb = makeFloatBuffer(arr);
		texCoordsPool.put(arr.length, fb);
		return fb;
	}
	
	public static final void drawNumbers(GL10 gl, float x, float y, float w, float h, int texture, int number, int figures, float r, float g, float b, float a) {
		float totalWidth = w * (float)figures;//n文字分の横幅
		float rightX = x + (totalWidth * 0.5f);//右端のx座標
		float fig1X = rightX - w * 0.5f;//一番右の桁の中心のx座標
		for (int i = 0; i < figures; i++) {
			float figNX = fig1X - (float)i * w;//n桁目の中心のx座標
			int numberToDraw = number / (int)Math.pow(10.0, (double)i) % 10;
			drawNumber(gl, figNX, y, w, h, texture, numberToDraw, 1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
	
	public static final void drawNumber(GL10 gl, float x, float y, float w, float h, int texture, int number, float r, float g, float b, float a) {
		float u = (float)(number % 4) * 0.25f;
		float v = (float)(number / 4) * 0.25f;
		drawTexture(gl, x, y, w, h, texture, u, v, 0.25f, 0.25f, r, g, b, a);
	}
	
	public static final void drawTexture(GL10 gl, float x, float y, float w, float h, int texture, float u, float v, float tex_w, float tex_h, float r, float g, float b, float a) {
		float[] vertices = getVertices(8);
		vertices[0] = -0.5f * w + x; vertices[1] = -0.5f * h + y;
		vertices[2] =  0.5f * w + x; vertices[3] = -0.5f * h + y;
		vertices[4] = -0.5f * w + x; vertices[5] =  0.5f * h + y;
		vertices[6] =  0.5f * w + x; vertices[7] =  0.5f * h + y;
		
		float[] colors = getColors(16);
		for (int i = 0; i < 16; i++) {
			colors[i++] = r;
			colors[i++] = g;
			colors[i++] = b;
			colors[i]   = a;
		}
		
		float[] coords = getCoords(8);
		coords[0] = u; coords[1] = v + tex_h;
		coords[2] = u + tex_w; coords[3] = v + tex_h;
		coords[4] = u; coords[5] = v;
		coords[6] = u + tex_w; coords[7] = v;

		FloatBuffer squareVertices = makeVerticesBuffer(vertices);
		FloatBuffer squareColors = makeColorsBuffer(colors);
		FloatBuffer texCoords = makeTexCoordsBuffer(coords);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, squareVertices);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, squareColors);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoords);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		
		
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	public static final void drawTexture(GL10 gl, float x, float y, float w, float h, int texture, float r, float g, float b, float a) {
		drawTexture(gl, x, y, w, h, texture, 0.0f, 0.0f, 1.0f, 1.0f, r, g, b, a);
	}
	
	public static final int loadTexture(GL10 gl, String key)
	{
		int[] textures = new int[1];
		
		Bitmap bmp = GraphicUtil.getBitmap(Global.activity, key);
		
		// OpenGL用のテクスチャを生成します
		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        
        //OpenGLへの転送が完了したので、VMメモリ上に作成したBitmapを破棄する
        bmp.recycle();
        
        //TextureManagerに登録する
        TextureManager.addTexture(key, textures[0]);
        
        return textures[0];
	}
	private static final BitmapFactory.Options options = new BitmapFactory.Options();
	static {
		options.inScaled = false;//リソースの自動リサイズをしない
		options.inPreferredConfig = Config.ARGB_8888;//32bit画像として読み込む
	}
	
	public static final void drawCircle(GL10 gl, Vector3D center, int divides, float radius, float r, float g, float b, float a) {
		drawCircle(gl, center.x, center.y, divides, radius, r, g, b, a);
	}
	
	public static final void drawCircle(GL10 gl, float x, float y, int divides, float radius, float r, float g, float b, float a) {
		float[] vertices = getVertices(divides * 3 * 2);
		
		int vertexId = 0;//頂点配列の要素の番号を記憶しておくための変数
		for (int i = 0; i < divides; i++) {
			//i番目の頂点の角度(ラジアン)を計算します
			float theta1 = 2.0f / (float)divides * (float)i * (float)Math.PI;
			
			//(i + 1)番目の頂点の角度(ラジアン)を計算します
			float theta2 = 2.0f / (float)divides * (float)(i+1) * (float)Math.PI;
			
			//i番目の三角形の0番目の頂点情報をセットします
			vertices[vertexId++] = x;
			vertices[vertexId++] = y;
			
			//i番目の三角形の1番目の頂点の情報をセットします (円周上のi番目の頂点)
			vertices[vertexId++] = (float)Math.cos((double)theta1) * radius + x;//x座標
			vertices[vertexId++] = (float)Math.sin((double)theta1) * radius + y;//y座標
			
			//i番目の三角形の2番目の頂点の情報をセットします (円周上のi+1番目の頂点)
			vertices[vertexId++] = (float)Math.cos((double)theta2) * radius + x;//x座標
			vertices[vertexId++] = (float)Math.sin((double)theta2) * radius + y;//y座標
		}
		FloatBuffer squareVertices = makeVerticesBuffer(vertices);
		
		//ポリゴンの色を指定します
		
		((GL11)gl).glColor4ub((byte)(int)(r*255), (byte)(int)(g*255), (byte)(int)(b*255), (byte)(int)(a*255));
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		

		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, squareVertices);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, divides * 3);
	}
		
	
	public static final void drawRectangle(GL11 gl, float x, float y, float w, float h, float r, float g, float b, float a) {
		float[] vertices = getVertices(8);
		vertices[0] = -0.5f * w + x; vertices[1] = -0.5f * h + y;
		vertices[2] =  0.5f * w + x; vertices[3] = -0.5f * h + y;
		vertices[4] = -0.5f * w + x; vertices[5] =  0.5f * h + y;
		vertices[6] =  0.5f * w + x; vertices[7] =  0.5f * h + y;

		float[] colors = getColors(16);
		for (int i = 0; i < 16; i++) {
			colors[i++] = r;
			colors[i++] = g;
			colors[i++] = b;
			colors[i]   = a;
		}
		
		FloatBuffer squareVertices = makeVerticesBuffer(vertices);
		FloatBuffer squareColors = makeColorsBuffer(colors);
		
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
		gl.glVertexPointer(2, GL11.GL_FLOAT, 0, squareVertices);
		gl.glColorPointer(4, GL11.GL_FLOAT, 0, squareColors);
		
		gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	public static final void drawSquare(GL11 gl, float x, float y, float r, float g, float b, float a) {
		drawRectangle(gl, x, y, 1.0f, 1.0f, r, g, b, a);
	}
	
	public static final void drawSquare(GL11 gl, float r, float g, float b, float a) {
		drawSquare(gl, 0.0f, 0.0f, r, g, b, a);
	}
	
	public static final void drawSquare(GL11 gl) {
		drawSquare(gl, 1.0f, 0, 0, 1.0f);
	}

    //------------------------------------------------------------
    //  球体の頂点バッファ、インデックスバッファを作成.
    //------------------------------------------------------------
    public static void makeSphere(GL11 gl, MyVbo vboObj, int devides)
    {
    	float[] vertices	  = new float[3*32*devides];
    	float[] texCoords	  = new float[3*32*devides];
        byte[]  sphereIndices  = new byte[3 * 32 * devides];
        
        vboObj.vCount = 3 * 32 * devides;
        
        // 頂点データを生成
        int vertexCount = 0;
        int texCount 	= 0;
        for(int i = 0 ; i <= devides ; ++i)
        {
            float v = i / (float)devides;
            float y = (float) Math.cos(Math.PI * v);
            float r = (float) Math.sin(Math.PI * v);
            for(int j = 0 ; j <= 16 ; ++j)
            {
                float u = j / 16.0f;
                vertices[vertexCount++] = (float) (Math.cos(2 * Math.PI * u) * r);
                vertices[vertexCount++] = y;
                vertices[vertexCount++] = (float) (Math.sin(2 * Math.PI * u) * r);
                
                texCoords[texCount++]   = u;
                texCoords[texCount++]   = v;
            }
        }
        
        // インデックスデータを生成
        int indexCount = 0;
        for(byte j = 0 ; j < devides ; ++j)
        {
            byte base = (byte) (j * 17);
            for(byte i = 0 ; i < 16 ; ++i)
            {
            	sphereIndices[indexCount++] = (byte) (base + i);
            	sphereIndices[indexCount++] = (byte) (base + i + 1);
            	sphereIndices[indexCount++] = (byte) (base + i + 17);
            	sphereIndices[indexCount++] = (byte) (base + i + 17);
            	sphereIndices[indexCount++] = (byte) (base + i + 1);
            	sphereIndices[indexCount++] = (byte) (base + i + 1 + 17);
            }
        }
        
        // VBOを作成
        vboObj.vbo = GraphicUtil.makeFloatVBO(gl, vertices);
        vboObj.nbo = GraphicUtil.makeFloatVBO(gl, vertices);
        vboObj.ibo = GraphicUtil.makeByteVBO(gl, sphereIndices);
        vboObj.tbo = GraphicUtil.makeUVBuffer(gl, texCoords);
    }

	
    public static void makeCylinder(GL11 gl, MyVbo vboObj)
    {
        int devides = 15;
        int plusDegree = 360/devides; 
        int degree = 0;
        
        float[] cylinderVertices = new float[devides * 7];
        float[] texCoords	  	 = new float[devides * 5];
        byte[] indecies 		 = new byte[devides * 2 * 6];

        int vertexIndex  = 0;
        int texIndex	 = 0;
        int indeciesIndex = 0;
        
        for(int ii=0;ii<=devides;ii++)
        {
            float radian = (float) (Math.PI / 180 * degree);
            float x = (float) Math.cos(radian);
            float z = (float) Math.sin(radian);
            
            float u = (1.0f / devides) * ii;
            
            cylinderVertices[vertexIndex++] = x;
            cylinderVertices[vertexIndex++] = 0.5f;
            cylinderVertices[vertexIndex++] = z;
            cylinderVertices[vertexIndex++] = x;
            cylinderVertices[vertexIndex++] = -0.5f;
            cylinderVertices[vertexIndex++] = z;
            texCoords[texIndex++] = u;
            texCoords[texIndex++] = 0.0f;
            texCoords[texIndex++] = u;
            texCoords[texIndex++] = 1.0f;
            
            degree = degree + plusDegree;
        }

        for(int ii=0;ii<devides*2;ii+=2)
        {
            indecies[indeciesIndex++] = (byte) (ii+1);
            indecies[indeciesIndex++] = (byte) (ii+2);
            indecies[indeciesIndex++] = (byte) ii;
            
            indecies[indeciesIndex++] = (byte) (ii+1);
            indecies[indeciesIndex++] = (byte) (ii+2);
            indecies[indeciesIndex++] = (byte) (ii+3);
        }
        
        vboObj.vCount = devides*2*6;
        vboObj.vbo = GraphicUtil.makeFloatVBO(gl, cylinderVertices);
        vboObj.nbo = GraphicUtil.makeFloatVBO(gl, cylinderVertices);
        vboObj.ibo = GraphicUtil.makeByteVBO(gl, indecies);
        vboObj.tbo = GraphicUtil.makeUVBuffer(gl, texCoords);
    }

	
    public static void makePyramid(GL11 gl, MyVbo vbo)
    {
        float vertices[] = {
 
            //debug rectangle
 //           -0.25f,  0.25f, 0.0f,
 //           -0.25f, -0.25f, 0.25f,
 //           0.25f,   0.25f, 0.0f,
 //           0.25f,  -0.25f, 0.25f,
            
              0.0f,  0.25f,  0.0f,
            -0.25f, -0.25f,  0.25f,
              0.0f, -0.25f,  0.25f,
             0.25f, -0.25f,  0.25f,
             0.25f, -0.25f,  0.0f,
             0.25f, -0.25f, -0.25f,
              0.0f, -0.25f, -0.25f,
            -0.25f, -0.25f, -0.25f,
            -0.25f, -0.25f,  0.0f,
              0.0f, -0.25f,  0.0f,
        };
        
        float textureCoords[] = {
            
            //debug for rectangle.
//            0.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 0.0f,
//            1.0f, 1.0f,
            
            0.5f, 0.0f,
            0.125f, 1.0f,
            0.250f, 1.0f,
            0.375f, 1.0f,
            0.500f, 1.0f,
            0.625f, 1.0f,
            0.750f, 1.0f,
            0.875f, 1.0f,
            1.000f, 1.0f,


        };
        
        byte indices[] = {  
            //debug for rectangle.
//            1,2,0,
//            1,2,0,
            
            0,1,2,
            0,3,2,
            0,3,4,
            0,5,4,
            0,5,6,
            0,7,6,
            0,1,8,
            0,7,8,
            3,1,7,
            3,5,7,
        };
        
		vbo.vCount = 10;
		vbo.vbo = GraphicUtil.makeFloatVBO(gl, vertices);
		vbo.nbo = GraphicUtil.makeFloatVBO(gl, vertices);
		vbo.ibo = GraphicUtil.makeByteVBO(gl, indices);
		vbo.tbo = GraphicUtil.makeUVBuffer(gl, textureCoords);
        
    }

	
	public static void makeCube(GL11 gl, MyVbo vbo)
	{
		 float vertices[] = {
	                // Front face
	                -1.0f, -1.0f,  1.0f,
	                1.0f, -1.0f,  1.0f,
	                1.0f,  1.0f,  1.0f,
	                -1.0f,  1.0f,  1.0f,
	                
	                // Back face
	                -1.0f, -1.0f, -1.0f,
	                -1.0f,  1.0f, -1.0f,
	                1.0f,  1.0f, -1.0f,
	                1.0f, -1.0f, -1.0f,
	                
	                // Top face
	                -1.0f,  1.0f, -1.0f,
	                -1.0f,  1.0f,  1.0f,
	                1.0f,  1.0f,  1.0f,
	                1.0f,  1.0f, -1.0f,
	                
	                // Bottom face
	                -1.0f, -1.0f, -1.0f,
	                1.0f, -1.0f, -1.0f,
	                1.0f, -1.0f,  1.0f,
	                -1.0f, -1.0f,  1.0f,
	                
	                // Right face
	                1.0f, -1.0f, -1.0f,
	                1.0f,  1.0f, -1.0f,
	                1.0f,  1.0f,  1.0f,
	                1.0f, -1.0f,  1.0f,
	                
	                // Left face
	                -1.0f, -1.0f, -1.0f,
	                -1.0f, -1.0f,  1.0f,
	                -1.0f,  1.0f,  1.0f,
	                -1.0f,  1.0f, -1.0f
		};
		
		float textureCoords[] = {
		                    // Front face
							0.0f, 0.0f,
							1.0f, 0.0f,
							1.0f, 1.0f,
							0.0f, 1.0f,
							
							// Back face
							1.0f, 0.0f,
							1.0f, 1.0f,
							0.0f, 1.0f,
							0.0f, 0.0f,
							
							// Top face
							0.0f, 1.0f,
							0.0f, 0.0f,
							1.0f, 0.0f,
							1.0f, 1.0f,
							
							// Bottom face
							1.0f, 1.0f,
							0.0f, 1.0f,
							0.0f, 0.0f,
							1.0f, 0.0f,
							
							// Right face
							1.0f, 0.0f,
							1.0f, 1.0f,
							0.0f, 1.0f,
							0.0f, 0.0f,
							
							// Left face
							0.0f, 0.0f,
							1.0f, 0.0f,
							1.0f, 1.0f,
							0.0f, 1.0f,
							};

		byte indices[] = {  
                0,  1,  2,      0,  2,  3,    // front  
                4,  5,  6,      4,  6,  7,    // back  
                8,  9,  10,     8,  10, 11,   // top  
                12, 13, 14,     12, 14, 15,   // bottom  
                16, 17, 18,     16, 18, 19,   // right  
                20, 21, 22,     20, 22, 23    // left  
		};

		vbo.vCount = 36;
		vbo.vbo = GraphicUtil.makeFloatVBO(gl, vertices);
		vbo.nbo = GraphicUtil.makeFloatVBO(gl, vertices);
		vbo.ibo = GraphicUtil.makeByteVBO(gl, indices);
		vbo.tbo = GraphicUtil.makeUVBuffer(gl, textureCoords);
	}
	
	
	
	public static void makePlane(GL11 gl, MyVbo vbo)
	{
	    float vertices[] = {
	        -0.5f,0.5f,0.0f,
	        -0.5f,-0.5f,0.0f,
	        0.5f,0.5f,0.0f,
	        0.5f,-0.5f,0.0f,
	     };

	    float normals[] = {
		        -0.5f,0.5f,0.0f,
		        -0.5f,-0.5f,0.0f,
		        0.5f,0.5f,0.0f,
		        0.5f,-0.5f,0.0f,
		     };

	    float div=(float)Math.sqrt((1.0f*1.0f)+(1.0f*1.0f)+(1.0f*1.0f));
	    for (int i=0;i<normals.length;i++) normals[i]/=div;
	    
	    float textureCoords[] = {
	        
	        0.0f,0.0f,
	        0.0f,1.0f,
	        1.0f,0.0f,
	        1.0f,1.0f,
	    };
	    
	    byte indices[] = {  
	        
	        1,  2,  0,      1,  2,  3,    // front  
	    };

		vbo.vCount = 6;
		vbo.vbo = GraphicUtil.makeFloatVBO(gl, vertices);
		vbo.nbo = GraphicUtil.makeFloatVBO(gl, normals);
		vbo.ibo = GraphicUtil.makeByteVBO(gl, indices);
		vbo.tbo = GraphicUtil.makeUVBuffer(gl, textureCoords);
	    
	}
	
	//3次元ベクトルの加算
	public static void Vec3Add(Vector3D c, Vector3D a, Vector3D b)
	{
	    c.x = a.x + b.x;
	    c.y = a.y + b.y;
	    c.z = a.z + b.z;
	}

	//３次元ベクトルの減算.
	public static void Vec3Sub(Vector3D c, Vector3D a, Vector3D b)
	{
	    c.x = a.x - b.x;
	    c.y = a.y - b.y;
	    c.z = a.z - b.z;
	}

	//外積の計算.
	public static void Vec3Cross(Vector3D c, Vector3D a, Vector3D b)
	{
	/*
	 ２つのベクトル
	 　ａ＝（ｘa、ｙa、ｚa）、ｂ＝（ｘb、ｙb、ｚb）
	 に対して、ａとｂの外積ａ×ｂを以下のように定義する。
	 　ａ×ｂ＝（ｙaｚb－ｙbｚa、ｚaｘb－ｚbｘa、ｘaｙb－ｘbｙa）
	 */
	    c.x = (a.y*b.z) - (b.y*a.z);
	    c.y = (a.z*b.x) - (b.z*a.x);
	    c.z = (a.x*b.y) - (b.x*a.y);
	}


	//内積の計算.
	public static float Vec3Dot(Vector3D a, Vector3D b)
	{
	    return a.x*b.x + a.y*b.y + a.z*b.z;
	}

 
	/**
	 * Make a direct NIO FloatBuffer from an array of floats
	 * @param arr The array
	 * @return The newly created FloatBuffer
	 */
	public static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}

	/**
	 * Make a direct NIO IntBuffer from an array of ints
	 * @param arr The array
	 * @return The newly created IntBuffer
	 */
	public static IntBuffer makeFloatBuffer(int[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer ib = bb.asIntBuffer();
		ib.put(arr);
		ib.position(0);
		return ib;
	}

	   //float配列をVBOに変換
    public static int makeFloatVBO(GL11 gl11,float[] array) {
        //float配列をFloatBufferに変換
        FloatBuffer fb=ByteBuffer.allocateDirect(array.length*4).order(
            ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(array).position(0);
        
        //FloatBufferをVBOに変換
        int[] bufferIds=new int[1];
        gl11.glGenBuffers(1,bufferIds,0);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER,bufferIds[0]);
        gl11.glBufferData(GL11.GL_ARRAY_BUFFER,
            fb.capacity()*4,fb,GL11.GL_STATIC_DRAW);
        return bufferIds[0];
    }
    
    //byte配列をVBOに変換
    public static int makeByteVBO(GL11 gl11,byte[] array) {
        //byte配列をByteBufferに変換
        ByteBuffer bb=ByteBuffer.allocateDirect(array.length).order(
            ByteOrder.nativeOrder());
        bb.put(array).position(0);
        
        //ByteBufferをVBOに変換
        int[] bufferIds=new int[1];
        gl11.glGenBuffers(1,bufferIds,0);
        gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER,bufferIds[0]);
        gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER,
            bb.capacity(),bb,GL11.GL_STATIC_DRAW);
        return bufferIds[0];
    }
	
    //UVバッファの生成
    public static int makeUVBuffer(GL11 gl11, float[] uvs) {
        FloatBuffer fb=makeFloatBuffer(uvs);
        
        //VOBによるUVバッファの設定
        int[] uvBufferIds=new int[1];
        gl11.glGenBuffers(1,uvBufferIds,0);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER,uvBufferIds[0]);
        gl11.glBufferData(GL11.GL_ARRAY_BUFFER,     
            fb.capacity()*4,fb,GL11.GL_STATIC_DRAW);
        return uvBufferIds[0];
    }

	public static Bitmap getBitmap(Activity activity, String key)
	{
		return BitmapFactory.decodeResource(activity.getResources(), activity.getResources().getIdentifier(key, "drawable", activity.getPackageName()));
	}

}
