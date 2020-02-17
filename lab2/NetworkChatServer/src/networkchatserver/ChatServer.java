/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkchatserver;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.crypto.Cipher;
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
    
    private Mac sha256_HMAC;
    private Cipher DES;
           
    private SecretKeySpec desKey;
    private SecretKeySpec hmacKey;
    
    final private String ALGO_HMAC;
    final private String ALGO_DES;
    final private String deskey;
    final private String hmackey;
    
    final private int HASH_LEN = 44;
    
    ListenThread list;
    SendThread send;
    Scanner sc;
    
    public ChatServer() throws Exception{
        Socket s;
        this.list = new ListenThread();
        this.send = new SendThread();
        this.port = 5000;
        this.clientSocket = null;
        
        this.ALGO_DES = "DES";
        this.deskey = "abcdABCD";
        this.DES = Cipher.getInstance(ALGO_DES);
        this.desKey = new SecretKeySpec(deskey.getBytes(), ALGO_DES);
        
        this.ALGO_HMAC = "HmacSHA256";
        this.hmackey = "hmacKeys";
        this.sha256_HMAC = Mac.getInstance(ALGO_HMAC);
        this.hmacKey = new SecretKeySpec(hmackey.getBytes(), ALGO_HMAC);
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
        @Override
        public void run(){
            try{
                String cipherReceive = "";
                String msgReceive = "";
                String hashReceive = "";
                String textReceive = "";
                String hashCompare = "";
                while(true)
                {
                    cipherReceive = rr.readLine();

                    /// decrypt message
                    DES.init(Cipher.DECRYPT_MODE, desKey);
                    System.out.println("LEN: " + cipherReceive.getBytes().length);
                    msgReceive = Base64.encode(DES.doFinal(cipherReceive.getBytes()));
                 
                    /// Separate the hash
                    hashReceive = msgReceive.substring(0, HASH_LEN);
                    textReceive = msgReceive.substring(HASH_LEN);
                    
                    
                    /// Hash the message (without the hash)
                    sha256_HMAC.init(hmacKey);
                    hashCompare = Base64.encode(sha256_HMAC.doFinal(textReceive.getBytes()));
                 
                    /// compare the hashes
                    // : )
                    System.out.printf("[RECEIVE] Cipher Text Received:\t%s\n", cipherReceive);
                    System.out.printf("[RECEIVE] Hash Received:\t%s\n"
                            + "[RECEIVE] Hash Computed:\t%s\n%s\n",
                                            hashReceive, hashCompare, 
                            (hashReceive.equals(hashCompare) ? "[GOOD HASH]" : "[BAD HASH]"));
                    
                    
                    System.out.printf("[RECEIVE] Plain Text:\t%s\n\n", textReceive);
                }
            }catch(Exception e)
            {
                System.out.println("[RECV] " + e.getMessage());
                System.exit(0);
            }
        }
    }
    
    class SendThread extends Thread
    {
        @Override
        public void run(){
            try{
                sc = new Scanner(System.in);
                String input = "";
                
                while(!input.equals("q")){
                    input = sc.nextLine();
                    
                    sha256_HMAC.init(hmacKey);
                    hash = Base64.encode(sha256_HMAC.doFinal(input.getBytes()));
                    input = hash + input;
                    
                    /// encrypt DES
                    DES.init(Cipher.ENCRYPT_MODE, desKey);
                    System.out.println("LEN: " + input.getBytes().length);
                    input = Base64.encode(DES.doFinal(input.getBytes()));
                    System.out.println("LEN: " + input.length());
                    
                    
                    //System.out.printl;
                    wr.println(input);
                    
                    System.out.println();
                }
                System.out.println("[ SEND THREAD EXIT ]");
                
            }catch(Exception e)
            {
                System.out.println("[SEND] " + e.getMessage());
                System.exit(0); 
            }
            System.exit(0);
        }
    }
}
