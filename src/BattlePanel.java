import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
 
public class BattlePanel extends JPanel {
    private final BattleManager manager;
    private final Runnable backToMenu; 
    private Image playerWeaponImg, enemyWeaponImg, victoryImg, defeatedImg, hpPotionImg, manaPotionImg;
    private Image[] backgrounds = new Image[5];
    private boolean facingRight = true;
    private final Set<Integer> pressedKeys = new HashSet<>();
    
    // Mga variable para sa zoom animation sa game over screen
    private double gameOverScale = 0.1; 
    private boolean zoomResetDone = false; 
 
    public BattlePanel(BattleManager manager, Runnable backToMenu) {
        this.manager = manager;
        this.backToMenu = backToMenu;
        this.setFocusable(true);
        this.setLayout(new BorderLayout());
        
        try {
            playerWeaponImg = ImageIO.read(new File("assets/img/pencil.png"));
            enemyWeaponImg = ImageIO.read(new File("assets/img/book.png"));
            victoryImg = ImageIO.read(new File("assets/img/victory.png"));
            defeatedImg = ImageIO.read(new File("assets/img/defeated.png"));
            hpPotionImg = ImageIO.read(new File("assets/img/hp_potion.png"));
            manaPotionImg = ImageIO.read(new File("assets/img/mana_potion.png"));
            backgrounds[1] = ImageIO.read(new File("assets/img/prelim_bg.png"));
            backgrounds[2] = ImageIO.read(new File("assets/img/midterm_bg.png"));
            backgrounds[3] = ImageIO.read(new File("assets/img/prefinal_bg.png"));
            backgrounds[4] = ImageIO.read(new File("assets/img/final_bg.png"));
        } catch (Exception e) { 
            System.out.println("⚠️ Image Missing " + e.getMessage());
        }
 
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_SPACE) manager.shootPlasma(facingRight);
                if (e.getKeyCode() == KeyEvent.VK_P) manager.isPaused = !manager.isPaused;
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (manager.isWaitingForNextStage) { manager.currentStage++; manager.spawnNextEnemy(); }
                    else if (manager.isGameOver) {
                        gameOverScale = 0.1;
                        zoomResetDone = false;
                        manager.resetGame();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && (manager.isGameOver || manager.isPaused)) backToMenu.run();
            }
            @Override public void keyReleased(KeyEvent e) { pressedKeys.remove(e.getKeyCode()); }
        });
 
        new Timer(30, e -> {
            if (!manager.isGameOver && !manager.isPaused && !manager.isWaitingForNextStage) {
                handleJoystickMovement();
                manager.updateAI();
                manager.player.frameIndex = (manager.player.frameIndex + 1) % 4;
                manager.bot.frameIndex = (manager.bot.frameIndex + 1) % 4;
                zoomResetDone = false; 
            } else if (manager.isGameOver) {
                if (gameOverScale < 1.0) {
                    gameOverScale += 0.05; 
                    if (gameOverScale > 1.0) gameOverScale = 1.0;
                }
            }
            repaint();
        }).start();
    }
 
    
    private void handleJoystickMovement() {
        int dx = 0, dy = 0;
        
   
        int speed = (getWidth() > 0) ? Math.max(10, (int)(getWidth() * 0.0125)) : 15;
        
        if (pressedKeys.contains(KeyEvent.VK_LEFT)) { dx -= speed; facingRight = false; }
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) { dx += speed; facingRight = true; }
        if (pressedKeys.contains(KeyEvent.VK_UP)) dy -= speed;
        if (pressedKeys.contains(KeyEvent.VK_DOWN)) dy += speed;
        
        manager.player.move(dx, dy);
    }
 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        if (backgrounds[manager.currentStage] != null) g2.drawImage(backgrounds[manager.currentStage], 0, 0, getWidth(), getHeight(), null);
 
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial Black", Font.BOLD, 35));
        String title = getStageTitle(manager.currentStage);
        g2.drawString(title, (getWidth() - g2.getFontMetrics().stringWidth(title))/2, 65);
 
        for (Potion p : manager.potions) {
            Image pi = (p.type == 0) ? hpPotionImg : manaPotionImg;
            g2.drawImage(pi, p.x, p.y, 150, 80, null);
        }
 
        if (manager.player.projectileActive) {
            g2.drawImage(playerWeaponImg, manager.player.projX, manager.player.projY, 50, 50, null);
        }
        
        for (EnemyProjectile ep : manager.enemyProjectiles) {
            Image currentProj = ep.isEnemy ? enemyWeaponImg : playerWeaponImg;
            g2.drawImage(currentProj, ep.x, ep.y, 65, 65, null);
        }
 
        drawPet(g2, manager.player, facingRight, false);
        if (!manager.bot.isInvisible) drawPet(g2, manager.bot, manager.bot.faceDir == 1, true);
 
        drawUI(g2, manager.player, true);
        drawUI(g2, manager.bot, false);
 
        if (manager.isPaused && !manager.isGameOver) {
            drawOverlay(g2, "PAUSED", "Press 'P' to Resume | ESC for Menu", Color.ORANGE);
        }
        if (manager.isWaitingForNextStage) {
            drawOverlay(g2, "YOU PASSED!", "Press ENTER to continue", Color.GREEN);
        }
        
        if (manager.isGameOver) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            Image res = manager.playerWon ? victoryImg : defeatedImg;
            
            if (res != null) {
                int imgWidth = (int) (getWidth() * gameOverScale);
                int imgHeight = (int) (getHeight() * gameOverScale);
                int x = (getWidth() - imgWidth) / 2;
                int y = (getHeight() - imgHeight) / 2;
                g2.drawImage(res, x, y, imgWidth, imgHeight, null);
            }
            
            if (gameOverScale >= 0.8) {
                g2.setFont(new Font("Arial Black", Font.BOLD, 28)); 
                g2.setColor(Color.WHITE);
                String sub = "ENTER to Restart | ESC for Menu";
                g2.drawString(sub, (getWidth() - g2.getFontMetrics().stringWidth(sub))/2, getHeight() - 120);
            }
        }
    }
 
    private void drawOverlay(Graphics2D g2, String main, String sub, Color color) {
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(color); g2.setFont(new Font("Arial Black", Font.BOLD, 60));
        g2.drawString(main, (getWidth() - g2.getFontMetrics().stringWidth(main))/2, getHeight()/2);
        g2.setFont(new Font("Arial Black", Font.BOLD, 20)); g2.setColor(Color.WHITE);
        g2.drawString(sub, (getWidth() - g2.getFontMetrics().stringWidth(sub))/2, (getHeight()/2) + 50);
    }
 
    private void drawPet(Graphics2D g2, Pet p, boolean right, boolean isBoss) {
        BufferedImage img = p.getCurrentFrame();
        if (img == null) return;
 
        if (right) {
            g2.drawImage(img, p.x, p.y, 128, 128, null);
        } else {
            g2.drawImage(img, p.x + 128, p.y, -128, 128, null);
        }
 
        if (isBoss && p.isShielded) { 
            g2.setColor(new Color(0, 255, 255, 100)); 
            g2.fillOval(p.x, p.y, 128, 128); 
        }
    }
 
    private void drawUI(Graphics2D g, Pet p, boolean isPlayer) {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(p.name, p.x + (64 - fm.stringWidth(p.name)/2), p.y - 45);
        g.setColor(Color.RED); g.fillRect(p.x + 14, p.y - 35, 100, 10);
        g.setColor(Color.GREEN); g.fillRect(p.x + 14, p.y - 35, (int)(p.hp/(float)p.maxHp * 100), 10);
        if (isPlayer) {
            g.setColor(Color.BLUE); g.fillRect(p.x + 14, p.y - 22, (int)(p.mana/(float)p.maxMana * 100), 8);
        } else {
            g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString(getSkillText(manager.currentStage), p.x + 14, p.y - 10);
        }
    }
 
    private String getStageTitle(int s) {
        switch(s) { 
            case 1: return "PRELIM: CONTRERAS"; 
            case 2: return "MIDTERM: BOLABOLA"; 
            case 3: return "PRE-FINAL: ABADINAS"; 
            case 4: return "FINALS: TABOADA"; 
            default: return ""; 
        }
    }
 
    private String getSkillText(int s) {
        switch(s) { 
            case 1: return "PASSIVE: REGEN"; 
            case 2: return "SKILL: 4-WAY"; 
            case 3: return "PASSIVE: STEALTH"; 
            case 4: return "PASSIVE: SHIELD"; 
            default: return ""; 
        }
    }
}
