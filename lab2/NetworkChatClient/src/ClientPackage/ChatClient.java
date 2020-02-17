/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientPackage;


import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    final private String deskey;
    final private String hmackey;
    
    final private int HASH_LEN = 44;
    
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
        this.deskey = "abcdABCD";
        this.hmackey = "hmacKeys";
    }
    
    public void start()
    {
        System.out.println("[ ATTEMPTING TO CONNECT TO HOST. . . ]");
        try{
            this.sock = new Socket(this.host, this.port);
            this.in = this.sock.getInputStream();
            this.rr = new BufferedReader(new InputStreamReader(this.in));
            this.out = this.sock.getOutputStream();
            this.wr = new PrintWriter(this.out, true);
            
        }catch(Exception e){
            System.out.println("[ . . .ERROR CONNECTING TO HOST ]");
            System.exit(0);
        }
        
        System.out.println("[ . . .SUCCESSFULLY CONNECTED TO HOST ]\n");
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
                    System.out.printf("Plain Text Received: %s\n\n", msgReceive);
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
                
            }catch(IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e)
            {
                System.exit(0);
            }
            System.exit(0);
        }
    }
}
