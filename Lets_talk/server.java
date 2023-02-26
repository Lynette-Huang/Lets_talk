import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class server extends JFrame implements Runnable, ListSelectionListener, ActionListener {

    private Socket s;
    private ServerSocket ss;
    //make the arraylist can be extended by the growth of user
    private ArrayList<ChatThread> users = new ArrayList<ChatThread>();
    DefaultListModel<String> dl = new DefaultListModel<String>();

    //Components that display a list of objects and allow the user to select one or more items.
    // A separate model, ListModel, maintains the contents of the list.
    private JList<String> userList = new JList<String>(dl);

    //create the GUI view
    private JPanel jpl = new JPanel();
    private JButton jbt = new JButton("Kick off");//kick off button
    private JButton jbt1 = new JButton("Send");//send message button
    private JTextField jtf = new JTextField();//message entering frame

    public server() throws Exception{

        this.setTitle("Server");
        this.add(userList, "North");
        this.add(jpl, "South");

        //set the public message entry bar as a separate line
        jtf.setColumns(1);
        jpl.setLayout(new BorderLayout());
        jpl.add(jtf, BorderLayout.NORTH);
        //the button to kick off the chatting room
        jpl.add(jbt,BorderLayout.EAST);
        //public message send button
        jpl.add(jbt1, BorderLayout.WEST);

        //connect with send message function
        jbt1.addActionListener(this);
        //connect with the kick-off function
        jbt.addActionListener(this);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocation(400,100);
        this.setSize(500, 400);
        this.setVisible(true);
        this.setAlwaysOnTop(true);
        ss = new ServerSocket(9999);
        //The listener for listening new user join in
        new Thread(this).start();
    }

    //Override the method in runnable interface
    @Override
    public void run() {
        while(true){
            try{
                s = ss.accept();
                //create a new thread for the client
                ChatThread ct = new ChatThread(s);
                //add each thread into users
                users.add(ct);
                //send user log in information
                // prevent the later log-in users cannot be updated with the previous user's info

                //inverse and get the JList data
                ListModel<String> model = userList.getModel();
                for(int i = 0; i < model.getSize(); i++){
                    ct.ps.println("USERS#" + model.getElementAt(i));
                }
                ct.start();
            }catch (Exception ex){
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,"Server Exception!");
                System.exit(0);
            }
        }
    }

    //Listener for the public message send action
    @Override
    public void actionPerformed(ActionEvent e) {

        String label = e.getActionCommand();
        if(label.equals("Send")){
            handleAll();
        }else if(label.equals("Kick off")){
            try {
                handleExpel();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    
    public void handleAll(){

        if(!jtf.getText().equals("")){

            sendMessage("ALL#" + jtf.getText());
            //clear the text box after sending the message
            jtf.setText("");
        }
    }

    public void handleExpel() throws IOException {

        sendMessage("OFFLINE#" + userList.getSelectedValuesList().get(0));
        //renew the default model
        dl.removeElement(userList.getSelectedValuesList().get(0));
        //renew JList
        userList.repaint();
    }

    public class ChatThread extends Thread{

        Socket s = null;
        private BufferedReader br = null;
        private PrintStream ps = null;
        public boolean canRun = true;
        String nickName = null;
        public ChatThread(Socket s) throws Exception{
            this.s = s;
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());
        }
        public void run(){
            while(canRun){
                try{
                    //receive the messages
                    String msg = br.readLine();
                    String[] strs = msg.split("#");
                    if(strs[0].equals("LOGIN")){
                        //receive the log-in messages
                        nickName = strs[1];
                        dl.addElement(nickName);
                        userList.repaint();
                        sendMessage(msg);
                    }else if(strs[0].equals("MSG") || strs[0].equals("SMSG") || strs[0].equals("FSMSG")){

                        sendMessage(msg);
                    }else if(strs[0].equals("OFFLINE")){
                        //receive the log-off messages
                        sendMessage(msg);
                        //System.out.println(msg);
                        dl.removeElement(strs[1]);
                        // renew the userList
                        userList.repaint();
                    }
                }catch (Exception ex){
                }
            }
        }
    }

    public void sendMessage(String msg){
        //the server can send message to all the users
        for(ChatThread ct : users){
            ct.ps.println(msg);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }

    public static void main(String[] args) throws Exception{

        new server();
    }
}