import java.io.*;
import java.util.Scanner;

public class project1_memory {
	public static void main(String[] args) throws Exception{
		//create memory array
		int[] mem = new int[2000];
		//string to hold a line from input file
		String line;
		//pointer for inserting program into memory
		int address = 0;
		
		//read file, change to getting file name through scanner
		File input = new File(args[0]);
		Scanner sc = new Scanner(input);
		
		//insert program in input file into memory
		while(sc.hasNextLine()) {
			//process next line in the file
			line = sc.nextLine();
			//if the next line isn't empty and begins with a number or ".", it's valid
			//otherwise, skip the line
			if(!(line.isEmpty()) && ((Character.isDigit(line.charAt(0))) || (line.charAt(0) == '.'))  )
			{
				//if the command is followed by text, remove the text
				if(line.indexOf(" ") != -1)
					line = line.substring(0, line.indexOf(" "));
				
				//check to see if line starts with "."
				if(line.substring(0, 1).equals(".")) {
					//jump to address following "."
					address = Integer.parseInt(line.substring(1));
				}
				else if(!Character.isDigit(line.charAt(0))) {}
				//insert input int into mem array
				else {
					mem[address] = Integer.parseInt(line);
					address++;
				}
			}
		}
		
		//set up pipe
		sc = new Scanner(System.in);
		
		//memory is read from or written to until end instruction is encountered
		while(true) {
			//get read or write command from cpu
			while(!sc.hasNextLine()) {}
			line = sc.nextLine();
			
			//if the string contains a space, it is a write command
			if(line.contains(" ")) {
				//set address to provided address
				address = Integer.parseInt(line.substring(0, line.indexOf(" ")));
				//write data to address
				mem[address] = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
				//send ok for synchronization
				System.out.println(0);
			}
			//otherwise, it is a read command
			else {
				//convert address to integer and send instruction/address at specified address
				address = Integer.parseInt(line);
				//exit if cpu sends command
				if(address == -1)
					System.exit(0);
				//otherwise send value at address
				else
					System.out.println(mem[address]);
			}
			
			//exit loop if end instruction was encountered
			if(mem[address] == 50)
				break;
		}
		//end execution once end instruction was encountered
		System.exit(0);		
	}
}
