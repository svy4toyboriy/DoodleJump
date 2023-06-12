import javax.swing.*; //для выводка окна
import javax.imageio.ImageIO;
import java.util.*;//для функции рандома.
import java.awt.*;//для рисования в окне
import java.awt.image.BufferedImage; // для изображений
import java.awt.event.KeyEvent;//для работы с клавиатурой
import java.awt.event.KeyListener;


class PlatformPosition {
    int x, y;
}
class MonsterPosition {
    int x, y;
}
class TrampolinePosition {
    int x, y;
}


public class DoodleJump extends JPanel implements Runnable, KeyListener {
    final int WIDTH = 400;//размеры окна игры
    final int HEIGHT = 533;

    boolean isRunning;//статус состояния игры
    BufferedImage view, background, defaultPlatform, brokenPlatform, doodle, monster, trampoline;
    Thread thread;

    int numberOfDefaultPlatforms = 12;
    int numberOfBrokenPlatforms = 3;
    int numberOfMonsters = 1;
    int numberOfTrampolines = 1;
    int updatedNumberOfDefaultPlatforms = numberOfDefaultPlatforms;

    int startOfMonsters = 4000; //numbers mean score(pixels)
    int startOfTrampolines = 1000;
    int startOfBrokenPlatforms = 3000;
    int startOfMoving = 5000;

    boolean[] isBrokenPlatformFalling = new boolean[numberOfBrokenPlatforms];
    boolean[] isMonsterFalling = new boolean[numberOfMonsters];
    int[] directionOfDefaultPlatform = new int[numberOfDefaultPlatforms];
    int[] directionOfBrokenPlatform = new int[numberOfBrokenPlatforms];

    MonsterPosition[] monstersPosition;
    PlatformPosition[] defaultPlatformsPosition, brokenPlatformsPosition;
    TrampolinePosition[] trampolinesPosition;

    int x = 165, y = 497, h = 150;
    float dy = 0;
    boolean right, left;

    // for calculating score
    float jumpHeight = 0;
    int lastJumpAt = 0; // score but without last jump
    int prevPlatform = -1; //index of previous platform you jumped from, -1 means last jump was from monster/trampoline
    float finalScore = 0;
    int finalScoreInt;

    public DoodleJump() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
    }

    public static void main(String[] args) {// настраеваем окно
        JFrame w = new JFrame("Doodle Jump");
        w.setResizable(false);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.add(new DoodleJump());
        w.pack();
        w.setLocationRelativeTo(null);
        w.setVisible(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    public void start() {
        dy = -10;
        try {
            view = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

            background = ImageIO.read(getClass().getResource("background.png"));//достаем картинки
            defaultPlatform = ImageIO.read(getClass().getResource("defaultPlatform.png"));
            brokenPlatform = ImageIO.read(getClass().getResource("brokenPlatform.png"));
            doodle = ImageIO.read(getClass().getResource("guy.png"));
            monster = ImageIO.read(getClass().getResource("monster.png"));
            trampoline = ImageIO.read(getClass().getResource("trampoline.png"));

            defaultPlatformsPosition = new PlatformPosition[numberOfDefaultPlatforms];
            brokenPlatformsPosition = new PlatformPosition[numberOfBrokenPlatforms];
            monstersPosition = new MonsterPosition[numberOfMonsters];
            trampolinesPosition = new TrampolinePosition[numberOfTrampolines];

            for (int i = 0; i < numberOfDefaultPlatforms; i++) {
                directionOfDefaultPlatform[i] = 2; //2 и 3 - нет движения, 0, 1 - влево, вправо
                defaultPlatformsPosition[i] = new PlatformPosition();//тут и снизу генерируем координаты платформ, монстров итд
                defaultPlatformsPosition[i].x = new Random().nextInt(400 - 70);
                defaultPlatformsPosition[i].y = new Random().nextInt(533);
            }
            for (int i = 0; i < numberOfMonsters; i++) {
                isMonsterFalling[i] = false;
                monstersPosition[i] = new MonsterPosition();
                monstersPosition[i].x = new Random().nextInt(400 - 70);
                monstersPosition[i].y = new Random().nextInt(800)*(-1);
            }
            for (int i = 0; i < numberOfBrokenPlatforms; i++) {
                isBrokenPlatformFalling[i] = false;
                directionOfBrokenPlatform[i] = 2;
                brokenPlatformsPosition[i] = new PlatformPosition();
                brokenPlatformsPosition[i].x = new Random().nextInt(400 - 70);
                brokenPlatformsPosition[i].y = new Random().nextInt(330)*(-1);
            }
            for (int i = 0; i < numberOfTrampolines; i++) {
                trampolinesPosition[i] = new TrampolinePosition();
                trampolinesPosition[i].x = new Random().nextInt(400 - 70);
                trampolinesPosition[i].y = new Random().nextInt(800)*(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (right) {
            x += 3;
        }
        if (left) {
            x -= 3;
        }
        dy += 0.2;
        y += dy;
        if (dy < 0) {
            jumpHeight -= dy;
            if (lastJumpAt + jumpHeight > finalScore) finalScore = lastJumpAt + jumpHeight;
        }
        if (x < 0) {
            x = 0;
        }
        if (x > 335) {
            x = 335;
        }
        if (y > 497) {
            isRunning = false;
        }
        int i;
        if (finalScore > startOfBrokenPlatforms) {
            updatedNumberOfDefaultPlatforms = numberOfDefaultPlatforms - numberOfBrokenPlatforms;
        }
        if (y < h) {
            y = h;
            for (i = 0; i < numberOfDefaultPlatforms; i++) {//перемещаем платформы вниз против движения игрока
                defaultPlatformsPosition[i].y = defaultPlatformsPosition[i].y - (int) dy;
                if (defaultPlatformsPosition[i].y > 533 && i < updatedNumberOfDefaultPlatforms) {// и создаем новые, если платформа вышла вниз.
                    defaultPlatformsPosition[i].y = new Random().nextInt(100)*(-1);
                    defaultPlatformsPosition[i].x = new Random().nextInt(330);
                    if (finalScore > startOfMoving) {
                        directionOfDefaultPlatform[i] = new Random().nextInt(4);//движение платформ влево-вправо
                    }
                }
            }
            if (finalScore > startOfBrokenPlatforms) {
                for (i = 0; i < numberOfBrokenPlatforms; i++) {//перемещаем платформы вниз против движения игрока
                    brokenPlatformsPosition[i].y = brokenPlatformsPosition[i].y - (int) dy;
                    if (brokenPlatformsPosition[i].y > 533) {// и создаем новые, если платформа вышла вниз.
                        brokenPlatformsPosition[i].y = new Random().nextInt(300)*(-1);
                        brokenPlatformsPosition[i].x = new Random().nextInt(400 - 68);
                        isBrokenPlatformFalling[i] = false;
                        if (finalScore > startOfMoving) {
                            directionOfBrokenPlatform[i] = new Random().nextInt(4);//движение платформ влево-вправо
                        }
                        if (isBrokenPlatformFalling[i]) {
                            isBrokenPlatformFalling[i] = false;
                        }
                    }
                }
            }
            if (finalScore > startOfMonsters) {
                monstersUpdate();
            }
            if (finalScore > startOfTrampolines) {
                trampolineUpdate();
            }
        }
        for (i = 0; i < numberOfDefaultPlatforms; i++) {
            if ((x + 30 > defaultPlatformsPosition[i].x) &&
                    (x < defaultPlatformsPosition[i].x + 48) &&
                    (y + 70 > defaultPlatformsPosition[i].y) &&
                    (y + 70 < defaultPlatformsPosition[i].y + 14) &&
                    (dy > 0)) {
                if (prevPlatform == -1) {
                    lastJumpAt += (jumpHeight - (533 - defaultPlatformsPosition[i].y));
                } else {
                    lastJumpAt += defaultPlatformsPosition[prevPlatform].y - defaultPlatformsPosition[i].y;
                }
                prevPlatform = i;
                dy = -10;
                jumpHeight = 0;
            }
        }
        if (finalScore > startOfMonsters) {
            monstersAction();
        }
        if (finalScore > startOfTrampolines) {
            trampolineAction();
        }
        if (finalScore > startOfBrokenPlatforms) {
            for (i = 0; i < numberOfBrokenPlatforms; i++) {//то же самое, но для ломающихся
                if ((x + 50 > brokenPlatformsPosition[i].x) &&
                        (x + 20 < brokenPlatformsPosition[i].x + 68) &&
                        (y + 70 > brokenPlatformsPosition[i].y) &&
                        (y + 70 < brokenPlatformsPosition[i].y + 14) &&
                        (dy > 0) && !isBrokenPlatformFalling[i]) {
                    isBrokenPlatformFalling[i] = true;
                }
            }
            for (i = 0; i < numberOfBrokenPlatforms; i++) {
                if (isBrokenPlatformFalling[i]) {
                    brokenPlatformsPosition[i].y += 13;//сломанная платформа улетает вниз по своему закону
                }
            }
        }
        if (finalScore > startOfMoving) {
            for (i = 0; i < numberOfDefaultPlatforms; i++) {
                if (directionOfDefaultPlatform[i] < 2) {//меняем направление двигающихся платформ
                    if (directionOfDefaultPlatform[i] == 0) defaultPlatformsPosition[i].x -= 1;
                    else defaultPlatformsPosition[i].x += 1;
                    if (defaultPlatformsPosition[i].x > 400 - 68 || defaultPlatformsPosition[i].x < 0)
                        directionOfDefaultPlatform[i] ^= 1;
                }
            }
            for (i = 0; i < numberOfBrokenPlatforms; i++) {
                if (directionOfBrokenPlatform[i] < 2) {//меняем направление двигающихся платформ
                    if (directionOfBrokenPlatform[i] == 0) brokenPlatformsPosition[i].x -= 1;
                    else brokenPlatformsPosition[i].x += 1;
                    if (brokenPlatformsPosition[i].x > 400 - 68 || brokenPlatformsPosition[i].x < 0)
                        directionOfBrokenPlatform[i] ^= 1;
                }
            }
        }
    }
    public void draw() {
        Graphics2D g2 = (Graphics2D) view.getGraphics();
        g2.drawImage(background, 0, 0, WIDTH, HEIGHT, null);
        g2.drawImage(doodle, x, y, doodle.getWidth(), doodle.getHeight(), null);

        for (int i = 0; i < numberOfDefaultPlatforms; i++) {
            if (defaultPlatformsPosition[i].y <= 533) {
                g2.drawImage(
                        defaultPlatform,
                        defaultPlatformsPosition[i].x,
                        defaultPlatformsPosition[i].y,
                        defaultPlatform.getWidth(),
                        defaultPlatform.getHeight(),
                        null
                );
            }
        }
        if (finalScore > startOfMonsters) {
            for (int i = 0; i < numberOfMonsters; i++) {
                g2.drawImage(
                        monster,
                        monstersPosition[i].x,
                        monstersPosition[i].y,
                        monster.getWidth(),
                        monster.getHeight(),
                        null
                );
            }
        }
        if (finalScore > startOfTrampolines) {
            for (int i = 0; i < numberOfTrampolines; i++) {
                g2.drawImage(
                        trampoline,
                        trampolinesPosition[i].x,
                        trampolinesPosition[i].y,
                        trampoline.getWidth(),
                        trampoline.getHeight(),
                        null
                );
            }
        }
        if (finalScore > startOfBrokenPlatforms) {
            for (int i = 0; i < numberOfBrokenPlatforms; i++) {
                g2.drawImage(
                        brokenPlatform,
                        brokenPlatformsPosition[i].x,
                        brokenPlatformsPosition[i].y,
                        brokenPlatform.getWidth(),
                        brokenPlatform.getHeight(),
                        null
                );
            }
        }
        Graphics g = getGraphics();
        g.drawImage(view, 0, 0, WIDTH, HEIGHT, null);
        finalScoreInt = Math.round(finalScore);
        String scoreS = "Score: " + finalScoreInt;
        g.setFont(new Font("Times New Roman", Font.BOLD, 30));
        g.drawString(scoreS, 5, 35);
        g.dispose();
    }

    public void monstersUpdate() {
        for (int i = 0; i < numberOfMonsters; i++) {
            monstersPosition[i].y = monstersPosition[i].y - (int) dy;
            if (monstersPosition[i].y > 533) {
                monstersPosition[i].y = new Random().nextInt(1600)*(-1);
                monstersPosition[i].x = new Random().nextInt(400 - 70);
                isMonsterFalling[i] = false;
            }
        }
    }

    public void monstersAction() {
        for(int i = 0 ; i < numberOfMonsters; i++)
            if (((x + 30 > monstersPosition[i].x) &&//проверка на прыжок на монстра
                    (x < monstersPosition[i].x + 48) &&
                    (y + 60 > monstersPosition[i].y) &&
                    (y < monstersPosition[i].y ) &&
                    (dy > 0))) {
                if (prevPlatform == -1) {
                    lastJumpAt += (jumpHeight - (533 - monstersPosition[i].y));
                } else {
                    lastJumpAt += defaultPlatformsPosition[prevPlatform].y - monstersPosition[i].y;
                }
                dy = -7;
                prevPlatform = -1;
                jumpHeight = 0;
                isMonsterFalling[i] = true;
            }
            else if (y < monstersPosition[i].y + 40 &&//проверка на прыжок в монстра
                    y + 40 > monstersPosition[i].y &&
                    x + 40 > monstersPosition[i].x &&
                    x < monstersPosition[i].x + 40) {
                isRunning = false;
            }
        for (int i = 0; i < numberOfMonsters; i++) {
            if (isMonsterFalling[i]) {
                monstersPosition[i].y+=6; //монстр летит вниз, если мы его убили
            }
        }
    }

    public void trampolineUpdate() {
        for (int i = 0; i < numberOfTrampolines; i++) {
            trampolinesPosition[i].y = trampolinesPosition[i].y - (int) dy;
            if (trampolinesPosition[i].y > 533) {
                trampolinesPosition[i].y = new Random().nextInt(1600)*(-1);
                trampolinesPosition[i].x = new Random().nextInt(400 - 30);
            }
        }
    }

    public void trampolineAction() {
        for (int i = 0; i < numberOfTrampolines; i++) {
            if ((x + 30 > trampolinesPosition[i].x) &&
                    (x < trampolinesPosition[i].x + 48) &&
                    (y + 60 > trampolinesPosition[i].y) &&
                    (y < trampolinesPosition[i].y ) &&
                    (dy > 0)) {
                if (prevPlatform == -1 ) {
                    lastJumpAt += (jumpHeight - (533 - trampolinesPosition[i].y));
                } else {
                    lastJumpAt += defaultPlatformsPosition[prevPlatform].y - trampolinesPosition[i].y;
                }
                prevPlatform = -1;
                dy = -18;
                jumpHeight = 0;
            }
        }
    }

    @Override
    public void run() {
        try {
            requestFocus();
            start();
            while (isRunning) {
                update();
                draw();
                Thread.sleep(560 / 60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = false;
        }
    }
}



