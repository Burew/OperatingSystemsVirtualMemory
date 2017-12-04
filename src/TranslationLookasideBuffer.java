public class TranslationLookasideBuffer {
	final int numEntries = 4,
			LRUIndex = 0,
			SPIndex = 1,
			FIndex = 2;
	int[][] data;
	
	public TranslationLookasideBuffer() {
		data = new int[numEntries][3]; //4 rows, 3 columns
		for (int i = 0; i != numEntries; ++i){
			data[i][SPIndex] = -1;
		}
	}
	
	public int lookupSP(int sp){
		for (int i=0; i<numEntries; ++i)
			if (data[i][SPIndex] == sp)
				return i;
		return -1;
	}
	
	public int findZeroLRU(){
		for (int i=0; i<numEntries; ++i)
			if (data[i][LRUIndex] == 0)
				return i;
		return -1;
	}
	
	public void decrementAllLRU(){
		for (int i=0; i<numEntries; ++i){
			if (data[i][LRUIndex] > 0)
				--data[i][LRUIndex];
		}
	}
	
	public int getLRU(int dataIndex){
		return data[dataIndex][LRUIndex]; 
	}
	
	public int getSP(int dataIndex){
		return data[dataIndex][SPIndex]; 
	}

	public int getF(int dataIndex){
		return data[dataIndex][FIndex]; 
	}

	public void setLRU(int dataIndex, int newLRU){
		data[dataIndex][LRUIndex] = newLRU; 
	}
	
	public void setSP(int dataIndex, int newSP){
		data[dataIndex][SPIndex] = newSP; 
	}

	public void setF(int dataIndex, int newF){
		data[dataIndex][FIndex] = newF; 
	}
	
	public void printTLB(){
		System.out.println("Printing TLB");
		System.out.println("LRU\tSP\tF");
		for (int i = 0; i < numEntries; ++i)
			System.out.println(String.format("%d,\t%d,\t%d",
					getLRU(i),
					getSP(i),
					getF(i)
			));
	}
	
}
