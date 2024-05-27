package sounds;


import javax.sound.sampled.*;
import java.io.IOException;

public class Sound {

	public static synchronized void playSound(final String strPath) {
	    new Thread(() -> {
          try {
            Clip clp = AudioSystem.getClip();

            AudioInputStream aisStream =
                    AudioSystem.getAudioInputStream(Sound.class.getResourceAsStream(strPath));


            clp.open(aisStream);
            clp.start();
          } catch (Exception e) {
            System.err.println(e.getMessage());
          }
        }).start();
	  }
	

	public static Clip clipForLoopFactory(String strPath){
		
		Clip clp = null;

		try {
			AudioInputStream aisStream = 
					  AudioSystem.getAudioInputStream(Sound.class.getResourceAsStream(strPath));
			clp = AudioSystem.getClip();
		    clp.open( aisStream );
				
		} catch(Exception exp){
			System.out.println("error");
		}
		
		return clp;
		
	}
}
