/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.diffusion;

import contrastenhancement.Gauss;
import contrastenhancement.Image2D;
import contrastenhancement.MainWindow;
import contrastenhancement.MultiResolution;

/**
 * Class that manages the diffusion process on image
 * @author Anna Petráková
 */
public class Diffuse {
    private double alpha;
    private double stopThreshold;
    private MainWindow main;
    private DiffusionStep step = null;
    private double[][] kernel;
    private int originalIterations;
    private int currentIterations = 0;
    /**
     * Gets threshold that differentiates the boundary edges from texture edges 
     * for conductance function g; is equal to value around 95% of cumulative distribution 
     * function; counts cumulative histogram and returns requested value
     * @param B boundary template; image from which we want to count the threshold
     * @param max maximum value in boundary template
     * @return threshold K
     */
    public double getK(Image2D B, double max) {
        int[] hist = B.histogram(max);
        int size = B.getNumPixels();
        int K1 = (int) (size*0.95+0.5);
                
        int K2 = 0;
        while (hist[K2] < K1 && K2 < hist.length) {
            K2 += 1;
        }
        double K = (K2)*(max/255.0);
        return K;
    }
    /**
     * Counts the weighting function for stopping criterion; is equal to normalized
     * first suppression term
     * @param L1 image to count the wt function from
     * @param gradMag image in which we store the gradient magnitude of L1
     * @return image with values as weights
     */
    public Image2D getWt(Image2D L1, Image2D gradMag) {
        Image2D wt = new Image2D(L1.getWidth(), L1.getHeight());
        L1.gradientMagnitude(gradMag);
        
        int half_kernel = (kernel.length-1)/2;
        double tMax = 0.0;
        double sum;
        
        Image2D gradMag2 = gradMag.addBorder(half_kernel);
        gradMag2.reflectBorder(half_kernel);
        // convolution between gradient magnitude of L1 and difference of Gaussian functions, stored in kernel
        for (int y = half_kernel; y < gradMag2.getHeight()-half_kernel; y++) {
            for (int x = half_kernel; x < gradMag2.getWidth()-half_kernel; x++) {
                sum = 0;
                for (int j = -half_kernel; j <= half_kernel; j++) {
                    for (int i = -half_kernel; i <= half_kernel; i++) {
                        sum += gradMag2.getPixel(x+i,y+j)*kernel[j+half_kernel][i+half_kernel];
                    }
                }
                if (sum > tMax) {
                    tMax = sum;
                }
                wt.setPixel(x-half_kernel, y-half_kernel, sum);
            }
        }
        // normalization of wt 
        for (int y = 0; y < wt.getHeight(); y++) {
            for (int x = 0; x < wt.getWidth(); x++) {
                wt.setPixel(x,y,wt.getPixel(x, y)/tMax);
            }
        }
       
        return wt;
    }
    /**
     * Computes the image with conductance function for diffusion from boundary template
     * @param B boundary template
     * @param g image in which we want to store the result
     * @param max maximum value in image B
     */
    public void diffusivity(Image2D B, Image2D g, double max) {
        double K = getK(B, max);
        double value;
                
        for (int y = 1; y < B.getHeight()-1; y++) {
            for (int x = 1; x < B.getWidth()-1; x++) {
      
                if (B.getPixel(x, y) <= K) {
                    value = 0.92*Math.pow(1-Math.pow(B.getPixel(x, y)/K,2),2) + 0.08;
                    g.setPixel(x,y,value);
                } else {
                    g.setPixel(x,y,0.08);
                }
            }
        }
        g.neumannBorder();
    }
    /**
     * Getting the suppressed gradient (boundary template) of image
     * @param L1 image we want to count the boundary template from
     * @param gradMag image in which we want to store the gradient magnitude
     * @param B image where we want to store the resulting boundary template
     * @return maximum value in boundary template
     */    
    public double getBoundaryTemplate(Image2D L1, Image2D gradMag,Image2D B) {
        L1.gradientMagnitude(gradMag);
        
        int half_kernel = (kernel.length-1)/2;
        double finalValue;
        double sum;
        double max = 0.0;
        // bigger image of gradient magnitude with reflected borders used for convolution;
        // suppression of textures with alpha value
        Image2D gradMag2 = gradMag.addBorder(half_kernel);
        gradMag2.reflectBorder(half_kernel);
        // convolution between gradient magnitude and the difference of gaussian functions kernel
        for (int y = half_kernel; y < gradMag2.getHeight()-half_kernel; y++) {
            for (int x = half_kernel; x < gradMag2.getWidth()-half_kernel; x++) {
                sum = 0;
                for (int j = -half_kernel; j <= half_kernel; j++) {
                    for (int i = -half_kernel; i <= half_kernel; i++) {
                        sum += gradMag2.getPixel(x+i, y+j)*kernel[j+half_kernel][i+half_kernel];
                    }
                }
                finalValue = gradMag.getPixel(x-half_kernel, y-half_kernel)-alpha*sum;
                if (finalValue < 0) {
                    finalValue = 0.0;
                }
                if (finalValue > max) {
                    max = finalValue;
                }
                B.setPixel(x-half_kernel,y-half_kernel,finalValue);
            }
        }
        return max;
                
    }
    /**
     * Criterion necessary for stopping the diffusion process
     * @param L1 image in k-th iteraton
     * @param L2 image in (k+1)-th iteration
     * @param wt weighting function
     * @return true if the criterion is satisfied, false else
     */        
    public boolean stopCriterion(Image2D L1, Image2D L2, Image2D wt) {
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int y = 1; y < L1.getHeight()-1; y++) {
            for (int x = 1; x < L1.getWidth()-1; x++) {
                norm1 += Math.pow(wt.getPixel(x, y)*(L2.getPixel(x, y)-L1.getPixel(x, y)),2);
                norm2 += Math.pow(L1.getPixel(x, y),2);
            }
        }
        
        double stopValue = Math.sqrt(norm1)/Math.sqrt(norm2);
        
        return stopValue <= stopThreshold;
    }
    /**
     * Counts the adjusted illumination from estimated illumination
     * @param Sa image where we want to store the result
     * @param L estimated illumination
     * @param S original brightness image
     * @return maximum value in adjusted image
     */
    public double getAdjustedIlum(Image2D Sa, Image2D L, Image2D S) {
        double log = Math.log(2);
        double reflectance;
        double la;
        double sa;
        double max = 0.0;
                
        for (int y = 1; y < L.getHeight()-1; y++) {
            for (int x = 1; x < L.getWidth()-1; x++) {
                reflectance = S.getPixel(x-1, y-1)/(L.getPixel(x, y)+7E-6);
                la = Math.log(L.getPixel(x, y)+1)/log;
                sa = reflectance*la;
                if (sa > max) {
                    max = sa;
                }
                Sa.setPixel(x-1, y-1, sa);
            }
        }
        return max;
    }
    /**
     * Cuts 0.5% of the pixels at the two ends of the histogram and rescale the dynamic range
     * @param Sa image we want to cut from
     * @param max maximum value in image Sa
     */
    public void histClipping(Image2D Sa, double max) {
        int[] hist = Sa.histogram(max);
        int low = (int) (Sa.getHeight()*Sa.getWidth()*0.005);
        int high = (int) (Sa.getHeight()*Sa.getWidth()*0.995);
        // find the position in histogram, under which lie 0.5% of pixels
        // finde the position in histogram, over which lie 0.5% of pixels
        int low1 = 0;
        int high1 = 0;
        for (int count = 0; count < hist.length; count++) {
            if (hist[count] < low) {
                low1 += 1;
            }
            if (hist[count] < high) {
                high1 += 1;
            }
        }
        // compute the intensity value under which lie 0.5% of pixels
        // compute the intensity value over which lie 0.5% of pixels
        // compute the factor for rescaling the dynamic range
        double lowFinal = low1*(max/255.0);
        double highFinal = high1*(max/255.0);
        double factor = 255.0/(highFinal-lowFinal);
        double finalValue;
        
        for (int y = 0; y < Sa.getHeight(); y++) {
            for (int x = 0; x < Sa.getWidth(); x++) {
                if (Sa.getPixel(x, y) < lowFinal) {
                    finalValue = 0.0;
                } else if (Sa.getPixel(x, y) > highFinal) {
                    finalValue = 255.0;
                } else {
                    finalValue = (Sa.getPixel(x, y)-lowFinal)*factor;
                }
                Sa.setPixel(x, y, finalValue);
            }
        }
                    
    }
    /**
     * Manages the diffusion process
     * @param mr multi-resolution object, contains just the original brightness image or a pyramid
     * @param params parameters for diffusion
     * @param main main window of the application
     * @return adjusted brightness image
     */
    public Image2D diffuse(MultiResolution mr, String[] params, MainWindow main) {
                
        String step1 = params[0];
        double tau = Double.parseDouble(params[1]);
        double sigma = Double.parseDouble(params[2]);
        alpha = Double.parseDouble(params[3]);
        boolean MRandAOS = Boolean.parseBoolean(params[4]) && "AOS".equals(step1);
        int iterations = Integer.parseInt(params[5]);
        stopThreshold = Double.parseDouble(params[6]);
                
        if ("explicit".equals(step1)) {
            step = new ExplicitDiffusion(tau);
        } else if ("AOS".equals(step1)) {
            step = new AOSdiffusion(tau, mr.getImage(mr.getN()).getWidth()+2, mr.getImage(mr.getN()).getHeight()+2);
        }
        originalIterations = iterations;
        this.main = main;
        
        Image2D original = mr.getImage(mr.getN());
        Image2D L1 = mr.getImageCopy(mr.getN()).addBorder(1);
         
        Gauss gauss = new Gauss(sigma);
        kernel = gauss.getKernel();
        // if multi-resolution and AOS are selected, do most iterations on the coarsest level
        if (MRandAOS) {
            iterations = iterations - mr.getN() + 1;
        }
        Image2D diffusedImage = diffusion(L1,original,iterations);
        // if multi-resolution is used
        for (int i = mr.getN(); i > 0; i--) {
            //check if the process wasn't cancelled
            if (main.getIsCancelled()) {
                return null;
            }
            
            original = mr.getImage(i-1);
            L1 = mr.getAdjustedImage(diffusedImage.removeBorder(1), i-1, this).addBorder(1);
            // if aos is selected, perform only 1 iteration on remaining levels and 0 on the original image
            if ("AOS".equals(step1)) {
                if (i == 1) {
                    diffusedImage = L1;
                    break;
                }
                step = new AOSdiffusion(tau, mr.getImage(i-1).getWidth()+2, mr.getImage(i-1).getHeight()+2);
                diffusedImage = diffusion(L1, original,1);
            } else {                                   
                diffusedImage = diffusion(L1, original,iterations-currentIterations);
            }
        }
        
        return getFinalImage(diffusedImage,original);
    }
    /**
     * Performs diffusion process
     * @param L1 image in which we want to perform the diffusion
     * @param original original image
     * @param iter number of iterations for diffusion
     * @return diffused image after iter iterations or if the stopping criterion returned true
     */         
    public Image2D diffusion(Image2D L1, Image2D original, int iter) {
        
        L1.neumannBorder();
                       
        Image2D gradMag = new Image2D(L1.getWidth(),L1.getHeight());
        Image2D B = new Image2D(L1.getWidth(),L1.getHeight());
                
        Image2D wt = getWt(L1, gradMag);
        
        Image2D g = new Image2D(L1.getWidth(),L1.getHeight());
        Image2D L2 = new Image2D(L1.getWidth(),L1.getHeight());
        Image2D temp;
        
        boolean stop = false;
        
        while (!stop && --iter >= 0) {
            // check if the process wasn't cancelled
            if (main.getIsCancelled()) {
                return null;
            }
            
            double maxBound = getBoundaryTemplate(L1,gradMag,B);
            diffusivity(B,g, maxBound);
                        
            step.diffusionStep(L1, L2, g, original);
            
            L2.neumannBorder();
            
            stop = stopCriterion(L1,L2,wt);
            // update the iterations counter in the main window
            currentIterations += 1;
            main.setIterationsText(String.valueOf(currentIterations) + "/" + String.valueOf(originalIterations));
            // put image with k-th iteration in L2 and image with (k+1)-th iteration in L1
            temp = L1;
            L1 = L2;
            L2 = temp;
        
        }
        
        return L1;
    }
    /**
     * Gets adjusted and clipped brightness image
     * @param L estimated illumination
     * @param originalImage original brightness image
     * @return final brightness image
     */
    public Image2D getFinalImage(Image2D L, Image2D originalImage) {
       
        if (L == null) {
            return null;
        }
        Image2D Sa = new Image2D(originalImage.getWidth(), originalImage.getHeight());               
        double max = getAdjustedIlum(Sa, L, originalImage);
               
        histClipping(Sa, max);
        
        return Sa;
    }
}
