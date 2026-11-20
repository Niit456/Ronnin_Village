package gameDev;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;

/** sound effects and music. Missing files are reported once and then ignored, so the game runs without sound. */
public final class Audio {
    private Audio() { }

    static final String BUTTON_SOUND = "res/button.wav";
    static final String LOADING_SOUND = "res/LoadingSound.wav";
    private static final java.util.Set<String> warned = new java.util.HashSet<>();

    private static void warnOnce(String what) {
        if (warned.add(what)) System.err.println(what);
    }

    /** the menu click */
    public static void playButton() { playOnce(BUTTON_SOUND, 6.0f); }
    public static void playLoading() { playOnce(LOADING_SOUND, 0f); }

    /** plays a short sound once (the clip closes itself); gainDb is clamped to what the sound card allows */
    public static void playOnce(String path, float gainDb) {
        try {
            File f = new File(path);
            if (!f.exists()) { warnOnce("Sound not found: " + f.getAbsolutePath()); return; }
            AudioInputStream audio = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(ev -> { if (ev.getType() == LineEvent.Type.STOP) clip.close(); });
            clip.open(audio);
            if (gainDb != 0 && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(Math.min(gainDb, gain.getMaximum()));
            }
            clip.start();
        } catch (Exception e) {
            if (warned.add(String.valueOf(e))) e.printStackTrace();
        }
    }

    /** a looping piece of music that can be started, stopped and turned down */
    public static final class Loop {
        private final String path;          // a file path, or a classpath resource when it starts with '/'
        private final float gainDb;
        private Clip clip;

        public Loop(String path, float gainDb) { this.path = path; this.gainDb = gainDb; }

        public void start() {
            if (clip != null && clip.isOpen()) return;                  // already playing
            try {
                AudioInputStream audio;
                if (path.startsWith("/")) {
                    URL url = Audio.class.getResource(path);
                    if (url == null) { warnOnce("Music not found: " + path); return; }
                    audio = AudioSystem.getAudioInputStream(url);
                } else {
                    File f = new File(path);
                    if (!f.exists()) { warnOnce("Music not found: " + f.getAbsolutePath()); return; }
                    audio = AudioSystem.getAudioInputStream(f);
                }
                Clip c = AudioSystem.getClip();
                c.open(audio);
                clip = c;
                setGain(gainDb);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (Exception e) {
                clip = null;
                if (warned.add(String.valueOf(e))) e.printStackTrace();
            }
        }

        public void setGain(float db) {
            if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
        }

        public void pause()  { if (clip != null && clip.isRunning()) clip.stop(); }
        public void resume() { if (clip == null) { start(); return; } if (!clip.isRunning()) clip.loop(Clip.LOOP_CONTINUOUSLY); }

        public void stop() {
            if (clip == null) return;
            clip.stop(); clip.close(); clip = null;
        }
    }
}