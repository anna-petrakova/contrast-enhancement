/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement;

/**
 * Class that creates a gauss kernel
 * @author Anna Petráková
 */
public class Gauss {
    private double sigma;
    private double[][] kernel;
    /**
     * Used for extracting the created kernel
     * @return gauss kernel
     */
    public double[][] getKernel() {
        return kernel;
    }
    /**
     * Contructor that creates the difference of two gaussian kernels
     * @param sigma standard deviation for the kernel
     */
    public Gauss(double sigma) {
                
        int half_kernel_size = (int) (3 * sigma + 0.5);
	int sz = 2 * half_kernel_size + 1;
	double[][] gauss = new double[sz][sz];
        double sum = 0;
        double finalValue;
				
	for (int i = -half_kernel_size; i <= half_kernel_size;i++) {
            for (int j =-half_kernel_size; j <= half_kernel_size;j++) {
				
                finalValue = gauss2(i,j,4*sigma)-gauss2(i,j,sigma);
                                
                if (finalValue < 0) {
                    finalValue = 0;
                }
                sum += finalValue;
                gauss[i + half_kernel_size][j+half_kernel_size] = finalValue;
            }
        }
        
        for (int i = 0; i < sz;i++) {
            for (int j = 0; j <sz;j++) {
                gauss[i][j] = gauss[i][j]/sum;
            }
        }
        this.kernel = gauss; 
        this.sigma = sigma;
    }
    /**
     * Computes the gauss value
     * @param x position x
     * @param y positin y
     * @param sigma standard deviation
     * @return the gauss value
     */
    private double gauss2(int x, int y, double sigma) {
        double value1 = 1/(2*Math.PI*sigma*sigma);
        double value2 = Math.exp(-(x*x+y*y)/(2*sigma*sigma));
        return value1*value2;
    }
}
