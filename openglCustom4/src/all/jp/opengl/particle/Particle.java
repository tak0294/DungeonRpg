package all.jp.opengl.particle;

public class Particle
{
	public float x;
	public float y;
	public float z;
	public float size;
	public boolean activeFlag;
	public float moveX;		//1フレームあたりの移動量X.
	public float moveY;		//1フレームあたりの移動量Y.
	public float moveZ;		//1フレームあたりの移動量Z.
	public int frame;		//生成されてからのフレーム数.
	public int lifeSpan;	//寿命（フレーム数）.
	
	public boolean isStarted;	//動いているかどうか.
	public int startWait;		//動き始めるまでのウェイト.
	
	public Particle()
	{
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
		size = 1.0f;
		activeFlag = false;
		moveX = 0.0f;
		moveY = 0.0f;
		moveZ = 0.0f;
		frame = 0;
		lifeSpan = 60;
		startWait = 0;
		isStarted = false;
	}
	
	public void draw(int texture)
	{
		
	}
	
	public void update()
	{
		if(startWait > 0)
		{
			startWait--;
			return;
		}

		if(!isStarted && startWait == 0)
			isStarted = true;
		
		frame++;
		
		if(frame > lifeSpan)
			activeFlag = false;
		
		x += moveX;
		y += moveY;
		z += moveZ;
	}
}
