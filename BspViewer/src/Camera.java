import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.util.glu.GLU.*;

import org.lwjgl.input.Mouse;
public class Camera {
	
	private float x,y,z,rx,ry,rz,fov,aspect,near,far;

	public Camera(float fov, float aspect, float near, float far){
		x = -1.1021822E-16f;
		y = 0;
		z = -1.8000002f;
		rx = 0;
		ry = 0;
		rz = 0;
		
		this.fov = fov;
		this.aspect = aspect;
		this.near = near;
		this.far =far;
		initProjection();
	}
	private void initProjection(){
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		gluPerspective(fov,aspect,near,far);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	public void view(){
		GL11.glRotatef(rx, 1, 0, 0);
		GL11.glRotatef(ry, 0, 1, 0);		
		GL11.glRotatef(rz, 0, 0, 1);
		GL11.glTranslatef(x, y, z);
	}
	public float GetX(){
		return x;
	}
	
	public float GetY(){
		return y;
	}
	public float GetZ(){
		return z;
	}
	
	public void SetX(float x){
		this.x = x;
	}
	public void SetY(float y){
		this.y = y;
	}
	public void SetZ(float z){
		this.z = z;
	}
	public float GetRX(){
		return rx;
	}
	
	public float GetRY(){
		return ry;
	}
	public float GetRZ(){
		return rz;
	}
	
	public void SetRX(float rx){
		this.rx = rx;
	}
	public void SetRY(float ry){
		this.ry = ry;
	}
	public void SetRZ(float rz){
		this.rz = rz;
	}
	
	public void move(float amt, float dir){
		z+=amt * Math.sin(Math.toRadians(ry+90*dir));
		x+=amt * Math.cos(Math.toRadians(ry+90*dir));
	}
	
	public void RotateY(float amt){
		ry+=amt;
	}
	
	public void PosY(float amt){
		y+=amt;
	}
	
	public void RotateX(float amt){
		rx+=amt;
	}
	
}


