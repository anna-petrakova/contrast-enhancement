/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.diffusion;

import contrastenhancement.Image2D;
import contrastenhancement.Vector;

/**
 * Performs diffusion by AOS scheme
 * @author Anna Petráková
 */
public class AOSdiffusion implements DiffusionStep {
    private Vector gammaRow;
    private Vector gammaColumn;
    private Image2D Lx;
    private Image2D Ly;
    private double tau;
    /**
     * Constructor for diffusion, sets dimensions for used images and vectors
     * @param tau time parameter for diffusion
     * @param width width of the image used for diffusion
     * @param height height of the image used for diffusion
     */
    public AOSdiffusion(double tau, int width, int height) {
        this.tau = tau;
        gammaRow = new Vector(width);
        gammaColumn = new Vector(height);
        Lx = new Image2D(width,height);
        Ly = new Image2D(width, height);
    }
    /**
     * Method that doues row diffusion for one row
     * @param g imge which contains diffusivity
     * @param L1 image which we want to diffuse
     * @param row number of the currently diffused row 
     */   
    public void solveRow(Image2D g,  Image2D L1, int row) {
        double gamma;
        double ro;
        double xValue;
        
        double a;
        double b;
        double c;
        
        a = 1.0+tau*(g.getPixel(0,row)+g.getPixel(1,row));
        b = -tau*(g.getPixel(0,row)+g.getPixel(1,row));
        
        gamma = b / a;
        ro = L1.getPixel(0, row) / a;
                
        gammaRow.setPixel(0, gamma);
        Lx.setPixel(0, row, ro);
        
        for (int i = 1; i < L1.getWidth()-1; i++) {
            c = -tau*(g.getPixel(i-1, row)+g.getPixel(i, row));
            a = 1.0+tau*(g.getPixel(i+1, row)+g.getPixel(i-1, row)+2*(g.getPixel(i, row)));
            b = -tau*(g.getPixel(i+1, row)+g.getPixel(i, row));
            
            gamma = b / (a - c*gammaRow.getPixel(i-1));
            ro = (L1.getPixel(i, row) - c*Lx.getPixel(i-1, row)) / (a - c*gammaRow.getPixel(i-1));
                       
            gammaRow.setPixel(i, gamma);
            Lx.setPixel(i, row, ro);
        }
        
        c = -tau*(g.getPixel(L1.getWidth()-2, row)+g.getPixel(L1.getWidth()-1, row));
        a = 1.0+tau*(g.getPixel(L1.getWidth()-2, row)+g.getPixel(L1.getWidth()-1, row));
        
        ro = (L1.getPixel(L1.getWidth()-1, row) - c*Lx.getPixel(L1.getWidth()-2, row)) / (a - c*gammaRow.getPixel(L1.getWidth()-2));
        Lx.setPixel(L1.getWidth()-1, row, ro);
        
        for (int i = L1.getWidth()-2; i >= 0; i--) {
            xValue = Lx.getPixel(i, row) - gammaRow.getPixel(i)*Lx.getPixel(i+1, row);
            Lx.setPixel(i, row, xValue);
        }
      
    }
    /**
     * Method that does column diffusion for one column
     * @param g image which contains diffusivity 
     * @param L1 image which we want to diffuse
     * @param column number of the currently diffused column
     */
    public void solveColumn(Image2D g, Image2D L1, int column) {
        double gamma;
        double ro;
        double xValue;
                
        double a;
        double b;
        double c;
        
        a = 1.0+tau*(g.getPixel(column, 0)+g.getPixel(column, 1));
        b = -tau*(g.getPixel(column, 0)+g.getPixel(column, 1));
        
        gamma = b / a;
        ro = L1.getPixel(column, 0) / a;
                
        gammaColumn.setPixel(0, gamma);
        Ly.setPixel(column, 0, ro);
        
        for (int i = 1; i < L1.getHeight()-1; i++) {
            c = -tau*(g.getPixel(column, i-1)+g.getPixel(column, i));
            a = 1.0+tau*(g.getPixel(column, i+1)+g.getPixel(column, i-1)+2*(g.getPixel(column, i)));
            b = -tau*(g.getPixel(column, i+1)+g.getPixel(column, i));
            
            gamma = b / (a - c*gammaColumn.getPixel(i-1));
            ro = (L1.getPixel(column, i) - c*Ly.getPixel(column, i-1)) / (a - c*gammaColumn.getPixel(i-1));
                       
            gammaColumn.setPixel(i, gamma);
            Ly.setPixel(column, i, ro);
        }
        
        c = -tau*(g.getPixel(column, L1.getHeight()-2)+g.getPixel(column, L1.getHeight()-1));
        a = 1.0+tau*(g.getPixel(column, L1.getHeight()-2)+g.getPixel(column, L1.getHeight()-1));
        
        ro = (L1.getPixel(column, L1.getHeight()-1) - c*Ly.getPixel(column, L1.getHeight()-2)) / (a - c*gammaColumn.getPixel(L1.getHeight()-2));
        Ly.setPixel(column, L1.getHeight()-1, ro);
        
        for (int i = L1.getHeight()-2; i >= 0; i--) {
            xValue = Ly.getPixel(column, i) - gammaColumn.getPixel(i)*Ly.getPixel(column, i+1);
            Ly.setPixel(column, i, xValue);
        }
      
    }
    
    @Override
    public void diffusionStep(Image2D L1, Image2D L2, Image2D g, Image2D original) {
        double finalValue;
        // diffusion all rows      
        for (int i = 1; i < L1.getHeight()-1; i++) {
            solveRow(g, L1, i);
        }
        // diffuse all columns
        for (int j = 1; j < L1.getWidth()-1; j++) {
            solveColumn(g, L1, j);
        }
        // decide whether the final value is greater than the value in original image
        for (int y = 1; y < L2.getHeight()-1; y++) {
            for (int x = 1; x < L2.getWidth()-1; x++) {
                finalValue = (Lx.getPixel(x, y)+Ly.getPixel(x, y))/2;
                
                if (original.getPixel(x-1, y-1) > finalValue) {
                    L2.setPixel(x, y, original.getPixel(x-1, y-1));
                } else {
                    L2.setPixel(x, y, finalValue);
                }
                
            }
        }
    }
    
}
