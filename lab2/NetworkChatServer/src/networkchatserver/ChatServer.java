/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkchatserver;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
    private String hash;
    final private String secret;
    
    
    
    ListenThread list;
    SendThread send;
    Scanner sc;
    
    public ChatServer(){
        Socket s;
        this.list = new ListenThread();
        this.send = new SendThread();
        this.port = 5000;
        this.clientSocket = null;
        this.secret = "HmacSHA256";
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
                while(!msgReceive.equals("q\n"))
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
                    
                    Mac sha256_HMAC = Mac.getInstance(secret);
                    SecretKeySpec secret_key = new SecretKeySpec(input.getBytes(), secret);
                    sha256_HMAC.init(secret_key);
                
                    hash = Base64.encode(sha256_HMAC.doFinal(input.getBytes()));
                    
                    wr.println(hash);
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
