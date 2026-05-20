import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
 
public class MainMenu extends JPanel {
    private BufferedImage background;
    private JButton startButton, exitButton;
 
    public MainMenu(Runnable onStart) {
        try {
            background = ImageIO.read(new File("assets/img/menu_bg.png"));
        } catch (Exception e) {
            System.out.println("Menu background not found");
        }
 
        this.setLayout(new BorderLayout());
 
        // --- GIPRESERBA NAKO ANG IMONG LABING BAG-O NGA SIZE SETTINGS ---
        int btnWidth = 500;  
        int btnHeight = 310;
 
        try {
            // 1. I-load ug i-scale ang bulag nga Start Exam image
            BufferedImage startImg = ImageIO.read(new File("assets/img/start_exam_btn.png"));
            Image scaledStart = startImg.getScaledInstance(btnWidth, btnHeight, Image.SCALE_SMOOTH);
            startButton = new JButton(new ImageIcon(scaledStart));
 
            // 2. I-load ug i-scale ang bulag nga Drop Out image
            BufferedImage dropImg = ImageIO.read(new File("assets/img/drop_out_btn.png"));
            Image scaledDrop = dropImg.getScaledInstance(btnWidth, btnHeight, Image.SCALE_SMOOTH);
            exitButton = new JButton(new ImageIcon(scaledDrop));
 
        } catch (Exception e) {
            System.out.println("Button images not found! Using fallback layout.");
            startButton = new JButton("START EXAM");
            exitButton = new JButton("DROP OUT");
        }
 
        // --- BUTTON STYLING ---
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        startButton.setFocusPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(e -> onStart.run());
 
        exitButton.setContentAreaFilled(false);
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> {
            System.out.println("Dropping out... Closing application.");
            System.exit(0);
        });
 
        // --- ABSOLUTE COORDS WITHOUT GLITCH OVERLAP ---
        JPanel overlayButtonPanel = new JPanel(null);
        overlayButtonPanel.setOpaque(false);
       
        // Gidakoan nato ang height sa panel container ngadto sa 600 para makaginhawa ang duha ka dako nga image
        overlayButtonPanel.setPreferredSize(new Dimension(btnWidth, 600));
 
        // GI-FIX ANG BOUNDS AT THE EXACT BALANCE:
        // Ang Start Exam magsugod sa Y: 0
        // Ang Drop Out gi-atras nato ngadto sa Y: 220 aron sakto ra ang iyang visual distance (dili layo, dili duol)
        // ug dili na gyud masalipdan ang mouse registration trigger ni Drop Out!
        startButton.setBounds(0, 0, btnWidth, btnHeight);
        exitButton.setBounds(0, 220, btnWidth, btnHeight);
 
        // I-add na silang duha sa sulod sa customized overlay panel container
        overlayButtonPanel.add(exitButton);
        overlayButtonPanel.add(startButton);
 
        // --- OUTER WRAPPER CONTAINER PARA SA POSITIONING ---
        JPanel rightStackPanel = new JPanel(new BorderLayout());
        rightStackPanel.setOpaque(false);
       
        // Gi-adjust ang top margin padding ngadto sa 130 aron ang tibuok pundok mosakay tungod sa fire extinguisher sa tuo
        rightStackPanel.setBorder(BorderFactory.createEmptyBorder(130, 0, 50, 160));
        rightStackPanel.add(overlayButtonPanel, BorderLayout.CENTER);
 
        this.add(rightStackPanel, BorderLayout.EAST);
    }
 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        else {
            g.setColor(new Color(58, 68, 84));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
