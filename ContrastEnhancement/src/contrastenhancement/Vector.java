/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement;

/**
 * Simple class for representing the column vector
 * @author Anna Petráková
 */
public class Vector {
    private double[] matrix;
    private int height;
    /**
     * Used for getting the height of the vector
     * @return height of the vector
     */           
    public int getHeight() {
        return height;
    }
    /**
     * Constructor that creates new vector with specified height
     * @param height height of the requested vector
     */
    public Vector(int height) {
        double[] m = new double[height];
        
        this.matrix = m;
        this.height = height;
    }
    /**
     * Used for retrieving a value from the vector
     * @param y position in vector
     * @return value in vector on position (0,y)
     */
    public double getPixel(int y) {
        if (y >= 0 && y <height) {
            return matrix[y];
        }
        return 0;
        
    }
    /**
     * Used for setting the value in the vector
     * @param y position in vector
     * @param value new value for the vector position
     */
    public void setPixel(int y, double value) {
        if (y >= 0 && y <height) {
            matrix[y] = value;
        }
    }
    
}
