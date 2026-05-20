import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;
 
public class BattleManager {
    public Pet player, bot;
    private final Runnable updateUI;
    public int currentStage = 1;
    public boolean isGameOver = false, playerWon = false;
    public boolean isPaused = false;
    public boolean isWaitingForNextStage = false;
 
    public List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    public List<Potion> potions = new ArrayList<>();
    private long lastEnemyShootTime = 0;
    private Random rand = new Random();
 
    public BattleManager(Pet player, Runnable updateUI) {
        this.player = player;
        this.updateUI = updateUI;
        spawnNextEnemy();
 
        Timer manaRegen = new Timer(1000, e -> {
            if (!isGameOver && !isPaused && player.mana < player.maxMana)
                player.mana = Math.min(player.maxMana, player.mana + 5);
        });
        manaRegen.start();
 
        Timer potionSpawner = new Timer(4000, e -> {
            if (!isGameOver && !isPaused && !isWaitingForNextStage) {
                int type = rand.nextInt(2);
                int range = 250;
                int rx = player.x + (rand.nextInt(range * 2) - range);
                int ry = player.y + (rand.nextInt(range * 2) - range);
                rx = Math.max(100, Math.min(rx, 1100));
                ry = Math.max(150, Math.min(ry, 650));
                potions.add(new Potion(rx, ry, type));
            }
        });
        potionSpawner.start();
    }
 
    public void resetGame() {
        currentStage = 1;
        isGameOver = false;
        playerWon = false;
        isPaused = false;
        isWaitingForNextStage = false;
        player.hp = player.maxHp;
        player.mana = player.maxMana;
        player.state = 0;
        spawnNextEnemy();
    }
 
    public void spawnNextEnemy() {
        player.x = 100; player.y = 400;
        enemyProjectiles.clear();
        potions.clear();
        isWaitingForNextStage = false;
        switch (currentStage) {
            case 1: bot = new Pet("Contreras", 100, 12, 5, "contreras.png", 800, 400) {}; break;
            case 2: bot = new Pet("Bolabola", 150, 18, 15, "bolabola (1).png", 800, 400) {}; break;
            case 3: bot = new Pet("Abadinas", 250, 25, 30, "abadinas.png", 800, 400) {}; break;
            case 4: bot = new Pet("Taboada", 500, 40, 50, "taboada.png", 800, 400) {}; break;
        }
    }
 
    public void shootPlasma(boolean isFacingRight) {
        if (isGameOver || isPaused || isWaitingForNextStage || player.projectileActive || player.mana < 20) return;
        player.mana -= 20;
        player.state = 1;
        player.projectileActive = true;
        player.projDir = isFacingRight ? 1 : -1;
        player.projX = isFacingRight ? (player.x + 80) : (player.x - 20);
        player.projY = player.y + 50;
 
        Timer t = new Timer(20, null);
        t.addActionListener(e -> {
            if (isPaused) return;
            player.projX += (22 * player.projDir);
            if (Math.abs(player.projX - bot.x) < 70 && Math.abs(player.projY - bot.y) < 70 && !bot.isInvisible) {
                bot.takeDamage(player.aura);
                player.projectileActive = false;
                if (bot.hp <= 0) checkStageProgress();
                t.stop();
            }
            if (player.projX > 2500 || player.projX < -300) { player.projectileActive = false; t.stop(); }
        });
        t.start();
        new Timer(450, e -> { if(player.state == 1) player.state = 0; }).start();
    }
 
    public void updateAI() {
        if (isGameOver || isPaused || isWaitingForNextStage) return;
        bot.chase(player);
        handleBossPassives();
        handlePotionPickup();
 
        long now = System.currentTimeMillis();
        if (now - lastEnemyShootTime > 1000) {
            bot.state = 1;
            enemyProjectiles.add(new EnemyProjectile(bot.x + 40, bot.y + 40, bot.faceDir, 0, true));
            lastEnemyShootTime = now;
            bot.attackCount++;
            new Timer(450, e -> { if(bot.state == 1) bot.state = 0; }).start();
        }
 
        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile p = enemyProjectiles.get(i);
            p.x += (14 * p.dirX); p.y += (14 * p.dirY);
            if (Math.abs(p.x - (player.x + 64)) < 55 && Math.abs(p.y - (player.y + 64)) < 55) {
                player.takeDamage(bot.aura);
                if (player.hp <= 0) { isGameOver = true; playerWon = false; }
                enemyProjectiles.remove(i);
            } else if (p.x < -200 || p.x > 2500) enemyProjectiles.remove(i);
        }
    }
 
    private void handlePotionPickup() {
        for (int i = potions.size() - 1; i >= 0; i--) {
            Potion p = potions.get(i);
            if (Math.abs((player.x + 64) - (p.x + 40)) < 70 && Math.abs((player.y + 64) - (p.y + 40)) < 70) {
                if (p.type == 0) player.hp = Math.min(player.maxHp, player.hp + 25);
                else player.mana = Math.min(player.maxMana, player.mana + 40);
                potions.remove(i);
            }
        }
    }
 
    private void checkStageProgress() {
        if (currentStage < 4) isWaitingForNextStage = true;
        else { isGameOver = true; playerWon = true; }
    }
 
    private void handleBossPassives() {
        long now = System.currentTimeMillis();
        switch (currentStage) {
            case 1: if (now - bot.lastSkillTime > 3000) { bot.hp = Math.min(bot.maxHp, bot.hp + 5); bot.lastSkillTime = now; } break;
            case 2: if (bot.attackCount >= 3) {
                enemyProjectiles.add(new EnemyProjectile(bot.x, bot.y, 1, 0, true)); 
                enemyProjectiles.add(new EnemyProjectile(bot.x, bot.y, -1, 0, true));
                enemyProjectiles.add(new EnemyProjectile(bot.x, bot.y, 0, 1, true)); 
                enemyProjectiles.add(new EnemyProjectile(bot.x, bot.y, 0, -1, true));
                bot.attackCount = 0;
            } break;
            case 3: if (now - bot.lastSkillTime > 4000) { bot.isInvisible = true; new Timer(2000, e -> bot.isInvisible = false).start(); bot.lastSkillTime = now; } break;
            case 4: if (now - bot.lastSkillTime > 4000) { bot.isShielded = true; new Timer(3000, e -> bot.isShielded = false).start(); bot.hp = Math.min(bot.maxHp, bot.hp + 12); bot.lastSkillTime = now; } break;
        }
    }
}
 
class EnemyProjectile { 
    int x, y, dirX, dirY; 
    boolean isEnemy; 
    public EnemyProjectile(int x, int y, int dx, int dy, boolean isEnemy) { 
        this.x = x; 
        this.y = y; 
        this.dirX = dx; 
        this.dirY = dy; 
        this.isEnemy = isEnemy; 
    } 
}
 
class Potion { 
    int x, y, type; 
    public Potion(int x, int y, int type) { 
        this.x = x; 
        this.y = y; 
        this.type = type; 
    } 
}
