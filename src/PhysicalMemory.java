import java.util.Arrays;

// Contains only the physical memory needed 
public class PhysicalMemory {
	protected int[] PM;
	protected final int	num_frames = 1024, 	
						size_frames = 512;	

	public PhysicalMemory() {
		PM = new int[num_frames * size_frames];
	}
	
	protected void clearMemory(){
		Arrays.fill(PM, 0);
	}
}
