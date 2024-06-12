import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sound {
	
	private Clip clip;
	private File soundFile;

	
	public Sound(String soundFilePath) {
		
		// load the sound file
		soundFile = new File(soundFilePath);
	}
	
	public void playSound() {
		try {
	        // Get an AudioInputStream from the sound file
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

	        // Get a Clip object to play the sound
	        clip = AudioSystem.getClip();

	        // Open the clip with the AudioInputStream
	        clip.open(audioInputStream);
	    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
	        e.printStackTrace();
	    }
	}
	public void loopSound() {
		// Set the clip to loop indefinitely
        clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	public void playOnce() {
		clip.start();
	}
	public void stopSound() {
	    if (clip != null && clip.isRunning()) {
	        clip.stop();
	        clip.flush();
	        clip = null;
	    }
	}
}
