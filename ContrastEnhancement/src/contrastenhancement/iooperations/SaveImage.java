/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.iooperations;

import contrastenhancement.Image2D;
import contrastenhancement.MainWindow;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Saves resulting image
 * @author Anna Petráková
 */
public class SaveImage {
    private JTextField filename = new JTextField();
    private JTextField dir = new JTextField();
    private String type;
    /**
     * Used for getting the name of the file, selected by user
     * @return file name
     */
    public String getFilename() {
        return filename.getText();
    }
    /**
     * Constructor that lets user select name of file and location for saving and saves the image
     * @param image image to be saved
    */    
    public SaveImage(BufferedImage image, MainWindow main) {
        JFileChooser save = new JFileChooser();
        
        save.setAcceptAllFileFilterUsed(false);
        save.addChoosableFileFilter(new FileNameExtensionFilter("*.png", "png"));
        save.addChoosableFileFilter(new FileNameExtensionFilter("*.jpg", "jpg"));
                        
        int value = save.showSaveDialog(main);
        if (value == JFileChooser.APPROVE_OPTION) {
            filename.setText(save.getSelectedFile().getName());
            dir.setText(save.getCurrentDirectory().toString());
            type = save.getFileFilter().getDescription();
                        
            save(image);
        }
        if (value == JFileChooser.CANCEL_OPTION) {
            filename.setText("");
            dir.setText("");
        }        
    }
    /**
     * Performs saving the image depending on what type of file was selected
     * @param image image to be saved
     */
    private void save(BufferedImage image) {
        try
        {
            if ("*.jpg".equals(type)) {
                ImageIO.write(image,"jpg", new File(dir.getText()+"\\"+filename.getText()+".jpg"));
            } else if ("*.png".equals(type)) {
                ImageIO.write(image,"png", new File(dir.getText()+"\\"+filename.getText()+".png"));
            }
        }
        catch(Exception exception)
        {
            Logger.getLogger(Image2D.class.getName()).log(Level.SEVERE, null, exception);
        }
    }
    
        
    
}
