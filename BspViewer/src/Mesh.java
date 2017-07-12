import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh {
	public FloatBuffer vertices,normals,textcoords,weights;
	public byte[] color = new byte[4];
	public IntBuffer indices = null;
	public ByteBuffer texture = null;

	public Mesh(FloatBuffer vectors, FloatBuffer normals2, FloatBuffer textcoords2,FloatBuffer weights2, IntBuffer indices2, byte[] color2,
			ByteBuffer makeTexture) {
		this.vertices = vectors;
		this.normals = normals2;
		this.textcoords = textcoords2;
		this.weights = weights2;
		this.indices = indices2;
		this.texture = makeTexture;
	}
}


