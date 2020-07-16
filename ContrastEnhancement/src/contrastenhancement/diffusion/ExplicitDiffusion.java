/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.diffusion;

import contrastenhancement.Image2D;

/**
 * Performs diffusion by explicit scheme
 * @author Anna Petráková
 */
public class ExplicitDiffusion implements DiffusionStep {
    private double tau;
    /**
     * Constructor used for setting the time value
     * @param tau time parameter for diffusion
     */
    public ExplicitDiffusion(double tau) {
        this.tau = tau;
    }
    
    @Override
    public void diffusionStep(Image2D L1, Image2D L2, Image2D g, Image2D original) {
        double newValue;
        double finalValue;
        for (int y = 1; y < L1.getHeight()-1; y++) {
            for (int x = 1; x < L1.getWidth()-1; x++) {
                newValue = 0;
                newValue += (g.getPixel(x, y)+g.getPixel(x+1, y))*(L1.getPixel(x+1,y)-L1.getPixel(x, y))/2;
                newValue += (g.getPixel(x, y)+g.getPixel(x-1, y))*(L1.getPixel(x-1,y)-L1.getPixel(x, y))/2;
                newValue += (g.getPixel(x, y)+g.getPixel(x, y+1))*(L1.getPixel(x,y+1)-L1.getPixel(x, y))/2;
                newValue += (g.getPixel(x, y)+g.getPixel(x, y-1))*(L1.getPixel(x,y-1)-L1.getPixel(x, y))/2;
                
                finalValue = L1.getPixel(x,y)+tau*newValue;
                // decide whether the final value is greater that the value in original image
                if (original.getPixel(x-1, y-1) > finalValue) {
                    L2.setPixel(x, y, original.getPixel(x-1, y-1));
                } else {
                    L2.setPixel(x, y, finalValue);
                }
                
            }
        }
        
    }
    
}
