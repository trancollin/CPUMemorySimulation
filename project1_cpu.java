import java.io.*;
import java.util.*;
import java.lang.Runtime;


public class project1_cpu {
	public static void main(String[] args) throws Exception{
		//registers
		int pc = 0, sp = 1000, ir = 0, ac = 0, x = 0, y = 0;
		//timer
		int interruptTimer = 0;
		//specified timer threshold
		int timerThreshold;
		//boolean to track if cpu is in kernel mode
		boolean kernelMode = false;
		//queue to keep track of interrupts
		Queue<Integer> interruptQueue = new LinkedList<>();
		
		//if there is no command line argument for interrupts, set interruptTimer to -1
		if(args.length == 1)
			timerThreshold = -1;
		//otherwise set it to the argument
		else
			timerThreshold = Integer.parseInt(args[1]);

		//fork memory process
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("java project1_memory " + args[0]);
		
		//wait for 5 seconds to ensure program is inserted into memory before cpu fetches
		System.out.println("Please wait 5 seconds.");
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		while(endTime - startTime < 5000) {
			endTime = System.currentTimeMillis();
		}
		
		//create input and output streams for pipe
		InputStream is = proc.getInputStream();
		OutputStream os = proc.getOutputStream();
		
		//create pipe between cpu and memory
		PrintWriter pw = new PrintWriter(os);
		Scanner sc = new Scanner(is);
		String line = "";
		
		//temporary variable for storing values
		int temp = 0;
		
		//cpu fetches instructions until the end instruction is executed
		while(true) {
			//check to see if an interrupt due to timer needs to occur
			if(interruptTimer == timerThreshold) {
				//add to interrupt queue and reset interrupt timer
				interruptQueue.add(1);
				interruptTimer = 0;
			}
			
			//if there isn't an interrupt currently happening and there are interrupts in the queue, execute timer interrupt
			if((interruptQueue.peek() != null) && (kernelMode == false)) {
				//remove from interrupt queue and enter kernel mode
				interruptQueue.remove();
				kernelMode = true;
				
				//point sp to system stack
				temp = sp;
				sp = 1999;
				
				//save current user sp and pc to system stack
				//current user sp is saved first
				pw.printf(sp + " " + temp + "\n");
				pw.flush();
				
				//wait for ok
				while(!sc.hasNextLine()) {}
				
				//empty pipe
				line = sc.nextLine();
				
				//decrement sp
				sp--;
				
				//pc is saved second
				pw.printf(sp + " " + (pc - 1) + "\n");
				pw.flush();
				
				//wait for ok
				while(!sc.hasNextLine()) {}
				
				//empty pipe
				line = sc.nextLine();
				
				//set pc to 1000
				pc = 1000;
			}
			
			//fetch instruction at address in pc
			pw.printf(pc + "\n");
			pw.flush();
			
			//wait until cpu fetches instruction through pipe
			while(!sc.hasNextLine()) {}
			
			//convert instruction to integer
			line = sc.nextLine();
			ir = Integer.parseInt(line);
			
			//perform action based on instruction value
			switch(ir) {
			
			//load value on next line into the ac
			case 1:
				//increment pc
				pc++;
				
				//fetch value at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
				
				//wait until cpu fetches value through pipe
				while(!sc.hasNextLine()) {}
				
				//load value into ac
				line = sc.nextLine();
				temp = Integer.parseInt(line);
				ac = temp;
				break;
				
			//load value at specified address into ac
			case 2:
				//increment pc
				pc++;
				 
				//fetch address at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
				
				//wait until cpu fetches address through pipe
				while(!sc.hasNextLine()) {}
				
				//load address into temp
				line = sc.nextLine();
				temp = Integer.parseInt(line);

				//check if address is outside user access
				if((temp >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//fetch value at address
					pw.printf(temp + "\n");
					pw.flush();
					
					//wait until cpu fetches value through pipe
					while(!sc.hasNextLine()) {}
					
					//load value into ac
					line = sc.nextLine();
					temp = Integer.parseInt(line);
					ac = temp;
				}
				break;
				
			//load value from address found at given address into ac
			case 3:
				//increment pc
				pc++;
				 
				//fetch address at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
				
				//wait until cpu fetches address through pipe
				while(!sc.hasNextLine()) {}
				
				//load address into temp
				line = sc.nextLine();
				temp = Integer.parseInt(line);
				
				//check if address is outside user access
				if((temp >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//get address at address in temp
					pw.printf(temp + "\n");
					pw.flush();
					
					//wait until cpu fetches address through pipe
					while(!sc.hasNextLine()) {}
					
					//load new address into temp
					line = sc.nextLine();
					temp = Integer.parseInt(line);
					
					//check if address is outside user access
					if((temp >= 1000) && (kernelMode == false)) {
						//send error message
						System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
						//send exit code
						pw.printf(-1 + "\n");
						pw.flush();
						
						//exit 
						proc.waitFor();
						System.out.println("Exited");
						System.exit(0);	
					}
					else {
						//get value at new address
						pw.printf(temp + "\n");
						pw.flush();
						
						//wait until cpu fetches value through pipe
						while(!sc.hasNextLine()) {}
						
						//load value into ac
						line = sc.nextLine();
						ac = Integer.parseInt(line);
					}
				}
				break;
				
			//load value at address + x into ac
			case 4:
				//increment pc
				pc++;
				 
				//fetch address at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
				
				//wait until cpu fetches address through pipe
				while(!sc.hasNextLine()) {}
				
				//load address into temp
				line = sc.nextLine();
				temp = Integer.parseInt(line);

				//check if address is outside user access
				if(((temp + x) >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//fetch value at address + x
					pw.printf((temp + x) + "\n");
					pw.flush();
					
					//wait until cpu fetches value through pipe
					while(!sc.hasNextLine()) {}
					
					//load value into ac
					line = sc.nextLine();
					temp = Integer.parseInt(line);
					ac = temp;
				}
				break;
				
			//load value at address + y into ac
			case 5:
				//increment pc
				pc++;
				 
				//fetch address at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
				
				//wait until cpu fetches address through pipe
				while(!sc.hasNextLine()) {}
				
				//load address into temp
				line = sc.nextLine();
				temp = Integer.parseInt(line);
				
				//check if address is outside user access
				if(((temp + y) >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//fetch value at address + y
					pw.printf((temp + y) + "\n");
					pw.flush();
					
					//wait until cpu fetches value through pipe
					while(!sc.hasNextLine()) {}
					
					//load value into ac
					line = sc.nextLine();
					temp = Integer.parseInt(line);
					ac = temp;
				}
				break;
				
			//load from "address = sp + x" into ac
			case 6:
				//check if address is outside user access
				if(((sp + x) >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//fetch from sp + x
					pw.printf((sp + x) + "\n");
					pw.flush();
					
					//wait until cpu fetches address through pipe
					while(!sc.hasNextLine()) {}
					
					//load value into ac
					line = sc.nextLine();
					ac = Integer.parseInt(line);
				}
				break;
				
			//store value in ac into memory address
			case 7:
				//increment pc
				pc++;
				 
				//fetch address at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
				
				//wait until cpu fetches address through pipe
				while(!sc.hasNextLine()) {}
				
				//load address into temp
				line = sc.nextLine();
				temp = Integer.parseInt(line);
				
				//check if address is outside user access
				if((temp >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//send specified address and ac to memory
					pw.printf(temp + " " + ac + "\n");
					pw.flush();
					
					//wait for ok
					while(!sc.hasNextLine()) {}
					
					//empty pipe
					line = sc.nextLine();
				}
				break;
				
			//get random int from 1 to 100 and place in ac
			case 8:
				ac = (int)((Math.random() * 100) + 1);
				break;
			
			//print ac to screen as int or char
			case 9:
				//increment pc
				pc++;
				
				//fetch value at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
				
				//wait until cpu fetches value through pipe
				while(!sc.hasNextLine()) {}
				
				//load value into ac
				line = sc.nextLine();
				temp = Integer.parseInt(line);
				
				//print ac as integer if port is 1
				if(temp == 1) 
					System.out.print(ac);
				//print ac as char if port is 2
				else
					System.out.print((char)ac);
				
				break;
				
			//add x to ac
			case 10:
				ac += x;
				break;
			
			//add y to ac
			case 11:
				ac += y;
				break;
				
			//subtract x from ac
			case 12:
				ac -= x;
				break;
				
			//subtract y from ac
			case 13:
				ac -= y;
				break;
				
			//copy value in ac to x
			case 14:
				x = ac;
				break;
				
			//copy value in x to ac
			case 15:
				ac = x;
				break;
				
			//copy value in ac to y
			case 16:
				y = ac;
				break;
				
			//copy value in y to ac
			case 17:
				ac = y;
				break;
				
			//copy value in ac to sp
			case 18:
				sp = ac;
				break;
				
			//copy value in sp to ac
			case 19:
				ac = sp;
				break;
				
			//jump to specified address
			case 20:
				//increment pc to next address (for fetching specified address and skipping specified address if no fetch)
				pc++;
				
				//fetch address at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
					
				//wait until cpu fetches address through pipe
				while(!sc.hasNextLine()) {}
					
				//load (address - 1) into pc
				line = sc.nextLine();
				temp = Integer.parseInt(line);
				
				//check if address is outside user access
				if((temp >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//(address - 1) is loaded since pc always increments after executing a case
					pc = temp - 1;
				}
				break;
				
			//jump to specified address if ac = 0
			case 21:
				//increment pc to next address (for fetching specified address and skipping specified address if no fetch)
				pc++;
				
				//jump to (specified address - 1) if ac is 0
				if(ac == 0) {
					//fetch address at incremented pc
					pw.printf(pc + "\n");
					pw.flush();
					
					//wait until cpu fetches address through pipe
					while(!sc.hasNextLine()) {}
					
					//load (address - 1) into pc
					line = sc.nextLine();
					temp = Integer.parseInt(line);
					
					//check if address is outside user access
					if((temp >= 1000) && (kernelMode == false)) {
						//send error message
						System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
						//send exit code
						pw.printf(-1 + "\n");
						pw.flush();
						
						//exit 
						proc.waitFor();
						System.out.println("Exited");
						System.exit(0);	
					}
					else {
						//(address - 1) is loaded since pc always increments after executing a case
						pc = temp - 1;
					}
				}
				
				break;
				
			//jump to specified address if ac != 0
			case 22:
				//increment pc to next address (for fetching specified address and skipping specified address if no fetch)
				pc++;
				
				//jump to (specified address - 1) if ac is not 0
				if(ac != 0) {
					//fetch address at incremented pc
					pw.printf(pc + "\n");
					pw.flush();
					
					//wait until cpu fetches address through pipe
					while(!sc.hasNextLine()) {}
					
					//load (address - 1) into pc
					line = sc.nextLine();
					temp = Integer.parseInt(line);
					
					//check if address is outside user access
					if((temp >= 1000) && (kernelMode == false)) {
						//send error message
						System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
						//send exit code
						pw.printf(-1 + "\n");
						pw.flush();
						
						//exit 
						proc.waitFor();
						System.out.println("Exited");
						System.exit(0);	
					}
					else {
						//(address - 1) is loaded since pc always increments after executing a case
						pc = temp - 1;
					}
				}				
				break;
			
			//push return address to stack and jump to specified address
			case 23:
				//decrement sp
				sp--;
				
				//send sp and pc + 1 (return address) to memory (pc + 1 since pc increments every iteration)
				pw.printf(sp + " " + (pc + 1) + "\n");
				pw.flush();
				
				//wait for ok for synchronization
				while(!sc.hasNextLine()) {}
				
				//empty pipe
				line = sc.nextLine();
				
				//increment pc to next address (for fetching specified address and skipping specified address if no fetch)
				pc++;
				
				//fetch address at incremented pc
				pw.printf(pc + "\n");
				pw.flush();
					
				//wait until cpu fetches address through pipe
				while(!sc.hasNextLine()) {}
					
				//load (address - 1) into pc
				line = sc.nextLine();
				temp = Integer.parseInt(line);
				
				//check if address is outside user access
				if((temp >= 1000) && (kernelMode == false)) {
					//send error message
					System.out.println("Memory violation: accessing system address " + temp + " in user mode.");
					//send exit code
					pw.printf(-1 + "\n");
					pw.flush();
					
					//exit 
					proc.waitFor();
					System.out.println("Exited");
					System.exit(0);	
				}
				else {
					//(address - 1) is loaded since pc always increments after executing a case
					pc = temp - 1;
				}
				break;
				
			//pop return address from stack and jump to it
			case 24:
				//read return address from stack
				pw.printf(sp + "\n");
				pw.flush();
				
				//wait for memory to send return address
				while(!sc.hasNextLine()) {}
				
				//read return address into pc
				line = sc.nextLine();
				pc = Integer.parseInt(line);
				
				//increment sp
				sp++;
				
				break;
				
			//increment x
			case 25:
				x++;
				break;
			
			//decrement x
			case 26:
				x--;
				break;
				
			//push ac to stack
			case 27:
				//decrement sp
				sp--;
				
				//send sp and ac to memory
				pw.printf(sp + " " + ac + "\n");
				pw.flush();
				
				//wait for ok for synchronization
				while(!sc.hasNextLine()) {}
				
				//empty pipe
				line = sc.nextLine();
				break;
			
			//pop from stack into ac
			case 28:
				//read from stack
				pw.printf(sp + "\n");
				pw.flush();
				
				//wait for memory to send return address
				while(!sc.hasNextLine()) {}
				
				//read return address into ac
				line = sc.nextLine();
				ac = Integer.parseInt(line);
				
				//increment sp
				sp++;
				break;
				
			//perform system call
			case 29:
				//enter kernel mode
				kernelMode = true;
				
				//point sp to system stack
				temp = sp;
				sp = 1999;
				
				//save current user sp and pc to system stack
				//current user sp is saved first
				pw.printf(sp + " " + temp + "\n");
				pw.flush();
				
				//wait for ok
				while(!sc.hasNextLine()) {}
				
				//empty pipe
				line = sc.nextLine();
				
				//decrement sp
				sp--;
				
				//pc is saved second
				pw.printf(sp + " " + pc + "\n");
				pw.flush();
				
				//wait for ok
				while(!sc.hasNextLine()) {}
				
				//empty pipe
				line = sc.nextLine();
				
				//set pc to 1499 since pc increments to 1500
				pc = 1499;
				break;
				
			//return from system call
			case 30:
				//pop pc from system stack
				pw.printf(sp + "\n");
				pw.flush();
				
				//wait for memory to send pc
				while(!sc.hasNextLine()) {}
				
				//read into pc
				line = sc.nextLine();
				pc = Integer.parseInt(line);
				
				//increment sp
				sp++;
				
				//pop sp from system stack
				pw.printf(sp + "\n");
				pw.flush();
				
				//wait for memory to send sp
				while(!sc.hasNextLine()) {}
				
				//read into sp
				line = sc.nextLine();
				sp = Integer.parseInt(line);
				
				//switch back to user mode
				kernelMode = false;
				break;
			
			//cpu process ends after memory process ends
			case 50:
				proc.waitFor();
				System.out.println("Exited");
				System.exit(0);	
				
			}
			//increment pc
			pc++;
			//increment timer
			interruptTimer++;
		}
	}
}
