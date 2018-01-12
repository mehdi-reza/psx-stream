package psx.remote;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class Remote {
	public static void main(String[] args) throws AWTException {
		Robot r=new Robot();
		holdAndPress(r, KeyEvent.VK_WINDOWS, KeyEvent.VK_M);
	}
	
	public static void holdAndPress(Robot r, int hold, int key) {
		r.keyPress(hold);
		r.keyPress(key);
		r.keyRelease(key);
		r.keyRelease(hold);
	}
}
