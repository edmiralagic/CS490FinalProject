package edu.ccsu.networking.tcp;

/**
 * Simple TCP server and client, connects sends a sentence periodically, the
 * server responds with the sentence capitalized. This is an adaption of the
 * code provided by the Computer Networking: A Top Down Approach book by Kurose
 * and Ross
 *
 * @author Chad Williams
 */
public class Main {

  public static void main(String[] args) {
    TCPServer serverThread = null;
    TCPClient clientThread = null;
    try {
      // Start server
      serverThread = new TCPServer("Server", 49000);
      serverThread.start();

      // Create client
      byte[] targetAdddress = {127, 0, 0, 1};
      TCPClient client1 = new TCPClient("CLIENT1", 49000);
      TCPClient client2 = new TCPClient("CLIENT2", 49000);
      client1.start();
      client2.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
