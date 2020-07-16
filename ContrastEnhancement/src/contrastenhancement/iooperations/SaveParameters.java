/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.iooperations;

import contrastenhancement.MainWindow;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Saves currently used parameters
 * @author Anna Petráková
 */
public class SaveParameters {
    private JTextField filename = new JTextField();
    private JTextField dir = new JTextField();
    private MainWindow main;
    /**
     * Constructor that lets user select name of file and location for saving and
     * saves the file with parameters
     * @param main 
     */
    public SaveParameters(MainWindow main) {
        this.main = main;
        
        JFileChooser save = new JFileChooser();
        save.setAcceptAllFileFilterUsed(false);
        save.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
        
        int value = save.showSaveDialog(main);
        if (value == JFileChooser.APPROVE_OPTION) {
            filename.setText(save.getSelectedFile().getName());
            dir.setText(save.getCurrentDirectory().toString());
            save();
        }
        if (value == JFileChooser.CANCEL_OPTION) {
            filename.setText("");
            dir.setText("");
        }        
    }
    /**
     * Performs saving the parameters into txt file in this order: scheme, multi-resolution,
     * tau, sigma, alpha, threshold, iterations. The pattern of line is - "parameter: value"
     */
    public void save() {
        BufferedWriter output = null;
        try {
            File file = new File(dir.getText()+"\\"+filename.getText()+".txt");
            output = new BufferedWriter(new FileWriter(file));
            
            output.write("Scheme: ");
            
            if ("explicit".equals(main.getSelectedScheme())) {
                output.write("explicit");
            } else  {
                output.write("aos");
            }
            output.newLine();
            output.write("Multi-Resolution: " + main.getMultiRes());
            output.newLine();
            output.write("Tau: " + main.getParameter("tau"));
            output.newLine();
            output.write("Sigma: " + main.getParameter("sigma"));
            output.newLine();
            output.write("Alpha: " + main.getParameter("alpha"));
            output.newLine();
            output.write("Threshold: " + main.getParameter("threshold"));
            output.newLine();
            output.write("Iterations: " + main.getParameter("iterations"));
        } catch ( IOException e ) {
            JOptionPane.showMessageDialog(null,"Error: Something went wrong while writing the file", 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
        } finally {
          if ( output != null ) {
              try {
                  output.close();
              } catch (IOException ex) {
                  Logger.getLogger(SaveParameters.class.getName()).log(Level.SEVERE, null, ex);
              }
          }
        }
    }
    
}
