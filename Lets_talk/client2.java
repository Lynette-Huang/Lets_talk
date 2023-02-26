import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class client2 extends JFrame implements Runnable,ActionListener {

    //north
    private JMenuBar bar = new JMenuBar();
    private JMenu menu = new JMenu("About");
    private JMenuItem about = new JMenuItem("About this application");
    private JMenuItem exit = new JMenuItem("Exit");
    JPanel north = new JPanel();
    //west
    JPanel west = new JPanel();

    //For modifying JList
    DefaultListModel<String> dl = new DefaultListModel<String>();

    //For showing and selection modifier
    private JList<String> userList = new JList<String>(dl);
    JScrollPane listPane = new JScrollPane(userList);

    //center
    JPanel center = new JPanel();
    JTextArea jta = new JTextArea(10,20);
    JScrollPane js = new JScrollPane(jta);

    //operation panel for send message
    JPanel operPane = new JPanel();
    JLabel input = new JLabel("Enter your message:");
    JTextField jtf = new JTextField(25);

    JButton jButton = new JButton("Send MSG");
    JButton fBtn = new JButton("File");

    private JButton jbt = new JButton("Send");
    private JButton jbt1 = new JButton("Private MSG");
    private BufferedReader br = null;
    private PrintStream ps = null;
    private String nickName = null;

    //private chat window
    JTextArea jTextArea = new JTextArea(11,45);
    JScrollPane js1 = new JScrollPane(jTextArea);
    JTextField jTextField = new JTextField(25);
    String suser = new String();

    double MAIN_FRAME_LOC_X;//main frame x coordinate
    double MAIN_FRAME_LOC_Y;//main frame y coordinate

    boolean FirstSecret = true;//Check if it is the first time chatting
    String sender = null;
    String receiver = null;

    //constructor
    //construct new client window
    public client2() throws Exception{

        // menu
        bar.add(menu);
        menu.add(about);
        menu.add(exit);
        about.addActionListener(this);
        exit.addActionListener(this);
        BorderLayout bl = new BorderLayout();
        north.setLayout(bl);
        north.add(bar,BorderLayout.NORTH);
        add(north,BorderLayout.NORTH);

        //connects' list
        Dimension dim = new Dimension(100,150);
        west.setPreferredSize(dim);//Use setPreferredSize to adjust the size of window
        BorderLayout bl2 = new BorderLayout();
        west.setLayout(bl2);
        west.add(listPane,BorderLayout.CENTER);//show connect list
        add(west,BorderLayout.EAST);
        userList.setFont(new Font("Calibri",Font.BOLD,15));

        //message window
        jta.setEditable(false);//the message window should not be edited
        jTextArea.setEditable(false);

        BorderLayout bl3 = new BorderLayout();
        center.setLayout(bl3);
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        operPane.setLayout(fl);
        operPane.add(input);
        operPane.add(jtf);
        jbt.setPreferredSize(new Dimension(40, 15));
        jbt1.setPreferredSize(new Dimension(80 , 15));
        operPane.add(jbt);
        operPane.add(jbt1);

        center.add(js,BorderLayout.CENTER);
        center.add(operPane,BorderLayout.SOUTH);
        add(center,BorderLayout.CENTER);

        //only show the scroll bar when needed
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        //listener for listening the action of mouse
        jbt.addActionListener(this);
        jbt1.addActionListener(this);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        nickName = JOptionPane.showInputDialog("User Name：");
        this.setTitle(nickName + "'s chatroom");
        this.setSize(700,400);
        this.setVisible(true);

        Socket s = new Socket("127.0.0.1", 9999);
        // Socket s = new Socket("10.17.5.140", 9999);
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ps = new PrintStream(s.getOutputStream());
        new Thread(this).start();
        ps.println("LOGIN#" + nickName);//log in information: LOGIN#nickName

        jtf.setFocusable(true);

        //listener for listening the action of keyboard
        jtf.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ps.println("MSG#" + nickName + "#" +  jtf.getText());
                    //reset the text entry box after message is sent out
                    jtf.setText("");
                }
            }
        });

        //set to use enter as the key to send message
        jTextField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSS();
                }
            }
        });

        //While someone in the same server log off, send message
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                ps.println("OFFLINE#" + nickName);
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            //监听父窗口大小的改变
            public void componentMoved(ComponentEvent e) {
                Component comp = e.getComponent();
                MAIN_FRAME_LOC_X = comp.getX();
                MAIN_FRAME_LOC_Y = comp.getY();
            }
        });
    }

    public void run(){
        //thread of user and server connection
        while (true){
            try{
                //see if the server has sent message to the user
                String msg = br.readLine();
                String[] strs = msg.split("#");
                //see if the message is from login
                if(strs[0].equals("LOGIN")){
                    if(!strs[1].equals(nickName)){
                        //check if the log in message is the users
                        jta.append(strs[1] + "logged in！\n");
                        dl.addElement(strs[1]);
                        userList.repaint();
                    }
                }else if(strs[0].equals("MSG")){
                    //receive message from the server
                    if(!strs[1].equals(nickName)){
                        jta.append(strs[1] + "said：" + strs[2] + "\n");
                    }else{
                        jta.append("I said：" + strs[2] + "\n");
                    }
                }else if(strs[0].equals("USERS")){
                    //user elements, for connecting on the list
                    dl.addElement(strs[1]);
                    userList.repaint();
                } else if(strs[0].equals("ALL")){
                    jta.append("System notification：" + strs[1] + "\n");
                }else if(strs[0].equals("OFFLINE")){
                    if(strs[1].equals(nickName)) {
                        //if get the notification of logged off, indicates that the user was kicked off
                        javax.swing.JOptionPane.showMessageDialog(this, "You have been kicked out of the chatroom！");
                        System.exit(0);
                    }
                    jta.append(strs[1] + "is offline！\n");
                    dl.removeElement(strs[1]);
                    userList.repaint();
                }else if((strs[2].equals(nickName) || strs[1].equals(nickName)) && strs[0].equals("SMSG")){
                    if(!strs[1].equals(nickName)){
                        jTextArea.append(strs[1] + "said:" + strs[3] + "\n");
                        jta.append("System notification:" + strs[1] + "sent private message to you: " + "\n");
                    }else{
                        jTextArea.append("I said: " + strs[3] + "\n");
                    }
                }else if((strs[2].equals(nickName) || strs[1].equals(nickName)) && strs[0].equals("FSMSG"))
                {
                    sender = strs[1];
                    receiver = strs[2];
                    //if it is the first time getting message, window open
                    if(!strs[1].equals(nickName)) {
                        FirstSecret = false;
                        jTextArea.append(strs[1] + "said：" + strs[3] + "\n");
                        jta.append("System notification:" + strs[1] + "sent private message to you: " + "\n");
                        handleSec(strs[1]);
                    }
                    else {
                        jTextArea.append("I said: " + strs[3] + "\n");
                    }
                }
            }catch (Exception ex){
                //If there's some issues on the server, clients will be kicked off as well.
                javax.swing.JOptionPane.showMessageDialog(this, "You have been forced quit!");
                System.exit(0);
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        //hear from action by mouse
        String label = e.getActionCommand();
        if(label.equals("Send")){
            //group message
            handleSend();
        }else if(label.equals("Private MSG") && !userList.isSelectionEmpty()){
            suser = userList.getSelectedValuesList().get(0);
            //creative a private chat window
            handleSec(suser);
            sender = nickName;
            receiver = suser;
        }else if(label.equals("Send MSG")){
            //send private message
            handleSS();
        }else if(label.equals("File")){
            System.out.println(handleFile());
        
        }else if(label.equals("About this application")){
            JOptionPane.showMessageDialog(this, "1.Can doing group chat in the chat window\n\n" +
                    "2.Can click someone to send private messages");
        }else if(label.equals("Exit")){
            JOptionPane.showMessageDialog(this, "You are successfully offline!");
            ps.println("OFFLINE#" + nickName);
            System.exit(0);
        } else{
            System.out.println("Action cannot be identified");
        }
    }

    public void handleSS(){
        //Send message in private chatting windows
        String name = sender;
        if(sender.equals(nickName)) {
            name = receiver;
        }
        if(FirstSecret) {
            ps.println("FSMSG#" + nickName + "#" + name + "#" + jTextField.getText());
            jTextField.setText("");
            FirstSecret = false;
        }
        else {
            ps.println("SMSG#" + nickName + "#" + name + "#" + jTextField.getText());
            jTextField.setText("");
        }
    }

    public void handleSend(){
        if(!jtf.getText().equals("")){
            //send group messages
            ps.println("MSG#" + nickName + "#" +  jtf.getText());
            //reset the text entry box after message is sent
            jtf.setText("");
        }else{
            jtf.setText("");
        }
        
    }

    public void handleSec(String name){
        //create new private message window
        JFrame jFrame = new JFrame();
        JPanel JPL = new JPanel();
        JPanel JPL2 = new JPanel();
        FlowLayout f2 = new FlowLayout(FlowLayout.LEFT);
        JPL.setLayout(f2);
        JPL.add(jTextField);
        JPL.add(jButton);
        JPL.add(fBtn);
        JPL2.add(js1,BorderLayout.CENTER);
        JPL2.add(JPL,BorderLayout.SOUTH);
        jFrame.add(JPL2);

        jButton.addActionListener(this);
        fBtn.addActionListener(this);
        jTextArea.setFont(new Font("Calibri", Font.PLAIN,15));
        jFrame.setSize(800,310);
        //Make the private chat window jump out from the main frame
        jFrame.setLocation((int)MAIN_FRAME_LOC_X+20,(int)MAIN_FRAME_LOC_Y+20);
        jFrame.setTitle("Private chat with: " + name);
        jFrame.setVisible(true);

        jTextField.setFocusable(true);

        //private chat window
        jFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                jTextArea.setText("");
                FirstSecret = true;
            }
        });
    }

    //open the file choosing window and get the file path
    public String handleFile(){
        String pathOfFile = "";
        JFileChooser fChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            // invoke the showsOpenDialog function to show the save dialog
            int fWindow = fChooser.showOpenDialog(null);
 
            // if the user selects a file
            if (fWindow == JFileChooser.APPROVE_OPTION){
                // set the label to the path of the selected file
                pathOfFile  = fChooser.getSelectedFile().getAbsolutePath();
            }else{
                System.out.println("the user cancelled the operation");
            }
        return pathOfFile;
    }

    public static void main(String[] args)throws Exception{
        new client2();
    }
}