package com.emirenesgames.xpsimulator;

import java.awt.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;

public class XpSimulator extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 320;
	public static final int HEIGHT = 240;
	public static final int SCALE = 3;

	private static boolean CreditsOpened = false;
	
	private int ticks = 0;

	private boolean keepRunning = true;
	private BufferedImage screenImage;

	private Bitmap screenBitmap;

	private InputHandler inputHandler;
	private Input mouse;
	
	private Button[] hitboxs = new Button[256];

	private boolean mouseWindowLogonunOzerinde =false;

	private boolean mouseWindowLogoyaTikladi = false;

	private boolean stop = false;

	private boolean warning = false,selected = false,welcomeScreen = true;

	private int y,x,cursorTexX=0,cursorTexY=0;

	private boolean CreditsCloseButton;

	private boolean controlPanelCloseButton;

	private boolean controlPanelOpened;

	private int selectedMouse;

	public XpSimulator() {
		Dimension size = new Dimension(WIDTH * SCALE, HEIGHT * SCALE);

		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);

		inputHandler = new InputHandler(this);
	}

	public void start() {
		new Thread(this, "Game Thread").start();
	}

	public void stop() {
		keepRunning = false;
	}

	public void init() {
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("./cursor.txt")));
			String a1;
			
			while((a1 = reader.readLine()) != null) {
				String[] amongus = a1.split(":");
				if(amongus[0].equalsIgnoreCase("cursorTexX")) {
					this.cursorTexX = Integer.parseInt(amongus[1]);
				}
                if(amongus[0].equalsIgnoreCase("cursorTexY")) {
                	this.cursorTexY = Integer.parseInt(amongus[1]);
				}
			}
		} catch (FileNotFoundException e) {
			this.cursorTexX= 0;
			this.cursorTexY=0;
		} catch (IOException e) {
			this.cursorTexX= 0;
			this.cursorTexY=0;
		}
		
		Art.init();
		screenImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		screenBitmap = new Bitmap(screenImage);
		mouse = inputHandler.updateMouseStatus(SCALE);

		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "invisible"));
		this.hitboxs[0] = new Button(0,HEIGHT-16,64,16);
		this.hitboxs[0].hide();
		this.hitboxs[1] = new Button(49,HEIGHT-35,16,16);
		this.hitboxs[1].hide();
		this.hitboxs[2] = new Button(WIDTH-16,HEIGHT-16,16,16);
		this.hitboxs[2].hide();
		this.hitboxs[3] = new Button(1,HEIGHT-75,61,11);
		this.hitboxs[3].hide();
		this.hitboxs[4] = new Button(273,221,46,18);
		this.hitboxs[4].hide();
		
		//Control Panel Hitboxs
		this.hitboxs[5] = new Button(305,1,14,13);
		this.hitboxs[5].hide();
		this.hitboxs[6] = new Button(1,HEIGHT-36,16,16);
		this.hitboxs[6].hide();
		requestFocus();
	}

	public void run() {
		init();

		double nsPerFrame = 1000000000.0 / 60.0;
		double unprocessedTime = 0;
		double maxSkipFrames = 10;

		long lastTime = System.nanoTime();
		long lastFrameTime = System.currentTimeMillis();
		int frames = 0;

		while (keepRunning) {
			long now = System.nanoTime();
			double passedTime = (now - lastTime) / nsPerFrame;
			lastTime = now;

			if (passedTime < -maxSkipFrames) passedTime = -maxSkipFrames;
			if (passedTime > maxSkipFrames) passedTime = maxSkipFrames;

			unprocessedTime += passedTime;

			while (unprocessedTime > 1) {
				unprocessedTime -= 1;
				mouse = inputHandler.updateMouseStatus(SCALE);
				tick();
			}
			{
				render(screenBitmap);
				frames++;
			}

			if (System.currentTimeMillis() > lastFrameTime + 1000) {
				System.out.println(frames + " fps");
				lastFrameTime += 1000;
				frames = 0;
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			swap();
		}
	}

	private void swap() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(2);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		int screenW = getWidth();
		int screenH = getHeight();
		int w = WIDTH * SCALE;
		int h = HEIGHT * SCALE;

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenW, screenH);
		g.drawImage(screenImage, (screenW - w) / 2, (screenH - h) / 2, w, h, null);
		g.dispose();

		bs.show();
	}

	private void render(Bitmap screen) {
		if(welcomeScreen) {
			screen.draw(Art.i.backgrounds[1][0], 0, 0);
			if(this.ticks == 150) {
				this.hitboxs[0].show();
				this.hitboxs[2].show();
				playSound("welcome.wav");
				this.welcomeScreen = false;
			}
			
		} else {
			if(!stop) {
				if(!this.CreditsCloseButton && CreditsOpened) {
					creditsMenu(screen);
				} else if(!this.controlPanelCloseButton && controlPanelOpened) {
					screen.draw(Art.i.backgrounds[0][0], 0, 0);
					screen.draw(Art.i.controlPanel,0,0);
					for(int i = 0; i < 20; i++) {
						screen.draw(Art.i.icons[0][0], 16*i, HEIGHT-16);
					}
					if(this.mouseWindowLogonunOzerinde) {
						screen.draw(Art.i.starticons[0][1], 0, HEIGHT-16);
					} else {
						screen.draw(Art.i.starticons[0][0], 0, HEIGHT-16);
					}
					screen.draw(Art.i.starticons[1][0], 64, HEIGHT-16);
					if(mouseWindowLogoyaTikladi) {
						screen.draw(Art.i.startMenu, 0, HEIGHT-98);
					}
					screen.draw(Art.i.icons[3][0], WIDTH-32, HEIGHT-16);
				
					//this.checkNetwork("8.8.8.8", screen);
					screen.draw(Art.i.icons[2][0], WIDTH-16, HEIGHT-16);
					
					if(warning) {
						screen.draw(Art.i.warning, WIDTH-128, HEIGHT-80);
					}
					
					screen.draw(Art.i.cursors[1][0], 13*this.selectedMouse+2, 57);
				} else {
					screen.draw(Art.i.backgrounds[0][0], 0, 0);
					for(int i = 0; i < 20; i++) {
						screen.draw(Art.i.icons[0][0], 16*i, HEIGHT-16);
					}
					if(this.mouseWindowLogonunOzerinde) {
						screen.draw(Art.i.starticons[0][1], 0, HEIGHT-16);
					} else {
						screen.draw(Art.i.starticons[0][0], 0, HEIGHT-16);
					}
					
					if(mouseWindowLogoyaTikladi) {
						screen.draw(Art.i.startMenu, 0, HEIGHT-98);
					}
					screen.draw(Art.i.icons[3][0], WIDTH-32, HEIGHT-16);
				
					//this.checkNetwork("8.8.8.8", screen);
					screen.draw(Art.i.icons[2][0], WIDTH-16, HEIGHT-16);
					
					if(warning) {
						screen.draw(Art.i.warning, WIDTH-128, HEIGHT-80);
					}
				}
			} else {
				shutdown(screen);
			}
		}
		if(selected) {
			int x1 = mouse.x;
			int y1 = mouse.y;
			int x0 = x;
			int y0 = y;
			if(x0 > x1) {
				int tmp = x0;
				x0 = x1;
				x1 = tmp;
			}
			if(y0 > y1) {
				int tmp = y0;
				y0 = y1;
				y1 = tmp;
			}
			screen.box(x0, y0, x1,y1, 0xf66fa4ff);
		}
		if (mouse.onScreen) screen.draw(Art.i.cursors[this.cursorTexX][this.cursorTexY], mouse.x - 1, mouse.y - 1);
	}
	
	private void shutdown(Bitmap screen) {
		screen.draw(Art.i.backgrounds[0][1], 0, 0);
		this.stop();
		this.swap();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void creditsMenu(Bitmap screen) {
		screen.draw(Art.i.credits, 0, 0);
	}

	private void tick() {
		if(hitboxs[0].intersects(new Button(mouse.x,mouse.y,2,2))) {
			mouseWindowLogonunOzerinde  = true;
		} else {
			mouseWindowLogonunOzerinde = false;
		}
		if(hitboxs[0].intersects(new Button(mouse.x,mouse.y,2,2)) && mouse.b0Clicked) {
			mouseWindowLogoyaTikladi  = !mouseWindowLogoyaTikladi;
			hitboxs[1].setEnable(!(hitboxs[1].getEnable()));
			hitboxs[3].setEnable(!(hitboxs[3].getEnable()));
			hitboxs[6].setEnable(!(hitboxs[6].getEnable()));
		}
		if(hitboxs[1].intersects(new Button(mouse.x,mouse.y,2,2)) && mouse.b0Clicked) {
			hitboxs[1].hide();
			hitboxs[0].hide();
			stop = true;
			
		}
		if(hitboxs[3].intersects(new Button(mouse.x,mouse.y,2,2)) && mouse.b0Clicked) {
			mouseWindowLogoyaTikladi = false;
			hitboxs[0].hide();
			hitboxs[1].hide();
			hitboxs[3].hide();
			hitboxs[6].hide();
			hitboxs[4].show();
			CreditsCloseButton=false;
			CreditsOpened = true;
			
		}
		if(hitboxs[4].intersects(new Button(mouse.x,mouse.y,2,2)) && mouse.b0Clicked) {
			hitboxs[4].hide();
			hitboxs[0].show();
		    CreditsOpened = false;
			this.CreditsCloseButton = true;
			
		}
		if(hitboxs[2].intersects(new Button(mouse.x,mouse.y,2,2)) && mouse.b0Clicked) {
			warning = !warning;
		}
		
		if(mouse.b0Clicked) {
			x = mouse.x;
			y = mouse.y;
			
			this.selected = true;
		}
		if(mouse.b0Released){
			this.selected  = false;
		}
		
		if(mouse.b0Clicked || mouse.b1Clicked || mouse.b2Clicked) {
			playSound("tiklama.wav");
		}
		
		if(this.controlPanelOpened && mouse.right.down && !CreditsOpened) {
			try {
				Thread.sleep(0500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			selectedMouse++;
			if(selectedMouse > 4) {
				selectedMouse = 0;
			}
		}
		
        if(this.controlPanelOpened && mouse.left.down && !CreditsOpened) {
        	try {
				Thread.sleep(0500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	selectedMouse--;
        	if(selectedMouse < 0) {
        		selectedMouse = 0;
        	}
		}
        
        if(this.controlPanelOpened && mouse.enter.down && !CreditsOpened) {
        	if(this.selectedMouse == 0) {
        		this.cursorTexX = 0;
        		this.cursorTexY = 0;
        	} else if(this.selectedMouse == 1) {
        		this.cursorTexX = 0;
        		this.cursorTexY = 1;
        	} else if(this.selectedMouse == 2) {
        		this.cursorTexX = 1;
        		this.cursorTexY = 1;
        	} else if(this.selectedMouse == 3) {
        		this.cursorTexX = 0;
        		this.cursorTexY = 2;
        	} else if(this.selectedMouse == 4) {
        		this.cursorTexX = 0;
        		this.cursorTexY = 3;
        	}
        	
        	try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./cursor.txt")));
				writer.write("cursorTexX:"+this.cursorTexX);
				writer.newLine();
				writer.write("cursorTexY:"+this.cursorTexY);
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
        if(hitboxs[6].intersects(new Button(mouse.x,mouse.y,2,2)) && mouse.b0Clicked) {
        	this.mouseWindowLogoyaTikladi = false;
			this.controlPanelCloseButton = false;
			this.controlPanelOpened = true;
			hitboxs[5].show();
			hitboxs[6].hide();
			hitboxs[1].hide();
			hitboxs[3].hide();
		}
        
        if(hitboxs[5].intersects(new Button(mouse.x,mouse.y,2,2)) && mouse.b0Clicked) {
			this.controlPanelCloseButton = true;
			this.controlPanelOpened = false;
			hitboxs[5].hide();
		}
		
		ticks++;
	}
	
	public static synchronized void playSound(final String url) {
		  new Thread(new Runnable() {
		  // The wrapper thread is unnecessary, unless it blocks on the
		  // Clip finishing; see comments.
		    public void run() {
		      try {
		        Clip clip = AudioSystem.getClip();
		        AudioInputStream inputStream = AudioSystem.getAudioInputStream(
		          XpSimulator.class.getResourceAsStream("/sound/" + url));
		        clip.open(inputStream);
		        clip.start(); 
		      } catch (Exception e) {
		        System.err.println(e.getMessage());
		      }
		    }
		  }).start();
		}

	public static void main(String[] args) {
		XpSimulator gameComponent = new XpSimulator();

		JFrame frame = new JFrame("Windows XP Sim");
		frame.add(gameComponent);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		gameComponent.start();
	}
}
