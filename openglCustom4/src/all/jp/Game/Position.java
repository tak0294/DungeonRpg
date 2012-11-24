package all.jp.Game;


public class Position
{
	public int floor;
	public int x,y;
	public int direction;
	
	public Position(int floor, int x, int y, int direction)
	{
		this.floor = floor;
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
	
	public Position(Position org)
	{
		this.floor = org.floor;
		this.x = org.x;
		this.y = org.y;
		this.direction = org.direction;
	}
}
