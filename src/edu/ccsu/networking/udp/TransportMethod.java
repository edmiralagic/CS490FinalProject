package edu.ccsu.networking.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class TransportMethod {

	/**
	*	This method is called after a packet is received by either the Sender or Reciever.
	*   It will check if the Sequence # is correct, if it is: deliver packet to the Application Layer,
	*   else discard the packet and send back an ACK with the Sequence # received through the packet.
	*	@param pkt 			Recieved packet
	*	@param expectedSeq  The expected sequence # for either the Sender or Reciever, depending on the caller of the method. 
	*/

	public DatagramPacket rdt_rcv(DatagramPacket pkt, int expectedSeq) {
		//Check wether the sequence # is right or not.
		if(isSeq(pkt, expectedSeq)) {
			
			//deliver(extractData(pkt));
		}

		//Send ACK regardless of the fact that the packet had the correct seq #
		byte[] ackArray = {pkt.getData()[0]};
		DatagramPacket ack = new DatagramPacket(ackArray, 1, pkt.getAddress(), pkt.getPort());

		return ack;
	}

	/**
	*	Checks the pkt packet's sequence # with the expected sequence #
	*/
	public boolean isSeq(DatagramPacket pkt, int expectedSeq) {
		
		int receivedSeq = extractData(pkt)[0];
		return (receivedSeq == expectedSeq);
	}

	public byte[] extractData(DatagramPacket pkt) {
		return pkt.getData();
	}

	public void rdt_send(DatagramSocket socket, DatagramPacket pkt) {
		//ocket.send(pkt);
	}

	public byte[] deliver(DatagramPacket pkt) {
		byte[] dataReceived = pkt.getData();
		byte[] dataToDeliver = new byte[5000];

                return dataReceived;
//try catch byte buffer overflow exception - if caught create a new array of double size, copy old into new, change the last element pointer
	}
}