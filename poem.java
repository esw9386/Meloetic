import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import javax.swing.*;

public class poem {
    static JTextArea ta;
    static Synthesizer synth;
    static Sequencer seqr;
    static MidiChannel channel;
    static PoemListener pl = new PoemListener();
    static int key = 3, scale = 0, instr = 0;
    public static void main(String[] args) {
        final String[] SCALES = {"Major", "Natural Minor", "Harmonic Minor"};
        final String[] INSTRS = {"Piano", "Violin", "Flute"};
        JFrame jf = new JFrame("Poem");
        jf.setSize(700, 700);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel main = new JPanel(new BorderLayout());
        
        JLabel prompt = new JLabel("Write a poem! Then press \"PLAY\" to hear its melody.");
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(prompt, BorderLayout.NORTH);
        
        try {
            synth = MidiSystem.getSynthesizer(); 
            seqr = MidiSystem.getSequencer();
            synth.open(); seqr.open();
        }
        catch (MidiUnavailableException mue) {System.out.println(mue);}
        channel = synth.getChannels()[0];
        Instrument instrument = synth.getDefaultSoundbank().getInstruments()[0];
        // boolean loaded = synth.loadInstrument(instrument);

        ta = new JTextArea();
        // tf.addActionListener(pl);
        main.add(ta, BorderLayout.CENTER);

        JPanel ctrl = new JPanel(new GridLayout(2,7));
        for (int i=0; i<7; i++) {
            IButton btn = new IButton(i, ""+(char)('A'+(6+i)%7));
            btn.addActionListener((e)-> {key=btn.i;});
            btn.setBorderPainted(false);
            ctrl.add(btn);
        }
        for (int i=0; i<3; i++) {
            IButton btn = new IButton(i, SCALES[i]); 
            btn.addActionListener((e)-> {scale=btn.i;});
            btn.setBorderPainted(false);
            ctrl.add(btn);
        }
        for (int i=0; i<3; i++) {
            IButton btn = new IButton(i, INSTRS[i]); 
            btn.addActionListener((e)-> {instr=btn.i;});
            btn.setBorderPainted(false);
            ctrl.add(btn);
        }
        JButton play = new JButton("PLAY");
        play.setBackground(Color.BLUE);
        play.setForeground(Color.WHITE);
        play.setBorderPainted(false);
        play.setOpaque(true);
        play.addActionListener(pl);
        ctrl.add(play);
        main.add(ctrl, BorderLayout.SOUTH);

        jf.add(main);
        jf.setVisible(true);
    }

    static class IButton extends JButton {
        int i;
        IButton(int i, String text) {super(text); this.i = i;}
    }

    static class PoemListener implements ActionListener {
        static final int[] RTS = {55, 57, 59, 60, 62, 64, 65};
        static final int[] MAJ = {0, 2, 4, 5, 7, 9, 11};
        static final int[] MIN = {0, 2, 3, 5, 7, 8, 10};
        static final int[] HRM = {0, 2, 3, 5, 7, 8, 11};
        static final int[][] STEPS = {MAJ, MIN, HRM};
        static final int value(int deg) {return (deg<8) ? STEPS[scale][deg-1] : 12 + value(deg-7);}
        static String[] tokens = {};

        @Override
        public void actionPerformed(ActionEvent e) {
            if (ta.getText().isBlank()) {return;}
            String[] area = ta.getText().split("\s+");
            if (!tokens.equals(area)) {
                tokens = area;
                try {
                    Sequence seq = new Sequence(Sequence.PPQ, 24);
                    Track tr = seq.createTrack();
                    for (String token : tokens) {
                        // System.out.println(token);
                        int pitch = RTS[key] + value(token.length());
                        ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, 50);
                        // channel.noteOn(RTS[key] + value(token.length()), 50);
                        tr.add(new MidiEvent(on, tr.ticks()));
                        // try {Thread.sleep(300);}
                        // catch (InterruptedException ie) {System.out.println(ie);}
                    }
                    seqr.setSequence(seq);
                }
                catch (InvalidMidiDataException imde) {System.out.println(imde);}
            }
            seqr.start();
        }
    }
}

// Features to add:
// Fix multiline parsing bug
// Tempo adjustment
// Multiple instruments/voices
// Read whitespace as rests (silences)
// Highlight words while respective notes are played?
// Assign percussion to punctuation?