/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientPackage;


import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 * @author tsuoi
 */
public class ChatClient {
    private PrintWriter wr;
    private BufferedReader rr;
    private OutputStream out;
    private InputStream in;
    
    private String host;
    private int port;
    private Socket sock;
    
    ListenThread list;
    SendThread send;
    Scanner sc;
    
    public ChatClient(){
        this.sock = null;
        this.host = "localhost";
        this.port = 5000;
        this.list = new ListenThread();
        this.send = new SendThread();
    }
    
    public void start()
    {
        try{
            this.sock = new Socket(this.host, this.port);
            this.in = this.sock.getInputStream();
            this.rr = new BufferedReader(new InputStreamReader(this.in));
            this.out = this.sock.getOutputStream();
            this.wr = new PrintWriter(this.out, true);
            
        }catch(Exception e){
            System.out.println("ERROR CONNECT TO HOST");
            System.exit(0);
        }
        this.list.start();
        this.send.start();
    }
    
    class ListenThread extends Thread
    {
        public void run(){
            try{
               
                String msgReceive = "";
                msgReceive = rr.readLine();
                
                /// decrypt message

                /// Separate the hash

                /// Decrypt the hash

                /// Hash the message (without the hash)

                /// compare the hashes
                // : )
                
                System.out.println("Received: " + msgReceive);
                
            }catch(Exception e)
            {
                System.exit(0);
            }
        }
    }
    
    class SendThread extends Thread
    {
        public void run(){
            try{                
                sc = new Scanner(System.in);
                String input = "";
                
                
                while(!input.equals("q")){
                    input = sc.nextLine();
                    
                    /// hash input
                
                    /// encrypt the hash

                    /// append the has to message

                    /// encrypt entire message+hash

                    /// send to user
                    
                    wr.println(input);
                    
                }
                System.out.println("[ SEND THREAD EXIT ]");
                
            }catch(Exception e)
            {
                System.exit(0);
            }
            System.exit(0);
        }
    }
}
