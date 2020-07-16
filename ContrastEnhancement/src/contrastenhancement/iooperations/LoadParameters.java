/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.iooperations;

import contrastenhancement.MainWindow;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Loads parameters from a txt file
 * @author Anna Petráková
 */
public class LoadParameters {
    private JTextField filename = new JTextField();
    private JTextField dir = new JTextField();
    private MainWindow main;
    private String line;
    private int lineNumber = 1;
    /**
     * Constructor that displays a file chooser for txt files, loads parameters from a it 
     * and sets them as values in parameters fields in main window
     * @param main main window of the application
     */
    public LoadParameters(MainWindow main) {
        this.main = main;
        
        JFileChooser load = new JFileChooser();
        load.setAcceptAllFileFilterUsed(false);
        load.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
        
        int value = load.showOpenDialog(main);
        if (value == JFileChooser.APPROVE_OPTION) {
            filename.setText(load.getSelectedFile().getName());
            dir.setText(load.getCurrentDirectory().toString());
            load();
        }
        if (value == JFileChooser.CANCEL_OPTION) {
            filename.setText("");
            dir.setText("");
        }    
        
    }
    /**
     * Performs reading the file and setting the parameters
     */
    public void load() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(dir.getText()+"\\"+filename.getText())))) {
               
            main.setScheme(parseLine(line = br.readLine())[1]);
            lineNumber++;
            main.setMultiRes(Boolean.valueOf(parseLine(line = br.readLine())[1]));
            lineNumber++;
            System.out.println(lineNumber);
            main.setParameter(parseLine(line = br.readLine())[1], "tau");
            lineNumber++;
            main.setParameter(parseLine(line = br.readLine())[1], "sigma");
            lineNumber++;
            main.setParameter(parseLine(line = br.readLine())[1], "alpha");
            lineNumber++;
            main.setParameter(parseLine(line = br.readLine())[1], "threshold");
            lineNumber++;
            main.setParameter(parseLine(line = br.readLine())[1], "iterations");
            
        } catch(IOException e) {
            
            JOptionPane.showMessageDialog(main,"Something went wrong while reading the file", 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            String s = "";
            switch (lineNumber) {
                case 1: s = "Scheme"; break;
                case 2: s = "Multi-Resolution"; break;
                case 3: s = "Tau"; break;
                case 4: s = "Sigma"; break;
                case 5: s = "Alpha"; break;
                case 6: s = "Threshold"; break;
                case 7: s = "Iterations"; break;
            }
            JOptionPane.showMessageDialog(main,"Wrong value in " + s + " parameter; input line: " + System.lineSeparator() + line, 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Parsing the line by " " character
     * @param line line to be parsed
     * @return array with two parts of the line
     */
    private String[] parseLine(String line) {
        String[] fields = line.split(" ");
        
        if (fields.length != 2) {
            
            return null;
                    
        }
        
        return fields;       
    }
    
}
