/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contrastenhancement;

import contrastenhancement.iooperations.DisplayImage;
import contrastenhancement.iooperations.Load;
import contrastenhancement.iooperations.LoadParameters;
import contrastenhancement.iooperations.SaveParameters;
import contrastenhancement.diffusion.Diffuse;
import static contrastenhancement.iooperations.Load.getBrightnessImageArray;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Toolkit;
import javax.swing.JDialog;

/**
 * Main window of the application
 * @author Anna Petráková
 */
public class MainWindow extends javax.swing.JFrame {
    private BufferedImage original = null;
    private List<JFrame> frames = new ArrayList<>();
    private String tauPrevious = "0.25";
    private static String[] images = new String[]{"cathedral.png", "house.png", "girl.jpg", "boy.jpg", "star_trek.jpg"};
        
    private final double maxTau = 25.0;
    private final double maxSigma = 15.0;
    private final double maxAlpha = 2.0;
    private final int maxIterations = 5000;
    private final double maxThreshold = 0.1;
    private final double minAlpha = -2.0;
    private final double minSigma = 0.0;
    private final double minTau = 0.0;
    private final int minIterations = 0;
    private final double minThreshold = 7E-7;
    private final double maxAOSMRTau = 15.0;
    
    private MainWindow main;
    private DiffusionProcess worker;
    private boolean isCancelled = false;
    private boolean inProgress = false;
    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        main = this;
        
        inicializeFields();
    }
    /**
     * Sets sigma field to a correct value depending on what is selected
     */
    public void setSigmaField() {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        // if multi-resolution is selected, sigma is counted from the image size of the middle image in pyramid
        if (multiRes.isSelected()) {
            double value1 = Math.max(original.getHeight(), original.getWidth()) / 200;
            int N = (int) Math.floor(Math.log(value1) / Math.log(2));
            double sigma;
            
            if (N % 2 == 0) {
                sigma = 0.0032*Math.sqrt((original.getWidth() / (Math.pow(2, N/2)))*(original.getHeight() / (Math.pow(2, N/2))));
            } else {
                sigma = 0.0032*Math.sqrt((original.getWidth() / (Math.pow(2, N/2+1)))*(original.getHeight() / (Math.pow(2, N/2+1))));
            }
            String replaced = df.format(sigma).replace(",", ".");
            if (sigma > maxSigma) {
                sigmaField.setText(String.valueOf(maxSigma));
            } else {
                sigmaField.setText(replaced);
            }
        } else {
            double sigma = 0.0032*Math.sqrt(original.getWidth() * original.getHeight());
            String replaced = df.format(sigma).replace(",", ".");
            sigmaField.setText(replaced);
        }
    }
    /**
     * Used for retrieving the number value of a text field
     * @param field field for retrieving
     * @param s name of the parameter
     * @return number value from field
     */
    public double getParameter(javax.swing.JTextField field, String s) {
        String value = field.getText();
        
        if ("".equals(value) || "+".equals(value) || ("alpha".equals(s) && "-".equals(value))) {
            return 0.0;
        }
        
        String replaced = value.replace(",", ".");
        double finalValue = 0.0;
        
        try {
            finalValue = Double.parseDouble(replaced);
        } 
        catch (Exception e){
            JOptionPane.showMessageDialog(this, "Wrong " + s + " value, please enter a number", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return finalValue;
        
    }
    /**
     * Used for retrieving int value from a text field
     * @param field field for retrieving
     * @param s name of the parameter
     * @return number value from field
     */
    public int getIntParameter(javax.swing.JTextField field, String s) {
        String value = field.getText();
        
        if ("".equals(value) || "+".equals(value)) {
            return 0;
        }
        
        int finalValue = 0;
        try {
            finalValue = Integer.parseInt(value);
        } 
        catch (Exception e){
            JOptionPane.showMessageDialog(this, "Wrong " + s + " value, please enter a number", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return finalValue;
    }
    /**
     * Used for ensuring that a number from parameter is within boundaries
     * @param number number to check
     * @param boundMax upper boundary
     * @param boundMin lower boundary
     * @param param name of the parameter
     * @return true if the number is within boundaries, false else
     */
    public boolean isWithinBounds(double number, double boundMax, double boundMin, String param) {
        if (number <= boundMax && number >= boundMin) {
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Error: Wrong " + param + 
                    " parameter value; please set a different value in " + param + " field", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    /**
     * Sets original image
     * @param b new original image
     */    
    public void setOriginal(BufferedImage b) {
        original = b;
    }
    /**
     * Enables or disables the list of images
     * @param state true if list is to be enabled, false else
     */
    public void setEnabledList(boolean state) {
        this.listImages.setEnabled(state);
    }
    /**
     * Clears the selection from list of images
     */
    public void clearList() {
        this.listImages.clearSelection();
    }
    /**
     * Sets the buttons to correct state and corrects their tool tips
     * @param state whether the buttons should be enabled or disabled
     */
    public void setEnabledButtons(boolean state) {
        startButton.setEnabled(state);
        progressLabel.setEnabled(state);
        iterations.setEnabled(state);
        resetAllButton.setEnabled(state);
        loadButton.setEnabled(!state);
        
        if (state) {
            startButton.setToolTipText("Start image enhancement");
            loadButton.setToolTipText("Close the image first to open a new one");
            listImages.setToolTipText("Close the image first to open a new one");
            
            setSigmaField();
        } else {
            startButton.setToolTipText("Open image first to start image enhancement ");
            loadButton.setToolTipText("Opens image");
            listImages.setToolTipText("Click to open one of the test images");
            iterations.setText(" ");
        }
    }
    /**
     * Adds listeners to parameter fields that check if the inserted value is correct
     * or warn the user otherwise
     */
    public void inicializeFields() {
        tauField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
            warn();
        }
        public void removeUpdate(DocumentEvent e) {
            warn();
        }
        public void insertUpdate(DocumentEvent e) {
          warn();
        }
        // displays warning if user put a wrong value in the field
        public void warn() {
           double tau = getParameter(tauField,"tau");
           if (aos.isSelected()) {
               if ( tau < minTau || tau > maxTau) {
                   JOptionPane.showMessageDialog(null,"Error: Please enter number between " + minTau + " and " + maxTau + "in tau field", "Error Message", 
                           JOptionPane.ERROR_MESSAGE);
                }
           } else {
               if ( tau < minTau || tau > 0.25) {
                   JOptionPane.showMessageDialog(null,"Error: Please enter number between " + minTau + " and 0.25 in tau field", "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                }
           }
        }
      });
        
        inicializeField(sigmaField, "sigma", maxSigma, minSigma );
        inicializeField(alphaField, "alpha",  maxAlpha, minAlpha );
        inicializeField(iterationsField, "iterations", maxIterations, minIterations);
    }
    /**
     * Adds listeners to parameter fields that check if the inserted value is correct
     * or warn the user otherwise
     * @param field field to inicialize
     * @param s1 name of the parameter
     * @param boundMax upper boundary
     * @param boundMin lower boundary
     */
    private void inicializeField(javax.swing.JTextField field, String s1, double boundMax, double boundMin) {
        field.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
            warn();
        }
        public void removeUpdate(DocumentEvent e) {
          warn();
        }
        public void insertUpdate(DocumentEvent e) {
          warn();
        }

        public void warn() {
            double value;
            if (field == iterationsField) {
                value = (double) getIntParameter(field,s1);
            } else {
                value = getParameter(field,s1);
            }
           if (value < boundMin || value > boundMax) {
               if (field == iterationsField) {
                   JOptionPane.showMessageDialog(null,"Error: Please enter number between " + (int)boundMin + " and  " + (int)boundMax
                           + " in " + s1 + " field", 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
               } else {
                    JOptionPane.showMessageDialog(null,"Error: Please enter number between " + boundMin + " and  " + boundMax
                            + " in " + s1 + " field" , 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
               }
           }
        }
      });
    }
    /**
     * Sets the scheme after loading the parameters
     * @param scheme string representation of the new scheme
     */
    public void setScheme(String scheme) {
        if ("explicit".equals(scheme)) {
            explicit.setSelected(true);
            tauField.setToolTipText("Value between 0 and 0.25");
            tauField.setEnabled(true);
            tauResetButton.setEnabled(true);
        } else if ("aos".equals(scheme)) {
            aos.setSelected(true);
            tauField.setToolTipText("Value between 0 and "+maxTau);
        } else {
            JOptionPane.showMessageDialog(this,"Error: Wrong value in numerical scheme", 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Sets the multiresolution to be on or off
     * @param state whether the multi-resolution is to be enabled or disabled
     */
    public void setMultiRes(boolean state) {
        multiRes.setSelected(state);
        if (state && aos.isSelected()) {
            tauField.setEnabled(false);
            tauResetButton.setEnabled(false);
        }
    }
    /**
     * Sets parameter in loading the parameters
     * @param param string representation of the new value in parameter
     * @param s name of the parameter
     */
    public void setParameter(String param, String s) {
        String replaced = param.replace(",", ".");
        double finalParam;
        try {
            finalParam = Double.parseDouble(replaced);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this,"Error: Wrong value in " + s, 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
            try {
                finalParam = Double.parseDouble(sigmaField.getText());
            } catch (Exception e1) {
                finalParam = 0.0;
            }
        }
        String finalValue = String.valueOf(finalParam);
        switch(s) {
            case "tau":
                tauField.setText(finalValue);
                break;
            case "sigma":
                sigmaField.setText(finalValue);
                break;
            case "alpha":
                alphaField.setText(finalValue);
                break;
            case "threshold":
                thresholdField.setText(finalValue);
                break;
            case "iterations":
                iterationsField.setText(String.valueOf((int)finalParam));
                break;
            default:
                JOptionPane.showMessageDialog(this,"Error: Unrecognized parameter " + s, 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Used for getting the currently selected scheme
     * @return selected scheme
     */
    public String getSelectedScheme() {
        if (explicit.isSelected()) {
            return "explicit";
        } else {
            return "aos";
        }
    }
    /**
     * Used for getting the state of multi-resolution technique
     * @return state of multi-resolution
     */
    public boolean getMultiRes() {
        return multiRes.isSelected();
    }
    /**
     * Used for getting the value of parameter
     * @param param name of the parameter 
     * @return value from text field of the parameter
     */
    public String getParameter(String param) {
        switch(param) {
            case "tau":
                return tauField.getText();
            case "sigma":
                return sigmaField.getText();
            case "alpha":
                return alphaField.getText();
            case "threshold":
                return thresholdField.getText();
            case "iterations":
                return iterationsField.getText();
            default:
                JOptionPane.showMessageDialog(this,"Error: Unrecognized parameter " + param, 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
                return "0";
        }
    }
    /**
     * Sets text in iterations field
     * @param iter text to be put in iterations field
     */
    public void setIterationsText(String iter) {
        iterations.setText(iter);
    }
    /**
     * Used for discovering whether the diffusion process was cancelled
     * @return true if the process was cancelled, false else
     */
    public boolean getIsCancelled() {
        return isCancelled;
    }
            
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        groupScheme = new javax.swing.ButtonGroup();
        panelLoad = new javax.swing.JPanel();
        loadButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listImages = new javax.swing.JList();
        loadParamsButton = new javax.swing.JButton();
        saveParamsButton = new javax.swing.JButton();
        panelScheme = new javax.swing.JPanel();
        labelScheme = new javax.swing.JLabel();
        explicit = new javax.swing.JRadioButton();
        aos = new javax.swing.JRadioButton();
        multiRes = new javax.swing.JCheckBox();
        labelResoolution = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        labelParameters = new javax.swing.JLabel();
        tauLabel = new javax.swing.JLabel();
        tauField = new javax.swing.JTextField();
        sigmaLabel = new javax.swing.JLabel();
        sigmaField = new javax.swing.JTextField();
        alphaLabel = new javax.swing.JLabel();
        alphaField = new javax.swing.JTextField();
        resetAllButton = new javax.swing.JButton();
        tauResetButton = new javax.swing.JButton();
        sigmaResetButton = new javax.swing.JButton();
        alphaResetButton = new javax.swing.JButton();
        iterationsLabel = new javax.swing.JLabel();
        iterationsField = new javax.swing.JTextField();
        thresholdLabel = new javax.swing.JLabel();
        thresholdField = new javax.swing.JTextField();
        threshResetButton = new javax.swing.JButton();
        iterResetButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();
        iterations = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Uneven Illumination Enhancement");
        setAlwaysOnTop(true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/resources/icon2.png")));
        setResizable(false);

        panelLoad.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        loadButton.setText("Load image");
        loadButton.setToolTipText("Opens image");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        listImages.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "cathedral.png", "house.png", "girl.jpg", "boy.jpg", "star_trek.jpg" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listImages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listImages.setToolTipText("Click to open one of the test images");
        listImages.setVisibleRowCount(5);
        listImages.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listImagesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listImages);

        loadParamsButton.setText("Load parameters");
        loadParamsButton.setToolTipText("Loads parameters from a .txt file");
        loadParamsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadParamsButtonActionPerformed(evt);
            }
        });

        saveParamsButton.setText("Save parameters");
        saveParamsButton.setToolTipText("Saves parameters into a .txt file");
        saveParamsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveParamsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLoadLayout = new javax.swing.GroupLayout(panelLoad);
        panelLoad.setLayout(panelLoadLayout);
        panelLoadLayout.setHorizontalGroup(
            panelLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(saveParamsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
            .addComponent(loadParamsButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        panelLoadLayout.setVerticalGroup(
            panelLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLoadLayout.createSequentialGroup()
                .addComponent(loadButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadParamsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveParamsButton)
                .addContainerGap())
        );

        panelScheme.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelScheme.setText("Numerical scheme: ");
        labelScheme.setToolTipText("Numerical scheme for solving the non-linear diffusion");

        groupScheme.add(explicit);
        explicit.setSelected(true);
        explicit.setText("Explicit");
        explicit.setToolTipText("Explicit scheme for diffusion");
        explicit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                explicitActionPerformed(evt);
            }
        });

        groupScheme.add(aos);
        aos.setText("AOS");
        aos.setToolTipText("Additive Operator Splitting (AOS) scheme for diffusion");
        aos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aosActionPerformed(evt);
            }
        });

        multiRes.setText("Multi-Resolution");
        multiRes.setToolTipText("Multi-Resolution technique is not used");
        multiRes.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                multiResItemStateChanged(evt);
            }
        });

        labelResoolution.setText("Acceleration technique:");

        javax.swing.GroupLayout panelSchemeLayout = new javax.swing.GroupLayout(panelScheme);
        panelScheme.setLayout(panelSchemeLayout);
        panelSchemeLayout.setHorizontalGroup(
            panelSchemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSchemeLayout.createSequentialGroup()
                .addGroup(panelSchemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(explicit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(aos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelScheme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelSchemeLayout.createSequentialGroup()
                        .addGroup(panelSchemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(multiRes)
                            .addComponent(labelResoolution))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelSchemeLayout.setVerticalGroup(
            panelSchemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSchemeLayout.createSequentialGroup()
                .addComponent(labelScheme, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(explicit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aos)
                .addGap(11, 11, 11)
                .addComponent(labelResoolution)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(multiRes)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelParameters.setText("Parameters:");
        labelParameters.setToolTipText("Parameters for image enhancement");

        tauLabel.setText("Tau:");
        tauLabel.setToolTipText("Time step used for diffusion");

        tauField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        tauField.setText("0.25");
        tauField.setToolTipText("Value between 0 and 0.25");

        sigmaLabel.setText("Sigma:");
        sigmaLabel.setToolTipText("Used for creating Gaussian kernel for texture suppression");

        sigmaField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        sigmaField.setText("3.0");
        sigmaField.setToolTipText("Value between " + minSigma + " and " + maxSigma);

        alphaLabel.setText("Alpha:");
        alphaLabel.setToolTipText("Suppression strength factor, influences the texture suppression effect");

        alphaField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        alphaField.setText("1.2");
        alphaField.setToolTipText("Value between " + minAlpha + " and " + maxAlpha);

        resetAllButton.setText("Reset all parameters");
        resetAllButton.setToolTipText("Resets all parameters to their initial values");
        resetAllButton.setEnabled(false);
        resetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllButtonActionPerformed(evt);
            }
        });

        tauResetButton.setBackground(new java.awt.Color(0, 0, 0));
        tauResetButton.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        tauResetButton.setForeground(new java.awt.Color(255, 255, 255));
        tauResetButton.setText("R");
        tauResetButton.setToolTipText("Reset button for tau parameter");
        tauResetButton.setBorder(null);
        tauResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tauResetButtonActionPerformed(evt);
            }
        });

        sigmaResetButton.setBackground(new java.awt.Color(0, 0, 0));
        sigmaResetButton.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        sigmaResetButton.setForeground(new java.awt.Color(255, 255, 255));
        sigmaResetButton.setText("R");
        sigmaResetButton.setToolTipText("Reset button for sigma parameter");
        sigmaResetButton.setBorder(null);
        sigmaResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sigmaResetButtonActionPerformed(evt);
            }
        });

        alphaResetButton.setBackground(new java.awt.Color(0, 0, 0));
        alphaResetButton.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        alphaResetButton.setForeground(new java.awt.Color(255, 255, 255));
        alphaResetButton.setText("R");
        alphaResetButton.setToolTipText("Reset button for alpha parameter");
        alphaResetButton.setBorder(null);
        alphaResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphaResetButtonActionPerformed(evt);
            }
        });

        iterationsLabel.setText("Iterations:");
        iterationsLabel.setToolTipText("Number of iterations for diffusion");

        iterationsField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        iterationsField.setText("200");
        iterationsField.setToolTipText("Value between " + minIterations + " and " + maxIterations);

        thresholdLabel.setText("Threshold:");
        thresholdLabel.setToolTipText("Stopping criterion for diffusion");

        thresholdField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        thresholdField.setText("9E-5");
        thresholdField.setToolTipText("Value between " + minThreshold + " and " + maxThreshold);
        thresholdField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thresholdFieldActionPerformed(evt);
            }
        });

        threshResetButton.setBackground(new java.awt.Color(0, 0, 0));
        threshResetButton.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        threshResetButton.setForeground(new java.awt.Color(255, 255, 255));
        threshResetButton.setText("R");
        threshResetButton.setToolTipText("Reset button for threshold parameter");
        threshResetButton.setBorder(null);
        threshResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                threshResetButtonActionPerformed(evt);
            }
        });

        iterResetButton.setBackground(new java.awt.Color(0, 0, 0));
        iterResetButton.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        iterResetButton.setForeground(new java.awt.Color(255, 255, 255));
        iterResetButton.setText("R");
        iterResetButton.setToolTipText("Reset button for iterations parameter");
        iterResetButton.setBorder(null);
        iterResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iterResetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(resetAllButton)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tauLabel)
                                    .addComponent(sigmaLabel))
                                .addGap(31, 31, 31)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(thresholdField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                                    .addComponent(alphaField, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(sigmaField, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(tauField, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addComponent(iterationsField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(iterationsLabel)
                        .addComponent(thresholdLabel)
                        .addComponent(alphaLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sigmaResetButton, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
                    .addComponent(tauResetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(alphaResetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(iterResetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(threshResetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 24, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(labelParameters)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tauLabel)
                        .addComponent(tauField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tauResetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(sigmaResetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(alphaResetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sigmaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sigmaLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(alphaLabel)
                            .addComponent(alphaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(thresholdLabel)
                    .addComponent(thresholdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(threshResetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iterationsLabel)
                    .addComponent(iterationsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iterResetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetAllButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        startButton.setText("Start");
        startButton.setToolTipText("Open image first to start image enhancement");
        startButton.setEnabled(false);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        helpButton.setText(" Help  ");
        helpButton.setToolTipText("Opens simple help window");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        aboutButton.setText("About");
        aboutButton.setToolTipText("Opens about the program window");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        iterations.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        progressLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        progressLabel.setText("Progress:");
        progressLabel.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(iterations, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(helpButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                    .addComponent(aboutButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(startButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(iterations, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(aboutButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(helpButton)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelLoad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelScheme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelScheme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(0, 10, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
/**
 * Used for loading the image from the list of available images
 * @param evt mouse click on the list
 */
    private void listImagesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listImagesMouseClicked
        if (listImages.isEnabled()) {
            JList list2 = (JList)evt.getSource();
            if (evt.getClickCount() == 1) {
                int index = list2.locationToIndex(evt.getPoint());

                BufferedImage image = null;

                try {
                    URL url = this.getClass().getResource("/resources/"+images[index]);
                    image = ImageIO.read(url);

                    this.original = image;

                    new DisplayImage(image,frames,this);

                } 
                catch (IOException e) {
                    JOptionPane.showMessageDialog(this,"Sorry, the loading failed, try another image", 
                           "Error Message", JOptionPane.ERROR_MESSAGE);
                }


            }
        }
    }//GEN-LAST:event_listImagesMouseClicked
/**
 * Used for loading images
 * @param evt action perfomed on the load button
 */
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        Load load = new Load(this);
        BufferedImage image = load.getImage();
        
        if (image != null) {
            this.original = image;
            new DisplayImage(image,frames,this);
           
        }
    }//GEN-LAST:event_loadButtonActionPerformed
/**
 * Used for reseting all parameters to initial values
 * @param evt action perfomed on the reset all parameters button
 */
    private void resetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllButtonActionPerformed
        if (!multiRes.isSelected() || !aos.isSelected()) {
            tauField.setText("0.25");
        }
        
        alphaField.setText("1.2");
        setSigmaField();
        if (aos.isSelected() && multiRes.isSelected()) {
            iterationsField.setText("10");
        } else {
            iterationsField.setText("200");
        }
        thresholdField.setText("9E-5");
    }//GEN-LAST:event_resetAllButtonActionPerformed
/**
 * Used for reseting the tau parameter
 * @param evt action perfomed on the tau reset button
 */
    private void tauResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tauResetButtonActionPerformed
        tauField.setText("0.25");
    }//GEN-LAST:event_tauResetButtonActionPerformed
/**
 * Used for reseting the sigma parameter
 * @param evt action perfomed on the sigma reset button
 */
    private void sigmaResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sigmaResetButtonActionPerformed
        if (original != null) {
            setSigmaField();
        } else {
            sigmaField.setText("3.0");
        }
    }//GEN-LAST:event_sigmaResetButtonActionPerformed
/**
 * Used for reseting the alpha parameter
 * @param evt action perfomed on the alpha reset button
 */
    private void alphaResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphaResetButtonActionPerformed
        alphaField.setText("1.2");
    }//GEN-LAST:event_alphaResetButtonActionPerformed

/**
 * Used for setting the correct values after aos scheme was selected
 * @param evt action perfomed on the aos option
 */
    private void aosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aosActionPerformed
        if (multiRes.isSelected()) {
            tauPrevious = tauField.getText();
            tauField.setText(String.valueOf(maxAOSMRTau));
            tauField.setEnabled(false);
            tauResetButton.setEnabled(false);
            tauField.setToolTipText("Tau is "+maxAOSMRTau+" for the combination of multi-resoution technique and AOS scheme");
            iterationsField.setText("10");
        } else {
            tauField.setToolTipText("Value between 0 and " + maxTau);
        }
    }//GEN-LAST:event_aosActionPerformed
/**
 * Used for setting the correct values after explicit scheme was selected
 * @param evt action perfomed on the explicit option
 */
    private void explicitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_explicitActionPerformed
        if (multiRes.isSelected()) {
            tauField.setText(tauPrevious);
            tauField.setEnabled(true);
            tauResetButton.setEnabled(true);
            
        }
        tauField.setToolTipText("Value between 0 and 0.25");
        if (getParameter(tauField, "tau") > 0.25) {
            tauField.setText("0.25");
        }
    }//GEN-LAST:event_explicitActionPerformed
/**
 * Used for starting or stopping the diffusion process on the image
 * @param evt action perfomed on the start/stop button
 */
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        if (inProgress) {
            worker = null;
            isCancelled = true;
            startButton.setText("Wait");
            startButton.setEnabled(false);
        } else {
            inProgress = true;
            startButton.setText("Stop");
            iterations.setText("0/"+iterationsField.getText());
            (worker = new DiffusionProcess()).execute();
        }
   
    }//GEN-LAST:event_startButtonActionPerformed
/**
 * Class that performs the diffusion as a background process and prevents the
 * freezing of the whole UI
 */
    private class DiffusionProcess extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            double tau = getParameter(tauField,"tau");
            double sigma = getParameter(sigmaField,"sigma");
            double alpha = getParameter(alphaField, "alpha");
            int iterations = getIntParameter(iterationsField, "iterations");
            double threshold = getParameter(thresholdField, "threshold");

            boolean validTau;
            if (aos.isSelected()) {
                validTau = isWithinBounds(tau,maxTau,minTau,"tau");
            } else {
                validTau = isWithinBounds(tau,0.25,minTau,"tau");
            }
            // verifies if the values in fields are valid
            boolean validSigma = isWithinBounds(sigma,maxSigma,minSigma,"sigma");
            boolean validAlpha = isWithinBounds(alpha,maxAlpha,minAlpha,"alpha");
            boolean validIterations = isWithinBounds(iterations, maxIterations, minIterations, "iterations");
            boolean validThreshold = isWithinBounds(threshold, maxThreshold, minThreshold, "threshold");

            if (validTau && validSigma && validAlpha && validIterations && validThreshold) {
                String[] params = new String[7];
                params[1] = String.valueOf(tau);
                params[2] = String.valueOf(sigma);
                params[3] = String.valueOf(alpha);
                if (explicit.isSelected()) {
                    params[0] = "explicit";
                } else {
                    params[0] = "AOS";
                }
                params[4] = "false";
                if (multiRes.isSelected()) {
                        params[4] = "true";
                }
                params[5] = String.valueOf(iterations);
                params[6] = String.valueOf(threshold);
                // extract the brightness image and scale it to range 0-255 for the 
                // multi-resolution to function properly
                Image2D L = getBrightnessImageArray(original);
                L.multiply(255.0);
                
                MultiResolution mr = new MultiResolution();
                mr.inicialize(L,multiRes.isSelected());
                mr.makePyramid(L);

                Diffuse dif = new Diffuse();
                Image2D Sa = dif.diffuse(mr, params, main);
                if (Sa != null) {
                    new DisplayImage(Sa, original, frames, params, main);
                }

            }
            return null;
        }

        @Override
        public void done() {
            inProgress = false;
            isCancelled = false;
            startButton.setEnabled(true);
            startButton.setText("Start");
            iterations.setText("Done");
        }
    }
    /**
     * Used for displaying a simple tutorial
     * @param evt action perfomed on the help button
     */
    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
            String pt1 = "<html><body width='";
            String pt2 =
                    "'><h2>Help</h2>" +
                    "<p>1. Open the image by \"Load Image\" button or click on one of the preloaded images.<br> " + 
                    "<p>2. Set parameters or keep default values. <br>" +
                    "<p>3. Start computing by clicking on the Start button. <br> " +
                    "<p>4. Wait until the computing is over and the final image is displayed.<br>" +
                    "<p>5. Save the enhanced image by clicking on the Save menu in the final image window.<br></html>";
            
            int width = 180;
            String s = pt1 + width + pt2 + width ;
            
            JOptionPane pane = new JOptionPane();
            JDialog dialog = pane.createDialog(this, "Help");
            dialog.setSize(200, 350);
            dialog.setModal(false);
            dialog.setVisible(true);
            pane.setMessage(s);
            
    }//GEN-LAST:event_helpButtonActionPerformed
/**
 * Used for displaying simple information about the program
 * @param evt action perfomed on the about button
 */
    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
            String pt1 = "<html><body width='";
            String pt2 =
                    "'><h2>About</h2>" +
                    "<p>This program was created as a part of my bachelors thesis.<br> " + 
                    "<p>Author: Anna Petráková <br>" +
                    "<p>Supervisor: RNDr. Martin Maška, Ph.D. <br> " +
                    "<p>Institution: Masaryk University <br>" +
                    "<p>Year: 2017 <br></html>";
            int width = 230;
            String s = pt1 + width + pt2 + width ;
            
            JOptionPane.showMessageDialog(this, s, "About", JOptionPane.INFORMATION_MESSAGE);
            
    }//GEN-LAST:event_aboutButtonActionPerformed
/**
 * Used for checking the correct value in threshold field
 * @param evt action perfomed on the threshold text field
 */
    private void thresholdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thresholdFieldActionPerformed
        double value = getParameter(thresholdField,"threshold");
            if (value < minThreshold || value > maxThreshold) {
                JOptionPane.showMessageDialog(null,"Error: Please enter number between " + minThreshold + " and  " + maxThreshold, 
                       "Error Message",JOptionPane.ERROR_MESSAGE);
           }
    }//GEN-LAST:event_thresholdFieldActionPerformed
/**
 * Used for saving current parameters into txt file
 * @param evt action perfomed on the save parameters button
 */
    private void saveParamsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveParamsButtonActionPerformed
        new SaveParameters(this);
    }//GEN-LAST:event_saveParamsButtonActionPerformed
/**
 * Used for loading parameters (and replacint current ones) form a txt file
 * @param evt 
 */
    private void loadParamsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadParamsButtonActionPerformed
        new LoadParameters(this);
    }//GEN-LAST:event_loadParamsButtonActionPerformed

    private void iterResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iterResetButtonActionPerformed
        if (aos.isSelected() && multiRes.isSelected()) {
            iterationsField.setText("10");
        } else {
            iterationsField.setText("200");
        }
    }//GEN-LAST:event_iterResetButtonActionPerformed

    private void threshResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_threshResetButtonActionPerformed
        thresholdField.setText("9E-5");
    }//GEN-LAST:event_threshResetButtonActionPerformed

    private void multiResItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_multiResItemStateChanged
       int state = evt.getStateChange();
        if (state == ItemEvent.SELECTED) {
            
            multiRes.setToolTipText("Multi-resolution technique is used");
            
            if (aos.isSelected()) {
                tauPrevious = tauField.getText();
                tauField.setText(String.valueOf(maxAOSMRTau));
                tauField.setEnabled(false);
                tauResetButton.setEnabled(false);
                tauField.setToolTipText("Tau is " + maxAOSMRTau + " for the combination of multi-resoution technique and AOS scheme");
                iterationsField.setText("10");
            }
            
        } else {
           
            multiRes.setToolTipText("Multi-resolution technique is not used");
            
            if (aos.isSelected()) {
                tauField.setText(tauPrevious);
                tauField.setEnabled(true);
                tauResetButton.setEnabled(true);
                tauField.setToolTipText("Value between 0 and " + maxTau);
            }
        }
        if (original != null) {
            setSigmaField();
        }
    }//GEN-LAST:event_multiResItemStateChanged
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JTextField alphaField;
    private javax.swing.JLabel alphaLabel;
    private javax.swing.JButton alphaResetButton;
    private javax.swing.JRadioButton aos;
    private javax.swing.JRadioButton explicit;
    private javax.swing.ButtonGroup groupScheme;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton iterResetButton;
    private javax.swing.JLabel iterations;
    private javax.swing.JTextField iterationsField;
    private javax.swing.JLabel iterationsLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelParameters;
    private javax.swing.JLabel labelResoolution;
    private javax.swing.JLabel labelScheme;
    private javax.swing.JList listImages;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton loadParamsButton;
    private javax.swing.JCheckBox multiRes;
    private javax.swing.JPanel panelLoad;
    private javax.swing.JPanel panelScheme;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton resetAllButton;
    private javax.swing.JButton saveParamsButton;
    private javax.swing.JTextField sigmaField;
    private javax.swing.JLabel sigmaLabel;
    private javax.swing.JButton sigmaResetButton;
    private javax.swing.JButton startButton;
    private javax.swing.JTextField tauField;
    private javax.swing.JLabel tauLabel;
    private javax.swing.JButton tauResetButton;
    private javax.swing.JButton threshResetButton;
    private javax.swing.JTextField thresholdField;
    private javax.swing.JLabel thresholdLabel;
    // End of variables declaration//GEN-END:variables
}
