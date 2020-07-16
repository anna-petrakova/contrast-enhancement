/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement.iooperations;

import contrastenhancement.Image2D;
import contrastenhancement.MainWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

/**
 * Used for displaying all images
 * @author Anna Petráková
 */
public class DisplayImage extends JPanel {
    private JPanel canvas;
    /**
     * Constructor that displays resulting images
     * @param image resulting brightness image in HSB model
     * @param original original image in RGB model
     * @param list list of all currently opened resulting images
     * @param params parameters for used diffusion
     */          
    public DisplayImage(Image2D image, BufferedImage original, List<JFrame> list, String[] params, MainWindow main) {
        // gets the resulting image in RGB model
        BufferedImage im = getModifiedImage(image,original);
        
        this.canvas = new JPanel() {
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(im, 0, 0, null);
            }
        };
        // if the image is bigger than screen size, it should be scrollable
        canvas.setPreferredSize(new Dimension(im.getWidth(), im.getHeight()));
        JScrollPane sp = new JScrollPane(canvas);
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame f = new JFrame();
                
        f.setTitle("Final image");
        JMenuBar menuBar = new JMenuBar();
        JMenuItem save = new JMenuItem("Save");
        save.setPreferredSize(new Dimension(60, save.getPreferredSize().height));
        save.setMinimumSize(new Dimension(60, save.getPreferredSize().height));
        save.setMaximumSize(new Dimension(60, save.getPreferredSize().height));
        
        save.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        menuBar.add(save);
        // listens for action performed on the save button and saves the image
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveImage save = new SaveImage(im,main);
                if (!"".equals(save.getFilename())) {
                    f.setTitle(save.getFilename());
                }
            }
        });

        JMenuItem parameters = new JMenuItem("Parameters");
        parameters.setPreferredSize(new Dimension(90, parameters.getPreferredSize().height));
        parameters.setMinimumSize(new Dimension(90, save.getPreferredSize().height));
        parameters.setMaximumSize(new Dimension(90, save.getPreferredSize().height));
        
        parameters.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        menuBar.add(parameters);
        // listens for action performed on parameters button and displays parameters
        parameters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new Parameters(params);
            }
        });

        f.setJMenuBar(menuBar);
        // listens for closing window event, displays confirmation window and closes the frame with image
        f.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            int confirmed = JOptionPane.showConfirmDialog(null, 
                        "Are you sure you want to close the image?", "Close Image Message Box",
                        JOptionPane.YES_NO_OPTION);

            if (confirmed == JOptionPane.YES_OPTION) {
                list.remove(f);
                f.setVisible(false);
                f.dispose();
            }
        }
        });

        list.add(f);
               
        f.setContentPane(this);
        f.pack();
        if (im.getWidth() > screenSize.getWidth() && im.getHeight() > screenSize.getHeight()) {
            f.setExtendedState( f.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        }
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setVisible(true);
        
    }
    /**
     * Constructor that displays original image
     * @param im loaded image
     * @param list list for all resulting images
     * @param mainWindow main window of the application
     */
    public DisplayImage(BufferedImage im, List<JFrame> list, MainWindow mainWindow) {
        this.canvas = new JPanel() {
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(im, 0, 0, null);
            }
        };
        // if the image is bigger than screen size, it should be scrollable
        canvas.setPreferredSize(new Dimension(im.getWidth(), im.getHeight()));
        JScrollPane sp = new JScrollPane(canvas);
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame f = new JFrame();
        f.setTitle("Original image");
        // listens for closing window event, displays confirmation window, closes all
        // resulting images together with the original one and enables buttons in main window
        f.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            
            int confirmed = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to close the original image and all resulting images?", 
                    "Close Image Message Box", JOptionPane.YES_NO_OPTION);

            if (confirmed == JOptionPane.YES_OPTION) {
                for (JFrame frame : list) {
                    frame.setVisible(false);
                    frame.dispose();
                }
                list.clear();
                mainWindow.setEnabledList(true);
                f.setVisible(false);
                f.dispose();
                mainWindow.clearList();
                mainWindow.setEnabledButtons(false);
                mainWindow.setOriginal(null);

            }
        }
        });
        
        f.setContentPane(this);
        f.pack();
        if (im.getWidth() > screenSize.getWidth() && im.getHeight() > screenSize.getHeight()) {
            f.setExtendedState( f.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        }
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setVisible(true);
        
        mainWindow.setEnabledList(false);
        mainWindow.setEnabledButtons(true);
    }
    /**
     * Method that uses the computed brightness image in HSB model and original
     * image in RGB model to create the final image
     * @param image brightness image
     * @param original original image
     * @return enhanced final image
     */
    public BufferedImage getModifiedImage(Image2D image, BufferedImage original) {
        int pixel;
        int red;
        int green;
        int blue;
        float[] hsv = new float[3];
        BufferedImage bi = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                pixel = original.getRGB(x,y);
                
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = (pixel) & 0xff;
                
                Color.RGBtoHSB(red,green,blue,hsv);
                hsv[2] = (float) (image.getPixel(x, y)/255.0);
                if (hsv[2] > 1.0) {
                    hsv[2] = (float) 1.0;
                }
                int i = Color.HSBtoRGB(hsv[0],hsv[1],hsv[2]);
                bi.setRGB(x,y, i);
            }
        }
        return bi;
    }
   /**
    * Displays information about parameters used for diffusion
    */
    private class Parameters extends JFrame {
       
        private Parameters(String[] params) {
           String method = params[0];
           String tau = params[1];
           String sigma = params[2];
           String alpha = params[3];
           String multiRes = params[4];
           String iterations = params[5];
           String threshold = params[6];
           
           this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
           this.setTitle("Parameters");
           this.setSize(250, 210);
           this.setResizable(false);
           this.setLayout(null);
                      
           JLabel methodLabel = new JLabel();
           JLabel methodValue = new JLabel();
           JLabel multiR = new JLabel();
           JLabel multiRValue = new JLabel();
           JLabel title = new JLabel();
           JLabel tauLabel = new JLabel();
           JLabel tauValue = new JLabel();
           JLabel alphaLabel = new JLabel();
           JLabel alphaValue = new JLabel();
           JLabel sigmaLabel = new JLabel();
           JLabel sigmaValue = new JLabel();
           JLabel iterLabel = new JLabel();
           JLabel iterValue = new JLabel();
           JLabel threshLabel = new JLabel();
           JLabel threshValue = new JLabel();
           
           title.setText("Parameters");
           methodLabel.setText("Method: ");
           tauLabel.setText("Tau: ");
           alphaLabel.setText("Alpha: ");
           sigmaLabel.setText("Sigma: ");
           multiR.setText("Multi-resolution: ");
           iterLabel.setText("Iterations: ");
           threshLabel.setText("Threshold: ");
           
           methodValue.setText(method);
           tauValue.setText(tau);
           sigmaValue.setText(sigma);
           alphaValue.setText(alpha);
           multiRValue.setText(multiRes);
           iterValue.setText(iterations);
           threshValue.setText(threshold);
           
           this.add(methodLabel);
           this.add(methodValue);
           this.add(multiR);
           this.add(multiRValue);
           this.add(title);
           this.add(tauLabel);
           this.add(tauValue);
           this.add(alphaLabel);
           this.add(alphaValue);
           this.add(sigmaLabel);
           this.add(sigmaValue);
           this.add(iterLabel);
           this.add(iterValue);
           this.add(threshLabel);
           this.add(threshValue);
           
           title.setLocation(80, 10);
           methodLabel.setLocation(10, 30);
           methodValue.setLocation(60, 30);
           multiR.setLocation(10, 50);
           multiRValue.setLocation(105, 50);
           tauLabel.setLocation(10, 70);
           tauValue.setLocation(40, 70);
           sigmaLabel.setLocation(10, 90);
           sigmaValue.setLocation(55, 90);
           alphaLabel.setLocation(10, 110);
           alphaValue.setLocation(50, 110);
           threshLabel.setLocation(10, 130);
           threshValue.setLocation(75, 130);
           iterLabel.setLocation(10, 150);
           iterValue.setLocation(73, 150);
                      
           title.setSize(80, 15);
           methodLabel.setSize(70, 15);
           methodValue.setSize(70, 15);
           multiR.setSize(120, 15);
           multiRValue.setSize(60, 15);
           tauLabel.setSize(40, 15);
           tauValue.setSize(100, 15);
           sigmaLabel.setSize(50, 15);
           sigmaValue.setSize(100, 15);
           alphaLabel.setSize(40, 15);
           alphaValue.setSize(100, 15);
           iterLabel.setSize(65, 15);
           iterValue.setSize(100, 15);
           threshLabel.setSize(65, 15);
           threshValue.setSize(100, 15);
           
           this.setVisible(true);
           
       }

    }
    
}
