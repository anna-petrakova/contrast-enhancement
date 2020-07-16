/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement;

/**
 * Class that represents image 
 * @author Anna Petráková
 */
public class Image2D {
    private double[] data;
    private int width;
    private int height;
    
    public Image2D() {
        
    }
    /**
     * Creates image
     * @param width pixel width of the requested image
     * @param height pixel height of the requested image
     */
    public Image2D(int width, int height) {
        double[] newData = new double[width*height];
        
        this.width = width;
        this.height = height;
        this.data = newData;
    }
    /**
     * Creates image with defined data
     * @param width pixel width of the requested image
     * @param height pixel height of the requested image
     * @param data values in pixels
     */
    public Image2D(int width, int height, double[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }
    /**
     * Used for getting the value from pixel
     * @param x position on x axis of the requested pixel
     * @param y position on y axis of the requested pixel 
     * @return value on position (x,y) in image
     */    
    public double getPixel(int x, int y) {
        if (y >= 0 && y <height && x >= 0 && x < width) {
            return data[y*width+x];
        }
        return 0;
        
    }
    /**
     * Used for setting the value of pixel
     * @param x position on x axis of the requested pixel
     * @param y position on y axis of the requested pixel 
     * @param value new value for position (x,y) 
     */
    public void setPixel(int x, int y, double value) {
        if (y >= 0 && y <height && x >= 0 && x < width) {
            data[y*width+x] = value;
        }
    }
    /**
     * Used for getting the height of the image
     * @return height of image
     */
    public int getHeight() {
        return height;
    }
    /**
     * Used for getting the width of the image
     * @return width of image
     */
    public int getWidth() {
        return width;
    }
    /**
     * Used for getting the number of the pixels
     * @return number of pixels
     */
    public int getNumPixels() {
        return width*height;
    }
    /**
     * Counts the gradient magnitude of image
     * @param gradMag image to store the counted gradient magnitude
     */
    public void gradientMagnitude(Image2D gradMag) {
        
        double xValue;
	double yValue;
                        
        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                xValue = (getPixel(x+1,y) - getPixel(x-1,y)) / 2;
                yValue = (getPixel(x,y+1) - getPixel(x,y-1)) / 2;
                gradMag.setPixel(x,y,Math.sqrt(xValue*xValue+yValue*yValue));
            }
        }
        
        gradMag.neumannBorder();
    }
    /**
     * Counts the cumulative histogram of the image
     * @param max maximum value in image
     * @return cumulative histogram
     */
    public int[] histogram(double max) {
        double value = max/255.0;
        int[] hist = new int[256];
        int position;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                position = (int) Math.floor(getPixel(x,y)/value);
                hist[position] += 1;
            }
        }
       
        for (int i = 1; i < hist.length; i++) {
            hist[i] = hist[i-1]+hist[i];
        }
        return hist;
    }
    /**
     * Used for setting neumann boundary in the image
     */     
    public void neumannBorder() {
               
        for (int y = 0; y < height; y++) {
            setPixel(0,y,getPixel(1,y));
            setPixel(width-1,y,getPixel(width-2,y));
        }
        
        for (int x = 0; x < width; x++) {
            setPixel(x,0,getPixel(x,1));
            setPixel(x,height-1,getPixel(x,height-2));
        }
    }
    /**
     * Reflects border in the image
     * @param border width of the reflected border
     */
    public void reflectBorder(int border) {
               
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < border; x++) {
                setPixel(x,y,getPixel(border,y));
                setPixel(width-x,y,getPixel(width-border,y));
            }
        }
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < border; y++) {
                setPixel(x,y,getPixel(x,border));
                setPixel(x,height-y,getPixel(x,height-border));
            }
        }
    }
    /**
     * Adds border to the image
     * @param border width of the added border
     * @return image with added border
     */
    public Image2D addBorder(int border) {
        Image2D dest = new Image2D(width+2*border,height+2*border);
               
        for (int y = border; y < dest.getHeight()-border; y++) {
            for (int x = border; x < dest.getWidth()-border; x++) {
                dest.setPixel(x, y, getPixel(x-border, y-border));
            }
        }
        return dest;
    }
    /**
     * Removes border for the image
     * @param border width of the removed border
     * @return image with removed border
     */
    public Image2D removeBorder(int border) {
        Image2D dest = new Image2D(width-2*border, height-2*border);
                
        for (int y = border; y < getHeight()-border; y++) {
            for (int x = border; x < getWidth()-border; x++) {
                dest.setPixel(x-border, y-border, getPixel(x, y));
            }
        }
        return dest;
    }
    /**
     * Multiplies every pixel value by a number
     * @param number value by which to multiply
     */    
    public void multiply(double number) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                setPixel(x, y, getPixel(x, y)*number);
            }
        }
    }
    /**
     * Divides every pixel value by a number
     * @param number value by which to divide
     */
    public void divide(double number) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                setPixel(x, y, getPixel(x, y)/number);
            }
        }
    }
      
}
