package com.project640;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.InterruptedIOException;


public class ClientUDP {
	private static byte[] bSendPacket=new byte[5000];                        // Byte array is declared in order to send the data
        private static byte[] bReceivedPacket=new byte[5000];                 // Byte array is declared in order to receive the data
	private static int iPortNum=3300;                                            // Port number initialized
	private static String sInput;       
	public static void main(String[] args) throws Exception
	{
		while(true) 
		{
		 sInput=getInput();                                                        // getInput method is called where user will enter the choice of file he/she wants to get from server
		sendClientMessage(sInput);                                                   // method called to send the message to server for requested file
		
		}
	}
	
	
	public static void sendClientMessage(String sInput) throws Exception {

		InetAddress ipAdd=InetAddress.getByName("localhost");                     // getting IP address of Server
        
                DatagramPacket dpReceived=new DatagramPacket(bReceivedPacket,bReceivedPacket.length); // Creating new datagram packet for received message
        
		DatagramSocket clientSocket=new DatagramSocket();                        //Opening a new datagram socket 
		
		
		String integrityStr="ENTS/1.0 Request"+"\r\n"+sInput+"\r\n";  
		
		int integrityChkValue=lengthAdjust(integrityStr);;                        // lengthAdjust method is called and integrity value received
		
		String checksumValue=String.valueOf(integrityChkValue);                   // converting integrity value into String
		System.out.println("Integrity check of Request Message :"+checksumValue);
		
		String sMessage=integrityStr+checksumValue+"\r\n";                          // Final request message to be sent
		
		bSendPacket=sMessage.getBytes();                                           // Converting final request message to array of bytes before sending it to server
		
		DatagramPacket dpSend=new DatagramPacket(bSendPacket, bSendPacket.length, ipAdd,iPortNum);  // Creating a new datagram packet for final request message
		
		int timeout=1000;
		
		boolean continueSending=true;                                              // Creating a flag and initializing it to true
		int count=0;                                                               // counter variable to keep the count of retransmission
		
		while(continueSending&&count<4)                                              // keep sending if continueSending is true and count is less than 4
		{
			clientSocket.send(dpSend);                                             // Final request message sent
			clientSocket.setSoTimeout(timeout);                                    //  Timer set to timeout=1000 (1000 millisecond=1 second)
			timeout=2*timeout;                                                    // doubled the timeout period
			count++;                                                              // counter incremented
		 
		try
		{
			clientSocket.receive(dpReceived);                                         // receive the response message from server
			continueSending=false;                                                    // if response message is received set continueSending value to false
			System.out.println("\nMessage received from the server");
		}
		catch(InterruptedIOException e)
		{
			System.out.println("Retransmitting the packet");
		}
		
		}
		if(count>3)
		{
		System.out.println("Communication failure!!!!");                                          // if timeout occurs more than 3 times, display error message "Communication failure"
		System.exit(0);
		}
		count=0;                                                                                   // reset the counter and timeout value.
		timeout=1000;
		
		
		bReceivedPacket=dpReceived.getData();                                                        // extracting the bytes received from received data 
		
		String recDataObj=new String(dpReceived.getData(),0,dpReceived.getLength(),"UTF-8");           // Creating String object for received data
		String recResponse=recDataObj.toString();                                                         // converting it to String
	
		
		ArrayList<String> list=new ArrayList<String>();                                                  // Creating a new ArrayList of type String
		
		for(String retval:recResponse.split("\r\n"))                                                        // Split the received message from server by "\r\n"
		{
			
		    list.add(retval);                                                                            // Added each String to ArrayList
		    
		}
		String resCode=list.get(1);                                                                    // 2nd String will give response code
		System.out.println("\nResponse code= "+resCode); 
		String fileContent="";
		String intgChkMsg="";                                                                          // Declared integrity check value variable and initialized it to 0
		int resIntgValue;
		
		if(resCode.equals("0"))                                                                            // if Response code is  0
		{
        String lastElement=list.get(list.size()-1);                                                        //this is the actual message along with integrity value at the end
       

         ArrayList<String> list2=new ArrayList<String>();                                                    // new ArrayList created
           for(String retval:lastElement.split("\n"))                                                      // Split the String by "\n"
         {
	      list2.add(retval);                                                                              // add each line to ArrayList
         }

         String resIntValue=list2.get(list2.size()-1);                                                    //extracting integrity value from message
         System.out.println("\nReceived checksum value of Response Message is "+resIntValue);
         for(int i=0;i<=list2.size()-2;i++){
	     fileContent=fileContent+list2.get(i)+"\n";                                                          //the message
        }

        intgChkMsg=list.get(0)+"\r\n"+list.get(1)+"\r\n"+list.get(2)+"\r\n"+fileContent;                   // Message which is to be sent for integrity check

        resIntgValue=lengthAdjust(intgChkMsg);                                                              // lengthAdjust method called and checksum value of response message stored in resIntgVal

        String intChkValue=String.valueOf(resIntgValue);                                                       // Checksum value converted to String
        System.out.println("\nCalculated Checksum value of Response Message :"+intChkValue);
     if(intChkValue.equals(resIntValue))
    {
    	 System.out.println("\nIntegrity Check Successful");
	 System.out.println("\n"+fileContent);                                                                    // if response integrity check value matches with the extracted value, file will be read and message will be displayed at client side
    }
    else{
	System.out.println("\nWould you like to resend the request? Y or N?");
	Scanner sc1=new Scanner(System.in);
	String reply=sc1.nextLine();
	if(reply.equalsIgnoreCase("y"))
	{
	sendClientMessage(sInput);                                                                                  //going back to step 2 and integrity of response message failed when resCode is 0
	}
	
   }
  }

		else                                                                                               //if response code is other than 0
		{
			for(int i=0;i<=list.size()-2;i++){
				intgChkMsg=intgChkMsg+list.get(i)+"\r\n";                                                   //response message will not have text file in it
	    }
			
			resIntgValue=lengthAdjust(intgChkMsg);
			
			String intChkValue=String.valueOf(resIntgValue);
			System.out.println("\n Calculated Checksum value of response message :"+intChkValue);
			
			if(intChkValue.equals(list.get(list.size()-1))){                                                    //if integrity check of the response message with response code other than 0 passes
				
				if(resCode.equals("1")){     
					System.out.println("\nIntegrity check failed. Would you like to resend the request? Y or N");
					Scanner sc=new Scanner(System.in);
					String reply=sc.nextLine();
					if(reply.equalsIgnoreCase("Y")){
						sendClientMessage(sInput);                                                             //When integrity check of request message on server side failed
					}
					
				}
				else if(resCode.equals("2")){
					System.out.println("\nMalformed request. The syntax of the request is not correct");
				}
				else if(resCode.equals("3")){
					System.out.println("\nThe file with the requested name does not exist");
				}
				else if(resCode.equals("4")){
					System.out.println("\nWrong version number. The version number in the request was different from 1.0");
					
				}
				
			}
			else
			{                                                                                                //when integrity check of response message with response code other than zero fails
				System.out.println("Would you like to resend the request? Y or N?");
				Scanner sc2=new Scanner(System.in);
				String reply=sc2.nextLine();
				if(reply.equalsIgnoreCase("y"))
				{
				sendClientMessage(sInput);
				}
				
			}
		}//else
		
		
	}



	 public static int lengthAdjust(String intgChkMsg)
	 {
		char[] cInputArr=intgChkMsg.toCharArray();                                                                  // converting String message to character array
		short[] wordArr;                                                                                              // Array of type short is defined to store the words
		short integrityChkValue;
		
		if(cInputArr.length%2==0)                                                                                      // if integrity check message has even number of characters
		{
		 wordArr=new short[cInputArr.length/2];                                                                         // length of word array= (length of character array/2)
		 integrityChkValue= calcChecksum(cInputArr,wordArr);                                                             // call the method for integrity check with character array and word array as arguments
		}
		else                                                                                                           // if integrity check message has odd number of characters
		{
			char[] cInputArrNew=new char[cInputArr.length+1];                                                              // created a new array with length= ( previous character array length+1)
			cInputArrNew=Arrays.copyOf(cInputArr, cInputArrNew.length);                                                   // copied the contents of old array into new array having 1 extra element
			cInputArrNew[cInputArrNew.length-1]='0';                                                                      // setting last element of array=0
			wordArr=new short[(cInputArrNew.length)/2];                                                                    //setting word array length=new input array length/2

			integrityChkValue=calcChecksum(cInputArrNew,wordArr);                                                                  // called calcWord method for integrity check and stored the value in integrityChkValue
						
	
			
		}
		return integrityChkValue;
		
	}


	public static short calcChecksum(char[] cInputArr, short[] wordArr)                                                     //method for calculating checksum value
	{
		int temp=0;                                                                                               //variable for pointing to elements of array cInputArr
		int i;
		
		for(i=0;i<wordArr.length;i++)
		{
			if(temp<cInputArr.length)
			{
			
		    short iAscValue1=(short)cInputArr[temp];                                                                  //taking ascii value of corresponding element in cInputArr array
			iAscValue1=(short)(iAscValue1<<8);                                                                        // left shifting that value by 8 bits  
			
			wordArr[i]=iAscValue1;                                                                                   //copying the left shifted value into an array which will store all the words
			temp++;                                                                                                  // value of temp incremented
		                 
	        short iAscValue2=(short)cInputArr[temp];                                                                //taking ascii value of next element of cInputArr
			wordArr[i]=(short)(wordArr[i]|iAscValue2);                                                             // performing or operation on i^th element of word array and new ascii value 
			                                                                                                       //thus giving the i^th word array element containing two ascii values
			temp++;                                                                                                //incrementing temp
		
			
			}
			
		}
			short S=0, index;
			short C=7919;
			int D= 65536;
			
			for(i=0;i<wordArr.length;i++)
			{
				index=(short) (S^wordArr[i]);                                                                    // array element is ORed with S and explicitly type casted to short
				
				S=(short) ((C*index)%D);                                                                        //value of S is obtained by  multiplying index with C and taking modulo of the product with D
                                                                                                                //and wrapping around integer values to short to avoid overflow
				}
			
		
		return S;
		}

	
	public static String getInput() 
	{
		String sFileName="";
		System.out.println("\nSelect the file you would like to receive from server :");
		System.out.println("1. Director's Message\n2. Program Overview\n3. Scholarly Paper\n\n Enter your choice :");
		Scanner input=new Scanner(System.in);                                                                            // Scanner object is defined to scan the user entered choice
		int iChoice=input.nextInt();                                                                                     // Scans the choice entered by user
		switch(iChoice)                                                                                                    // switch the choice entered by user
		{
		case 1: sFileName="directors_message.txt";
		        break;
		        
		case 2: sFileName="program_overview.txt";
		        break;      
		case 3: sFileName="scholarly_paper.txt";
		         break;
		default: System.out.println("Wrong choice...!! Please enter again..");
		         getInput();
		}
		 
		return sFileName;                                                                                                // return the choice to main method 
	} 


}
