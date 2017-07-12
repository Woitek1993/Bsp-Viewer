import java.util.ArrayList;

public class Type{

	public int type;
	public int size;
	public int tof;
	public byte[] data;
	public ArrayList<Integer> childs = new ArrayList<Integer>();
	
	Type(){
	}
	
	Type(int type, int size, int tof, byte[]data){
		this.type = type;
		this.size = size;
		this.tof = tof;
		this.data = data;
	}
	
	public int type(){
		return type;
	}
	public int size(){
		return size;
	}
	public int tof(){
		return tof;
	}
	public byte[] data(){
		return data;
	}
	
    public Type clone() {
    	Type chunk = new Type();
    	chunk.type = this.type;
    	chunk.size = this.size;
    	chunk.tof = this.tof;
    	chunk.data = this.data;
        return chunk;
    }
}
	
