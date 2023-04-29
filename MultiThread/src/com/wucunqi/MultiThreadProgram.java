package com.wucunqi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class MultiThreadProgram {
	public static void main(String[] args) {
		var f = new MainFrame();
	}
}

class MainFrame extends JFrame implements Runnable,ActionListener{
	public int seconds = 0;
	public static int successNum = 0;//拦截个数
	public volatile boolean isRunning = false;//计时
	public boolean firstRun = true;
	private final int DEFAULT_WIDTH = 700;
	private final int DEFAULT_HEIGHT = 700;
	public static CenterJPanel cJPanel;
	public static SouthJPanel sJPanel;
	
	public MainFrame() {
		super("多线程程序----开发者：吴存其");
		setBounds(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
		setLayout(new BorderLayout());
		
		sJPanel = new SouthJPanel();
		sJPanel.start.addActionListener(this);
		sJPanel.add.addActionListener(this);
		sJPanel.launch.addActionListener(this);
		sJPanel.sub.addActionListener(this);
		sJPanel.end.addActionListener(this);
		sJPanel.sub.setEnabled(false);
		add(sJPanel,BorderLayout.SOUTH);
		
		cJPanel = new CenterJPanel();
		cJPanel.setBackground(Color.white);
		add(cJPanel,BorderLayout.CENTER);
		
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setVisible(true);
		cJPanel.getWH();//setVisible()后才可以
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Thread t1 = new Thread(this);
		Thread t2 = new Thread(cJPanel);
		t1.setDaemon(true);
		
		if(e.getSource() == sJPanel.start && sJPanel.start.isEnabled()) {
			if(firstRun) {
				sJPanel.start.setEnabled(false);
				sJPanel.end.setEnabled(true);
				
				cJPanel.initTargets();
				isRunning = true;//绘图
				cJPanel.isRunning = true;
				t1.start();
				t2.start();
				firstRun = false;
			}
			else {
				isRunning = true;
				cJPanel.isRunning = true;
				sJPanel.end.setEnabled(true);
				sJPanel.start.setEnabled(false);
				t1.start();
				t2.start();
//				for(int i = 0; i < 5; i ++) {
//					if(cJPanel.launchers[i] != null) {
//						Launcher temp = cJPanel.launchers[i];
//						(new Thread(temp)).start();
//							
//					}
//				}
			}
		}
		else if(e.getSource() == sJPanel.end) {
			isRunning = false;
			cJPanel.isRunning = false;
			for(int i = 0; i < 5; i++) {
				if(cJPanel.launchers[i] != null)
					cJPanel.launchers[i].isRunning = false;
			}
			sJPanel.end.setEnabled(false);
			sJPanel.start.setEnabled(true);
		}
		else if(e.getSource() == sJPanel.add) {
			if(1 <= Launcher.speed && Launcher.speed < 5) {
				Launcher.speed ++;
				sJPanel.sub.setEnabled(true);
			}
			else if(Launcher.speed == 5)
				sJPanel.add.setEnabled(false);
			
		}
		else if(e.getSource() == sJPanel.sub) {
			if(1 < Launcher.speed && Launcher.speed <= 5) {
				Launcher.speed --;
				sJPanel.add.setEnabled(true);
			}
			else if(Launcher.speed == 1)
				sJPanel.sub.setEnabled(false);
		}
		else if(e.getSource() == sJPanel.launch) {
			if(cJPanel.num < 5) {
				cJPanel.addLauncher();
			}
		}
	}
	
	@Override
	public void run() {
		while(isRunning) {
			seconds ++;
			sJPanel.timeValue.setText(String.valueOf(seconds));
			sJPanel.countValue.setText(String.valueOf(successNum));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}


class SouthJPanel extends JPanel{
	public JLabel time,count;
	public JTextField timeValue,countValue;
	public JButton start,add,launch,sub,end;
	public SouthJPanel() {
		time = new JLabel("计时（秒）：",SwingConstants.CENTER);
		timeValue = new JTextField(5);
		start = new JButton("开始");
		add = new JButton("加速");
		launch = new JButton("发射");
		launch.setPreferredSize(new Dimension(120,30));
		sub = new JButton("减速");
		end = new JButton("停止");
		end.setEnabled(false);
		count = new JLabel("计数（次）：",SwingConstants.CENTER);
		countValue = new JTextField(5);
		add(time);add(timeValue);add(start);add(add);add(launch);add(sub);add(end);add(count);add(countValue);
	}
}


class CenterJPanel extends JPanel implements Runnable{
	public static volatile boolean isRunning = false;//这个变量是volatile的，这意味着它的值在多个线程之间是可见的，并且不会被缓存
	public static int height,width;
	public static final int amount = 10;//导弹的总数
	public ArrayList<Target> targets = null;
	public Launcher[] launchers = new Launcher[5];
	public int num = 0;//记录拦截器的数量，屏幕上最多只有5个拦截器
	
	public CenterJPanel() {
		for(int i =0; i < 5; i++)
			launchers[i] = null;
	}

	public void getWH() {
		height = getHeight();
		width = getWidth();
	}

	public void initTargets() {
		targets = new ArrayList<Target>();
		for(int i = 0; i < amount; i++)
			targets.add(new Target());
	}
	
	public void addLauncher() {
		for(int i = 0; i < 5; i++) {
			if(launchers[i] == null) {
				launchers[i] = new Launcher();
				break;
			}
		}
		num ++;
	}
	
	public void subLauncher(int i) {
		if((i < 5 && i > -1)) {
			launchers[i] = null;
			num --;
		}
	}
	
	public boolean isCollision(Launcher l) {
		for(int j = 0; j < CenterJPanel.amount; j++) {
			if( Math.abs(l.x - targets.get(j).x) < 25 && Math.abs(l.y - targets.get(j).y) < 25) {
				targets.set(j, new Target());
				MainFrame.successNum ++;
				java.awt.Toolkit.getDefaultToolkit().beep();  
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void run() {
		while(isRunning) {
			for(int i = 0; i < amount; i++) {
				if(targets.get(i).dir == 0 && targets.get(i).x < 1.5 * width) {
					targets.get(i).x ++;
				}
				else if(targets.get(i).dir == 1 && targets.get(i).x > -2 * targets.get(i).radius - 0.5 * width) {
					targets.get(i).x --;
				}
			}
			for(int i = 0; i < 5; i++) {
				if(launchers[i] != null) {
					launchers[i].y -= Launcher.speed;
				}
			}
			
			repaint();
			try {
				Thread.sleep(8);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if(targets != null) {//用于展示界面
			for(int i = 1; i < amount; i++) {
				if(targets.get(i).dir == 0 && targets.get(i).x >= 1.5*CenterJPanel.width) {
					targets.set(i, new Target());
				}
				else if(targets.get(i).dir == 1 && targets.get(i).x <= -2*targets.get(i).radius - 0.5*CenterJPanel.width) {
					targets.set(i, new Target());
				}
				g2.setColor(Color.blue);
				g2.fillOval(targets.get(i).x, targets.get(i).y, Target.radius, Target.radius);
			}
			
		}
		for(int i = 0; i < 5; i++) {
			if(launchers[i] != null) {
				if(launchers[i].y < -launchers[i].radius) {
					subLauncher(i);
					continue;
				}
				g2.setColor(Color.red);
				g2.fillOval(launchers[i].x, launchers[i].y, launchers[i].radius, launchers[i].radius);
				if( isCollision(launchers[i]) ) {
					subLauncher(i);
				}
			}
		}
	}
}


class Target{
	public static final int radius = 25;
	public volatile int x;
	public volatile int y;
	public int dir;
	
	public Target() {
		dir = Math.random() < 0.5 ? 0 : 1;
		if(dir == 0) {
			int tempY = (int) (Math.random() * CenterJPanel.height);
			y =  tempY < (int)CenterJPanel.height/2 ? tempY + 3*radius : tempY - 3*radius;
			x = (int) (-2 * radius - Math.random() * CenterJPanel.width / 2);
		}
		else {
			int tempY = (int) (Math.random() * CenterJPanel.height);
			y =  tempY < (int)CenterJPanel.height/2 ? tempY + 3*radius : tempY - 3*radius;
			x = (int) (CenterJPanel.width  + Math.random() * CenterJPanel.width / 2);
		}
	}

	@Override
	public String toString() {
		return "Target [x=" + x + ", y=" + y + ", dir=" + dir + "]";
	}
	
}

class Launcher{
	public static final int radius = 30;
	public volatile int x;
	public volatile int y;
	public static int speed = 1;
	public volatile boolean isRunning = true;
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	
	public Launcher() {
		x = CenterJPanel.width/2 - radius/2;
		y = CenterJPanel.height;
	}
}
