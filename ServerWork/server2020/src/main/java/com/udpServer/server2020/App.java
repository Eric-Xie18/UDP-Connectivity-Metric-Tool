package com.udpServer.server2020;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.zip.CRC32;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import ca.NetSysLab.ProtocolBuffers.Message.Msg;
import ca.NetSysLab.ProtocolBuffers.RequestPayload.ReqPayload;
import ca.NetSysLab.ProtocolBuffers.ResponsePayload.ResPayload;
import ca.NetSysLab.ProtocolBuffers.ResponsePayload.ResPayload.Builder;

public class App 
{
	
	private final static int SERVERPORT = 50000;
	private final static int MAXSIZE = 100000;
	
    public static void main( String[] args ) throws SocketException
    {
    	 DatagramSocket socket = new DatagramSocket(SERVERPORT);
    	 byte[] receive;
    	 DatagramPacket get;
    	 byte[] receiveByte;
    	 
    	 while (true) {
             receive = new byte[MAXSIZE];
             get = new DatagramPacket(receive, receive.length);        
   					try {
   						socket.receive(get);
   					
   		  receiveByte = new byte[get.getLength()];
   					for(int i=0;i<get.getLength();i++) {
   	                receiveByte[i] = receive[i];
   					}
   					
   	               try {
   	               Msg response =  Msg.parseFrom(receiveByte);
   	               int messageID = response.getMessageID(); 
   	         	  // ignore malformed request 
   	         	  if(!isCheckSumMatch(response.getMessageID(),response.getPayload(),response.getCheckSum())) {
   	         		  continue;
   	         	  }
   	         	  else {
   	         		 byte[] rawClientPayload= response.getPayload().toByteArray();
	            	 ReqPayload reqPayload = ReqPayload.parseFrom(rawClientPayload);
	            	 int size = reqPayload.getPayloadSize();
	            	 Random rd = new Random();
	         	     byte[] arr = new byte[size];
	         	     rd.nextBytes(arr);
   	         		 ResPayload.Builder resPayload = ResPayload.newBuilder(); 
   	         		 resPayload.setPayloadSize(size);
   	         		 resPayload.setServerResponse(ByteString.copyFrom(arr));
   	         		 Msg.Builder msg = Msg.newBuilder();
   	         		
   	    	        msg.setCheckSum(getCheckSum(messageID, resPayload.build().toByteString()));
   	    	        msg.setMessageID(messageID);
   	    	        msg.setPayload(resPayload.build().toByteString());
   	    	        
   	    	        byte[] buffer = msg.build().toByteArray();
   	    	        DatagramPacket send = new DatagramPacket(buffer, buffer.length, get.getAddress(), get.getPort());
   	    	        socket.send(send);
   	         	  } 
   	        
   	         	  }  catch(InvalidProtocolBufferException b) {
   	                	// a malformed packet
   	                }
   	               
   					} catch (IOException e) {
   						// TODO Auto-generated catch block
   						e.printStackTrace();
   					}
    }
}
    
    
    
    
    
    
    private static long getCheckSum(int messageID, ByteString payload) throws IOException {
   	 CRC32 checksum = new CRC32();
   	 ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write((byte)messageID);
        outputStream.write(payload.toByteArray());
        byte c[] = outputStream.toByteArray( );
        checksum.update(c);
   	return checksum.getValue();
   }
    
    private static boolean isCheckSumMatch(int messageID,ByteString payLoad,long checksum) throws IOException {
    	   ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    	   outputStream.write((byte)messageID);
    	   outputStream.write(payLoad.toByteArray());
    	   byte c[] = outputStream.toByteArray( );
    	   CRC32 check = new CRC32();
    	   check.update(c);
    	   return(check.getValue()==checksum);
    	} 

}