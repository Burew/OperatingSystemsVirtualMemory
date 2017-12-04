import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class InputOutput {
	public static void main(String[] args){
		VirtualMemory vm = new VirtualMemory();
		
		PrintWriter pw = null, pw2 = null;
		FileReader initFileReader = null, translateFileReader = null;
		Scanner initFile = null, translateFile = null;
		String outputFileName1 = "nnn-1.txt",//"D:/TestOutputs/nnn-1.txt", 	//no TLB used
				outputFileName2 = "nnn-2.txt",//"D:/TestOutputs/nnn-2.txt",	//TLB used
				initFileName = "input1.txt", //"D:/testCases/my_input1.txt",
				operationFileName = "input2.txt"; //"D:/testCases/my_input2.txt";
		try {
			pw = new PrintWriter(outputFileName1);
			pw2 = new PrintWriter(outputFileName2);
			initFileReader = new FileReader(initFileName);
			translateFileReader = new FileReader(operationFileName);
			initFile = new Scanner(initFileReader);
			translateFile = new Scanner(translateFileReader);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		//init physical mem
		String pageTableInput = null, pageInput = null;
		String [] parsed_input1 = null, parsed_input2;
		pageTableInput = initFile.nextLine();
		pageInput = initFile.nextLine();
		if (pageTableInput != null && pageInput != null){
			parsed_input1 = pageTableInput.trim().split("\\s+");
			parsed_input2 = pageInput.trim().split("\\s+");
			vm.init(parsed_input1, parsed_input2);
		}
		System.out.println("Init Done\n");

		//translate virtual to physical
		String translateInput = null;
		String [] parsed_translateInput = null;
		translateInput = translateFile.nextLine();
		if (translateInput != null){
			parsed_translateInput = translateInput.trim().split("\\s+"); 
			pw.println(vm.translate(parsed_translateInput));
		}
		System.out.println("Translate No TLB Done\n");

		
/* Re try with TLB 
 * -clear memory, bitmap, and reset filebuffers
 * */		
		vm.clearMemory();
		vm.resetBitMap();
		try {
			initFile = new Scanner(new FileReader(initFileName));
			translateFile = new Scanner(new FileReader(operationFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//init physical mem
		pageTableInput 	= pageInput 	= null;
		parsed_input1 	= parsed_input2 = null;
		pageTableInput 	= initFile.nextLine();
		pageInput 		= initFile.nextLine();
		if (pageTableInput != null && pageInput != null){
			parsed_input1 = pageTableInput.trim().split("\\s+");
			parsed_input2 = pageInput.trim().split("\\s+");
			vm.init(parsed_input1, parsed_input2);
		}
		System.out.println("Init2 Done\n");

		//translate virtual to physical
		translateInput = null;
		parsed_translateInput = null;
		translateInput = translateFile.nextLine();
		if (translateInput != null){
			parsed_translateInput = translateInput.trim().split("\\s+"); 
			pw2.println(vm.translateTLB(parsed_translateInput));
		}
		System.out.println("Translate TLB Done\n");
		vm.printBitMap();
		
		initFile.close();
		translateFile.close();
		pw.close();
		pw2.close();
	}
}
