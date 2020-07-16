/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.iooperations;

import contrastenhancement.Image2D;
import contrastenhancement.MainWindow;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Loads an image
 * @author Anna Petráková
 */
public class Load {
    private JTextField filename = new JTextField();
    private JTextField dir = new JTextField();
    private BufferedImage image;
    
    /**
     * Constructor that displays file chooser that opens .jpg, .png images and loads
     * the selected image
     */
    public Load(MainWindow main) {
        JFileChooser load = new JFileChooser();
        load.setAcceptAllFileFilterUsed(false);
        load.setFileFilter(new FileNameExtensionFilter("*.jpg", ".jpeg", ".png", "jpg", "jpeg", "png"));
        
        int value = load.showOpenDialog(main);
        if (value == JFileChooser.APPROVE_OPTION) {
            filename.setText(load.getSelectedFile().getName());
            dir.setText(load.getCurrentDirectory().toString());
            loadImage(dir.getText()+"\\"+filename.getText());
        }
        if (value == JFileChooser.CANCEL_OPTION) {
            filename.setText("");
            dir.setText("");
        }    
    }
    /**
     * Used for getting the loaded image
     * @return loaded image
     */
    public BufferedImage getImage() {
        return image;
    }
    /**
     * Reads the selected file and creates buffered image from it
     * @param file string representation of the path to file
     */    
    public void loadImage(String file) {
        BufferedImage image = null;
        
        try {
            image = ImageIO.read(new File(file));
        } 
        catch (IOException e) {
            JOptionPane.showMessageDialog(null,"Sorry, the loading failed, try another image", 
                       "Error Message", JOptionPane.ERROR_MESSAGE);
        }
        this.image = image;       
    }
    /**
     * Used for getting the brightness image from RGB loaded image, converted to HSB model
     * @param image RGB image from which we want to extract the brightness image
     * @return brightness image
    */
    public static Image2D getBrightnessImageArray(BufferedImage image) {
        int pixel;
        int red;
        int green;
        int blue;
        float[] hsv = new float[3];
        double[] pixels = new double[image.getHeight()*image.getWidth()];
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixel = image.getRGB(x,y);
                
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = (pixel) & 0xff;
                
                Color.RGBtoHSB(red,green,blue,hsv);
                pixels[y*image.getWidth()+x] = (double) hsv[2];
            }
        }
        Image2D brightness = new Image2D(image.getWidth(), image.getHeight(), pixels);
        return brightness;
    }
    
}
