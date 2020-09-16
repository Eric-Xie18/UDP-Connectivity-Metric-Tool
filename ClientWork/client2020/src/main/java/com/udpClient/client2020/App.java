package com.udpClient.client2020;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.CRC32;

import com.google.protobuf.ByteString;

import ca.NetSysLab.ProtocolBuffers.Message;
import ca.NetSysLab.ProtocolBuffers.Message.Msg;
import ca.NetSysLab.ProtocolBuffers.RequestPayload.ReqPayload;
import ca.NetSysLab.ProtocolBuffers.ResponsePayload.ResPayload;

public class App 
{
	private final static int STARTSIZE = 350;
	private final static int ENDSIZE = 350;
	private static final int TIMEOUT=1000;
	private static final int HARDTIMEOUT =3000 ;
	private final static int SERVERPUDPPORT = 50000;
	private final static int SERVERPTCPPORT = 50001;
	private final static int RUN = 100;
	//private final static int TICKRATE = 70;
	//private final static int TESTMINUTES = 15;
	//private final static byte[] LOCALHOST = {(byte) 192,(byte) 168,56,1};
	private final static int COUNT = 1000*4*1;
	//private final static int MAXRETRY = 2;
    public static void main( String[] args ) throws IOException, InterruptedException
    {
    	
    	   if (args.length != 1) {
	             System.out.println("Please specify a host name usage: <serverIP>");
	             return;
	       }	
    	   
    	   String serverAdd = args[0];
	       String[] serverIP = serverAdd.split("\\.");
           byte serverIPByte[] = new byte[serverIP.length];
           int count = 1;
           
           for(int i=0;i<serverIP.length;i++) {
           	serverIPByte[i]= (byte)Integer.parseInt(serverIP[i]);
           }
    	 
        DatagramSocket udpSocket = new DatagramSocket(50000);
        
           // send request by getting the user input        
        
        InetAddress address = InetAddress.getByAddress(serverIPByte);
        int udpPortNum = SERVERPUDPPORT;
    	System.out.println("Testing UDP connectivity");
    	
    	while(count<=RUN) {
    		System.out.println("Run " + count);
    		test(address,udpPortNum,serverAdd,udpSocket);
    		System.out.println("");
    		count++;
    	}
    	
    udpSocket.close();
      

        
        
        
      }
    
    
     public static void test(InetAddress address, int udpPortNum, String serverAdd, DatagramSocket udpSocket) throws IOException {
       double lossSoFar = 0;
   	   double totalCount = 0;
   	   double totalAvgMaxRtt = 0;
   	   double totalcorrupted = 0;
   	   double finalAvgRtt = 0;
   	   double totalAvgMaxRttCount = 0;
    	 
    	
         int curByte =  STARTSIZE;
         while(curByte<=ENDSIZE) {
         	int count = 0;
         	int totalLoss =0;
         	int outofOrder = 0;
         	int messageID = 0;
         	boolean isTimeout;
         	long[] sentTime = new long[COUNT];
         	long[] receiveTime = new long[COUNT];
         	long[] packetCorrupt = new long[COUNT];
         	HashSet<Integer> h = new HashSet<Integer>();
         	LinkedList<Double> rtts = new LinkedList<Double>();
         	
         	Arrays.fill(sentTime, -1);
         	Arrays.fill(receiveTime, -1);
         	Arrays.fill(packetCorrupt, 0);
         	Arrays.fill(receiveTime, -1);
         	

         	while(count < COUNT) {
         		if((double)count/(double)COUNT ==0.10){
         			System.out.println("10% completed");
         		}	
         		
         		
         		if((double)count/(double)COUNT ==0.25){
         			System.out.println("25% completed");
         		}	
         			
         		if((double)count/(double)COUNT ==0.50){
         			System.out.println("50% completed");
         		}	
         		
         		if((double)count/(double)COUNT ==0.75){
         			System.out.println("75% completed");
         		}	
         			
         		if((double)count/(double)COUNT ==0.85) {
         			System.out.println("85% completed");
         		}	
         		Msg.Builder requestMessage = Msg.newBuilder();
         		requestMessage.setMessageID(messageID);
         		Random rd = new Random();
         	    byte[] arr = new byte[curByte];
         	    rd.nextBytes(arr);
         		
         		ReqPayload.Builder requestPayLoad = ReqPayload.newBuilder();
         		requestPayLoad.setClientRequest(ByteString.copyFrom(arr));
         		requestPayLoad.setPayloadSize(curByte);
         		
     	        CRC32 checksum = new CRC32();
     	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
     	        outputStream.write((byte)requestMessage.getMessageID());
     	        outputStream.write(requestPayLoad.build().toByteArray());
     	        byte c[] = outputStream.toByteArray( );
     	        checksum.update(c);
     	        requestMessage.setCheckSum(checksum.getValue());
     	      //  System.out.println("Request message checksum is " + checksum.getValue());
     	        ByteString payLoadByte= requestPayLoad.build().toByteString();
     	        requestMessage.setPayload(payLoadByte);
     	        //write to buffer
     	        ByteArrayOutputStream output = new ByteArrayOutputStream();
     	        requestMessage.build().writeTo(output);
     	        
     	        byte[] toSend = output.toByteArray();
     	       
     	     /*   for(int i=0;i<buf.length;i++) {
            		 System.out.println("Current hex of index "+ i + " is " + String.format("%02x",buf[i]));
            		 
            		 
            	 }*/
     	        
     	        DatagramPacket packet = new DatagramPacket(toSend, toSend.length, address, udpPortNum);    
     	        byte[] received = new byte[ENDSIZE+2000];
     	        sentTime[messageID] = System.nanoTime();
     	        udpSocket.send(packet);
     	        udpSocket.setSoTimeout(HARDTIMEOUT);
     	        try {
 	                
     	        	DatagramPacket getack = new DatagramPacket(received, received.length);
     	        	udpSocket.receive(getack);
     	        	receiveTime[messageID] = System.nanoTime();
 	                byte[] responseByte = new byte[getack.getLength()];
 	                for(int i=0;i<getack.getLength();i++) {
 	                	responseByte[i]=getack.getData()[i];
 	                	//System.out.println("Current get back hex at position " + i + " is " + String.format("%02x", getack.getData()[i]));
 	                }
 	                 Msg response =  Msg.parseFrom(responseByte);
 	              //   System.out.println("The back byte string is " + response.hasCheckSum());
 	            	 int backID = response.getMessageID();
 	            	
 	            	
 	             
 	            	 byte[] rawBackPayload= response.getPayload().toByteArray();
 	            	
 	            	 long backCheckSum = response.getCheckSum();
 	            	 ByteArrayOutputStream stream = new ByteArrayOutputStream( );
 	            	 stream.write((byte)backID);
 	            	 stream.write(rawBackPayload);
 	            	 byte[] toCheck = stream.toByteArray();
 	            	 CRC32 backChecksum = new CRC32();
 	            	 backChecksum.update(toCheck);
 	            	 if(backChecksum.getValue()!=backCheckSum) {
 	            		packetCorrupt[messageID]=1;	
 	            		totalcorrupted ++;
 	            	  }
 	            	if(backID!=messageID) {
 	            		outofOrder++;
 	            	}
 	            	 
 	           } catch (SocketTimeoutException e) {
 	        	   receiveTime[messageID] = -1;
         	}
         	count++;
         	messageID++;
         //	System.gc();
         	//Thread.sleep(500);
         	}
         	
         	for(int i=0;i<COUNT;i++) {
         		if(packetCorrupt[i]==1 ||(receiveTime[i]==-1)) {
         			totalLoss++;
         		}
         		else {
         			rtts.add((double) (receiveTime[i]-sentTime[i])/1000000);
         			
         		}
         	}
         	int receivedTotal = count - totalLoss;
         	double PL = ((double)count - (double)receivedTotal)/(double)count;
         	double totalLatency = 0;
         	double minLatency = 100000;
         	double maxLatency = 0;
         	for(int i =0;i<rtts.size();i++) {
         		totalLatency = totalLatency + rtts.get(i);
         		if(minLatency>rtts.get(i)) {
         			minLatency = rtts.get(i);
         		}
         		if(maxLatency<rtts.get(i)) {
         			maxLatency = rtts.get(i);
         		}
         	}
         	double greaterThanCount = 0;
         	double sum = 0;
         	double average = totalLatency/rtts.size();
         	double greaterSum = 0;
         	for(int i=0;i<rtts.size();i++) {
         		if(rtts.get(i)>(totalLatency/rtts.size())) {
         			greaterThanCount++;
         			greaterSum = greaterSum + rtts.get(i) - average;
         		}
         		sum = (rtts.get(i)-average)*(rtts.get(i)-average) + sum;
         	}
         	
         	double sdd = Math.sqrt((sum/(double)(rtts.size())));
         	double meanGreaterDifference = greaterSum/greaterThanCount;
         
         	System.out.println("Sent "+ count + " of " + curByte + " bytes to " + serverAdd + " "  + "Received " + receivedTotal + " Corrupted packets: " + totalcorrupted + " Packet Loss Percentage: " + PL*100 + " Average RTT: " + average + " ms " +"Minimum RTT: " + minLatency + " ms " + "Maximum RTT: " + maxLatency + "ms " + "Standard Deviation: " + sdd + " Greater than average Ratio: " + greaterThanCount/(double)rtts.size() + " Mean Difference from Greater: " + meanGreaterDifference + " Out of Order: " + outofOrder);
         	totalAvgMaxRtt += maxLatency;
         	finalAvgRtt += totalLatency/rtts.size();
         	totalAvgMaxRttCount++;
         	curByte = curByte + 500;
          lossSoFar = lossSoFar + totalLoss; 
          totalCount = totalCount + count;
         }
         System.out.println("Total average packet loss percentage is: " + lossSoFar/totalCount*100 );
         System.out.println("Average Max RTT is: " + totalAvgMaxRtt/totalAvgMaxRttCount);
         System.out.println("Average RTT is: " + finalAvgRtt/totalAvgMaxRttCount);
    	
    }
    
}
