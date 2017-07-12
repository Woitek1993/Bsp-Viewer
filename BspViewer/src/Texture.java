import org.lwjgl.opengl.GL11;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.lwjgl.BufferUtils;

public class Texture {
	private int id;
	private int width;
	private int height;
	public Texture(ImageIcon image){
		
		BufferedImage bi;
		try{
			bi = (BufferedImage) image.getImage();
			width = bi.getWidth();
			height = bi.getHeight();
			
			
		int[] pixels_raw = new int[width * height *4];
			pixels_raw = bi.getRGB(0,0,width,height,null,0,width);
			ByteBuffer pixels = BufferUtils.createByteBuffer(width*height*4);
			
			for(int i = height-1; i>=0; i--){
				for(int j = 0;j<width; j++){
					int pixel = pixels_raw[i*width+j];
					pixels.put((byte)((pixel >> 16) & 0xFF));
					pixels.put((byte)((pixel >> 8) & 0xFF));
					pixels.put((byte)(pixel & 0xFF));
					pixels.put((byte)((pixel >> 24) & 0xFF));
					
				}
			}
			pixels.flip();
			
			id = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0 , GL11.GL_RGBA, width, height,0,GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,pixels);
		}catch (Exception e){}
	}
	public void bind(){
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	}
	public void cleanTexture(){
			GL11.glDeleteTextures(id);
	}
}
