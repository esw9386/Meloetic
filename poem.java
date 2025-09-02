import java.awt.*;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class poem {
    public static void main(String[] args) {
        JFrame jf = new JFrame("Poem");
        jf.setSize(300, 150);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel main = new JPanel(new BorderLayout());

        JTextPane jtp = new JTextPane();
        jtp.setText("Write a poem! Then choose a key and press \"PLAY\".");
        StyledDocument doc = jtp.getStyledDocument();
        SimpleAttributeSet ctr = new SimpleAttributeSet();
        StyleConstants.setAlignment(ctr, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), ctr, false);
        main.add(jtp, BorderLayout.NORTH);

        JTextField jtf = new JTextField();
        main.add(jtf, BorderLayout.CENTER);
        jf.add(main);
        jf.setVisible(true);
    }
}
