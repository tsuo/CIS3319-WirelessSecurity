/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkchatserver;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 * @author tsuoi
 */
public class ChatServer {
    private PrintWriter wr;
    private BufferedReader rr;
    private OutputStream out;
    private InputStream in;
    
    private int port;
    private ServerSocket servSocket;
    private Socket clientSocket;
    
    ListenThread list;
    SendThread send;
    Scanner sc;
    
    public ChatServer(){
        Socket s;
        this.list = new ListenThread();
        this.send = new SendThread();
        this.port = 5000;
        this.clientSocket = null;
    }
    
    public void start()
    {
        try{
            System.out.println("[ SERVER BINDING POIRT ]");
            this.servSocket = new ServerSocket(this.port);
            System.out.println("[ SERVER AWAITING CLIENT. . . ]");
            this.clientSocket = this.servSocket.accept();
            System.out.println("[ . . .SERVER ACCEPTED CLIENT ]");
            
            this.in = this.clientSocket.getInputStream();
            this.rr = new BufferedReader(new InputStreamReader(this.in));
            this.out = this.clientSocket.getOutputStream();
            this.wr = new PrintWriter(this.out, true);
        }
        catch(Exception e){
            System.out.println("Error port");
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
                while(!msgReceive.equals("q\0"))
                {
                    msgReceive = rr.readLine();
                    
                    /// decrypt message
                    
                    /// Separate the hash
                    
                    /// Decreypt the hash
                    
                    /// Hash the message (without the hash)
                    
                    /// compare the hashes
                    // : )
                    
                    System.out.println("Received: " + msgReceive);
                }
                
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
