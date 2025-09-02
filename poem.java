import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import javax.swing.*;

public class poem {
    static JTextField tf;
    static Synthesizer synth;
    static MidiChannel channel;
    static int scale = 0;
    static final int MIDC = 60;
    public static void main(String[] args) {
        JFrame jf = new JFrame("Poem");
        jf.setSize(300, 300);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel main = new JPanel(new BorderLayout());
        
        JLabel prompt = new JLabel("Write a poem! Then press Enter/Return to hear its melody.");
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(prompt, BorderLayout.NORTH);
        
        try {synth = MidiSystem.getSynthesizer(); synth.open();}
        catch (MidiUnavailableException mue) {System.out.println(mue);}
        channel = synth.getChannels()[0];
        Instrument instrument = synth.getDefaultSoundbank().getInstruments()[0];
        boolean loaded = synth.loadInstrument(instrument);

        tf = new JTextField();
        tf.addActionListener(new PoemListener());
        main.add(tf, BorderLayout.CENTER);

        jf.add(main);
        jf.setVisible(true);
    }

    static class PoemListener implements ActionListener {
        static final int[] MAJ = {0, 2, 4, 5, 7, 9, 11};
        static final int[] MIN = {0, 2, 3, 5, 7, 8, 10};
        static final int[] HRM = {0, 2, 3, 5, 7, 8, 11};
        static final int[][] SCALES = {MAJ, MIN, HRM};
        static final int value(int deg) {return (deg<8) ? SCALES[scale][deg-1] : 12 + value(deg-7);}

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] words = tf.getText().split("\s+");
            for (int i=0; i<words.length; i++) { // convert to ranged for
                channel.noteOn(MIDC + value(words[i].length()), 50);
                try {Thread.sleep(300);}
                catch (InterruptedException ie) {System.out.println(ie);}
            }
        }

    }
}

// Features to add:
// Multiple keys
// Tempo adjustment
// Multiple instruments/voices
// Enter button for multiline poems
// Read whitespace as rests (silences)
// Highlight words while respective notes are played?