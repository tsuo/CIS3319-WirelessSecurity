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
    final private String deskey;
    final private String hmackey;
    
    final private int HASH_LEN = 44;
    
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
        this.deskey = "abcdABCD";
        this.hmackey = "hmacKeys";
    }
    
    public void start()
    {
        try{
            System.out.println("[ SERVER BINDING POIRT ]");
            this.servSocket = new ServerSocket(this.port);
            System.out.println("[ SERVER AWAITING CLIENT. . . ]");
            this.clientSocket = this.servSocket.accept();
            System.out.println("[ . . .SERVER ACCEPTED CLIENT ]\n");
            
            this.in = this.clientSocket.getInputStream();
            this.rr = new BufferedReader(new InputStreamReader(this.in));
            this.out = this.clientSocket.getOutputStream();
            this.wr = new PrintWriter(this.out, true);
        }
        catch(Exception e){
            System.out.println("[ . . .ERROR ]");
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
                String hashReceive = "";
                String hashCompare = "";
                while(true)
                {
                    msgReceive = rr.readLine();
                    
                    /// decrypt message
                    
                    /// Separate the hash
                    hashReceive = msgReceive.substring(0, HASH_LEN);
                    msgReceive = msgReceive.substring(HASH_LEN);
                    
                    
                    /// Hash the message (without the hash)
                    Mac sha256_HMAC = Mac.getInstance(secret);
                    SecretKeySpec secret_key = new SecretKeySpec(hmackey.getBytes(), secret);
                    sha256_HMAC.init(secret_key);
                    
                    hashCompare = Base64.encode(sha256_HMAC.doFinal(msgReceive.getBytes()));
                 
                    /// compare the hashes
                    // : )
                    System.out.printf("\nHash Received: %s\nHash Computed: %s\n%s\n",
                                            hashReceive, hashCompare, 
                            (hashReceive.equals(hashCompare) ? "GOOD HASH" : "BAD HASH"));
                    
                    System.out.printf("Cipher Text Received: %s\n", "temp cipher");
                    System.out.printf("Plain Text Received: %s\n\n",msgReceive);
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
                    SecretKeySpec secret_key = new SecretKeySpec(hmackey.getBytes(), secret);
                    sha256_HMAC.init(secret_key);
                    
                    hash = Base64.encode(sha256_HMAC.doFinal(input.getBytes()));
                    input = hash + input;
                    
                    /// encrypt DES
                    
                    
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
