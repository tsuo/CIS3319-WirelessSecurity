/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientPackage;


import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
    private String hash;
    private String msgReceive;
    final private String secret;
    
    
    ListenThread list;
    SendThread send;
    Scanner sc;
    
    public ChatClient(){
        this.sock = null;
        this.host = "localhost";
        this.port = 5000;
        this.list = new ListenThread();
        this.send = new SendThread();
        this.secret = "HmacSHA256";
        this.msgReceive = "";
        
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
                    
                    Mac sha256_HMAC = Mac.getInstance(secret);
                    SecretKeySpec secret_key = new SecretKeySpec(input.getBytes(), secret);
                    sha256_HMAC.init(secret_key);
                
                    hash = Base64.encode(sha256_HMAC.doFinal(input.getBytes()));
    
                    wr.println(hash);
                    
                }
                System.out.println("[ SEND THREAD EXIT ]");
                
            }catch(IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e)
            {
                System.exit(0);
            }
            System.exit(0);
        }
    }
}
