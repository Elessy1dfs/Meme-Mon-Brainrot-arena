import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.Timer;
public abstract class Pet {
    public String name;
    public int hp, maxHp, aura, rizz;
    public int mana, maxMana = 100;
    public int x, y;
    public int frameIndex = 0;
    public int state = 0;
    public BufferedImage spriteSheet;
    public boolean projectileActive = false;
    public int projX, projY, projDir = 1;
    public long lastSkillTime = 0;
    public int attackCount = 0;
    public boolean isInvisible = false, isShielded = false;
    public int faceDir = -1;
    public Pet(String name, int hp, int aura, int rizz, String spritePath, int x, int y) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.mana = maxMana;
        this.aura = aura;
        this.rizz = rizz;
        this.x = x;
        this.y = y;
        try {
            this.spriteSheet = ImageIO.read(new File("assets/img/" + spritePath));
        } catch (Exception e) {
            System.out.println("Resource error: " + spritePath);
        }
    }
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
 
        if (this.x < 0) this.x = 0;
        if (this.x > 1800) this.x = 1800;
 
        if (this.y < 50) this.y = 50;   
        if (this.y > 900) this.y = 900;
 
        if (dx > 0) faceDir = 1;
        else if (dx < 0) faceDir = -1;
    }
    public void chase(Pet target) {
        int speed = 3;
        if (this.x < target.x) { this.x += speed; faceDir = 1; }
        else if (this.x > target.x) { this.x -= speed; faceDir = -1; }
        if (this.y < target.y) this.y += speed;
        else if (this.y > target.y) this.y -= speed;
    }
    public void takeDamage(int damage) {
        if (isShielded) return;
        state = 2; // Switch to hit state animation
        // Fast reset: Switch back to idle/walking state after 150ms
        Timer hitResetTimer = new Timer(150, e -> {
            if (state == 2) { 
                state = 0; 
            }
        });
        hitResetTimer.setRepeats(false); // Run only once per hit
        hitResetTimer.start();
 
        int actualDamage = Math.max(1, damage - (rizz / 10));
        this.hp = Math.max(0, this.hp - actualDamage);
    }
    public BufferedImage getCurrentFrame() {
        if (spriteSheet == null) return null;
        int colWidth = spriteSheet.getWidth() / 4;
        int rowHeight = spriteSheet.getHeight() / 3;
        int startY = state * rowHeight;
        int yOffset = 0; 
        return spriteSheet.getSubimage(frameIndex * colWidth, startY + yOffset, colWidth, rowHeight - yOffset);
    }
}
