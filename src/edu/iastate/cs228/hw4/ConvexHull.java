package edu.iastate.cs228.hw4;

/**
 *  
 * @author Brad Boswell
 *
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException; 
import java.util.InputMismatchException; 
import java.io.PrintWriter;
import java.util.Random; 
import java.util.Scanner;

/**
 * 
 * This class implements Graham's scan that constructs the convex hull of a finite set of points. 
 *
 */

public class ConvexHull 
{
	// ---------------
	// Data Structures 
	// ---------------
	
	/**
	 * The array points[] holds an input set of Points, which may be randomly generated or 
	 * input from a file.  Duplicates may appear. 
	 */
	private Point[] points;    
	private int numPoints;            // size of points[]
	

	/**
	 * Lowest point from points[]; and in case of a tie, the leftmost one of all such points. 
	 * To be set by the private method lowestPoint(). 
	 */
	private Point lowestPoint; 

	
	/**
	 * This array stores the same set of points from points[] with all duplicates removed. 
	 */
	private Point[] pointsNoDuplicate; 
	private int numDistinctPoints;    // size of pointsNoDuplicate[]

	
	/**
	 * Points on which Graham's scan is performed.  They are copied from pointsNoDuplicate[] 
	 * with some points removed.  More specifically, if multiple points from the array 
	 * pointsNoDuplicate[] have the same polar angle with respect to lowestPoint, only the one 
	 * furthest away from lowestPoint is included. 
	 */
	private Point[] pointsToScan; 
	private int numPointsToScan;     // size of pointsToScan[]
	
	
	/**
	 * Vertices of the convex hull in counterclockwise order are stored in the array 
	 * hullVertices[], with hullVertices[0] storing lowestPoint. 
	 */
	private Point[] hullVertices;
	private int numHullVertices;     // number of vertices on the convex hull
	
	
	/**
	 * Stack used by Grahma's scan to store the vertices of the convex hull of the points 
	 * scanned so far.  At the end of the scan, it stores the hull vertices in the 
	 * counterclockwise order. 
	 */
	private PureStack<Point> vertexStack;  

	

	// ------------
	// Constructors
	// ------------
	
	
	/**
	 * Generate n random points within the box range [-50, 50] x [-50, 50]. Duplicates are 
	 * allowed. Store the points in the private array points[]. 
	 * 
	 * @param n >= 1; otherwise, exception thrown.  
	 */
	public ConvexHull(int n) throws IllegalArgumentException 
	{
		points = new Point[n];
		if(n < 0){
			throw new IllegalArgumentException();
		}
		Random rand = new Random();
		for(int i = 0; i < n; i += 1){
			points[i] = new Point(rand.nextInt(101) - 50, rand.nextInt(101) - 50);
		}
		numPoints = points.length;
	}
	
	/**
	 * Read integers from an input file.  Every pair of integers represent the x- and y-coordinates 
	 * of a point.  Generate the points and store them in the private array points[]. The total 
	 * number of integers in the file must be even.
	 * 
	 * You may declare a Scanner object and call its methods such as hasNext(), hasNextInt() 
	 * and nextInt(). An ArrayList may be used to store the input integers as they are read in 
	 * from the file.  
	 * 
	 * @param  inputFileName
	 * @throws FileNotFoundException
	 * @throws InputMismatchException   when the input file contains an odd number of integers
	 */
	public ConvexHull(String inputFileName) throws FileNotFoundException, InputMismatchException
	{
		File f = new File(inputFileName);
		Scanner s = new Scanner(f);
		ArrayList<Integer> a = new ArrayList<Integer>();
		while(s.hasNext()){
			a.add(s.nextInt());
		}
		points = new Point[a.size() / 2];
		int t = 0;
		for(int i = 0; i < a.size() / 2; i += 1){
			points[i] = new Point(a.get(t), a.get(t + 1));
			t += 2;
		}
		s.close();
		numPoints = points.length;
	}

	
	
	// -------------
	// Graham's scan
	// -------------
	
	/**
	 * This method carries out Graham's scan in several steps below: 
	 * 
	 *     1) Call the private method lowestPoint() to find the lowest point from the input 
	 *        point set and store it in the variable lowestPoint. 
	 *        
	 *     2) Call the private method setUpScan() to sort all points by polar angle with respect
	 *        to lowestPoint.  After elimination of all duplicates in points[], the points are 
	 *        stored in pointsNoDuplicate[].  Next, for multiple points having the same polar angle 
	 *        with respect to lowestPoint, keep only the one furthest from lowestPoint.  The points
	 *        after the second round of elimination are stored in the array pointsToScan[].  
	 *        
	 *     3) Perform Graham's scan on the points in the array pointsToScan[]. To initialize the 
	 *        scan, push pointsToScan[0] and pointsToScan[1] onto vertexStack.  
	 * 
     *     4) As the scan terminates, vertexStack holds the vertices of the convex hull.  Pop the 
     *        vertices out of the stack and add them to the array hullVertices[], starting at index
     *        numHullVertices - 1, and decreasing the index toward 0.  Set numHullVertices.  
     *        
     * Two special cases below must be handled: 
     * 
     *     1) The array pointsToScan[] could contain just one point, in which case the convex
     *        hull is the point itself. 
     *     
     *     2) Or it could contain two points, in which case the hull is the line segment 
     *        connecting them.   
	 */
	public void GrahamScan()
	{
		if(numPoints == 1){
			hullVertices = new Point[1];
			hullVertices[0] = points[0];
		} else if(numPoints == 2){
			hullVertices = new Point[2];
			hullVertices[0] = points[0];
			hullVertices[1] = points[1];
		} else {
		lowestPoint();
		setUpScan();
		vertexStack = new ArrayBasedStack<Point>();
		vertexStack.push(pointsToScan[0]);
		vertexStack.push(pointsToScan[1]);
		vertexStack.push(pointsToScan[2]);
		boolean r = true;
		for(int i = 3; i < pointsToScan.length; i += 1){
			Point topS = vertexStack.pop();
			r = true;
			while(r){
				PointComparator comp = new PointComparator(vertexStack.peek());
				if(comp.comparePolarAngle(topS, pointsToScan[i]) >= 0){
					topS = vertexStack.pop();
				} else {
					r = false;
				}
			}
			vertexStack.push(topS);
			vertexStack.push(pointsToScan[i]);
		}
		hullVertices = new Point[vertexStack.size()];
		for(int i = hullVertices.length - 1; i > -1; i -= 1){
			hullVertices[i] = vertexStack.pop();
		}
		numHullVertices = hullVertices.length;
		}
	}

	
	
	// ------------------------------------------------------------
	// toString() and Files for Convex Hull Plotting in Mathematica
	// ------------------------------------------------------------
	
	/**
	 * The string displays the convex hull with vertices in counter clockwise order starting at  
	 * lowestPoint.  When printed out, it will list five points per line with three blanks in 
	 * between. Every point appears in the format "(x, y)".  
	 * 
	 * For illustration, the convex hull example in the project description will have its 
	 * toString() generate the output below: 
	 * 
	 * (-7, -10)   (0, -10)   (10, 5)   (0, 8)   (-10, 0)   
	 * 
	 * lowestPoint is listed only ONCE.  
	 */
	public String toString()
	{
		String toS = "";
		for(int i = 1; i < hullVertices.length; i += 1){
			toS = toS.concat(hullVertices[i - 1].toString() + "   ");
			if(i % 5 == 0){
				toS = toS.concat("\n");
			}
		}
		System.out.println(toS);
		return toS;
	}
	
	
	/** 
	 * For plotting in Mathematica. 
	 * 
	 * Writes to the file "hull.txt" the vertices of the constructed convex hull in counterclockwise 
	 * order for rendering in Mathematica.  These vertices are in the array hullVertices[], starting 
	 * at lowestPoint.  Every line in the file displays the x and y coordinates of only one point.  
	 * Write the coordinates of lowestPoint again to end the file.   
	 * 
	 * For instance, the file "hull.txt" generated for the convex hull example in the project 
	 * description will have the following content: 
	 * 
     *  -7 -10 
     *  0 -10
     *  10 5
     *  0  8
     *  -10 0
     *  -7 -10 
	 * 
	 * Note that lowestPoint (-7, -10) has its coordinates listed in the first and last lines.  This 
	 * is for Mathematica to plot the hull as a polygon rather than one missing the edge connecting
	 * (-10, 0) and (-7, -10).  
	 * 
	 * Called only after GrahamScan().  
	 * 
	 * 
	 * @throws IllegalStateException  if hullVertices[] has not been populated (i.e., the convex 
	 *                                   hull has not been constructed)
	 */
	public void hullToFile() throws IllegalStateException 
	{
		String toS = "";
		for(int i = 0; i < hullVertices.length; i += 1){
			toS = toS.concat(hullVertices[i].getX() + "" + " " + hullVertices[i].getY() + "" + "\n");
		}
		toS = toS.concat(lowestPoint.getX() + "" + "  "+ lowestPoint.getY() + "" + "\n");
		File f = new File("hull.txt");
		try {
			PrintWriter p = new PrintWriter(f);
			p.print(toS);
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * For plotting in Mathematica. 
	 * 
	 * Writes to the file "points.txt" the points stored in the array pointsNoDuplicate[]. The 
	 * format is the same as required for the method hullToFile(), except that the coordinates 
	 * of lowestPoint appear only once.
	 * 
	 * Called only after setUpScan() or GrahamScan(). 
	 * 
	 * @throws IllegalStateException  if pointsNoDuplicate[] has not been populated. 
	 */
	public void pointsToFile() throws IllegalStateException 
	{
		String toS = "";
		for(int i = 0; i < pointsNoDuplicate.length; i += 1){
			toS = toS.concat(pointsNoDuplicate[i].getX() + "" + " " + pointsNoDuplicate[i].getY() + "" + "\n");
		}
		File f = new File("points.txt");
		try {
			PrintWriter p = new PrintWriter(f);
			p.print(toS);
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Also implement this method for testing purpose. 
	 * 
	 * Writes to the file "pointsScanned.txt" the points stored in the array pointsToScan[]. The 
	 * format is the same as required for the method pointsToFile(). 
	 * 
	 * Called only after setUpScan() or GrahamScan().  
	 * 
	 * @throws IllegalStateException  if pointsToScan[] has not been populated. 
	 */
	public void pointsScannedToFile() throws IllegalStateException 
	{
		String toS = "";
		for(int i = 0; i < pointsToScan.length; i += 1){
			toS = toS.concat(pointsToScan[i].getX() + "" + " " + pointsToScan[i].getY() + "" + "\n");
		}
		File f = new File("pointsScanned.txt");
		try {
			PrintWriter p = new PrintWriter(f);
			p.print(toS);
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	// ---------------
	// Private Methods
	// ---------------
	
	/**
	 * Find the point in the array points[] that has the smallest y-coordinate.  In case of a tie, 
	 * pick the point with the smallest x-coordinate. Set the variable lowestPoint to the found 
	 * point. z
	 * Multiple elements from points[] could coincide at the lowestPoint (i.e., they are the same 
	 * point).  This situation could happen, though with a very small chance.  In this case, any of 
	 * them can be lowestPoint. 
	 * 
	 * Ought to be private, but is made public for testing convenience. 
	 */
	public void lowestPoint()
	{
		lowestPoint = points[0];
		for(int i = 1; i < points.length; i += 1){
			if(points[i].getY() < lowestPoint.getY()){
				lowestPoint = points[i];
			}
			if(points[i].getY() == lowestPoint.getY()){
				if(points[i].getX() < lowestPoint.getX()){
					lowestPoint = points[i];
				}
			}
		}
	}
	
	
	/**
	 * Call quickSort() on points[].  After the sorting, duplicates in points[] will appear next 
	 * to each other, with those equal to lowestPoint at the beginning of the array.  
	 * 
	 * Copy the points from points[] into the array pointsNoDuplicate[], eliminating all duplicates. 
	 * Update numDistinctPoints. 
	 * 
	 * Copy the points from pointsNoDuplicate[] into the array pointsToScan[] and eliminate some 
	 * as follows.  If multiple points have the same polar angle, eliminate all but the one that is 
	 * the furthest from lowestPoint.  Update numPointsToScan. 
	 * 
	 * Ought to be private, but is made public for testing convenience. 
	 *
	 */
	public void setUpScan()
	{
		quickSort();
		numDistinctPoints = 1;
		for(int i = 1; i < points.length; i += 1){
			numDistinctPoints += 1;
			if(points[i - 1].equals(points[i])){
				numDistinctPoints -= 1;
			}
		}
		int place = 1;
		pointsNoDuplicate = new Point[numDistinctPoints];
		pointsNoDuplicate[0] = points[0];
 		for(int i = 1; i < points.length; i += 1){
			if(!(points[i - 1].equals(points[i]))){
				pointsNoDuplicate[place] = points[i];
				place += 1;
			}
		}
		numPointsToScan = 1;
		PointComparator comp = new PointComparator(lowestPoint);
		for(int i = 1; i < pointsNoDuplicate.length; i += 1){
			numPointsToScan += 1;
			if(comp.comparePolarAngle(pointsNoDuplicate[i - 1], pointsNoDuplicate[i]) == 0){
				numPointsToScan -= 1;
			}
		}
		place = 1;
		pointsToScan = new Point[numPointsToScan];
		pointsToScan[0] = pointsNoDuplicate[0];
		for(int i = 1; i < pointsNoDuplicate.length; i += 1){
			if(!(comp.comparePolarAngle(pointsNoDuplicate[i - 1], pointsNoDuplicate[i]) == 0)){
				pointsToScan[place] = pointsNoDuplicate[i];
				place += 1;
			}
		}
	}

	
	/**
	 * Sort the array points[] in the increasing order of polar angle with respect to lowestPoint.  
	 * Use quickSort.  Construct an object of the pointComparator class with lowestPoint as the 
	 * argument for point comparison.  
	 * 
	 * Ought to be private, but is made public for testing convenience.   
	 */
	public void quickSort()
	{
		quickSortRec(0, points.length - 1);
	}
	
	
	/**
	 * Operates on the subarray of points[] with indices between first and last. 
	 * 
	 * @param first  starting index of the subarray
	 * @param last   ending index of the subarray
	 */
	private void quickSortRec(int first, int last)
	{
		if(first >= last){
			return;
		}
		int p = partition(first, last);
		quickSortRec(first, p - 1);
		quickSortRec(p + 1, last);
	}
	
	
	/**
	 * Operates on the subarray of points[] with indices between first and last.
	 * 
	 * @param first
	 * @param last
	 * @return
	 */
	private int partition(int first, int last)
	{
		PointComparator pC = new PointComparator(lowestPoint);
		Point pivot = points[last];
		int i = first - 1;
		Point temp = null;
		for(int j = first; j < last; j += 1){
			if(pC.compare(points[j], pivot) <= 0){
				i += 1;
				temp = points[i];
				points[i] = points[j];
				points[j] = temp;
			}
		}
		temp = points[i + 1];
		points[i + 1] = points[last];
		points[last] = temp;
		return i + 1;
	}	
			
}
