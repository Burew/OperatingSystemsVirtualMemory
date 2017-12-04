import java.util.Arrays;

public class VirtualMemory extends PhysicalMemory {
	public int[] frame_bitmap;
	public TranslationLookasideBuffer TLB;
	public static byte	size_s = 9,
						size_p = 10,
						size_w = size_s;
	public static int 	mask_s = ((1 << size_s) - 1) << (size_w + size_p),
						mask_p = ((1 << size_p) - 1) << size_w,
						mask_w = (1 << size_w) - 1,
						frame_size = 512;

	public VirtualMemory() {
		super();
		frame_bitmap = new int[num_frames / Integer.SIZE];
		setBitMapIndex(0, 1); //reserve first block
		TLB = new TranslationLookasideBuffer();
	} 	

	public String read(int s, int p, int w){
		// read/process Segment Table entry
		if (PM[s] == -1){
			System.out.println("Read: pf");
			return "pf";
		} else if (PM[s] == 0){
			System.out.println("Read: err");
			return "err";
		}
		// read/process Page Table entry
		if (PM[PM[s] + p] == -1){
			System.out.println("Read: pf");
			return ("pf");
		} else if (PM[PM[s] + p] == 0){
			System.out.println("Read: err");
			return ("err");
		}
		
		System.out.println("Read returns " + Integer.toString(PM[PM[s] + p] + w));
		return Integer.toString(PM[PM[s] + p] + w);
	}	

	public String write(int s, int p, int w){
		// read/process segment table entry
		if (PM[s] == -1){
			System.out.println("Write: pf");
			return ("pf");
		} else if (PM[s] == 0){
			//allocate new Page Table
			int newPageTableAddr = findFreePageTableAddr();
			if (newPageTableAddr == -1){
				System.out.println("Write: err");
				return ("err");
			}
			System.out.println("Write: Page Table alloc at " + 
					Integer.toString(newPageTableAddr/frame_size) + "," +
					Integer.toString(newPageTableAddr/frame_size+ 1)
			);
			
			//set bitmap
			setBitMapIndex(newPageTableAddr/frame_size, 1);
			setBitMapIndex(newPageTableAddr/frame_size+ 1, 1);
			//update segment table
			PM[s] = newPageTableAddr;
		}

		// read/process page table entry
		if (PM[PM[s] + p] == -1){
			System.out.println("Write: pf");
			return ("pf");
		} else if (PM[PM[s] + p] == 0){
			//allocate new Page
			int newPageAddr = findFreePageAddr();
			if (newPageAddr == -1){
				System.out.println("Write: err");
				return ("err");
			}
			System.out.println("Write: new Page allocated at " + newPageAddr/frame_size);

			//set bitmap
			setBitMapIndex(newPageAddr/frame_size, 1);
			//update page table
			PM[PM[s] + p] = newPageAddr;
		}
		
		System.out.println("Write: returns " + Integer.toString(PM[PM[s] + p] + w));
		return Integer.toString(PM[PM[s] + p] + w);
	}
	
	public void init(String[] segmentAddr, String[] pageAddr){
		//init segments and their PT, these get 2 blocks
		for (int i = 0; i<segmentAddr.length; i += 2){
			int 	s = Integer.parseInt(segmentAddr[i]),
					f = Integer.parseInt(segmentAddr[i+1]);
			if (0 <= s && s < 512){ // check if valid
				PM[s] = f;
				if (f > 0){ //cannot set frame if 0 or -1
					setBitMapIndex(f/frame_size, 1);
					setBitMapIndex(f/frame_size+ 1, 1);
					System.out.println("Init: Page Table alloc at " + 
							Integer.toString(f/frame_size) + "," +
							Integer.toString(f/frame_size + 1)
					);
				}
			}
		}
		
		//init individual pages for segments, these get 1 block
		for (int i = 0; i<pageAddr.length; i += 3){
			int 	p = Integer.parseInt(pageAddr[i]),
					s = Integer.parseInt(pageAddr[i+1]),		
					f = Integer.parseInt(pageAddr[i+2]);		
			if (PM[s] > 0){ //check if valid page table
				PM[PM[s] + p] = f;
				if (f > 0) {
					setBitMapIndex(f / frame_size, 1);
					System.out.println("Init: Page alloc at " + Integer.toString(f/frame_size));
				}
			}
		}
		
	}
	
	public String translate(String[] inputLine){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inputLine.length; i += 2){
			int 	operation = Integer.parseInt(inputLine[i]),
					virtualAddr = Integer.parseInt(inputLine[i+1]),
					s = (virtualAddr & mask_s) >> (size_w + size_p),
					p = (virtualAddr & mask_p) >> size_w, 
					w = virtualAddr & mask_w;
						
			System.out.println("\nTranslating va: " + inputLine[i+1]);
			System.out.format("\ts:%d, p:%d, w:%d\n", s, p, w);
			if (operation == 0){ //read
				sb.append(read(s, p, w) + (i + 2 == inputLine.length? "": " "));
			} else { //write
				sb.append(write(s, p, w)+ (i + 2 == inputLine.length? "": " "));
			}
		}
		return sb.toString();
	}
	
	public String translateTLB(String[] inputLine){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inputLine.length; i += 2){
			int 	operation = Integer.parseInt(inputLine[i]),
					virtualAddr = Integer.parseInt(inputLine[i+1]);
			sb.append(processTLBInput(operation, virtualAddr) + (i + 2 == inputLine.length? "": " "));
		}
		return sb.toString();
	}

	public String processTLBInput(int operation, int VirtualAddr){
		int 	sp = (VirtualAddr & (mask_s | mask_p)) >> size_w,
				s = (VirtualAddr & mask_s) >> (size_w + size_p),
				p = (VirtualAddr & mask_p) >> size_w, 
				w = VirtualAddr & mask_w;
		int TLBindex = -1, PA = -1;
		
		//if TLB hit
		if ((TLBindex = TLB.lookupSP(sp)) != -1){
			int f = TLB.getF(TLBindex);
			PA = f + w;
			
			//update TLB contents - dec all LRU's > currentLRU
			int currentLRU = TLB.getLRU(TLBindex);
			for (int i = 0; i < TLB.numEntries; ++i){
				int testLRU = TLB.getLRU(i);
				if (testLRU > currentLRU)
					TLB.setLRU(i, testLRU - 1);
			}
			//set LRU to 3
			TLB.setLRU(TLBindex, 3);
			return String.format("h %d", PA);
		} else { //TLB miss
			String physicalAddr;
			if (operation == 0)
				physicalAddr = read(s, p, w);
			else 
				physicalAddr = write(s, p, w);
			if (physicalAddr.equals("pf") || physicalAddr.equals("err"))
				return String.format("m %s", physicalAddr);

			int freeTLBIndex = TLB.findZeroLRU();
			TLB.setSP(freeTLBIndex, sp);
			TLB.setF(freeTLBIndex, PM[PM[s] + p]);
			
			TLB.decrementAllLRU();
			TLB.setLRU(freeTLBIndex, 3);
			return String.format("m %s", physicalAddr);
		}
	}
	
	public void resetBitMap(){
		Arrays.fill(frame_bitmap, 0);
		setBitMapIndex(0, 1);
	}
	
/* Helper methods */
	
	//returns starting addr of free block
	private int findFreePageAddr(){
		for (int i = 0; i < num_frames / Integer.SIZE; ++i){
			for (int j = 0; j < Integer.SIZE; ++j){
				if ((frame_bitmap[i] & (1 << (Integer.SIZE - j - 1))) == 0){
					return (i * Integer.SIZE + j) * frame_size;
				}
			}
		}
		return -1;
	}
	
	//returns starting addr of free 2block
	private int findFreePageTableAddr(){
		for (int i = 0; i < num_frames / Integer.SIZE; ++i){
			boolean prevFree = false;
			for (int j = 0; j < Integer.SIZE; ++j){
				if ((frame_bitmap[i] & (1 << (Integer.SIZE - j - 1))) == 0){
					if (prevFree == false)
						prevFree = true;
					else
						return (i * Integer.SIZE + j - 1) * frame_size;
				} else {
					prevFree = false;
				}
			}
		}
		return -1;
	}
	
	private void setBitMapIndex(int frameNum, int value) {
		/* Note: index 0 corresponds to block 1
		 * 	this is due to zero-based indexing
		 */
		if (frameNum > num_frames - 1 || frameNum < 0)
			return;
		int i = frameNum / 32, j = frameNum % 32;
		frame_bitmap[i] |= (1 << (Integer.SIZE - 1 - j));
//		if (value == 0) {
//			frame_bitmap[i] &= ~(1 << (Integer.SIZE - 1 - j));
//		} else {
//			frame_bitmap[i] |= (1 << (Integer.SIZE - 1 - j));
//		}
	}

	public void printBitMap(){
		System.out.println("Printing Bitmap");
		for (int i = 0; i<frame_bitmap.length; ++i){
			System.out.println(String.format("%32s", Integer.toBinaryString(frame_bitmap[i])).replace(' ', '0'));
		}
	}
	
} 	//end Class Virtual Memory