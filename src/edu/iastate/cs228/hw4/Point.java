package edu.iastate.cs228.hw4;

/**
 *  
 * @author Brad Boswell
 *
 */

public class Point 
{
	private int x; 
	private int y;
	
	public Point()  // default constructor
	{
		// x and y get default value 0
		x = 0;
		y = 0;
	}
	
	public Point(int x, int y)
	{
		this.x = x;  
		this.y = y;   
	}
	
	public int getX()   
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || obj.getClass() != this.getClass())
		{
			return false;
		}
    
		Point other = (Point) obj;
		return x == other.x && y == other.y;   
	}
	
	/**
	 * Output a point in the standard form (x, y). 
	 */
	@Override
    public String toString() 
	{
		return "(" + x + ", " + y + ")" ; 
	}
}
