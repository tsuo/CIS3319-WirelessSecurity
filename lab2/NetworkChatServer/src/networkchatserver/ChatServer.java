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
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author tsuoi
 */
public class ChatServer {
    private DataOutputStream wr;
    private DataInputStream rr;
    //private PrintWriter wr;
    //private BufferedReader rr;
    //private OutputStream out;
    //private InputStream in;
    
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
            
            //this.in = this.clientSocket.getInputStream();
            this.rr = new DataInputStream(this.clientSocket.getInputStream());
            //this.out = this.clientSocket.getOutputStream();
            this.wr = new DataOutputStream(this.clientSocket.getOutputStream());
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
                byte[] msgBytes;
                String hashReceive = "";
                String hashCompare = "";
                /// encrypt 
                SecretKeyFactory key = SecretKeyFactory.getInstance("DES");
                    DESKeySpec deskey = new DESKeySpec("12345678".getBytes());
                    SecretKey mykey = key.generateSecret(deskey);
                    //byte[] enc = mykey.getEncoded();
                    //for(int i = 0; i < enc.length; i++)
                    //{
                    //    System.out.println(enc[i]);
                    //}
                    
                while(true)
                {
                    byte[] buf = new byte[256];
                    //buf[63] = 1;
                    //msgReceive = 
                    rr.readFully(buf, 0, 256);
                    //System.out.println("Encrypted Received: " + new String(buf, "UTF-8"));
                    //msgBytes = msgReceive.getBytes("UTF-16");
                    
                    //for(int i = 0; i < buf.length; i++){
                    //    System.out.print(buf[i] + " ");
                    //}
                    
                    /// decrypt message
                    //System.out.println(msgBytes.length + ",|" + msgReceive +"|");
                    
                    Cipher desCipher;
                    desCipher = Cipher.getInstance("DES/CBC/NoPadding");
                    desCipher.init(Cipher.DECRYPT_MODE, mykey, new IvParameterSpec(new byte[8]));
                    byte[] dec = desCipher.doFinal(buf);
                    //for(int i = 0; i < dec.length; i++){
                    //    System.out.print(dec[i] + " ");
                    //}
                    String textDecrypted = new String(desCipher.doFinal(buf), "UTF-8");
                    System.out.println("Decrypted DES: " +  textDecrypted);     
                        
                        
                    /// Separate the hash
                    hashReceive = textDecrypted.substring(0, HASH_LEN);
                    msgReceive = textDecrypted.substring(HASH_LEN);
                    
                    
                    /// Hash the message (without the hash)
                    Mac sha256_HMAC = Mac.getInstance(secret);
                    SecretKeySpec secret_key = new SecretKeySpec(hmackey.getBytes(), secret);
                    sha256_HMAC.init(secret_key);
                    
                    //System.out.println("Len:" +msgReceive.trim().length() +"MESSAGE: |"+msgReceive+"|");
                    hashCompare = Base64.encode(sha256_HMAC.doFinal(msgReceive.trim().getBytes()));
                    /// compare the hashes
                    // : )
                    System.out.printf("\nHash Received: %s\nHash Computed: %s\n%s\n",
                                            hashReceive, hashCompare, 
                            (hashReceive.equals(hashCompare) ? "GOOD HASH" : "BAD HASH"));
                    
                    System.out.printf("Cipher Text Received: %s\n", new String(buf, "UTF-8"));
                    System.out.printf("Plain Text Received: %s\n\n",msgReceive);
                }
            }catch(Exception e)
            {
                System.out.println(e.getMessage());
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

                    //System.out.println("Len:" +input.length() +"INPUT IS: |"+input+"|");
                    hash = Base64.encode(sha256_HMAC.doFinal(input.getBytes()));
                    input = hash + input;

                    System.out.println("Hash: " + hash);
                    /// encrypt
                    //KeyGenerator key = KeyGenerator.getInstance("DES");
                    SecretKeyFactory key = SecretKeyFactory.getInstance("DES");
                    DESKeySpec deskey = new DESKeySpec("12345678".getBytes());
                    SecretKey mykey = key.generateSecret(deskey);


                    try {

                        Cipher desCipher;

                        desCipher = Cipher.getInstance("DES/CBC/NoPadding");

                        desCipher.init(Cipher.ENCRYPT_MODE, mykey, new IvParameterSpec(new byte[8]));

                        byte[] buf1 = new byte[256];
                        byte[] text = input.getBytes("UTF-8");
                        for(int i = 0; i < text.length; i++){
                            buf1[i] = text[i];
                        }
                        
                        System.out.println("Plain Text WITH HASH to Send: " + input);
                        //System.out.println("Text1: " +  new String(text, "UTF-8"));

                        byte[] textEncry = desCipher.doFinal(buf1);
                        byte[] buf2 = new byte[256];
                        for(int i = 0; i < buf2.length; i++){
                            buf2[i] = textEncry[i];
                            //else buf2[i] = 1;
                        }

                        System.out.println("Text Encrypted: " +  new String(textEncry, "UTF-8"));

                        //desCipher = Cipher.getInstance("DES");
                        //desCipher.init(Cipher.DECRYPT_MODE, mykey);
                        //byte[] textDecrypted = desCipher.doFinal(textEncry);
                        //System.out.println("Text2: " +  new String(textDecrypted, "UTF-8"));

                        
                        //for (int i = 0; i < buf2.length; i++) {
                        //    System.out.print(buf2[i] +" ");
                        //}
                        //buf2[63] = 1;
                        wr.write(buf2);
                        //wr.println(textEncry.length + "," + new String(textEncry, "UTF-8"));

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
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
