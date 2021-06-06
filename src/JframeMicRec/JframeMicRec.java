package JframeMicRec;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;

public class JframeMicRec {

	
	  static volatile Boolean isRecording = false; 
	  static volatile AudioFormat aFormat = new AudioFormat(44100, 8, 2, false, false);
	  static volatile AudioFormat slowFormat = new AudioFormat(32050, 8, 2, false, false);
	  static volatile AudioFormat fastFormat = new AudioFormat(52000, 8, 2, false, false);
	  static volatile TargetDataLine tDl = null;
	  static volatile SourceDataLine sDl = null;
	  static volatile DataLine.Info tDli = new DataLine.Info(TargetDataLine.class, aFormat);
	  static volatile DataLine.Info sDli = new DataLine.Info(SourceDataLine.class, aFormat);
	  //static volatile File cachedWaveInfo = new File("C:\\Users\\tre20\\Desktop\\testSOunds\\wavCache.wav");
	  static volatile byte[] tempBuffer;
	  static volatile int tDlBufferSize = 0;
	  static volatile int numBytesSent = 0;
	 
	static Thread workerThread1;
	static Thread workerThread2;
	static Thread workerThread3;
	
	// Handler oHdlr;
	static volatile JButton stopBtn = new JButton();
	static volatile JFrame currentFrame = new JFrame("Mic check V.123");
	static volatile JButton recBtn = new JButton();
	static volatile JButton startBtn = new JButton();
	static volatile JButton saveBtn = new JButton();
	static volatile ByteArrayOutputStream boptstr;
	static volatile String[] pbSpeed = {"Normal", "Fast", "Slow"};
	static volatile JComboBox<String> speedSelector = new JComboBox<>(pbSpeed);
	
	static volatile JFileChooser jfObj = new JFileChooser();
	static volatile FileNameExtensionFilter fFilterObj = new FileNameExtensionFilter("Wav",".wav");
	static volatile File tempAudioFileObj;
	static volatile AudioInputStream aIs1;
	static volatile InputStream bIs1;
	static volatile AudioFileFormat.Type aType = new AudioFileFormat.Type("WAVE", "wav");
	
	
	
	
	public JframeMicRec() throws LineUnavailableException {
		
		JMenuBar mB = new JMenuBar();
		JMenu m1 = new JMenu("File");
		JMenuItem mI1 = new JMenuItem("New");
		JMenuItem mI2 = new JMenuItem("Save");
		JMenuItem mI3 = new JMenuItem("Exit");
		
		
		mI1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Action name: " + e.getActionCommand());
				
				boptstr.reset();
				
			}
			
		});
		
		mI2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				byte[] tempA = boptstr.toByteArray();
				AudioFormat saveSpeed = null;
				System.out.println("Selected: " + speedSelector.getSelectedItem().toString());
				switch (speedSelector.getSelectedItem().toString()) {
				
				case "Normal": 
					saveSpeed = aFormat;
					break;
					
				case "Fast":
					saveSpeed = fastFormat;
					break;
					
				case "Slow":
					saveSpeed = slowFormat;
					break;				
				}
				
				//saveSpeed.toString();
				// TODO Auto-generated method stub
				//System.out.println("The action is: " + e.getActionCommand());
				//System.out.println("Save Ran");
				jfObj.setFileFilter(fFilterObj);
				int selectedInt = jfObj.showSaveDialog(currentFrame);
				
				if(selectedInt == JFileChooser.APPROVE_OPTION) {
					tempAudioFileObj = jfObj.getSelectedFile();
					bIs1 = new ByteArrayInputStream(tempA);
					aIs1 = new AudioInputStream(bIs1, saveSpeed, tempA.length);
					
					
					try {
						AudioSystem.write(aIs1, aType, tempAudioFileObj);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
				} 
			}
			
		});
		
		
		mI3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0); // stop program
				currentFrame.dispose(); // close window
				currentFrame.setVisible(false); // hide window
			}
			
		});
		
		m1.add(mI1);
		m1.add(mI2);
		m1.add(mI3);
		mB.add(m1);
		currentFrame.setJMenuBar(mB);
		

		
		
		speedSelector.setBounds(340, 35, 100, 20);
		speedSelector.setSelectedIndex(1);
		currentFrame.add(speedSelector);

		currentFrame.setSize(480, 140);
		currentFrame.setLayout(null);
		currentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		currentFrame.setVisible(true);
		currentFrame.setResizable(false);

		// set up record button
		recBtn.setBounds(10, 20, 100, 50);
		recBtn.setText("Record!");
		currentFrame.add(recBtn);
		recBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				startRecording();
			}

		});

		// set up button
		startBtn.setBounds(120, 20, 100, 50);
		startBtn.setText("Play!");
		currentFrame.add(startBtn);
		startBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				playRecording();
			}
			
		});

		// setup stop button
		stopBtn.setBounds(230, 20, 100, 50);
		stopBtn.setText("Pause!");
		currentFrame.add(stopBtn);
		stopBtn.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				pauseRecording();
			}
			
		});

		// save btn
		saveBtn.setBounds(340, 20, 100, 50);
		saveBtn.setText("Save!");
		//currentFrame.add(saveBtn);
		
		
		initTdl();
		initSdl();
	}

	public static void main(String[] args) throws LineUnavailableException {
		// TODO Auto-generated method stub
		new JframeMicRec();

	}

	public static void startRecording() {

		workerThread1 = new Thread() {

			@Override
			public void run() {
				isRecording = true;
				try {
					tDl.open(aFormat);
					tDl.start();
					
					while(isRecording) {
						numBytesSent = tDl.read(tempBuffer, 0, tempBuffer.length);
						boptstr.write(tempBuffer);	
					}
				} catch (LineUnavailableException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};
		workerThread1.start();

	}
	
	
	public static void pauseRecording() {
		
		
		isRecording = false;
		tDl.stop();
		workerThread1.stop();
		
	}
	
	public static void playRecording() {
		pauseRecording();
		
		if(workerThread3 != null) {
			workerThread3.stop();
		}
		
		workerThread3 = new Thread() {
			@Override
			public void run() {
				try {

					sDl.open(getSelectedSpeed());
					sDl.start();
					sDl.write(boptstr.toByteArray(), 0, boptstr.size());
					sDl.close();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		workerThread3.start();
		
	}
	
	public static AudioFormat getSelectedSpeed() {
		
		AudioFormat tempAformat = null;
		
		switch (speedSelector.getSelectedItem().toString()) {
		
		case "Fast":
			tempAformat =  fastFormat;
			break;
			
		case "Slow":
			tempAformat = slowFormat;
			break;
			
		case "Normal":
			tempAformat = aFormat;
			break;
		}
		
		return tempAformat;
				
	}
	
	public static void initTdl() throws LineUnavailableException{
		tDl = (TargetDataLine) AudioSystem.getLine(tDli);
		tDlBufferSize = tDl.getBufferSize()/5;
		tempBuffer = new byte[tDlBufferSize];
		boptstr = new ByteArrayOutputStream(tDlBufferSize);
	}
	
	public static void initSdl() throws LineUnavailableException {
		sDl = (SourceDataLine) AudioSystem.getLine(sDli);
	}

}
