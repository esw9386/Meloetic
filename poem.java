import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.midi.*;
import javax.swing.*;

public class poem {
    static JTextArea ta;
    static Synthesizer synth;
    static Sequencer seqr;
    static Sequence seq;
    static Track tr;
    static MidiChannel channel;
    static PoemListener pl = new PoemListener();
    static IButton play = new IButton(0, "PLAY");
    static Color DARK_BLUE = new Color(0x0000A0);
    static int key = 0, oct = 5, scale = 0;
    static float tempo = 120;
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
        
        try {
            synth = MidiSystem.getSynthesizer(); 
            seqr = MidiSystem.getSequencer();
            synth.open(); seqr.open();
            seq = new Sequence(Sequence.PPQ, 24);
            seqr.addMetaEventListener(m->{if(m.getType()==0x2F){play.setBackground(Color.BLUE);}});
            tr = seq.createTrack();
        }
        catch (MidiUnavailableException | InvalidMidiDataException me) {System.out.println(me);}
        channel = synth.getChannels()[0];

        ta = new JTextArea();
        main.add(ta, BorderLayout.CENTER);

        JPanel ctrl = new JPanel();
        ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel();
        JSlider root = new JSlider(0,11,0);
        Dictionary<Integer, JLabel> labels = new Hashtable<>();
        for (int i=0; i<12; i++) {labels.put(i, new JLabel(KEYS[i]));}
        root.setLabelTable(labels);
        root.setPaintLabels(true);
        root.setMajorTickSpacing(1);
        root.setPaintTicks(true);
        root.setSnapToTicks(true);
        root.addChangeListener(_->key=root.getValue());
        row1.add(new JLabel("Root:")); row1.add(root);

        final String OCT = "Octave: ";
        JLabel octave = new JLabel(OCT + oct);
        JPanel btnsOct = new JPanel();
        btnsOct.setLayout(new BoxLayout(btnsOct, BoxLayout.Y_AXIS));
        JButton octUp = new JButton("\u2191");
        JButton octDown = new JButton("\u2193");
        octUp.addActionListener(_->{if(oct<9){oct++;octave.setText(OCT+String.valueOf(oct));}});
        octDown.addActionListener(_->{if(oct>0){oct--;octave.setText(OCT+String.valueOf(oct));}});
        btnsOct.add(octUp); btnsOct.add(octDown);
        row1.add(octave); row1.add(btnsOct);
        
        JTextField tmp = new JTextField("120.0");
        tmp.setColumns(4); tmp.setHorizontalAlignment(JTextField.CENTER);
        tmp.addActionListener(_->{
            try {
                float temp = Float.parseFloat(tmp.getText());
                if (5<temp && temp<300) {tempo=temp;}
                else {System.err.println("Tempos between 5.0 and 300.0 BPM accepted");}
            } catch (NumberFormatException nfe) {System.err.println("Non-numerical tempo");}
            tmp.setText(String.valueOf(tempo));
        });
        row1.add(new JLabel("Tempo:")); row1.add(tmp);
        ctrl.add(row1);

        JPanel row2 = new JPanel(new GridLayout(1,7));
        for (int i=0; i<6; i++) { // this loop >>
            IButton btn = i<3 ? new IButton(i, SCALES[i]) : new IButton(PROGS[i-3], INSTRS[i-3]); 
            btn.addActionListener(i<3 ? _->{scale=btn.i;} : _->{channel.programChange(btn.i);});
            row2.add(btn);
        }
        play.setBackground(Color.BLUE); play.setForeground(Color.WHITE);
        play.addActionListener(pl);
        play.setOpaque(true);
        row2.add(play);
        ctrl.add(row2);
    
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
        static String[] words = {};
        static int[] degs = {}; // to compare isomorphic (or something) poems

        @Override
        public void actionPerformed(ActionEvent e) {
            seqr.stop(); seqr.setTickPosition(0); seqr.setTempoInBPM(tempo);
            String poem = ta.getText();
            if (poem.isBlank()) {return;}
            String[] tokens = poem.split("\\s+"); // \s being any whitespace char
            if (!words.equals(tokens)) {
                words = tokens;
                while (tr.size()>0) {tr.remove(tr.get(tr.size()-1));}
                try {
                    for (String word:words) {
                        int pitch = 12*oct + key + value(word.length());
                        ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, 100);
                        ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, 0, pitch, 0);
                        tr.add(new MidiEvent(on, tr.ticks()));
                        tr.add(new MidiEvent(off, tr.ticks()+24)); // 24 ticks/beat
                    }
                    seqr.setSequence(seq);
                } catch (InvalidMidiDataException imde) {System.out.println(imde);}
            }
            play.setBackground(DARK_BLUE);
            seqr.start();
        }
    }
}

// Features to add:
// Restore instrument selection
// Read whitespace as rests (silences)
// Highlight words while respective notes are played?
// Assign percussion to punctuation?