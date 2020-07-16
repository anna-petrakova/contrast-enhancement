/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.diffusion;

import contrastenhancement.Image2D;

/**
 * Interface for used diffusion scheme
 * @author Anna Petráková
 */
public interface DiffusionStep {
    /**
     * Method that performs diffusion on image
     * @param L1 image we want to diffuse
     * @param L2 image in which we want to store the result
     * @param g diffusivity image
     * @param original original image
     */
    public void diffusionStep(Image2D L1, Image2D L2, Image2D g, Image2D original);
}
