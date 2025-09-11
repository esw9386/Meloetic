import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.midi.*;
import javax.swing.*;

public class poem {
    static JTextArea ta;
    static Synthesizer synth;
    static MidiChannel channel;
    static PoemListener pl = new PoemListener();
    static int key = 0, oct = 5, scale = 0;
    public static void main(String[] args) {
        final String[] SCALES = {"Maj", "Nat Min", "Hrm Min"};
        final String[] INSTRS = {"Piano", "Guitar", "Flute"};
        final int[] PROGS = {0,24,73};
        final String[] KEYS = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"};
        JFrame jf = new JFrame("Meloetic");
        jf.setSize(700, 700); jf.setMinimumSize(new Dimension(500,200));
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel main = new JPanel(new BorderLayout());
        
        JLabel prompt = new JLabel("Write a poem! Then press \"PLAY\" to hear its melody.");
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(prompt, BorderLayout.NORTH);
        
        try {synth = MidiSystem.getSynthesizer(); synth.open();}
        catch (MidiUnavailableException mue) {System.out.println(mue);}
        channel = synth.getChannels()[0];
        // Instrument instrument = synth.getDefaultSoundbank().getInstruments()[0];
        // boolean loaded = synth.loadInstrument(instrument);

        ta = new JTextArea();
        main.add(ta, BorderLayout.CENTER);

        JPanel ctrl = new JPanel();
        ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.Y_AXIS));

        JPanel pitch = new JPanel();
        JLabel rootLbl = new JLabel("Root:");
        JSlider root = new JSlider(0,11,0);
        Dictionary<Integer, JLabel> labels = new Hashtable<>();
        for (int i=0; i<12; i++) {labels.put(i, new JLabel(KEYS[i]));}
        root.setLabelTable(labels);
        root.setPaintLabels(true);
        root.setMajorTickSpacing(1);
        root.setPaintTicks(true);
        root.setSnapToTicks(true);
        root.addChangeListener((e)->key=root.getValue());
        pitch.add(rootLbl); pitch.add(root);

        JLabel octave = new JLabel("Octave: " + oct);
        JPanel btnsOct = new JPanel();
        btnsOct.setLayout(new BoxLayout(btnsOct, BoxLayout.Y_AXIS));
        JButton octUp = new JButton("\u2191");
        JButton octDown = new JButton("\u2193");
        octUp.addActionListener((e)->{if(oct<9){oct++;}});
        octDown.addActionListener((e)->{if(oct>0){oct--;}});
        btnsOct.add(octUp); btnsOct.add(octDown);
        pitch.add(octave); pitch.add(btnsOct);
        ctrl.add(pitch);

        JPanel btns = new JPanel(new GridLayout(1,7));
        for (int i=0; i<3; i++) {
            IButton btn = new IButton(i, SCALES[i]); 
            btn.addActionListener((e)->{scale=btn.i;});
            btns.add(btn);
        }
        for (int i=0; i<3; i++) {
            IButton btn = new IButton(PROGS[i], INSTRS[i]); 
            btn.addActionListener((e)->{channel.programChange(btn.i);});
            btns.add(btn);
        }
        JButton play = new JButton("PLAY");
        play.setBackground(Color.BLUE); play.setForeground(Color.WHITE);
        play.setBorderPainted(false);
        play.setOpaque(true);
        play.addActionListener(pl);
        btns.add(play);
        ctrl.add(btns);
    
        main.add(ctrl, BorderLayout.SOUTH);

        jf.add(main); jf.setVisible(true);
    }

    static class IButton extends JButton {
        int i;
        IButton(int i, String text) {super(text); this.i = i; setBorderPainted(false);}
    }

    static class PoemListener implements ActionListener {
        static final int[] MAJ = {0, 2, 4, 5, 7, 9, 11};
        static final int[] MIN = {0, 2, 3, 5, 7, 8, 10};
        static final int[] HRM = {0, 2, 3, 5, 7, 8, 11};
        static final int[][] STEPS = {MAJ, MIN, HRM};
        static final int value(int deg) {return (deg<8) ? STEPS[scale][deg-1] : 12 + value(deg-7);}

        @Override
        public void actionPerformed(ActionEvent e) {
            if (ta.getText().isBlank()) {return;}
            String[] words = ta.getText().split("\s+");
            for (String word : words) {
                channel.noteOn(12*oct + key + value(word.length()), 100);
                try {Thread.sleep(300);}
                catch (InterruptedException ie) {System.out.println(ie);}
            }
        }
    }
}

// Features to add:
// Live octave label
// Tempo adjustment
// Correct newline parsing
// Read whitespace as rests (silences)
// Highlight words while respective notes are played?
// Assign percussion to punctuation?