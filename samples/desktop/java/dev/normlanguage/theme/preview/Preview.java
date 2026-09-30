package dev.normlanguage.theme.preview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class Preview implements AutoCloseable {
    private final LinkedBlockingQueue<String> events = new LinkedBlockingQueue<>();
    private JFrame frame;
    private JPanel canvas;
    private JPanel sidebar;
    private JLabel title;
    private JLabel sideTitle;
    private JTextField input;
    private JButton toggle;
    private JButton brand;
    private boolean verify;
    private int step;
    private Color initialCanvas;
    private Color initialSidebar;
    private Color darkButton;

    public Preview() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame("Norm theme · independent UI adapter");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent event) { events.add("close"); }
            });
            canvas = new JPanel(new BorderLayout(24, 24));
            canvas.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
            title = new JLabel("One seed. A complete theme.");
            title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
            canvas.add(title, BorderLayout.NORTH);
            JPanel content = new JPanel(new GridLayout(3, 1, 0, 16));
            content.setOpaque(false);
            input = new JTextField("This text survives theme changes");
            input.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            input.select(5, 9);
            content.add(input);
            toggle = new JButton("Switch light / dark");
            toggle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            toggle.addActionListener(event -> events.add("toggle"));
            content.add(toggle);
            brand = new JButton("Use teal brand");
            brand.setFont(toggle.getFont());
            brand.addActionListener(event -> events.add("brand"));
            content.add(brand);
            canvas.add(content, BorderLayout.CENTER);
            sidebar = new JPanel(new BorderLayout());
            sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            sideTitle = new JLabel("Local scope · always dark");
            sideTitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            sidebar.add(sideTitle);
            canvas.add(sidebar, BorderLayout.SOUTH);
            frame.setContentPane(canvas);
            frame.setPreferredSize(new Dimension(720, 460));
            frame.pack();
            frame.setLocationRelativeTo(null);
        });
    }

    public void show(boolean verify) throws Exception {
        this.verify = verify;
        SwingUtilities.invokeAndWait(() -> {
            initialCanvas = canvas.getBackground();
            initialSidebar = sidebar.getBackground();
            frame.setVisible(true);
        });
    }

    public void apply(String background, String foreground, String buttonBackground, String buttonForeground,
                      String sidebarBackground, String sidebarForeground) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            canvas.setBackground(Color.decode(background.substring(0, 7)));
            title.setForeground(Color.decode(foreground.substring(0, 7)));
            input.setBackground(canvas.getBackground());
            input.setForeground(title.getForeground());
            input.setCaretColor(title.getForeground());
            Color buttonBg = Color.decode(buttonBackground.substring(0, 7));
            Color buttonFg = Color.decode(buttonForeground.substring(0, 7));
            for (JButton button : new JButton[]{toggle, brand}) {
                button.setBackground(buttonBg);
                button.setForeground(buttonFg);
                button.setOpaque(true);
                button.setBorderPainted(false);
            }
            sidebar.setBackground(Color.decode(sidebarBackground.substring(0, 7)));
            sideTitle.setForeground(Color.decode(sidebarForeground.substring(0, 7)));
            canvas.repaint();
        });
    }

    public String nextAction() throws Exception {
        if (verify) {
            SwingUtilities.invokeAndWait(() -> {
                if (!input.getText().equals("This text survives theme changes") || input.getSelectionStart() != 5 || input.getSelectionEnd() != 9) {
                    throw new IllegalStateException("Input state changed during theme switch");
                }
                if (step == 0) toggle.doClick();
                else if (step == 1) {
                    if (canvas.getBackground().equals(initialCanvas)) throw new IllegalStateException("Theme did not switch");
                    if (!sidebar.getBackground().equals(initialSidebar)) throw new IllegalStateException("Local dark scope changed mode");
                    darkButton = toggle.getBackground();
                    brand.doClick();
                } else {
                    if (toggle.getBackground().equals(darkButton)) throw new IllegalStateException("Brand did not change");
                    events.add("close");
                }
                step++;
            });
        }
        return events.take();
    }

    public void save(String destination) throws Exception {
        Path path = Path.of(destination).toAbsolutePath();
        Files.createDirectories(path.getParent());
        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            var graphics = image.createGraphics();
            try { canvas.printAll(graphics); } finally { graphics.dispose(); }
        });
        ImageIO.write(image, "png", path.toFile());
    }

    @Override public void close() throws Exception {
        SwingUtilities.invokeAndWait(() -> frame.dispose());
    }
}
