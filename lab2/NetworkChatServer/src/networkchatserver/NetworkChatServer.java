/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkchatserver;

/**
 *
 * @author tsuoi
 */
public class NetworkChatServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       ChatServer serv = new ChatServer();
       serv.start();
    }
    
}
