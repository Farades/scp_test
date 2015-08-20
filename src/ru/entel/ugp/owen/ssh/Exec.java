package ru.entel.ugp.owen.ssh;

import com.jcraft.jsch.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Exec{
    private ArrayList<String> filesList = new ArrayList<>();
    private final String user = "root";
    private String host;
    private final String remoteDir = "/mnt/ufs/media/mmcblk0p1/archive/";
    private String prefix;
    private JSch jsch;
    private Session session;

    private void initLocalDir() throws FileNotFoundException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите директорию куда скачать");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getSelectedFile() : " + fileChooser.getSelectedFile());
            this.prefix = fileChooser.getSelectedFile().toString() + File.separator;
        } else {
            throw new FileNotFoundException("Directory don't select");
        }
    }

    public void downloadFiles() throws FileNotFoundException, IllegalAccessException, JSchException {
        if (this.host != null && !this.host.equals("")) {
            initLocalDir();
            initJSCH();
            initFilesList();
            for (String rFile : filesList) {
                System.out.println(rFile);
                downloadFile(rFile);
            }
            closeJSCH();

        } else {
            throw new IllegalAccessException("Ip addr == null || empty");
        }

    }

    private void downloadFile(String file) {
        FileOutputStream fos=null;
        try {
            Channel channel=session.openChannel("exec");
            String rfile = remoteDir + file;

            String command="scp -f "+rfile;
            ((ChannelExec)channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out=channel.getOutputStream();
            InputStream in=channel.getInputStream();

            channel.connect();

            byte[] buf=new byte[1024];

            // send '\0'
            buf[0]=0; out.write(buf, 0, 1); out.flush();

            while(true){
                int c=checkAck(in);
                if(c!='C'){
                    break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize=0L;
                while(true){
                    if(in.read(buf, 0, 1)<0){
                        // error
                        break;
                    }
                    if(buf[0]==' ')break;
                    filesize=filesize*10L+(long)(buf[0]-'0');
                }

//                String file=null;
                for(int i=0;;i++){
                    in.read(buf, i, 1);
                    if(buf[i]==(byte)0x0a){
                        file=new String(buf, 0, i);
                        break;
                    }
                }

                //System.out.println("filesize="+filesize+", file="+file);

                // send '\0'
                buf[0]=0; out.write(buf, 0, 1); out.flush();

                // read a content of lfile
                fos=new FileOutputStream(prefix+file);
                int foo;
                while(true){
                    if(buf.length<filesize) foo=buf.length;
                    else foo=(int)filesize;
                    foo=in.read(buf, 0, foo);
                    if(foo<0){
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize-=foo;
                    if(filesize==0L) break;
                }
                fos.close();
                fos=null;

                if(checkAck(in)!=0){
                    System.exit(0);
                }

                // send '\0'
                buf[0]=0; out.write(buf, 0, 1); out.flush();
            }
            channel.disconnect();
        }
        catch(Exception e){
            System.out.println(e);
            try{if(fos!=null)fos.close();
            }
            catch(Exception ee){}
        }
    }

    private void initJSCH() throws JSchException {
        jsch = new JSch();
        session=jsch.getSession(user, host, 22);
        session.setTimeout(5000);
        UserInfo ui=new MyUserInfo();
        session.setUserInfo(ui);
        session.connect();
    }

    private void closeJSCH() {
        session.disconnect();
    }

    private void initFilesList() {
        try{
            Channel channel=session.openChannel("exec");
            String command = "ls " + remoteDir;
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            InputStream in=channel.getInputStream();
            channel.connect();

            String lsResult = "";
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    lsResult = new String(tmp, 0, i);
                }
                if(channel.isClosed()){
                    if(in.available()>0) continue;
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            filesList.addAll(Arrays.asList(lsResult.split("\\n")));
            channel.disconnect();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    static int checkAck(InputStream in) throws IOException{
        int b=in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(b==0) return b;
        if(b==-1) return b;

        if(b==1 || b==2){
            StringBuffer sb=new StringBuffer();
            int c;
            do {
                c=in.read();
                sb.append((char)c);
            }
            while(c!='\n');
            if(b==1){ // error
                System.out.print(sb.toString());
            }
            if(b==2){ // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
        public String getPassword(){ return passwd; }
        public boolean promptYesNo(String str){
            Object[] options={ "yes", "no" };
            int foo=JOptionPane.showOptionDialog(null,
                    str,
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);

            return foo==0;
        }

        String passwd;
        JTextField passwordField=(JTextField)new JPasswordField(20);

        public String getPassphrase(){ return null; }
        public boolean promptPassphrase(String message){ return true; }
        public boolean promptPassword(String message){
            Object[] ob={passwordField};
            int result=
                    JOptionPane.showConfirmDialog(null, ob, message,
                            JOptionPane.OK_CANCEL_OPTION);
            if(result==JOptionPane.OK_OPTION){
                passwd=passwordField.getText();
                return true;
            }
            else{
                return false;
            }
        }
        public void showMessage(String message){
            JOptionPane.showMessageDialog(null, message);
        }
        final GridBagConstraints gbc =
                new GridBagConstraints(0,0,1,1,1,1,
                        GridBagConstraints.NORTHWEST,
                        GridBagConstraints.NONE,
                        new Insets(0,0,0,0),0,0);
        private Container panel;
        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo){
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts=new JTextField[prompt.length];
            for(int i=0; i<prompt.length; i++){
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]),gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if(echo[i]){
                    texts[i]=new JTextField(20);
                }
                else{
                    texts[i]=new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }

            if(JOptionPane.showConfirmDialog(null, panel,
                    destination+": "+name,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)
                    ==JOptionPane.OK_OPTION){
                String[] response=new String[prompt.length];
                for(int i=0; i<prompt.length; i++){
                    response[i]=texts[i].getText();
                }
                return response;
            }
            else{
                return null;  // cancel
            }
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setHost(String host) {
        this.host = host;
    }
}