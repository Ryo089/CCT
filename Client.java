
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class Client{
	private final int row = 11;
	private final int line = 11;
	private int id;	//自分の対戦内id
	private GameMap game = new GameMap();//GameMapクラス
	private Player[] player = new Player[5]; //プレイヤクラス
	private Player myPlayer;
	private ClientFrame cf;
	private Login login;
	private Receiver receiver;
	private PrintWriter out;
	private boolean[] cfrag = new boolean[5];
	private int turn;
	private boolean[] afrag = new boolean[4]; //捕まったかどうかのフラグ
	
	//テスト用プレイヤクラス
	private String[] playerName = {"player01","player02","player03","player04","player05"};	//プレイヤ名(後に対戦準備で設定)
	
	//コマンド関連の配列
	private ActionCommand command[] = new ActionCommand[5];
	private String[] commandName = {"↑","←","停止","→","↓"};
	
	Client(String server, int port){
		//コマンドクラスの初期化
		command[0] = new Command01();
		command[1] = new Command02();
		command[2] = new Command03();
		command[3] = new Command04();
		command[4] = new Command05();
		
		//コマンドフラグの初期化
		for(int i=0;i<cfrag.length;i++) {
			cfrag[i] = false;
		}
		
		//ターン数の初期化
		turn = 20;
		
		//プレイヤクラスの初期化(削除予定)
		/*id = 0;
		for(int i=0;i<5;i++) {
			player[i] = new Player(playerName[i]);
		}*/
		
		login = new Login(server,port);
		
	}


	class ClientFrame extends JFrame implements ActionListener{
		//UI
		private JButton nButton[] = new JButton[5];	//基本コマンド(移動に関するコマンド)ボタン
		private JLabel label[][] = new JLabel[line][row];
		private ImageIcon boardIcon,humanIcon,blockIcon,demonIcon,nextIcon,lightIcon;
		private String[][] board = new String[line][row];
		private JLabel[] commandLine = new JLabel[11];
		private JButton ok,delete;
		private JPanel pc,pg;
		
		ClientFrame(){
			//盤面の描写
			JPanel p = new JPanel(new FlowLayout());
			
			JPanel p1 = new JPanel();
			
			pg = new JPanel(new GridLayout(row,line));
			
			boardIcon = new ImageIcon("test01.png");
			humanIcon = new ImageIcon("test02.png");
			blockIcon = new ImageIcon("test03.png");
			demonIcon = new ImageIcon("test04.png");
			nextIcon = new ImageIcon("test05.png");
			lightIcon = new ImageIcon("test06.png");
			
			int n=0;
			
			for(int i=0;i<line;i++) {
				for(int j=0;j<row;j++) {
					board[i][j] = game.getMap(i,j);
					
					label[i][j] = new JLabel();
					if(board[i][j].equals("board")) {
						label[i][j].setIcon(boardIcon);
						
					}else if(board[i][j].equals("human")) {
						label[i][j].setIcon(humanIcon);
						
						player[n].setLocation(j,i);
						player[n].setPreLocation();
						n++;
						
					}else if(board[i][j].equals("block")) {
						label[i][j].setIcon(blockIcon);
						
					}else if(board[i][j].equals("demon")) {
						label[i][j].setIcon(demonIcon);
						player[4].setLocation(j,i);
						player[4].setPreLocation();//鬼のidは4
					}
					
					pg.add(label[i][j]);
				}
				
			}
			
			pg.setPreferredSize(new Dimension(430,430));
			p1.add(pg);
			
			
			//コマンドボタンの描写
			JPanel pb = new JPanel(new GridLayout(3,3));
			JLabel[] blank = new JLabel[5];
			
			for(int i=0;i<blank.length;i++) {	//ボタンの初期化
				blank[i] = new JLabel();
				nButton[i] = new JButton(commandName[i]);
				nButton[i].addActionListener(this);
			}
			
			pb.add(blank[0]);	pb.add(nButton[0]);	pb.add(blank[1]);
			pb.add(nButton[1]);	pb.add(nButton[2]); pb.add(nButton[3]);
			pb.add(blank[2]);	pb.add(nButton[4]); pb.add(blank[3]);
			pb.setPreferredSize(new Dimension(225,75));
			p1.add(pb);
			
			p1.setPreferredSize(new Dimension(450,560));
			p.add(p1);
			
			
			//選択したコマンドの描写
			JPanel p2 = new JPanel();
			
			for(int i=0;i<commandLine.length;i++) {
				commandLine[i] = new JLabel();
				
				if(i%2 == 0) {
					commandLine[i].setHorizontalAlignment(JLabel.CENTER);
					commandLine[i].setFont(new Font("MS ゴシック",Font.BOLD,15));
					
				}else {
					commandLine[i].setIcon(nextIcon);
				}
			}
			
			pc = new JPanel(new GridLayout((player[id].getCommandNum())*2-1,1));
			for(int i=0;i<(player[id].getCommandNum())*2-1;i++) {
				pc.add(commandLine[i]);
			}
			
			LineBorder border = new LineBorder(Color.RED,2,true);	//枠線の表示
			pc.setBorder(border);
			pc.setBackground(Color.WHITE);
			pc.setPreferredSize(new Dimension(180,160));
			p2.add(pc);
			
			ok = new JButton("OK");
			ok.addActionListener(this);
			ok.setPreferredSize(new Dimension(90,30));
			p2.add(ok);
			
			delete = new JButton("削除");
			delete.addActionListener(this);
			delete.setPreferredSize(new Dimension(90,30));
			p2.add(delete);
			
			p2.setPreferredSize(new Dimension(200,200));
			p.add(p2);
			
		
			add(p);
			System.out.println("draw");
			
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("ログイン画面");// ウィンドウのタイトル
			setSize(800,600); 
			setVisible(true); 
		}
		
		int selected = 0;
		
		public void actionPerformed(ActionEvent e) {
			
			Object obj = e.getSource();
			int x= player[id].getLocationX();
			int y= player[id].getLocationY();
			
			
			if(obj.equals(ok) && (selected == player[id].getCommandNum())) {
				//配列データの送信
				
				
				System.out.println("ok");
				
				if(game.getBoard(x, y, 0).equals("board")) {	//進行予定地の表示取消
					label[y][x].setIcon(boardIcon);
				}
				
				player[id].resetLocation();	//コマンド実行前の位置に戻る
				
				for(int i=0;i<selected;i++) {
					commandLine[i*2].setText("");
				}
				
				cfrag[id] = true;
				
				if(cfrag[0] && cfrag[1] && cfrag[2] && cfrag[3] && cfrag[4]) {	//if:全員コマンド入力 then:コマンド実行
					actCommand();
					selected = 0;
					id = 0;	//テスト用
					updateCommandLine();
				}else {
					id++; //テスト用
					selected = 0;
					updateCommandLine();	//コマンドラインの変更
				}
				
			}else if(obj.equals(delete)) {
				
				if(game.getBoard(x, y, 0).equals("board")) { //進行予定地の表示取消
					label[y][x].setIcon(boardIcon);
				}
				
				for(int i=0;i<selected;i++) {
					commandLine[i*2].setText("");
					player[id].setActCommand(-1, i);
				}
				selected = 0;
				player[id].resetLocation();	//コマンド実行前の位置に戻る
				System.out.println("delete");
				
			}else {
				
				for(int i=0; i<5; i++) {
					if(obj.equals(nButton[i])  && (selected<player[id].getCommandNum())) {
						
						player[id].setActCommand(i,selected);	//実行予定の配列に該当コマンドを代入
						commandLine[selected*2].setText(commandName[player[id].getMyCommand(i)]);	//コマンドラインの更新
						
						if(game.getBoard(x, y, 0).equals("board")) {
							label[y][x].setIcon(boardIcon);
						}
						
						command[player[id].getMyCommand(i)].actionCommand(player[id], game);
						x = player[id].getLocationX();
						y = player[id].getLocationY();
						System.out.println("x = " + x + ", y = " + y);
						
						if(game.getBoard(x, y, 0).equals("board")) {
							label[y][x].setIcon(lightIcon);
						}
						
						selected++;
						break;
				     }
			     }   
			}
		}
		
		void updateCommandLine() {
			
			pc.removeAll();
			
			pc.setLayout(new GridLayout((player[id].getCommandNum()*2-1),1));
			for(int i=0;i<(player[id].getCommandNum())*2-1;i++) {
				pc.add(commandLine[i]);
			}
		}
		
		
		void updateDisp() {
			for(int i=0;i<line;i++) {
				for(int j=0;j<row;j++) {
					
					board[i][j] = game.getBoard(j,i,0);
					
					if(board[i][j].equals("board")) {
						label[i][j].setIcon(boardIcon);
					}else if(board[i][j].equals("block")) {
						label[i][j].setIcon(blockIcon);
					}else if(board[i][j].equals("human")) {
						label[i][j].setIcon(humanIcon);
					}else if(board[i][j].equals("demon")) {
						label[i][j].setIcon(demonIcon);
					}
					
				}
			}
			
			pg.paintImmediately(pg.getVisibleRect());
		}
	}
	
    public void connectServer(String server, int port) {
    	Socket s = null;
    	try {
    		s = new Socket(server,port);
    		
    		OutputStream os = s.getOutputStream();   		
    		out = new PrintWriter(os,true);	//出力用クラスの初期化
    
    		/*
    		System.out.println("サーバに送信");
    		ObjectOutputStream oos = new ObjectOutputStream(os);
    		oos.writeObject(myPlayer);*/
    		
    		//データの受信を開始
    		receiver = new Receiver(s);
    		receiver.start();
    		
    	}catch(Exception e) {
    		System.err.println("接続エラーが発生しました"+e);
    	}
    }
	
	class Receiver extends Thread{
		
		private InputStreamReader isr;
		private InputStream is;
		private	ObjectInputStream ois;
		private BufferedReader br;
		private String ss;
		
		Receiver(Socket s){
			try {
				is = s.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				
			}catch(Exception e) {
				System.err.println(e);
			}
		}
		
		public void run(){
			try {
				while(true) {
					String s = br.readLine();
					if(s!=null) {
						System.out.println("メッセージが送信されました:"+s);
						char c = s.charAt(0);	//1文字目：受信タイミングの確認
						ss = s.substring(1);	//2文字目以降:受信メッセージ
						
						switch(c){
						
						case 'O':	//対戦準備
							
							//自身の対戦内idを獲得
							id = Integer.parseInt(ss);
							System.out.println("ゲーム内ID = "+id);
							
							//オブジェクトの受信
							ois = new ObjectInputStream(is);	//StreamReaderの更新
							while(true) {
								Player p = (Player)ois.readObject();
								if(p!=null) {
									player[p.getNum()] = p;
									cfrag[p.getNum()] = true;
									
									if(cfrag[0] && cfrag[1] && cfrag[2] && cfrag[3] && cfrag[4]) {
										for(int i=0;i<cfrag.length;i++) {
											cfrag[i] = false;
										}
										break;
									}
								}
							}
							for(int i=0;i<5;i++) {
								System.out.println(player[i].getName() + " " + player[i].getRole());
							}
							
							isr = new InputStreamReader(is);	//StreamReaderの更新
							br = new BufferedReader(isr);
							System.out.println("start");
							
							break;
						
						case 'C':	//対戦中
							//データの受信
							
						}
					}
				}
				
			}catch(Exception e) {
				e.setStackTrace(getStackTrace());
			}
		}
	}
	
	//データ送信用メソッド
	public void sendMessage(String s) {
		out.println(s);
		out.flush();
	}
	
	//ログイン及び対戦準備のクラス
	class Login extends JFrame{

		Container contentPane;
	    JPanel cardPanel;
	    CardLayout layout;
	    ImageIcon blackIcon, whiteIcon, boardIcon, sel_boardIcon;
	    JButton[][] buttonArray;
	    JPanel pn_ingame;
	    JLabel mp_username, pc_username, settings_username, label_ingame;
	    JComboBox combo;
	    public int[] choice; //選択用配列
	    public String[] weather; //天気用配列   
	    
		//private Receiver receiver;

	   /* public static void main(String[] args) {
	        Client02 frame = new Client02();
	        frame.setTitle("CCT");
	        frame.setSize(500, 700);
	        frame.setLocationRelativeTo(null);
	        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	        frame.setVisible(true);
	        //frame.connectServer("192.168.1.13", 10000);
	    }*/

	    Login(String server, int port) {
	    	
	    	 setTitle("CCT");
	         setSize(500, 700);
	         setLocationRelativeTo(null);
	         setDefaultCloseOperation(EXIT_ON_CLOSE);
	         setVisible(true);
	    	
	    	
	    	
	    	String gt = new String("CommandChaseTag"); //タイトル
	    	choice = new int[3];
	    	weather = new String[3];
	    	//player = new Player();
	    	
	    	
	    	// ログイン画面
	    	JLabel title_log = new JLabel(gt, JLabel.CENTER);
	        JPanel pn_log = new JPanel();
	        pn_log.setLayout(null);
	        JLabel lbname_log = new JLabel("プレイヤー名", JLabel.CENTER);
			JTextField name_log = new JTextField(16);
			JLabel lbpwd_log = new JLabel("パスワード", JLabel.CENTER);
			JPasswordField pwd_log = new JPasswordField(16);
			JButton logb = new JButton("ログイン");
			JButton supb = new JButton("新規登録はこちら");
			
			title_log.setBounds(0, 15, 500, 80);  //画面配置
			title_log.setFont(new Font("Serif", Font.BOLD, 40));
			lbname_log.setBounds(30, 150, 80, 50);
			name_log.setBounds(140, 150, 300, 50);
			lbpwd_log.setBounds(30, 220, 80, 50);
			pwd_log.setBounds(140, 220, 300, 50);
			logb.setBounds(80, 350, 350, 100);
			supb.setBounds(130, 500, 250, 50);
	        
			logb.addActionListener(new ActionListener() { //ログインボタン
				public void actionPerformed(ActionEvent e) {
					String user_name = name_log.getText();
					myPlayer = new Player(user_name);
					String user_pwd = new String(pwd_log.getPassword());
					if (user_name.length() == 0) {
						JOptionPane.showMessageDialog(new JFrame(), "名前が未入力です");
					} else if (user_pwd.length() == 0) {
						JOptionPane.showMessageDialog(new JFrame(), "パスワードが未入力です");
					} else if (user_name.indexOf(' ') != -1) {
						JOptionPane.showMessageDialog(new JFrame(), "名前に空白は含まれません");
					} else if (user_pwd.indexOf(' ') != -1) {
						JOptionPane.showMessageDialog(new JFrame(), "パスワードに空白は含まれません");
					} else {
						//player.setName(user_name);
						//sendMessage("L" + user_name + " " + user_pwd);
						layout.show(cardPanel,"mypage");  //マイページへ遷移
					}
					name_log.setText(null);
					pwd_log.setText(null);
				}
			});
			
			supb.addActionListener(new ActionListener() { //新規登録ボタン
				public void actionPerformed(ActionEvent e) {
					name_log.setText(null);
					pwd_log.setText(null);
					layout.show(cardPanel, "signup");  //新規登録画面へ遷移
				}
			});
			
			pn_log.add(title_log); //各種コンポーネント追加
			pn_log.add(lbname_log);
			pn_log.add(name_log);
			pn_log.add(lbpwd_log);
			pn_log.add(pwd_log);
			pn_log.add(logb);
			pn_log.add(supb);
			
			// 新規登録画面
			JLabel title_sup = new JLabel(gt, JLabel.CENTER);
			JPanel pn_sup = new JPanel();
	        pn_sup.setLayout(null);
	        JLabel lb_sup = new JLabel("プレイヤー名/パスワードを入力してください");
	        JLabel lbname_sup = new JLabel("プレイヤー名", JLabel.CENTER);
			JTextField name_sup = new JTextField(16);
			JLabel lbpwd_sup = new JLabel("パスワード", JLabel.CENTER);
			JPasswordField pwd_sup = new JPasswordField(16);
			JButton btn_sup = new JButton("登録");
			JButton btn_back_sup = new JButton("もどる");
			
			title_sup.setBounds(0, 15, 500, 80);  //画面配置
			title_sup.setFont(new Font("Serif", Font.BOLD, 40));
			lb_sup.setBounds(50, 100, 500, 30);
			lb_sup.setFont(new Font("Serif", Font.BOLD, 20));
			lbname_sup.setBounds(30, 150, 80, 50);
			name_sup.setBounds(140, 150, 300, 50);
			lbpwd_sup.setBounds(30, 220, 80, 50);
			pwd_sup.setBounds(140, 220, 300, 50);
			btn_sup.setBounds(150, 400, 200, 100);
			btn_back_sup.setBounds(30, 620, 75, 25);
			
			btn_sup.addActionListener(new ActionListener() {  //登録ボタン
				public void actionPerformed(ActionEvent e) {
					String user_name = name_sup.getText();
					String user_pwd = new String(pwd_sup.getPassword());
					if (user_name.length() == 0) {
						JOptionPane.showMessageDialog(new JFrame(), "名前が未入力です");
					} else if (user_pwd.length() == 0) {
						JOptionPane.showMessageDialog(new JFrame(), "パスワードが未入力です");
					} else if (user_name.indexOf(' ') != -1) {
						JOptionPane.showMessageDialog(new JFrame(), "名前に空白は含まないでください");
					} else if (user_pwd.indexOf(' ') != -1) {
						JOptionPane.showMessageDialog(new JFrame(), "パスワードに空白は含まないでください");
					} else if (user_name.length() > 16) {
						JOptionPane.showMessageDialog(new JFrame(), "名前は16文字以内で入力してください");
					} else {
						//sendMessage("R" + user_name + " " + user_pwd);
						layout.show(cardPanel,"login");  //ログイン画面へ遷移
					}
					name_sup.setText(null);  //初期化
					pwd_sup.setText(null);
				}
			});
			btn_back_sup.addActionListener(new ActionListener() {  //戻るボタン
				public void actionPerformed(ActionEvent e) {
					name_sup.setText(null);
					pwd_sup.setText(null);
					layout.show(cardPanel, "login");  //ログイン画面へ遷移
				}
			});
			
			pn_sup.add(title_sup);  //各種コンポーネント追加
			pn_sup.add(lb_sup);
			pn_sup.add(lbname_sup);
			pn_sup.add(name_sup);
			pn_sup.add(lbpwd_sup);
			pn_sup.add(pwd_sup);
			pn_sup.add(btn_sup);
			pn_sup.add(btn_back_sup);
			
			// マイページ画面
			JLabel title_mp = new JLabel(gt, JLabel.CENTER);
			mp_username = new JLabel("");
			mp_username.setBounds(5, 4, 365, 15);		
			String[] combodata = {"設定","ログアウト","退会","パスワード変更"};
			combo = new JComboBox(combodata);
			combo.setSelectedItem("設定");
			
			JPanel pn_mp = new JPanel();
	        pn_mp.setLayout(null);
	        JLabel lb_mp = new JLabel("マイページ");
			JButton btn_mp = new JButton("対戦");
			
			title_mp.setBounds(0, 15, 500, 80);  //画面設定
			title_mp.setFont(new Font("Serif", Font.BOLD, 40));
			lb_mp.setBounds(180, 100, 300, 50);
			lb_mp.setFont(new Font("Serif", Font.BOLD, 30));
			btn_mp.setBounds(150, 250, 200, 100);
			combo.setBounds(150, 420, 200, 50);
	        
			combo.addActionListener(new ActionListener() {  //設定ボックス
				public void actionPerformed(ActionEvent e) {
					String msg = (String)combo.getSelectedItem();
					if(msg == "ログアウト") {
						combo.setSelectedItem("設定");
						layout.show(cardPanel,"login");  //ログイン画面へ遷移
					}
					else if(msg == "退会") {
						combo.setSelectedItem("設定");
						int sign = JOptionPane.showConfirmDialog(new JFrame(), "退会してよろしいですか？");
						if(sign == 0) {
							//sendMessage("X" + player.getName());
							JOptionPane.showMessageDialog(new JFrame(), "退会が完了しました");
							layout.show(cardPanel, "login");  //ログイン画面へ遷移
						}
					}
					else if(msg == "パスワード変更") {
						combo.setSelectedItem("設定");
						layout.show(cardPanel, "pwdchange");  //パスワード変更画面へ遷移
					}
				}
			});
			
	        btn_mp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					layout.show(cardPanel, "rolechoice");  //ロール選択画面へ遷移
				}
			});
	        
			pn_mp.add(title_mp);  //各種コンポーネント配置
			pn_mp.add(lb_mp);
			pn_mp.add(btn_mp);
			pn_mp.add(mp_username);
			pn_mp.add(combo);
			
			
			// パスワード変更画面
			JLabel title_pc = new JLabel(gt, JLabel.CENTER);	
			JPanel pn_pc = new JPanel();
	        pn_pc.setLayout(null);       
	        JLabel lb1_pc = new JLabel("新しいパスワードを入力してください");
			JPasswordField pwd1_pc = new JPasswordField(16);
			JLabel lb2_pc = new JLabel("もう一度入力してください");
			JPasswordField pwd2_pc = new JPasswordField(16);
			JButton btn_pc = new JButton("変更する");
			JButton btn_back_pc = new JButton("もどる");
			
			title_pc.setBounds(0, 15, 500, 80);  //画面設定
			title_pc.setFont(new Font("Serif", Font.BOLD, 40));
			lb1_pc.setBounds(150, 170, 300, 25);
			pwd1_pc.setBounds(80, 200, 350, 50);
			lb2_pc.setBounds(170, 320, 300, 25);
			pwd2_pc.setBounds(80, 350, 350, 50);
			btn_pc.setBounds(200, 500, 100, 60);
			btn_back_pc.setBounds(30, 620, 75, 25);
			
			btn_pc.addActionListener(new ActionListener() {  //変更ボタン
				public void actionPerformed(ActionEvent e) {
					String pass1 = new String(pwd1_pc.getPassword());
					String pass2 = new String(pwd2_pc.getPassword());
					if(pass1.length() == 0) {
						JOptionPane.showMessageDialog(new JFrame(), "パスワードが未入力です");
					}else if(pass1.indexOf(' ') != -1) {
						JOptionPane.showMessageDialog(new JFrame(), "パスワードに空白は含まないでください");
					}else if(pass1.length() > 16) {
						JOptionPane.showMessageDialog(new JFrame(), "パスワードは16文字以内で入力してください");
					}else{
					    if(pass1.equals(pass2)) {
						    //sendMessage("C" + player.getName() + " " + pass1);
						    JOptionPane.showMessageDialog(new JFrame(), "パスワードの変更が完了しました");
				            layout.show(cardPanel, "mypage");
						
					    }else{
						    JOptionPane.showMessageDialog(new JFrame(), "2つのパスワードが一致しません。\nもう一度入力してください");
					    }
					}
					pwd1_pc.setText(null);  //初期化
					pwd2_pc.setText(null);
				}
			});
			
			btn_back_pc.addActionListener(new ActionListener() {  //戻るボタン
				public void actionPerformed(ActionEvent e) {
					pwd1_pc.setText(null);
					pwd2_pc.setText(null);
					layout.show(cardPanel, "mypage");  //マイページへ遷移
				}
			});
			
			pn_pc.add(title_pc);  //各種コンポーネント配置
			pn_pc.add(lb1_pc);
			pn_pc.add(pwd1_pc);
			pn_pc.add(lb2_pc);
			pn_pc.add(pwd2_pc);
			pn_pc.add(btn_pc);
			pn_pc.add(btn_back_pc);
			
			
			// ロール選択画面
			JLabel title_role = new JLabel(gt, JLabel.CENTER);	
			JPanel pn_role = new JPanel();
	        pn_role.setLayout(null);        
	        JLabel lb_role = new JLabel("ロール選択");
	        JButton hm_role = new JButton("ヒト");
	        JButton oni_role = new JButton("オニ");
	        JButton btn_back_role = new JButton("戻る");
			
	        title_role.setBounds(0, 15, 500, 80);  //画面配置
			title_role.setFont(new Font("Serif", Font.BOLD, 40));
			lb_role.setBounds(200, 100, 300, 50);
			lb_role.setFont(new Font("Serif", Font.BOLD, 20));
			hm_role.setBounds(150, 200, 200, 100);
			oni_role.setBounds(150, 400, 200, 100);
			btn_back_role.setBounds(30, 620, 75, 25);
			
			hm_role.addActionListener(new ActionListener() {  //ヒト選択ボタン
				public void actionPerformed(ActionEvent e) {
					myPlayer.setRole("human");
					layout.show(cardPanel, "areachoice");  //地域選択画面へ遷移
				}
			});	
			
			oni_role.addActionListener(new ActionListener() {  //オニ選択ボタン
				public void actionPerformed(ActionEvent e) {
					myPlayer.setRole("demon");	
					layout.show(cardPanel, "areachoice");  //地域選択画面へ遷移
				}
			});
			
			btn_back_role.addActionListener(new ActionListener() {  //戻るボタン
				public void actionPerformed(ActionEvent e) {
					layout.show(cardPanel, "mypage");  //マイページへ遷移
				}
			});
			
			pn_role.add(title_role);  //各種コンポーネント配置
			pn_role.add(lb_role);
			pn_role.add(hm_role);
			pn_role.add(oni_role);
			pn_role.add(btn_back_role);
			
			//地域選択画面
			JLabel title_area = new JLabel(gt, JLabel.CENTER);
			JPanel pn_area = new JPanel();
			pn_area.setLayout(null);
			
			weather[0] = "晴れ"; //北海道の天気
			weather[1] = "曇り"; //東京の天気
			weather[2] = "雨"; //沖縄の天気
			
			JLabel lb_area = new JLabel("地域選択");
			JButton btn1_area = new JButton("<html>北海道<br>" + weather[0]);
			JButton btn2_area = new JButton("<html>東京<br>" + weather[1]);
			JButton btn3_area = new JButton("<html>沖縄<br>" + weather[2]);
			JButton btn_back_area = new JButton("戻る");
			
			title_area.setBounds(0, 15, 500, 80);  //画面設定
			title_area.setFont(new Font("Serif", Font.BOLD, 40));
			lb_area.setBounds(210, 100, 300, 50);
			lb_area.setFont(new Font("Serif", Font.BOLD, 20));
			btn1_area.setBounds(130, 200, 250, 100);
			btn2_area.setBounds(130, 350, 250, 100);
			btn3_area.setBounds(130, 500, 250, 100);
			btn_back_area.setBounds(30, 620, 75, 25);
			
			btn1_area.addActionListener(new ActionListener() {  //北海道選択ボタン
				public void actionPerformed(ActionEvent e) {
					myPlayer.setHopeStage(1);
					layout.show(cardPanel, "commandsetchoice");  //コマンドセット選択画面へ遷移
				}
			});
			btn2_area.addActionListener(new ActionListener() {  //東京選択ボタン
				public void actionPerformed(ActionEvent e) {
					myPlayer.setHopeStage(2);
					layout.show(cardPanel, "commandsetchoice");  //コマンドセット選択画面へ遷移
				}
			});
			btn3_area.addActionListener(new ActionListener() {  //沖縄選択ボタン
				public void actionPerformed(ActionEvent e) {
					myPlayer.setHopeStage(3);
					layout.show(cardPanel, "commandsetchoice");  //コマンドセット選択画面へ遷移
				}
			});
			btn_back_area.addActionListener(new ActionListener() {  //戻るボタン
				public void actionPerformed(ActionEvent e) {
					myPlayer.setHopeStage(0);//ロール選択初期化
					layout.show(cardPanel, "rolechoice");  //ロール選択画面へ遷移
				}
			});
			
			pn_area.add(title_area);  //各種コンポーネント配置
			pn_area.add(lb_area);
			pn_area.add(btn1_area);
			pn_area.add(btn2_area);
			pn_area.add(btn3_area);
			pn_area.add(btn_back_area);
			
			//コマンドセット選択画面←ボタン配列でいけないか？？？（合わせてエラー処理（choiceが規定値以外をとる）も行いたい）
			JLabel title_cs = new JLabel(gt, JLabel.CENTER);
			JPanel pn_cs = new JPanel();
			pn_cs.setLayout(null);
			JLabel lb_cs = new JLabel("コマンドセット選択");
			JButton btn1_cs = new JButton("<html>コマンド<br>" + "<html>セットA<br>" + "<html>◯◯<br>" + "<html>◯◯<br>" + "◯◯");
			JButton btn2_cs = new JButton("<html>コマンド<br>" + "<html>セットB<br>" + "<html>◯◯<br>" + "<html>◯◯<br>" + "◯◯");
			JButton btn3_cs = new JButton("<html>コマンド<br>" + "<html>セットC<br>" + "<html>◯◯<br>" + "<html>◯◯<br>" + "◯◯");
			JButton btn_back_cs = new JButton("戻る");
			
			title_cs.setBounds(0, 15, 500, 80);  //画面設定
			title_cs.setFont(new Font("Serif", Font.BOLD, 40));
			lb_cs.setBounds(150, 100, 300, 50);
			lb_cs.setFont(new Font("Serif", Font.BOLD, 20));
			btn1_cs.setBounds(2, 200, 150, 300);
			btn2_cs.setBounds(160, 200, 150, 300);
			btn3_cs.setBounds(318, 200, 150, 300);
			btn_back_cs.setBounds(30, 620, 75, 25);
			
			btn1_cs.addActionListener(new ActionListener() {  //コマンドセットAボタン
				public void actionPerformed(ActionEvent e) {
					choice[2] = 1;
					layout.show(cardPanel, "waiting");  //待機画面へ遷移
					connectServer(server,port);
				}
			});
			btn2_cs.addActionListener(new ActionListener() {  //コマンドセットBボタン
				public void actionPerformed(ActionEvent e) {
					choice[2] = 2;
					layout.show(cardPanel, "waiting");  //待機画面へ遷移
					connectServer(server,port);
				}
			});
			btn3_cs.addActionListener(new ActionListener() {  //コマンドセットCボタン
				public void actionPerformed(ActionEvent e) {
					choice[2] = 3;
					layout.show(cardPanel, "waiting");  //待機画面へ遷移
					connectServer(server,port);
				}
			});
			btn_back_cs.addActionListener(new ActionListener() {  //戻るボタン
				public void actionPerformed(ActionEvent e) {
					choice[1] = 0;  //地域選択初期化
					layout.show(cardPanel, "areachoice");  //地域選択画面へ遷移
				}
			});
			
			pn_cs.add(title_cs);  //各種コンポーネント配置
			pn_cs.add(lb_cs);
			pn_cs.add(btn1_cs);
			pn_cs.add(btn2_cs);
			pn_cs.add(btn3_cs);
			pn_cs.add(btn_back_cs);
			
			// ゲーム待機画面
			JLabel title_wt = new JLabel(gt, JLabel.CENTER);
			JPanel pn_wt= new JPanel();
	        pn_wt.setLayout(null);
	        
	        JLabel lb1_wt = new JLabel("対戦待機中");
	        JLabel lb2_wt = new JLabel("ただいまマッチングしています...");
	        JLabel lb3_wt = new JLabel("しばらくお待ちください");     
	        JButton btn_back_wt = new JButton("戻る");
	        
	        title_wt.setBounds(0, 15, 500, 80);  //画面設定
	        title_wt.setFont(new Font("Serif", Font.BOLD, 40));
	        lb1_wt.setBounds(210, 150, 300, 100);
	        lb2_wt.setBounds(160, 250, 200, 15);
	        lb3_wt.setBounds(190, 270, 200, 15);
	        btn_back_wt.setBounds(30, 620, 75, 25);
	        
	        btn_back_wt.addActionListener(new ActionListener() {  //戻るボタン
				public void actionPerformed(ActionEvent e) {
					choice[2] = 0;  //コマンドセット選択初期化
					layout.show(cardPanel, "commandsetchoice");  //コマンドセット選択画面へ遷移
				}
			});
	        
	        pn_wt.add(title_wt);  //各種コンポーネント配置
	        pn_wt.add(lb1_wt);
	        pn_wt.add(lb2_wt);
	        pn_wt.add(lb3_wt);
	        pn_wt.add(btn_back_wt);
			
			// ゲーム画面
			pn_ingame = new JPanel();
	        pn_ingame.setLayout(null);
			
	        cardPanel = new JPanel();
	        layout = new CardLayout();
	        cardPanel.setLayout(layout);
	        
	        cardPanel.add(pn_log, "login");
	        cardPanel.add(pn_sup, "signup");
	        cardPanel.add(pn_mp, "mypage");
	        cardPanel.add(pn_pc, "pwdchange");
	        cardPanel.add(pn_role, "rolechoice");
	        cardPanel.add(pn_area, "areachoice");
	        cardPanel.add(pn_cs, "commandsetchoice");
	        cardPanel.add(pn_wt, "waiting");
	        cardPanel.add(pn_ingame, "ingame");
	        
	        contentPane = getContentPane();
	        contentPane.add(cardPanel, BorderLayout.CENTER);
	    }
	 	
		public void mouseEntered(MouseEvent e) {} // マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {} // マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {} // マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {} // マウスで押していたオブジェクトを離したときの処理

	}
	
	
	
	public void actCommand() {	//コマンドを実行する
		
		System.out.println("act command!");
		int x,y;
		
		for(int i=0;i<player.length;i++) {
			x = player[i].getLocationX();
			y = player[i].getLocationY();
			
			for(int j=0;j<player[i].getCommandNum();j++) {
				
				game.setBoard(x,y,"board",0);
				
				command[player[i].getActCommand(j)].actionCommand(player[i], game);
				x = player[i].getLocationX();	//コマンド実行後の地点
				y = player[i].getLocationY();
				System.out.println("x = " + x + ", y = " + y);
				
				
				if((game.getBoard(x,y,0)).equals("board")) {
					game.setBoard(x,y,player[i].getRole(),0);
				}else {
					game.setBoard(x,y,game.getBoard(x,y,0),1);
					game.setBoard(x, y, player[i].getRole(),0);
				}
				
				if(game.checkBoard(player[i])) {	//プレイヤがいるマスの情報を入手
					break;
				}
				
				if(i==4 && player[i].getStatus().equals("human")) {
					for(int k=0;k<player.length-1;k++) {
						game.checkArrest(player[k]);
					}
				}
				
				
				cf.updateDisp();
				
				try{
					Thread.sleep(100);
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}	
			player[i].setPreLocation(); //preLocationを更新
			cfrag[i] = false;
		}
		
		checkGameSet();
		
	}
	
	
	public void checkGameSet() {
		
		for(int i=0; i<player.length-1;i++) {
			afrag[i] = player[i].getStatus().equals("arrest");
		}
		
		turn--;
		
		if(afrag[0] && afrag[1] && afrag[2] && afrag[3]) {
			System.out.println("鬼側の勝ちです。");
		}else if(turn == 0) {
			System.out.println("逃げる側の勝ちです。");
		}else {
			System.out.println("ゲーム続行");
		}
		
	}
	

	public static void main(String[] args) {
		
		String server = args[0];
		int port = Integer.parseInt(args[1]);
		
		Client c = new Client(server,port);
		
	}

}
