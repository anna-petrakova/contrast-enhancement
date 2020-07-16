/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement;

import contrastenhancement.diffusion.Diffuse;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Implements multi-resolution technique for acceleration
 * @author Anna Petráková
 */
public class MultiResolution {
    private int N;
    private Image2D[] pyramid;
    /**
    * Used for gettting the image on i-th position in pyramid
    * @param i position in pyramid
    * @return image form the pyramid
    */
    public Image2D getImage(int i) {
        return pyramid[i];
    }
    /**
     * Used for getting the number of levels in pyramid
     * @return number of levels
     */
    public int getN() {
        return N;
    }
    /**
     * Used for getting the copy of an image on i-th position in pyramid
     * @param i position in pyramid
     * @return image from the pyramid
     */
    public Image2D getImageCopy(int i) {
        Image2D im = new Image2D(pyramid[i].getWidth(), pyramid[i].getHeight());
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                im.setPixel(x, y, pyramid[i].getPixel(x, y));
            }
        }
        return im;
    }
    /**
     * Inicialize the multi-resolution object depending on whether the technique is
     * used or not
     * @param L1 the original image, the biggest in the pyramid
     * @param doPyramid do multi-resolution or not
     */
    public void inicialize(Image2D L1, boolean doPyramid) {
        if (doPyramid) {
            double value1 = Math.max(L1.getHeight(), L1.getWidth()) / 200;
            N = (int) Math.floor(Math.log(value1) / Math.log(2));
        } else {
            // if the multi-resolution is not used, pyramid contains only original image
            N = 0;
        }
        pyramid = new Image2D[N+1];
        pyramid[0] = L1;
    }
    /**
     * Make a pyramid by resizing the image to half of its size in each iteration
     * @param im largest image in the pyramid
     * @return smallest image in the pyramid
     */
    public Image2D makePyramid(Image2D im) {
        Image2D image = im;
        BufferedImage BI;
        
        int newWidth;
        int newHeight;
        // resize image to half of its size in each iteration and save it in pyramid array        
        for (int i = 1; i <= N; i++) {
            BI = getGreyImage(image);
            newWidth = BI.getWidth() / 2;
            newHeight = BI.getHeight() / 2;
            BI = resize(BI, newWidth, newHeight);
            image = getImage2D(BI);
            pyramid[i] = image;
        }
        
        return pyramid[N];
    }
    /**
     * Resizes image to a specified width and height
     * @param b image to be resized
     * @param width requested width of the new image
     * @param height requested height of the new image
     * @return scaled image
     */
    public BufferedImage resize(BufferedImage b, int width, int height) {
        BufferedImage scaledBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledBI.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(b, 0, 0, width, height, null);
        g.dispose();
        return scaledBI;
    }
    /**
     * Creates BufferedImage object from Image2D object
     * @param im initial image
     * @return final buffered image
     */
    public BufferedImage getGreyImage(Image2D im) {
        int pixel;
        BufferedImage bi = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                
                int value = (int)(im.getPixel(x,y));
                pixel = (value<<16) | (value<<8) | value;
                bi.setRGB(x,y, pixel);
            }
        }
        
        return bi;
    }
    /**
     * Creates Image2D object form BufferedImage object
     * @param B initial buffered image
     * @return final image2D
     */
    public Image2D getImage2D(BufferedImage B) {
        Image2D im = new Image2D(B.getWidth(), B.getHeight());
        int pixel;
        int red;
                
        for (int y = 0; y < B.getHeight(); y++) {
            for (int x = 0; x < B.getWidth(); x++) {
                pixel = B.getRGB(x, y);
                
                red = (pixel >> 16) & 0xff;
                               
                im.setPixel(x, y, red);
            }
        }
        return im;
    }
    /**
     * Creates adjusted image by mixing together the initial image on i-th position
     * in pyramid and resized diffused image
     * @param im diffused image
     * @param i i-th position in pyramid
     * @param diffuse diffused object from which the method is called
     * @return adjusted image
     */
    public Image2D getAdjustedImage(Image2D im, int i, Diffuse diffuse) {
        BufferedImage b = getGreyImage(im);
        BufferedImage b2 = resize(b, pyramid[i].getWidth(), pyramid[i].getHeight());
        Image2D resizedImage = getImage2D(b2);
        Image2D adjustedImage = getAdjImage(pyramid[i], resizedImage, diffuse);
        
        return adjustedImage;
    }
    /**
     * Mixing together two images
     * @param orig original image from the pyramid
     * @param newImage resized diffused image
     * @param diffuse diffuse object
     * @return adjusted image
     */
    private Image2D getAdjImage(Image2D orig, Image2D newImage, Diffuse diffuse) {
                        
        Image2D adjustedImage = new Image2D(newImage.getWidth(), newImage.getHeight());
        Image2D gradMag = new Image2D(orig.getWidth(), orig.getHeight());
        Image2D w = new Image2D(orig.getWidth(), orig.getHeight());
        // counts the weight by normalizing the boundary template that helps to replace the blurred edges
        double max = diffuse.getBoundaryTemplate(newImage, gradMag, w);
        w.divide(max);
        
        double value;
        
        for (int y = 0; y < orig.getHeight(); y++) {
            for (int x = 0; x < orig.getWidth(); x++) {
                value = (1-w.getPixel(x, y))*newImage.getPixel(x, y) + w.getPixel(x, y)*orig.getPixel(x, y);
                adjustedImage.setPixel(x, y, value);
            }
        }
                
        return adjustedImage;
    }
}