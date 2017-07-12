import java.util.ArrayList;

public class Bone {
	int id;
	ArrayList<Bone> childs = new ArrayList<Bone>();
	int model = -1;
	
	Bone(int id){
		this.id = id;
	}
}
