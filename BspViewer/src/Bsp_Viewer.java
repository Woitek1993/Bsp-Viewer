import java.awt.Button;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
//java OpenGL
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//gzip
import java.util.zip.GZIPInputStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class Bsp_Viewer{

	private FileNameExtensionFilter filter;
	private File file,imgFile,expFile;
	private DefaultTreeModel model;
	private DefaultMutableTreeNode root;
	
	private JFrame frmBspviewer;
	private ArrayList<Type> object;
	private ArrayList<String> materialname;
	private ArrayList<String> animname;
	private ArrayList<String> materialid;
	private ArrayList<String> texturename;
	private ArrayList<Integer> tlevel;
	private ArrayList<Integer> parent;
	private ArrayList<byte[]>  texturedata;	
	private JLabel lblNewLabel;
	int counter,ttree;
	int offset,current,next,level,framecounter;
	public byte[] bytes;
	public int licznik, difference;
	JTextArea textArea;
	public int nameold;
	JLabel lbl_Name,label,label_1,label_2,label_3,label_4,label_5;
	JButton saveChanges;
	
	
	private boolean selectBone = false;
	private int indexBoneS = 0;
	
	
	//export node
	private int secondIndex;
	
	//animation editor
	private boolean done;
	private int[] boneIndexes;
	private float minTime,maxTime;
	
	//search
	ArrayList<DefaultMutableTreeNode> searchNode;
	String previous;
	int searchCounter;
	
	//Image
	int texturec;
	
	//frame(bone count)
	private ArrayList<String> bonehash,bonename;
	
	
	//MaterialHash
	 private ArrayList<String> hashname_old;
	 private ArrayList<String> hashname_new;
	 
	Color colors,colors2;
 	int old_size;
 	int new_size;
	
	//JprogressBar
	JTree tree;
	boolean modified = false;
	//Model Load
	ArrayList<Bone> bones;
	
	ArrayList<Model> meshes;
	ArrayList<Texture> textures;
	ArrayList<Vector3f> transBounds;
	JCheckBoxMenuItem texturesOn,wireframe,BonesOn,showModel,debugMode;
	
	//1005
	ArrayList<float[]> skinMatrix,transMatrix;
	ArrayList<Integer> skinHash;
	
	//1026
	//private float[] currentJoint;
	//1002
	int meshcounter;
	//1009
	int record;
	int currentLevel;
	int nextLevel;
	private ArrayList<float[]> currentMatrix;
	private ArrayList<float[]> boneMatrix;
	private JTextField textField;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Bsp_Viewer window = new Bsp_Viewer();
					window.frmBspviewer.setVisible(true);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	
	public Bsp_Viewer() {
		initialize();
	}
	
	public int toInteger(byte[] data, int index){
		return ((data[index+3] & 0xFF) << 24) | ((data[index+2] & 0xFF) << 16) | ((data[index+1] & 0xFF) << 8) | (data[index] & 0xFF);
	}
	
	public short toShort(byte[] data, int index){
		return (short) (((data[index+1] & 0xFF) << 8) | (data[index] & 0xFF));
	}
	
	public float toFloat(byte[] data, int index){
		return Float.intBitsToFloat(((data[index+3] & 0xFF) << 24) | ((data[index+2] & 0xFF) << 16) | ((data[index+1] & 0xFF) << 8) | (data[index] & 0xFF));
	}
	
	public void data(Type chunk){
		chunk.data =  Arrays.copyOfRange(bytes, counter, counter+chunk.size);
		counter += chunk.size;
	}
	
	public String dataToString(byte[] data){
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i<data.length; i++){
        	builder.append(String.format("%02X ",data[i]));
        }
        return builder.toString();
	}
	
	public String GetName(byte[] data, int type){
		StringBuilder builder = new StringBuilder();
        for (int j = 128; j<data.length;j++){
        	builder.append((char)data[j]);
        }
        return builder.toString();
	}
	

	public String makeString(byte[] data,int start, int bytecount){
		StringBuilder builder = new StringBuilder();
		//for(int i = start+bytecount-1; i>=start; i--){
		for(int i = start; i<start+bytecount; i++){
        	builder.append(String.format("%02x",data[i]));
        }
        return builder.toString();
	}
	
	public byte[] hexStringToByteArray(String s) {
		try{
			int len = s.length();
			byte[] data = new byte[len / 2];
			for (int i = 0; i < len; i += 2) {
				data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
			}
		
			return data;
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Wrong number of arguments");
		}
		return null;
	}
	private void exportNode(int index){
		if(object.get(index).childs.size()!= 0){
			exportNode(object.get(index).childs.get(object.get(index).childs.size()-1));
		}else{
			secondIndex = index;
		}
	}
	
	
	private void removeNode(int index){
		if(object.get(index).childs.size()!= 0){
			for(int i = object.get(index).childs.size()-1; i>=0; i--){
				removeNode(object.get(index).childs.get(i));
			}
		}
		difference -= object.get(index).size + 12;
		object.remove(index);
	}
	
	
	public void unzip_load() throws IOException{
		byte[] buffer = new byte[1024];

	     try{

	    	 GZIPInputStream gzis =
	    		new GZIPInputStream(new FileInputStream(file.getAbsolutePath()));
	    	 
	    	 FileOutputStream out =
	            new FileOutputStream(file.getParent()+"\\"+file.getName()+"test");

	        int len;	        
	        while ((len = gzis.read(buffer)) > 0) {
	        	out.write(buffer, 0, len);
	        }
	        
	        gzis.close();
	    	out.close();

	    }catch(IOException ex){
	       ex.printStackTrace();
	    }
	     bytes = null;
	     Path path = Paths.get(file.getParent()+"\\"+file.getName()+"test");
	     bytes = Files.readAllBytes(path);
	     Files.delete(path);
	}
	
	private File returnPath(String name, String Format, String Dialog, String regPath){
		JFileChooser jfimportNode =  new JFileChooser(Advapi32Util.registryGetStringValue(
		WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", regPath));
		
    	FileNameExtensionFilter fnefImportNode = new FileNameExtensionFilter(name, Format);	
    	jfimportNode.setDialogTitle(Dialog);
    	jfimportNode.setFileFilter(fnefImportNode);
    	jfimportNode.showOpenDialog(null);
    	File fileImportNode = jfimportNode.getSelectedFile();	
		Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExport", fileImportNode.getPath());
    	return fileImportNode;
	}
	
	
	private void replaceMeshHash(){
		 //replace in all 1002
			int licznik =0;
			int hashindex;
				for(int c = 0; c<object.size(); c++){
					if(object.get(c).type == 1002 ){
						hashindex = hashname_old.indexOf(
						""+String.format("%02x", object.get(licznik).data[42])
						+""+String.format("%02x", object.get(licznik).data[43])
						+""+String.format("%02x", object.get(licznik).data[44])
						+""+String.format("%02x", object.get(licznik).data[45]));
						if(hashindex != -1){								
						    int len = hashname_new.get(hashindex).length();
						    byte[] names = new byte[len / 2];
						    for (int i = 0; i < len; i += 2) {
						        names[i / 2] = (byte) ((Character.digit(hashname_new.get(hashindex).charAt(i), 16) << 4)
						                             + Character.digit(hashname_new.get(hashindex).charAt(i+1), 16));
						    }
							object.get(licznik).data[42] = names[0];
							object.get(licznik).data[43] = names[1];
							object.get(licznik).data[44] = names[2];
							object.get(licznik).data[45] = names[3];
						}
					}
					licznik++;
				}
	}
	
	private void replaceMaterial(byte[] replace){
		try{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
    	//replace in all materials
			 StringBuilder builder= new StringBuilder(),builder2 = new StringBuilder();
			 int counter = toInteger(object.get(1).data, 0);
			 for(int indexof = 2; indexof<counter+2; indexof++){
			 int name = toInteger(object.get(indexof).data,96);
			 int namecounter = 0;
				if (name != -1){
					namecounter = toInteger(object.get(indexof).data,100);
					builder = new StringBuilder();
					for(int i = 104; i<104+namecounter*4-4;i=i+4){
						builder.append((char)object.get(indexof).data[i]);
					}
					if(object.get(indexof).data[104+namecounter*4+44] == 1){
						builder2 = new StringBuilder();
						int namecounter2 = toInteger(object.get(indexof).data,104+namecounter*4+48);
    					for(int i = 104+namecounter*4+52; i<104+namecounter*4+52+namecounter2*4-4;i=i+4){
    						builder2.append((char)object.get(indexof).data[i]);
    					}
					}
            	}
					if(builder.toString().equals(texturename.get(node.getParent().getIndex(node)).substring(0, texturename.get(node.getParent().getIndex(node)).length()-1))){	
						materialhash(object.get(indexof).data, replace, namecounter, false);
					}else if(builder2.toString().equals(texturename.get(node.getParent().getIndex(node)).substring(0, texturename.get(node.getParent().getIndex(node)).length()-1))){
						materialhash(object.get(indexof).data, replace, namecounter, true);
					}
			 }
			 JOptionPane.showMessageDialog(null,"Texture was imported successfully!");
        }catch(Exception z){
        	z.printStackTrace();
        }
	}
	
	private byte[] importTga(Path path, byte[] materialname){
		try{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
			byte[] targa = null;
			if(path == null){
				File impFile = returnPath("Targa Format", "tga","Open Texture","pathImport");
				targa = Files.readAllBytes(impFile.toPath());
			}else{
				targa = Files.readAllBytes(path);
			}
        


			byte[] uncompressed = new byte[] {0,0,2,0,0,0,0,0,0,0,0,0};
			byte[] test = Arrays.copyOfRange(targa, 0, 12);
			if(Arrays.equals(test, uncompressed)){
				int twidth = (targa[13]<<8 | targa[12] & 0xFF);
				int theight = (targa[15]<<8 | targa[14] & 0xFF);
				byte bpp = targa[16];
				int textname = 0,value1 = 0,value2 = 0;
				
				if(path == null){
					textname = toInteger(texturedata.get(node.getParent().getIndex(node)),0);
					int width = toInteger(texturedata.get(node.getParent().getIndex(node)),4+textname*4+4);
					int height = toInteger(texturedata.get(node.getParent().getIndex(node)),4+textname*4+8);
					value1 = toInteger(texturedata.get(node.getParent().getIndex(node)),4+textname*4+12);
					value2 = toInteger(texturedata.get(node.getParent().getIndex(node)),4+textname*4+16);
				
					int diff = 0;
					int chunksize = object.get(0).size;
					if(twidth*theight > width*height){
						diff = (twidth*theight)-(width*height);
						chunksize+=diff*16;
						difference+=diff*16;
						object.get(0).size = chunksize;
						
					}else if(twidth*theight < width*height){
						diff = (width*height)-(twidth*theight);
						chunksize-=diff*16;	
						difference-=diff*16;	
						object.get(0).size = chunksize;
					}else{}
				
					ByteBuffer size = ByteBuffer.allocate(8);
					size.putInt(Integer.reverseBytes(twidth));
					size.putInt(Integer.reverseBytes(theight));
					byte [] newsize = size.array();
					for(int i = 0; i<newsize.length; i++){
						texturedata.get(node.getParent().getIndex(node))[4+textname*4+4+i] =  newsize[i];
					}
				}
			int col = 0;
			int k,l;
			k = 0;
			l = twidth-1;
			int[][]expimage =  new int[theight][twidth];
			if(bpp == 32){ //BRGA
				for(int j = 18+(twidth*theight*4)-1; j>18; j-=4 ){
					//RGBA
					//BGRA
					col = ((targa[j-1]& 0xFF) << 24) | ((targa[j-2]& 0xFF) << 16) | ((targa[j-3]& 0xFF) << 8) | (targa[j] & 0xFF);
					expimage[k][l] = col;
					l--;
						if(l< 0){
							k++;
							l = twidth-1;
						}
					}
					
				}else if(bpp ==24){ //BGR
					for(int j = 18+(twidth*theight*3)-1; j>18; j-=3 ){
						//RGBA
						//BGR
						col = ((targa[j] & 0xFF) << 24) | ((targa[j-1] & 0xFF) << 16) | ((targa[j-2] & 0xFF) << 8) | (byte)255 & 0xFF;
						expimage[k][l] = col;
						l--;
						if(l< 0){
							k++;
							l = twidth-1;
						}	
					}
				
				}
			if(debugMode.isSelected()){
				int once;
				String hashme = "00000000B71DC1046E3B8209D926430DDC7604136B6BC517B24D861A0550471EB8ED08260FF0C922D6D68A2F61CB4B2B649B0C35D386CD310AA08E3CBDBD4F3870DB114CC7C6D0481EE09345A9FD5241ACAD155F1BB0D45BC2969756758B5652C836196A7F2BD86EA60D9B6311105A6714401D79A35DDC7D7A7B9F70CD665E74E0B6239857ABE29C8E8DA191399060953CC0278B8BDDE68F52FBA582E5E66486585B2BBEEF46EABA3660A9B7817D68B3842D2FAD3330EEA9EA16ADA45D0B6CA0906D32D42770F3D0FE56B0DD494B71D94C1B36C7FB06F7C32220B4CE953D75CA28803AF29F9DFBF646BBB8FBF1A679FFF4F63EE143EBFFE59ACDBCE82DD07DEC77708634C06D4730194B043DAE56C539AB0682271C1B4323C53D002E7220C12ACF9D8E1278804F16A1A60C1B16BBCD1F13EB8A01A4F64B057DD00808CACDC90C07AB9778B0B6567C69901571DE8DD475DBDD936B6CC0526FB5E6116202FBD066BF469F5E085B5E5AD17D1D576660DC5363309B4DD42D5A490D0B1944BA16D84097C6A5AC20DB64A8F9FD27A54EE0E6A14BB0A1BFFCAD60BB258B23B69296E2B22F2BAD8A98366C8E41102F83F60DEE87F35DA9994440689D9D662B902A7BEA94E71DB4E0500075E4892636E93E3BF7ED3B6BB0F38C7671F7555032FAE24DF3FE5FF0BCC6E8ED7DC231CB3ECF86D6FFCB8386B8D5349B79D1EDBD3ADC5AA0FBD8EEE00C6959FDCD6D80DB8E6037C64F643296087A858BC97E5CAD8A73EBB04B77560D044FE110C54B383686468F2B47428A7B005C3D66C158E4408255535D43519E3B1D252926DC21F0009F2C471D5E28424D1936F550D8322C769B3F9B6B5A3B26D6150391CBD40748ED970AFFF0560EFAA011104DBDD014949B93192386521D0E562FF1B94BEEF5606DADF8D7706CFCD2202BE2653DEAE6BC1BA9EB0B0668EFB6BB27D701A6E6D3D880A5DE6F9D64DA6ACD23C4DDD0E2C004F6A1CDB3EB60C97E8D3EBDC990FFB910B6BCB4A7AB7DB0A2FB3AAE15E6FBAACCC0B8A77BDD79A3C660369B717DF79FA85BB4921F4675961A163288AD0BF38C742DB081C330718599908A5D2E8D4B59F7AB085440B6C95045E68E4EF2FB4F4A2BDD0C479CC0CD43217D827B9660437F4F460072F85BC176FD0B86684A16476C93300461242DC565E94B9B115E565A1587701918306DD81C353D9F0282205E065B061D0BEC1BDC0F51A69337E6BB52333F9D113E8880D03A8DD097243ACD5620E3EB152D54F6D4297926A9C5CE3B68C1171D2BCCA000EAC8A550ADD6124D6CD2CB6B2FDF7C76EEDBC1CBA1E376D660E7AFF023EA18EDE2EE1DBDA5F0AAA064F4738627F9C49BE6FD09FDB889BEE0798D67C63A80D0DBFB84D58BBC9A62967D9EBBB03E930CADFF97B110B0AF060D71ABDF2B32A66836F3A26D66B4BCDA7B75B8035D36B5B440F7B1";
			    byte[] hashvalues = new byte[hashme.length() / 2];
			    for (int i = 0; i < hashvalues.length; i++) {
			      int index = i * 2;
			      int v = Integer.parseInt(hashme.substring(index, index + 2), 16);
			      hashvalues[i] = (byte) v;
			    }
				for(int z = 0; z<hashvalues.length; z+=4){
					once = ((hashvalues[z+3] & 0xFF) << 24) | ((hashvalues[z+2] & 0xFF) << 16) | ((hashvalues[z+1] & 0xFF) << 8) | (hashvalues[z] & 0xFF);
					System.out.print(once + ",");
				}
			}
			
			
			 int[]hashvalues = {0,79764919,159529838,222504665,319059676,398814059,445009330,507990021,638119352,583659535,797628118,726387553,890018660,835552979,1015980042,944750013,1276238704,1221641927,1167319070,1095957929,1595256236,1540665371,1452775106,1381403509,1780037320,1859660671,1671105958,1733955601,2031960084,2111593891,1889500026,1952343757,-1742489888,-1662866601,-1851683442,-1788833735,-1960329156,-1880695413,-2103051438,-2040207643,-1104454824,-1159051537,-1213636554,-1284997759,-1389417084,-1444007885,-1532160278,-1603531939,-734892656,-789352409,-575645954,-646886583,-952755380,-1007220997,-827056094,-898286187,-231047128,-151282273,-71779514,-8804623,-515967244,-436212925,-390279782,-327299027,881225847,809987520,1023691545,969234094,662832811,591600412,771767749,717299826,311336399,374308984,453813921,533576470,25881363,88864420,134795389,214552010,2023205639,2086057648,1897238633,1976864222,1804852699,1867694188,1645340341,1724971778,1587496639,1516133128,1461550545,1406951526,1302016099,1230646740,1142491917,1087903418,-1398421865,-1469785312,-1524105735,-1578704818,-1079922613,-1151291908,-1239184603,-1293773166,-1968362705,-1905510760,-2094067647,-2014441994,-1716953613,-1654112188,-1876203875,-1796572374,-525066777,-462094256,-382327159,-302564546,-206542021,-143559028,-97365931,-17609246,-960696225,-1031934488,-817968335,-872425850,-709327229,-780559564,-600130067,-654598054,1762451694,1842216281,1619975040,1682949687,2047383090,2127137669,1938468188,2001449195,1325665622,1271206113,1183200824,1111960463,1543535498,1489069629,1434599652,1363369299,622672798,568075817,748617968,677256519,907627842,853037301,1067152940,995781531,51762726,131386257,177728840,240578815,269590778,349224269,429104020,491947555,-248556018,-168932423,-122852000,-60002089,-500490030,-420856475,-341238852,-278395381,-685261898,-739858943,-559578920,-630940305,-1004286614,-1058877219,-845023740,-916395085,-1119974018,-1174433591,-1262701040,-1333941337,-1371866206,-1426332139,-1481064244,-1552294533,-1690935098,-1611170447,-1833673816,-1770699233,-2009983462,-1930228819,-2119160460,-2056179517,1569362073,1498123566,1409854455,1355396672,1317987909,1246755826,1192025387,1137557660,2072149281,2135122070,1912620623,1992383480,1753615357,1816598090,1627664531,1707420964,295390185,358241886,404320391,483945776,43990325,106832002,186451547,266083308,932423249,861060070,1041341759,986742920,613929101,542559546,756411363,701822548,-978770311,-1050133554,-869589737,-924188512,-693284699,-764654318,-550540341,-605129092,-475935807,-413084042,-366743377,-287118056,-257573603,-194731862,-114850189,-35218492,-1984365303,-1921392450,-2143631769,-2063868976,-1698919467,-1635936670,-1824608069,-1744851700,-1347415887,-1418654458,-1506661409,-1561119128,-1129027987,-1200260134,-1254728445,-1309196108};
			 ByteBuffer texture;
			 if(path != null){
				 int namecounter = toInteger(materialname,100);
				 texture = ByteBuffer.allocate(4+namecounter*4+40+theight*twidth*16);
				 texture.put(Arrays.copyOfRange(materialname,100,104+namecounter*4));
				 texture.putInt(0);
				 texture.putInt(Integer.reverseBytes(twidth));
				 texture.putInt(Integer.reverseBytes(theight));
				 texture.put(Arrays.copyOfRange(materialname,104+namecounter*4+4,104+namecounter*4+32));
				 value1 = toInteger(texture.array(),4+namecounter*4+12);
				 value2 = toInteger(texture.array(),4+namecounter*4+16);
			 }else{
				 texture = ByteBuffer.allocate(4+textname*4+40+theight*twidth*16);
				 texture.put(Arrays.copyOfRange(texturedata.get(node.getParent().getIndex(node)), 0, 4+textname*4+40));
			 }

			 int hashsum1 = 0;
			 for(int i = 0; i<theight; i++)
			 {
			     for(int j = 0; j<twidth; j++)
			     {
			     texture.put((byte) ((expimage[i][j]>> 24) & 0xFF) );texture.put((byte)0);texture.put((byte)0);texture.put((byte)0);
				 texture.put((byte) ((expimage[i][j]>> 16) & 0xFF) );texture.put((byte)0);texture.put((byte)0);texture.put((byte)0);
				 texture.put((byte) ((expimage[i][j]>> 8) & 0xFF) );texture.put((byte)0);texture.put((byte)0);texture.put((byte)0);
				 texture.put((byte) (expimage[i][j] & 0xFF) );texture.put((byte)0);texture.put((byte)0);texture.put((byte)0);
					 hashsum1 = hashvalues[(int)(((hashsum1 >> 24)) ^ ((byte) ((expimage[i][j]>> 24) & 0xFF) ))& 0xFF] ^ (hashsum1 << 8);
					 hashsum1 = hashvalues[(int)(((hashsum1 >> 24)) ^ ((byte) ((expimage[i][j]>> 16) & 0xFF) ))& 0xFF] ^ (hashsum1 << 8);
					 hashsum1 = hashvalues[(int)(((hashsum1 >> 24)) ^ ((byte) ((expimage[i][j]>> 8) & 0xFF) )) & 0xFF] ^ (hashsum1 << 8);
					 hashsum1 = hashvalues[(int)(((hashsum1 >> 24)) ^ ((byte) (expimage[i][j] & 0xFF) )) & 0xFF] ^ (hashsum1 << 8);
			     }
			 }
			 if(path != null){
				 boolean exist = false;
				 byte[] replace = texture.array();
				 for(int i = 0; i<texturedata.size(); i++){
					 if(Arrays.equals(texturedata.get(i), replace)){
						 exist = true;
						 break;
					 }
				 }
				 if(!exist){
					 texturedata.add(replace);
					 difference += texture.limit();
					 object.get(0).size += texture.limit();
				 }
			 }else{
				 texturedata.set((node.getParent().getIndex(node)), texture.array());
			 }
			 
			 ByteBuffer bfhash = ByteBuffer.allocate(8);
			 bfhash.putInt(Integer.reverseBytes(value2));
			 bfhash.putInt(Integer.reverseBytes(value1));
			 
			 int hashsum3 = 0;
			 byte[]hasharray = bfhash.array();
			 for (int u = 0; u<hasharray.length;u++){
				 hashsum3 = hashvalues[(int)((hashsum3 >> 24) ^ (hasharray[u])) & 0xFF] ^ (hashsum3 << 8);
			 }
			 int hashsum4 = (hasharray.length ^ hashsum3);
			 int hashsum2 = (twidth*theight*4) ^ hashsum1;
			 int sum = hashsum2 ^ hashsum4;
			 if(debugMode.isSelected()){
				 System.out.println(Integer.toHexString(Integer.reverseBytes(sum)));
			 }
			 

			 
			 ByteBuffer replace = ByteBuffer.allocate(4);
			 
			 replace.putInt(Integer.reverseBytes(sum));
			 replace.flip();
			 return replace.array();
			}else{
    			JOptionPane.showMessageDialog(null,"Wrong targa type!");
    		}
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	
	private int addMaterial(){
		try{
			File fileMtl = returnPath("MTL Format","mtl","Open Material file","pathImportModel");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileMtl), "UTF-8"));
			String str;
			
			int[] ambient = new int[3];
			int[] diffuse =  new int[3];
			int isAlpha = 0;
			int namecounter = -1;
			String[] fname = null;
			String path = null;
			while ((str = in.readLine()) != null) {
				if(str.startsWith("Kd")){
					String[] temp = str.split("\\s+");
					diffuse[0] = (int)(255*Float.parseFloat(temp[1]));
					diffuse[1] = (int)(255*Float.parseFloat(temp[2]));
					diffuse[2] = (int)(255*Float.parseFloat(temp[3]));
				}else if(str.startsWith("Ka")){
					String[] temp = str.split("\\s+");
					ambient[0] = (int)(255*Float.parseFloat(temp[1]));
					ambient[1] = (int)(255*Float.parseFloat(temp[2]));
					ambient[2] = (int)(255*Float.parseFloat(temp[3]));
				}else if(str.startsWith("map_Kd")){
					String[] temp = str.split("\\s+");
					path = fileMtl.getParent()+ "\\" + temp[1];
					fname = temp[1].split("\\.");
					
					namecounter = fname[0].length()+1;
					byte[] texture = Files.readAllBytes(Paths.get(path));
					//sprawdz czy jest 32bit
					byte bpp = texture[16];
					if(bpp == 32){
						isAlpha = 8;
					}
				}
			}
			ByteBuffer newMaterial = ByteBuffer.allocate(104 + namecounter*4 + 116);
			//otworz plik
			newMaterial.putInt(Integer.reverseBytes(isAlpha));
			newMaterial.put(new byte[8]);
			newMaterial.putInt(Integer.reverseBytes(ambient[0]));
			newMaterial.putInt(Integer.reverseBytes(ambient[1]));
			newMaterial.putInt(Integer.reverseBytes(ambient[2]));
			newMaterial.putInt(Integer.reverseBytes(255));
			newMaterial.putInt(Integer.reverseBytes(diffuse[0]));
			newMaterial.putInt(Integer.reverseBytes(diffuse[1]));
			newMaterial.putInt(Integer.reverseBytes(diffuse[2]));
			newMaterial.putInt(Integer.reverseBytes(255));
			newMaterial.put(new byte[]{0x00,0x00,0x00,0x00,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x05,0x00,0x00,0x00,0x06,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x05,0x00,0x00,0x00,0x00,0x00,0x00,0x3F,0x01,0x00,0x00,0x00,0x04,0x00,0x00,0x00});
			newMaterial.putInt(Integer.reverseBytes(-1));
			newMaterial.put(new byte[]{0x00,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
			newMaterial.putInt(Integer.reverseBytes(namecounter));
			if(namecounter != 0){
				byte[] name = fname[0].getBytes();
				for(int i = 0; i<name.length; i++){
					newMaterial.put(new byte[]{name[i], 0x00, 0x00, 0x00});
				}
				newMaterial.put(new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
				newMaterial.putInt(Integer.reverseBytes(diffuse[0]));
				newMaterial.putInt(Integer.reverseBytes(diffuse[1]));
				newMaterial.putInt(Integer.reverseBytes(diffuse[2]));
				newMaterial.putInt(Integer.reverseBytes(255));
			}
			texturename.add(fname[0]);
			newMaterial.putInt(-1);//hash
			newMaterial.putInt(Integer.reverseBytes(-1));
			newMaterial.putInt(Integer.reverseBytes(0));
			newMaterial.putInt(Integer.reverseBytes(-1));
			newMaterial.putInt(Integer.reverseBytes(0));
			newMaterial.putInt(Integer.reverseBytes(-1));
			newMaterial.putInt(Integer.reverseBytes(0));
			newMaterial.putInt(Integer.reverseBytes(-1));
			newMaterial.putInt(Integer.reverseBytes(0));
			newMaterial.put(new byte[48]);
			
			newMaterial.flip();
			
			byte[] material = newMaterial.array();
			//add material
			int counter = toInteger(object.get(1).data, 0);
			
			materialhash(material,importTga(Paths.get(path), material),namecounter, false);
			
			int index = materialid.indexOf(hashname_new.get(hashname_new.size()-1));
			if(index == -1){
				counter++;
				object.get(1).data = new byte[]{(byte)counter,(byte)(counter >> 8),(byte)(counter >> 16),(byte)(counter >> 24)};
				
				ArrayList<Type> temp1 = new ArrayList<Type>(object.subList(0, 1+counter));
				ArrayList<Type> temp2 = new ArrayList<Type>(object.subList(1+counter, object.size()));
				
				Type temp = new Type(5,newMaterial.limit(),1698,material);
				
				difference += temp.size + 12;
				
				temp1.add(temp);
				temp1.addAll(temp2);
				object = new ArrayList<Type>(temp1);
			}else{
				JOptionPane.showMessageDialog(null,"material: " + fname[0] + " exists in file!");
				return index+1;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return -1;
	}
	
	private ByteBuffer prepareToExport(int index){
		int textname = toInteger(texturedata.get(index),0);
		int width = toInteger(texturedata.get(index),4+textname*4+4);
		int height = toInteger(texturedata.get(index),4+textname*4+8);
		int r,g,b,a;
		ByteBuffer expimg = ByteBuffer.allocate(18+width*height*4);
		expimg.put(new byte[] {0,0,2,0,0,0,0,0,0,0,0,0});
		expimg.put((byte) (width & 0xFF));
		expimg.put((byte) ((width >> 8) & 0xFF));
		expimg.put((byte) (height & 0xFF));
		expimg.put((byte) ((height >> 8) & 0xFF));
		expimg.put((byte) (32));
		expimg.put((byte) (0));
		//for(int i = 4+textname*4+40;i<4+textname*4+40+width*height*16; i+=16){
		 int col = 0;
		 int k,l;
		 k = 0;
		 l = width-1;
		 int[][]expimage =  new int[height][width];
		 for(int i = 4+textname*4+40+width*height*16;i>4+textname*4+40; i-=16){
						r = toInteger(texturedata.get(index),i-16);
						g = toInteger(texturedata.get(index),i-12);
						b = toInteger(texturedata.get(index),i-8);
						a = toInteger(texturedata.get(index),i-4);
						col = (b << 24) | (g << 16) | (r << 8) | a;	
						expimage[k][l] = col;
						l--;
						if(l< 0){
							k++;
							l = width-1;
						}
		}
		 for(int i = 0; i<height; i++)
		 {
		     for(int j = 0; j<width; j++)
		     {
		    	 expimg.putInt(expimage[i][j]);
		     }
		 }
		 expimg.flip();
		 return expimg;
	}
	
	
	private Type importObjModel(){
		try {
			File fileObj = returnPath("OBJ Format","obj","Open OBJ file","pathImportModel");
			
			//load obj
			
			ArrayList<Vector3f> vertex = new ArrayList<Vector3f>();
			ArrayList<Vector2f> txtcoord = new ArrayList<Vector2f>();
			ArrayList<Vector3f> normal = new ArrayList<Vector3f>();
			ArrayList<Integer> indices = new ArrayList<Integer>();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileObj), "UTF-8"));
			String str;
			int textcoords = 0;
			while ((str = in.readLine()) != null) {
				String[] temp = str.split("\\s+");
				if(str.startsWith("v ")){ //vertices
					vertex.add(new Vector3f(Float.parseFloat(temp[1]),Float.parseFloat(temp[2]),Float.parseFloat(temp[3])));
				}
				else if(str.startsWith("vt ")){ //texture coords
					txtcoord.add(new Vector2f(Float.parseFloat(temp[1]),Float.parseFloat(temp[2])*-1));
				}
				else if(str.startsWith("vn ")){ //normals
					normal.add(new Vector3f(Float.parseFloat(temp[1]),Float.parseFloat(temp[2]),Float.parseFloat(temp[3])));
				}
				else if(str.startsWith("f ")){ //indices
					if(txtcoord.size() != 0){
						textcoords = 8;
						String[] vertex1 = temp[1].split("/");
						String[] vertex2 = temp[2].split("/");
						String[] vertex3 = temp[3].split("/");
						indices.add(Integer.reverseBytes(Integer.parseInt(vertex1[0])-1));
						
						indices.add(Integer.reverseBytes(Integer.parseInt(vertex2[0])-1));
						
						indices.add(Integer.reverseBytes(Integer.parseInt(vertex3[0])-1));
					}else{
						String[] vertex1 = temp[1].split("//");
						String[] vertex2 = temp[2].split("//");
						String[] vertex3 = temp[3].split("//");
						
						indices.add(Integer.reverseBytes(Integer.parseInt(vertex1[0])-1));
						
						indices.add(Integer.reverseBytes(Integer.parseInt(vertex2[0])-1));
						
						indices.add(Integer.reverseBytes(Integer.parseInt(vertex3[0])-1));
					}
				}
			}
			ByteBuffer newModel = ByteBuffer.allocate(78+vertex.size()*(12+12+textcoords) +indices.size()*4);
			newModel.put(new byte[]{(byte)0x00,(byte)0x7C,(byte)0x0E,(byte)0x0E,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x48,(byte)0x0C,(byte)0x00,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
			if(txtcoord.size()!= 0){
				newModel.put((byte)0x01);
			}else{
				newModel.put((byte)0x00);
			}
			newModel.put(new byte[]{(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00});
			newModel.putInt(Integer.reverseBytes(vertex.size()));
			newModel.putInt(Integer.reverseBytes((indices.size()/3)));
			newModel.put(new byte[2]);
			newModel.putInt(Integer.reverseBytes(-1)); // hash
			newModel.put(new byte[16]);
			newModel.put(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x80,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
			for(int i = 0; i< vertex.size(); i++){
				newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(vertex.get(i).x)));
				newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(vertex.get(i).y)));
				newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(vertex.get(i).z)));
				
				newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(normal.get(i).x)));
				newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(normal.get(i).y)));
				newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(normal.get(i).z)));
				
				if(txtcoord.size()!= 0){
					newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(txtcoord.get(i).x)));
					newModel.putInt(Integer.reverseBytes(Float.floatToIntBits(txtcoord.get(i).y)));
				}
			}
			for(int i = 0; i<indices.size(); i++){
				newModel.putInt(indices.get(i));
			}
			newModel.flip();
			
			return new Type(1002,newModel.limit(),1698,newModel.array());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	private int getTreeIndex(){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		Pattern p = Pattern.compile("\\{(.*?)\\}");
		Matcher m = p.matcher(String.valueOf(node.toString()));
		if(m.find())
		{
			return Integer.parseInt(m.group(1));
		}
		return -1;
	}
	
	
	private void searchNode(String nodeStr)
    {
	  searchNode = new ArrayList<DefaultMutableTreeNode>();
      DefaultMutableTreeNode node = null;
      Enumeration<?> e = root.breadthFirstEnumeration();
      while (e.hasMoreElements())
      { 
        node = (DefaultMutableTreeNode) e.nextElement();            
        if ((node.getUserObject().toString()).contains(nodeStr)){
        	searchNode.add(node);
        }
      }
    }
	
	public int degreeToBrad(double angle){
		return (byte)(Math.toRadians(angle)*40.7436654315d) & 0xFF;
	}
	
	public double bradToRad(int brad){
		return brad/40.7436654315d;
	}
	
	public double bradToDegrees(int brad, boolean isBrad){
		if(!isBrad){
			return Math.toDegrees(brad/40.7436654315d);
		}else{
			return brad;
		}
	}
	
	
	public Matrix4f getMatrix2(int i,float weight, float x, float y, float z){
		NMatrix4f calculate = new NMatrix4f();
		NMatrix4f invmatrix = new NMatrix4f();
		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
		fb.put(toFloat(object.get(i).data, 0));
		fb.put(toFloat(object.get(i).data, 4));
		fb.put(toFloat(object.get(i).data, 8));
		fb.put(0);
		fb.put(toFloat(object.get(i).data, 12));
		fb.put(toFloat(object.get(i).data, 16));
		fb.put(toFloat(object.get(i).data, 20));
		fb.put(0);
		fb.put(toFloat(object.get(i).data, 24));
		fb.put(toFloat(object.get(i).data, 28));
		fb.put(toFloat(object.get(i).data, 32));
		fb.put(0);
		fb.put(toFloat(object.get(i).data, 36));
		fb.put(toFloat(object.get(i).data, 40));
		fb.put(toFloat(object.get(i).data, 44));
		fb.put(1);
		invmatrix.loadTranspose(fb);
		ByteBuffer jt = BufferUtils.createByteBuffer(64);
		jt.put(Arrays.copyOfRange(object.get(i+1).data, 20, 68));
		jt.putInt(0);
		jt.putInt(0);
		jt.putInt(0);
		jt.put((byte)0);
		jt.put((byte)0);
		jt.put((byte)128);
		jt.put((byte)63);
		Matrix4f bonematrix = new Matrix4f();
		FloatBuffer temp = BufferUtils.createFloatBuffer(16);
		GL11.glPushMatrix();
		{
			GL11.glMultMatrix(jt.asFloatBuffer());
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, temp);
		}
		bonematrix.load(temp);
		GL11.glPopMatrix();
		Matrix4f.mul(invmatrix, bonematrix, calculate);
		Vector4f test = new Vector4f(x,y,z,1);
		calculate.multiply(weight);
		//calculate.
		
		
		return null;	
	}
	
	public void savefile(){
		try{
			 FileChannel inChannel = new FileInputStream(file.getParent()+"\\"+file.getName()).getChannel();
			    FileChannel outChannel = new FileOutputStream(file.getParent()+"\\"+file.getName()+".backup").getChannel();
			    try {
			        inChannel.transferTo(0, inChannel.size(), outChannel);
			    } catch (IOException e) {
			        throw e;
			    } finally {
			        if (inChannel != null)
			            inChannel.close();
			        if (outChannel != null)
			            outChannel.close();
			    }
			    
			    
			FileChannel out = new FileOutputStream(file.getParent()+"\\"+file.getName()).getChannel();
			
			ByteArrayOutputStream test = new  ByteArrayOutputStream();
			
			if(texturedata != null){
				int integ = texturedata.size();
				ByteBuffer bb = ByteBuffer.allocate(4);
				bb.putInt(Integer.reverseBytes(integ));
				test.write(bb.array());
			}
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length + difference);
			for(int c = 0; c<object.size(); c++){
				buffer.putInt(Integer.reverseBytes(object.get(c).type));
				buffer.putInt(Integer.reverseBytes(object.get(c).size));
				buffer.putInt(Integer.reverseBytes(object.get(c).tof));
				if(c == 0){
					if(texturedata != null){
						for(int i = 0; i<texturedata.size(); i++){
							test.write(texturedata.get(i));
						}
						object.get(c).data = test.toByteArray();
					}
				}
					buffer.put(object.get(c).data);
			}
			buffer.flip();
			out.write(buffer);
			out.close();
			modified = false;
			frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]");
			JOptionPane.showMessageDialog(null,"File was saved successfully!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void AnimLoad(byte[] data1){
		int datacounter;
		
		datacounter = toInteger(data1,12);
		StringBuilder builder = new StringBuilder();
		for(int i = 24+(datacounter*8); i<data1.length; i++){
			builder.append((char)data1[i]);
		}
		animname.add(builder.toString());
	}
	
	public void materialhash(byte[] materialchunk, byte[] replace, int namecounter , boolean isLight) {
		//material hash									
		 ByteBuffer bbmesh = ByteBuffer.allocate(152); // 112 + 40 bytes
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,0)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,8)));
		 
		 bbmesh.put((byte)(toInteger(materialchunk,12)));
		 bbmesh.put((byte)(toInteger(materialchunk,16)));
		 bbmesh.put((byte)(toInteger(materialchunk,20)));
		 bbmesh.put((byte)(toInteger(materialchunk,24)));
		 
		 
		 bbmesh.put((byte)(toInteger(materialchunk,28)));
		 bbmesh.put((byte)(toInteger(materialchunk,32)));
		 bbmesh.put((byte)(toInteger(materialchunk,36)));
		 bbmesh.put((byte)(toInteger(materialchunk,40)));
		 
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,44)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,48)));
		 
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,76)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,80)));
		 
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,52)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,56)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,60)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,64)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,68)));
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,72)));
		 
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,88)));
		 
		 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,92)));
		 
		 
		 bbmesh.put(new byte[40]);
		
		 bbmesh.put(materialchunk[96]);
		 bbmesh.put(materialchunk[97]);
		 bbmesh.put(materialchunk[98]);
		 bbmesh.put(materialchunk[99]);
		 int extradata = 0;
		 int adress;
		 int namecounter_1,namecounter_2,namecounter_3,namecounter_4;
		 boolean fi = false,se = false,th = false,fo = false;
		 	if(materialchunk[(104+namecounter*4+36)]!= -1 && materialchunk[(104+namecounter*4+36)] != 0 && namecounter !=0 || materialchunk[104+namecounter*4+44] == 1){ //first texture
	 			if(materialchunk[104+namecounter*4+44] == 1){
	 				extradata = 8;
	 			}
	 			if(extradata !=8 || !isLight){
	 				adress = 104+namecounter*4+36;
	 				materialchunk[adress-4] =  replace[0];
					materialchunk[adress-3] =  replace[1];
					materialchunk[adress-2] =  replace[2];
					materialchunk[adress-1] =  replace[3];
	 			}
				adress = 104+namecounter*4+36+extradata;
				if(extradata == 8){
					bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,adress-4)));
					bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,adress)));
				}else{
					bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,adress)));
				}
		 		namecounter_1 = toInteger(materialchunk,adress+4);
		 		fi = true;
					 if(materialchunk[104+namecounter*4+44+extradata+namecounter_1*4+36] != -1 && materialchunk[104+namecounter*4+44+extradata+namecounter_1*4+36] != 0){ //second texture
						 adress = 104+namecounter*4+44+extradata+namecounter_1*4+36;
						 materialchunk[adress-4] =  replace[0];
						 materialchunk[adress-3] =  replace[1];
						 materialchunk[adress-2] =  replace[2];
						 materialchunk[adress-1] =  replace[3];
						 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,adress)));
					 	 namecounter_2 = toInteger(materialchunk,adress+4);
						 se =true;
						 if(materialchunk[104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+36] != -1 && materialchunk[104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+36] != 0){
							 adress = 104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+36;	
							 materialchunk[adress-4] =  replace[0];
							 materialchunk[adress-3] =  replace[1];
							 materialchunk[adress-2] =  replace[2];
							 materialchunk[adress-1] =  replace[3];
						 	 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,adress)));
						 	 namecounter_3 = toInteger(materialchunk,adress+4);
							 th = true;
							 if(materialchunk[104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+44+namecounter_3*4+36] != -1
							 && materialchunk[104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+44+namecounter_3*4+36] != 0){
								 adress = 104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+44+namecounter_3*4+36;
								 materialchunk[adress-4] =  replace[0];
								 materialchunk[adress-3] =  replace[1];
								 materialchunk[adress-2] =  replace[2];
								 materialchunk[adress-1] =  replace[3];
							 	 bbmesh.putInt(Integer.reverseBytes(toInteger(materialchunk,adress)));
							 	 namecounter_4 = toInteger(materialchunk,adress+4);
								 fo = true;
								 
								 bbmesh.put(materialchunk[adress-4]);
								 bbmesh.put(materialchunk[adress-3]);
								 bbmesh.put(materialchunk[adress-2]);
								 bbmesh.put(materialchunk[adress-1]);
								 
							 }else{
								 adress = 104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+36;
								 namecounter_3 = toInteger(materialchunk,adress+4);
								 adress = 104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+44+namecounter_3*4+36;
								 bbmesh.put(materialchunk[adress+3]);
								 bbmesh.put(materialchunk[adress+2]);
								 bbmesh.put(materialchunk[adress+1]);
								 bbmesh.put(materialchunk[adress]);
								 //hash
								 materialchunk[adress-4] =  replace[0];
								 materialchunk[adress-3] =  replace[1];
								 materialchunk[adress-2] =  replace[2];
								 materialchunk[adress-1] =  replace[3];
								 bbmesh.put(materialchunk[adress-4]);
								 bbmesh.put(materialchunk[adress-3]);
								 bbmesh.put(materialchunk[adress-2]);
								 bbmesh.put(materialchunk[adress-1]);   
							 }
						 }else{
							 adress = 104+namecounter*4+44+extradata+namecounter_1*4+36;
							 namecounter_2 = toInteger(materialchunk,adress+4);
							 adress = 104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+36;
							 bbmesh.put(materialchunk[adress+3]);
							 bbmesh.put(materialchunk[adress+2]);
							 bbmesh.put(materialchunk[adress+1]);
							 bbmesh.put(materialchunk[adress]);
							 
							 bbmesh.put(materialchunk[adress+8+3]);
							 bbmesh.put(materialchunk[adress+8+2]);
							 bbmesh.put(materialchunk[adress+8+1]);
							 bbmesh.put(materialchunk[adress+8]);
							 //hash
							 materialchunk[adress-4] =  replace[0];
							 materialchunk[adress-3] =  replace[1];
							 materialchunk[adress-2] =  replace[2];
							 materialchunk[adress-1] =  replace[3];
							 bbmesh.put(materialchunk[adress-4]);
							 bbmesh.put(materialchunk[adress-3]);
							 bbmesh.put(materialchunk[adress-2]);
							 bbmesh.put(materialchunk[adress-1]);   
						 }
					 }else{
					 	 adress = 104+namecounter*4+36+extradata; 
						 namecounter_1 = toInteger(materialchunk,adress+4);
						 adress = 104+namecounter*4+44+extradata+namecounter_1*4+36;
						 
						 bbmesh.put(materialchunk[adress+3]);
						 bbmesh.put(materialchunk[adress+2]);
						 bbmesh.put(materialchunk[adress+1]);
						 bbmesh.put(materialchunk[adress]);
						 
						 bbmesh.put(materialchunk[adress+8+3]);
						 bbmesh.put(materialchunk[adress+8+2]);
						 bbmesh.put(materialchunk[adress+8+1]);
						 bbmesh.put(materialchunk[adress+8]);
						 if(extradata != 8){
						 bbmesh.put(materialchunk[adress+16+3]);
						 bbmesh.put(materialchunk[adress+16+2]);
						 bbmesh.put(materialchunk[adress+16+1]);
						 bbmesh.put(materialchunk[adress+16]);
						 }
						 //hash
						 if(isLight){
							 materialchunk[adress-4] =  replace[0];
							 materialchunk[adress-3] =  replace[1];
							 materialchunk[adress-2] =  replace[2];
							 materialchunk[adress-1] =  replace[3];
						 }
						 if(extradata == 8){
							 int adr = 104+namecounter*4+36;
							 bbmesh.put(materialchunk[adr-4]);
							 bbmesh.put(materialchunk[adr-3]);
							 bbmesh.put(materialchunk[adr-2]);
							 bbmesh.put(materialchunk[adr-1]); 
						 }else{
						 bbmesh.put(materialchunk[adress-4]);
						 bbmesh.put(materialchunk[adress-3]);
						 bbmesh.put(materialchunk[adress-2]);
						 bbmesh.put(materialchunk[adress-1]);
						 }
					 }
				 }else{
					 if(namecounter == 0){
						 adress = 96;

						 bbmesh.put(materialchunk[adress+8+3]);
						 bbmesh.put(materialchunk[adress+8+2]);
						 bbmesh.put(materialchunk[adress+8+1]);
						 bbmesh.put(materialchunk[adress+8]);
						 
						 bbmesh.put(materialchunk[adress+16+3]);
						 bbmesh.put(materialchunk[adress+16+2]);
						 bbmesh.put(materialchunk[adress+16+1]);
						 bbmesh.put(materialchunk[adress+16]);
						 
						 bbmesh.put(materialchunk[adress+24+3]);
						 bbmesh.put(materialchunk[adress+24+2]);
						 bbmesh.put(materialchunk[adress+24+1]);
						 bbmesh.put(materialchunk[adress+24]);
						 
						 bbmesh.put(materialchunk[adress+32+3]);
						 bbmesh.put(materialchunk[adress+32+2]);
						 bbmesh.put(materialchunk[adress+32+1]);
						 bbmesh.put(materialchunk[adress+32]);
					 }else{
					 adress = 104+namecounter*4+36;
					 bbmesh.put(materialchunk[adress+3]);
					 bbmesh.put(materialchunk[adress+2]);
					 bbmesh.put(materialchunk[adress+1]);
					 bbmesh.put(materialchunk[adress]);

					 bbmesh.put(materialchunk[adress+8+3]);
					 bbmesh.put(materialchunk[adress+8+2]);
					 bbmesh.put(materialchunk[adress+8+1]);
					 bbmesh.put(materialchunk[adress+8]);
					 
					 bbmesh.put(materialchunk[adress+16+3]);
					 bbmesh.put(materialchunk[adress+16+2]);
					 bbmesh.put(materialchunk[adress+16+1]);
					 bbmesh.put(materialchunk[adress+16]);
					 
					 bbmesh.put(materialchunk[adress+24+3]);
					 bbmesh.put(materialchunk[adress+24+2]);
					 bbmesh.put(materialchunk[adress+24+1]);
					 bbmesh.put(materialchunk[adress+24]);
					 //hash
					 materialchunk[adress-4] =  replace[0];
					 materialchunk[adress-3] =  replace[1];
					 materialchunk[adress-2] =  replace[2];
					 materialchunk[adress-1] =  replace[3];
					 bbmesh.put(materialchunk[adress-4]);
					 bbmesh.put(materialchunk[adress-3]);
					 bbmesh.put(materialchunk[adress-2]);
					 bbmesh.put(materialchunk[adress-1]);   
					 }
				 }
		 		 if(fi == true){
		 			 if(extradata == 8){
			 			 bbmesh.put(new byte[4]);
		 			 }
				 	 adress = 104+namecounter*4+36+extradata;
					 namecounter_1 = toInteger(materialchunk,adress+4);
					 bbmesh.put(materialchunk[adress+8+namecounter_1*4+32]);
					 bbmesh.put(materialchunk[adress+8+namecounter_1*4+33]);
					 bbmesh.put(materialchunk[adress+8+namecounter_1*4+34]);
					 bbmesh.put(materialchunk[adress+8+namecounter_1*4+35]);
					 if (se == true){
						 adress = 104+namecounter*4+44+extradata+namecounter_1*4+36;
						 namecounter_2 = toInteger(materialchunk,adress+4);
						 bbmesh.put(materialchunk[adress+8+namecounter_2*4+32]);
						 bbmesh.put(materialchunk[adress+8+namecounter_2*4+33]);
						 bbmesh.put(materialchunk[adress+8+namecounter_2*4+34]);
						 bbmesh.put(materialchunk[adress+8+namecounter_2*4+35]);
						 if(th == true){
							 adress = 104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+36;
							 namecounter_3 = toInteger(materialchunk,adress+4);
							 bbmesh.put(materialchunk[adress+8+namecounter_3*4+32]);
							 bbmesh.put(materialchunk[adress+8+namecounter_3*4+33]);
							 bbmesh.put(materialchunk[adress+8+namecounter_3*4+34]);
							 bbmesh.put(materialchunk[adress+8+namecounter_3*4+35]);
							 if(fo == true){
								 adress = 104+namecounter*4+44+extradata+namecounter_1*4+44+namecounter_2*4+44+namecounter_3*4+36;
								 namecounter_4 = toInteger(materialchunk,adress+4);
								 bbmesh.put(materialchunk[adress+8+namecounter_4*4+32]);
								 bbmesh.put(materialchunk[adress+8+namecounter_4*4+33]);
								 bbmesh.put(materialchunk[adress+8+namecounter_4*4+34]);
								 bbmesh.put(materialchunk[adress+8+namecounter_4*4+35]);
								 
								 bbmesh.put(new byte[8]);
						 		 
							 }else{
								 bbmesh.put(new byte[12]);
							 }
						 }else{
							 bbmesh.put(new byte[16]);
						 }
					 }else{
						 if(extradata == 8){
							 bbmesh.put(new byte[12]); 
						 }else{
						 bbmesh.put(new byte[20]);
						 }
					 }
		 		 }else{
		 			bbmesh.put(new byte[24]);
		 			if(namecounter == 0){
		 				bbmesh.put(new byte[4]);
		 			}
		 		 }
		 
		 
		 
		 int new_hashsum = 0;
		 byte[] calculate = bbmesh.array();
		 
		 //Debug tool for array - compare to exe array.
		 if(debugMode.isSelected()){
			   StringBuilder sb = new StringBuilder(calculate.length * 2);
			   for(byte b: calculate){
			      sb.append(String.format("%02x", b & 0xff));
			   }
			   System.out.println(sb.toString());
		 }
		 
		 int[]hashvalues = {0,79764919,159529838,222504665,319059676,398814059,445009330,507990021,638119352,583659535,797628118,726387553,890018660,835552979,1015980042,944750013,1276238704,1221641927,1167319070,1095957929,1595256236,1540665371,1452775106,1381403509,1780037320,1859660671,1671105958,1733955601,2031960084,2111593891,1889500026,1952343757,-1742489888,-1662866601,-1851683442,-1788833735,-1960329156,-1880695413,-2103051438,-2040207643,-1104454824,-1159051537,-1213636554,-1284997759,-1389417084,-1444007885,-1532160278,-1603531939,-734892656,-789352409,-575645954,-646886583,-952755380,-1007220997,-827056094,-898286187,-231047128,-151282273,-71779514,-8804623,-515967244,-436212925,-390279782,-327299027,881225847,809987520,1023691545,969234094,662832811,591600412,771767749,717299826,311336399,374308984,453813921,533576470,25881363,88864420,134795389,214552010,2023205639,2086057648,1897238633,1976864222,1804852699,1867694188,1645340341,1724971778,1587496639,1516133128,1461550545,1406951526,1302016099,1230646740,1142491917,1087903418,-1398421865,-1469785312,-1524105735,-1578704818,-1079922613,-1151291908,-1239184603,-1293773166,-1968362705,-1905510760,-2094067647,-2014441994,-1716953613,-1654112188,-1876203875,-1796572374,-525066777,-462094256,-382327159,-302564546,-206542021,-143559028,-97365931,-17609246,-960696225,-1031934488,-817968335,-872425850,-709327229,-780559564,-600130067,-654598054,1762451694,1842216281,1619975040,1682949687,2047383090,2127137669,1938468188,2001449195,1325665622,1271206113,1183200824,1111960463,1543535498,1489069629,1434599652,1363369299,622672798,568075817,748617968,677256519,907627842,853037301,1067152940,995781531,51762726,131386257,177728840,240578815,269590778,349224269,429104020,491947555,-248556018,-168932423,-122852000,-60002089,-500490030,-420856475,-341238852,-278395381,-685261898,-739858943,-559578920,-630940305,-1004286614,-1058877219,-845023740,-916395085,-1119974018,-1174433591,-1262701040,-1333941337,-1371866206,-1426332139,-1481064244,-1552294533,-1690935098,-1611170447,-1833673816,-1770699233,-2009983462,-1930228819,-2119160460,-2056179517,1569362073,1498123566,1409854455,1355396672,1317987909,1246755826,1192025387,1137557660,2072149281,2135122070,1912620623,1992383480,1753615357,1816598090,1627664531,1707420964,295390185,358241886,404320391,483945776,43990325,106832002,186451547,266083308,932423249,861060070,1041341759,986742920,613929101,542559546,756411363,701822548,-978770311,-1050133554,-869589737,-924188512,-693284699,-764654318,-550540341,-605129092,-475935807,-413084042,-366743377,-287118056,-257573603,-194731862,-114850189,-35218492,-1984365303,-1921392450,-2143631769,-2063868976,-1698919467,-1635936670,-1824608069,-1744851700,-1347415887,-1418654458,-1506661409,-1561119128,-1129027987,-1200260134,-1254728445,-1309196108};
		 
		 for (int mesh = 0; mesh<calculate.length; mesh++){
			 new_hashsum = hashvalues[(int)((new_hashsum >> 24) ^ (calculate[mesh])) & 0xFF] ^ (new_hashsum << 8);
		 }
		 
		 int sum2 = (112+40) ^ new_hashsum;
		 
		 ByteBuffer replace2 = ByteBuffer.allocate(4);
		 replace2.putInt(Integer.reverseBytes(sum2));
		 
			hashname_old.add(
			""+String.format("%02x", materialchunk[84])
			+""+String.format("%02x", materialchunk[85])
			+""+String.format("%02x", materialchunk[86])
			+""+String.format("%02x", materialchunk[87]));
			
			
			materialchunk[84] =  replace2.get(0);
			materialchunk[85] =  replace2.get(1);
			materialchunk[86] =  replace2.get(2);
			materialchunk[87] =  replace2.get(3);
			
			
			hashname_new.add(
			""+String.format("%02x", materialchunk[84])
			+""+String.format("%02x", materialchunk[85])
			+""+String.format("%02x", materialchunk[86])
			+""+String.format("%02x", materialchunk[87]));
			
	}
	
	private float[] getMatrix(){
		float[] temp = null;
		if(currentMatrix.size() != 0 ){
			temp = currentMatrix.get(0);
			if(currentMatrix.size() > 1){
				for(int i = 1; i<currentMatrix.size(); i++){
					temp = matrix_mult_float(temp, currentMatrix.get(i));
				}
			}
		}
		//temp = currentMatrix.get(currentMatrix.size()-1);
		return temp;
	}
	

	private float[] matrix_mult_float(float[] first, float[] second){
		float[] test = new float[16];
		
		test[0] = 1f;
		test[5] = 1f;
		test[10] = 1f;
		test[15] = 1f;
		
        test[0] = first[0] * second[0] + first[4] * second[1] + first[8] * second[2] + first[12] * second[3];
        test[1] = first[1] * second[0] + first[5] * second[1] + first[9] * second[2] + first[13] * second[3];
        test[2] = first[2] * second[0] + first[6] * second[1] + first[10] * second[2] + first[14] * second[3];
        test[3] = first[3] * second[0] + first[7] * second[1] + first[11] * second[2] + first[15] * second[3];

        test[4] = first[0] * second[4] + first[4] * second[5] + first[8] * second[6] + first[12] * second[7];
        test[5] = first[1] * second[4] + first[5] * second[5] + first[9] * second[6] + first[13] * second[7];
        test[6] = first[2] * second[4] + first[6] * second[5] + first[10] * second[6] + first[14] * second[7];
        test[7] = first[3] * second[4] + first[7] * second[5] + first[11] * second[6] + first[15] * second[7];

        test[8] = first[0] * second[8] + first[4] * second[9] + first[8] * second[10] + first[12] * second[11];
        test[9] = first[1] * second[8] + first[5] * second[9] + first[9] * second[10] + first[13] * second[11];
        test[10] = first[2] * second[8] + first[6] * second[9] + first[10] * second[10] + first[14] * second[11];
        test[11] = first[3] * second[8] + first[7] * second[9] + first[11] * second[10] + first[15] * second[11];

        test[12] = first[0] * second[12] + first[4] * second[13] + first[8] * second[14] + first[12] * second[15];
        test[13] = first[1] * second[12] + first[5] * second[13] + first[9] * second[14] + first[13] * second[15];
        test[14] = first[2] * second[12] + first[6] * second[13] + first[10] * second[14] + first[14] * second[15];
        test[15] = first[3] * second[12] + first[7] * second[13] + first[11] * second[14] + first[15] * second[15];
		
		
		return test;
	}
	
	public void matrix_mult(ArrayList<JSpinner> translate, ArrayList<JSpinner> value){		
		Matrix4f trans = new Matrix4f();
		Matrix4f rotx = new Matrix4f();
		Matrix4f roty = new Matrix4f();
		Matrix4f rotz = new Matrix4f();
		Matrix4f scale = new Matrix4f();
		Matrix4f temp = new Matrix4f();
		trans.setIdentity();
		rotx.setIdentity();
		roty.setIdentity();
		rotz.setIdentity();
		scale.setIdentity();
		
		float rad_x = (float)Math.toRadians((float)translate.get(3).getValue());
		float rad_y = (float)Math.toRadians((float)translate.get(4).getValue());
		float rad_z = (float)Math.toRadians((float)translate.get(5).getValue());
		
		trans.m03 = (float)translate.get(0).getValue();
		trans.m13 = (float)translate.get(1).getValue();
		trans.m23 = (float)translate.get(2).getValue();
		
		rotx.m11 = (float) Math.cos(rad_x);
		rotx.m12 = (float) Math.sin(rad_x);	
		rotx.m21 = (float)-Math.sin(rad_x);
		rotx.m22 = (float) Math.cos(rad_x);
		
		roty.m00 = (float) Math.cos(rad_y);	
		roty.m02 = (float) -Math.sin(rad_y);
		roty.m20 = (float) Math.sin(rad_y);
		roty.m22 = (float) Math.cos(rad_y);
		
		rotz.m00 = (float) Math.cos(rad_z);
		rotz.m01 = (float) Math.sin(rad_z);
		rotz.m10 = (float) -Math.sin(rad_z);
		rotz.m11 = (float) Math.cos(rad_z);
		
		scale.m00 = (float)translate.get(6).getValue();
		scale.m11 = (float)translate.get(7).getValue();
		scale.m22 = (float)translate.get(8).getValue();
		
		
		Matrix4f.mul(scale, rotx, temp);
		Matrix4f.mul(temp, roty, temp);
		Matrix4f.mul(temp, rotz, temp);
		Matrix4f.mul(temp, trans, temp);
		
		
		//print
		value.get(0).setValue(temp.m00);
		value.get(1).setValue(temp.m01);
		value.get(2).setValue(temp.m02);
		
		value.get(4).setValue(temp.m10);
		value.get(5).setValue(temp.m11);
		value.get(6).setValue(temp.m12);
		
		value.get(8).setValue(temp.m20);
		value.get(9).setValue(temp.m21);
		value.get(10).setValue(temp.m22);
		
		value.get(12).setValue(temp.m03);
		value.get(13).setValue(temp.m13);
		value.get(14).setValue(temp.m23);
		
		translate.get(0).setValue(new Float((Float)value.get(12).getValue()));
		translate.get(1).setValue(new Float((Float)value.get(13).getValue()));
		translate.get(2).setValue(new Float((Float)value.get(14).getValue()));
		//rotate
		translate.get(3).setValue((float) (Math.atan(-(Float)value.get(9).getValue()/(Float)value.get(10).getValue()) * (180/Math.PI)));
		translate.get(4).setValue((float) (Math.asin((Float)value.get(8).getValue()) * (180/Math.PI)));
		translate.get(5).setValue((float) (Math.atan(-(Float)value.get(4).getValue()/(Float)value.get(0).getValue()) * (180/Math.PI)));
		//size	
		translate.get(6).setValue((float)Math.sqrt(Math.pow((Float)value.get(0).getValue(), 2)+ Math.pow((Float)value.get(1).getValue(), 2) + Math.pow((Float)value.get(2).getValue(), 2)));
		translate.get(7).setValue((float)Math.sqrt(Math.pow((Float)value.get(4).getValue(), 2)+ Math.pow((Float)value.get(5).getValue(), 2) + Math.pow((Float)value.get(6).getValue(), 2)));
		translate.get(8).setValue((float)Math.sqrt(Math.pow((Float)value.get(8).getValue(), 2)+ Math.pow((Float)value.get(9).getValue(), 2) + Math.pow((Float)value.get(10).getValue(), 2)));
		
	}
	
	
	
	
	public void MaterialLoad(byte[] data){
		int namecounter,name;
		String mathash = makeString(data,84,4);
		name = toInteger(data,96);
		try{
		if (name != -1){
			namecounter = toInteger(data,100);
			StringBuilder builder = new StringBuilder();
			for(int i = 104; i<104+namecounter*4;i=i+4){
				builder.append((char)data[i]);
			}
			materialname.add(builder.toString());
			materialid.add(mathash);
		}else{
			materialname.add("");
			materialid.add(mathash);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void TextureLoad(){
		if(object.get(0).data.length > 8){ // if bigger than null
			int counters,namecounter,width,height,offset_tex,help;
			texturec = toInteger(object.get(0).data,0);
			counters = 0;
			offset_tex = 0;
			texturedata = new ArrayList<byte[]>();
			texturename = new ArrayList<String>();
			while(counters < texturec){
				if (counters == 0){
					help = 0;
				}else{
					help = -4;
				}
				namecounter = toInteger(object.get(0).data,4+offset_tex+help);
				StringBuilder builder = new StringBuilder();
				for(int i = offset_tex+help+8;i<offset_tex+help+(namecounter*4+8);i+=4){
					builder.append((char)object.get(0).data[i]);
				}
				texturename.add(builder.toString());
				width = toInteger(object.get(0).data,offset_tex+help+(namecounter*4+12));
				height = toInteger(object.get(0).data,offset_tex+help+(namecounter*4+16));
				
				byte[] data_texture  = Arrays.copyOfRange(object.get(0).data, offset_tex+help+4, offset_tex+help+(48+namecounter*4)+width*height*16);
				texturedata.add(data_texture);
				offset_tex = offset_tex +help+(48+namecounter*4)+width*height*16;
				counters++;
			}
		}
	}

	public ImageIcon TexturePrint(int index){
		int namecounter,width,height;
		int type,b,g,r,a,col; //SetTexture
		 	namecounter = toInteger(texturedata.get(index),0);
		 	width = toInteger(texturedata.get(index),4+namecounter*4+4);
		 	height = toInteger(texturedata.get(index),4+namecounter*4+8);
			type = BufferedImage.TYPE_INT_ARGB;
			ArrayList<Integer> texturetest = new ArrayList<Integer>();
			BufferedImage image = new BufferedImage(width, height, type);
			for(int j = 4+namecounter*4+40; j<4+namecounter*4+40+width*height*16; j+=16){
				r = toInteger(texturedata.get(index),j);
				g = toInteger(texturedata.get(index),j+4);
				b = toInteger(texturedata.get(index),j+8);
				a = toInteger(texturedata.get(index),j+12);
				
				col = (a << 24) | (r << 16) | (g << 8) | b;	
				texturetest.add(col);
			}
				for(int y = 0; y < height; y++) {
					for(int x = 0; x < width; x++) {
					image.setRGB(x, y, texturetest.get(x + y * width));
					}
				}
				return new ImageIcon(image);
	}
	public ByteBuffer makeTexture(int index){
		int namecounter,width,height;
		int b,g,r,a;//col //SetTexture
		 	namecounter = toInteger(texturedata.get(index),0);
		 	width = toInteger(texturedata.get(index),4+namecounter*4+4);
		 	height = toInteger(texturedata.get(index),4+namecounter*4+8);
			ByteBuffer bb = BufferUtils.createByteBuffer(width*height*4);
		 	for(int j = 4+namecounter*4+40; j<4+namecounter*4+40+width*height*16; j+=16){
				r = toInteger(texturedata.get(index),j);
				g = toInteger(texturedata.get(index),j+4);
				b = toInteger(texturedata.get(index),j+8);
				a = toInteger(texturedata.get(index),j+12);
				
				//col = (a << 24) | (b << 16) | (g << 8) | r;	
				bb.put((byte) g);
				bb.put((byte) b);
				bb.put((byte) r);
				bb.put((byte) a);
			}
		 	bb.flip();
			return bb;
	}
	
	public void code_1009(){
		if(current == next || current > next && next != -1){
			ArrayList<Integer> tlevel2 = new ArrayList<Integer>(tlevel.subList(0, next));
				tlevel.clear();
				tlevel.addAll(tlevel2);
				tlevel2.clear();
		}
		framecounter++;	
	}

	private void calculateChildDiff(byte[] temp, byte[] child, int index) {
		if(temp.length > child.length || temp.length < child.length){
    		int lenghtdiff = temp.length-child.length;
    		difference+=lenghtdiff;
    		object.get(index).size+=lenghtdiff;
    		child = temp;
    	}
	}	
	
	private void calculateDiff(byte[] temp, int index) {
		if(temp.length > object.get(index).data.length || temp.length < object.get(index).data.length){
    		int lenghtdiff = temp.length-object.get(index).data.length;
    		difference+=lenghtdiff;
    		object.get(index).size+=lenghtdiff;
    	}
		object.get(index).data = temp;
		
	}
	
	private void changeReferences(int child, int i){
		int index = parent.get(child);
		if(object.get(child).type == 1002){
    		short scounter = toShort(object.get(index).data , 4);
    		scounter = (short) (scounter + i);
    		object.get(index).data[4] = (byte)scounter;
    		object.get(index).data[5] = (byte)(scounter >> 8);
		}else if((object.get(child).type == 20001 || object.get(child).type == 1010)){
			int scounter = toInteger(object.get(index).data, 0);
			scounter = scounter + i;
			object.get(index).data[0] = (byte)scounter;
			object.get(index).data[1] = (byte)(scounter >> 8);
			object.get(index).data[2] = (byte)(scounter >> 16);
			object.get(index).data[3] = (byte)(scounter >> 24); 
		}
		else if((object.get(child).type == 1027 )){// clip
			int scounter = toInteger(object.get(index).data, object.get(index).data.length-4);
			scounter = scounter + i;
			object.get(index).data[object.get(index).data.length-4] = (byte)scounter;
			object.get(index).data[object.get(index).data.length-3] = (byte)(scounter >> 8);
			object.get(index).data[object.get(index).data.length-2] = (byte)(scounter >> 16);
			object.get(index).data[object.get(index).data.length-1] = (byte)(scounter >> 24); 
		}
	}
	
	public void ModelLoad(int model , boolean export){
		try{
			String material_id;
			int weight,areCoords,vertex_count,index_count,vector,norm,texture,extra,extra2;
			
			vertex_count = toInteger(object.get(model).data,32);
			index_count = toInteger(object.get(model).data,36);
			areCoords = (object.get(model).data[24] & 0xFF);
			weight = (object.get(model).data[25] & 0xFF);
			material_id = makeString(object.get(model).data,42,4);
			vector = 12;
			norm = 12;
			texture = 8;
			extra = 0;
			extra2 = 0;
			if(areCoords == 0){
				texture = 0;
			}else if(areCoords == 1){
				
			}else if(areCoords == 2){
				extra = 8;
			}else if(areCoords == 3){
				extra = 16;
			}
			if(weight == 53){
				extra2 = 8;
			}else if(weight == 13){
				extra2 = 4;
			}
			//weight 5 nothing add
				
			FloatBuffer vectors = BufferUtils.createFloatBuffer(vertex_count*vector);
			FloatBuffer normals = BufferUtils.createFloatBuffer(vertex_count*vector);
			FloatBuffer textcoords = BufferUtils.createFloatBuffer(vertex_count*texture);
			FloatBuffer weights = BufferUtils.createFloatBuffer(vertex_count*extra2);
			IntBuffer indices = BufferUtils.createIntBuffer(index_count*12);
			
			float[] resultMatrix = getMatrix();
			
			for(int i = 78; i<78+vertex_count*(vector+norm+texture+extra+extra2); i+=(vector+norm+texture+extra+extra2)){
				if(resultMatrix != null){
					float[] point = new float[]{toFloat(object.get(model).data,i), toFloat(object.get(model).data,i+4), toFloat(object.get(model).data,i+8), 1};
					float[] result = new float[4];
					for(int k = 0; k<4; k++){
						for(int l = 0; l<4; l++){
							int index = k + l*4;
							result[k] = result[k] + resultMatrix[index] * point[l];
						}
					}
				/*	if(extra2 == 8){
						short b1 = toShort(object.get(model).data, i+28);
						short b2 = toShort(object.get(model).data, i+30);
						if(b1 != -1){
							float[] result2 = new float[4];
							for(int k = 0; k<4; k++){
								for(int l = 0; l<4; l++){
									int index = k + l*4;
									result2[k] = result2[k] + boneMatrix.get(b1)[index] * result[l];
								}
							}
							if(b2 != -1){
								float[] result3 = new float[4];
								for(int k = 0; k<4; k++){
									for(int l = 0; l<4; l++){
										int index = k + l*4;
										result3[k] = result3[k] + boneMatrix.get(b1)[index] * result[l];
									}
								}
								
								result2[0] = result2[0]+ result3[0];
								result2[1] = result2[1]+ result3[1];
								result2[2] = result2[2]+ result3[2];
								result2[3] = result2[3]+ result3[3];
								
								vectors.put(result2[0]);
								vectors.put(result2[1]);
								vectors.put(result2[2]);
							}else{
								vectors.put(result2[0]);
								vectors.put(result2[1]);
								vectors.put(result2[2]);
							}
						}else{
							vectors.put(result[0]);
							vectors.put(result[1]);
							vectors.put(result[2]);
						}

					}else{
						vectors.put(result[0]);
						vectors.put(result[1]);
						vectors.put(result[2]);
					}*/
					vectors.put(result[0]);
					vectors.put(result[1]);
					vectors.put(result[2]);
				}else{
					vectors.put(toFloat(object.get(model).data,i));
					vectors.put(toFloat(object.get(model).data,i+4));
					vectors.put(toFloat(object.get(model).data,i+8));
				}

				//normals
				normals.put(toFloat(object.get(model).data,i+12));
				normals.put(toFloat(object.get(model).data,i+16));
				normals.put(toFloat(object.get(model).data,i+20));
				//textcoord
				if (areCoords != 0){
					textcoords.put(toFloat(object.get(model).data,i+24+extra2));
					textcoords.put((toFloat(object.get(model).data,i+28+extra2)*-1));
				}
				if (extra2 == 4){
					//weights.put(toFloat(object.get(model).data,i+24));
				}else if (extra2 == 8){
					//weights.put(toFloat(object.get(model).data,i+24));
					//weights.put(toFloat(object.get(model).data,i+28));
				}
			}
			//indices
			for(int j = 78+vertex_count*(vector+norm+texture+extra+extra2); j<78+vertex_count*(vector+norm+texture+extra+extra2)+index_count*12;j=j+12){
				indices.put(toInteger(object.get(model).data,j));
				indices.put(toInteger(object.get(model).data,j+4));
				indices.put(toInteger(object.get(model).data,j+8));
			}
			
			int color = 0;
				if(!export){
					if(areCoords !=0){
						if (materialid.contains(material_id)) {
							  if(texturename.contains(materialname.get(materialid.indexOf(material_id)))){
								  textures.add(new Texture(TexturePrint(texturename.indexOf(materialname.get(materialid.indexOf(material_id))))));
							  }
						}
					}else{
						textures.add(null);
					}
					if(materialid.indexOf(material_id) != -1){	
					color = (toInteger(object.get(materialid.indexOf(material_id)+2).data,12) << 24) | (toInteger(object.get(materialid.indexOf(material_id)+2).data,16) << 16) 
							| ((toInteger(object.get(materialid.indexOf(material_id)+2).data,20) << 8) | toInteger(object.get(materialid.indexOf(material_id)+2).data,24));
					}else{
						color = 255 << 24 | 255 << 16 | 255 << 8 | 255;
					}
				}
					vectors.flip();
					normals.flip();
					textcoords.flip();
					weights.flip();
					indices.flip();
					
					if(!export){
						meshes.add(new Model(vectors,normals,textcoords,weights,indices,color));
					}else{
						ModelExport(vectors,normals,textcoords,indices,material_id , model);
					}
		}catch(Exception e){e.printStackTrace();}
}
	
	public void exportMaterials(String path){
		//export all textures
		try{
			FileChannel out;
			//Files.createDirectories(Paths.get(path+ "\\"+"Materials"+"\\"));
			//Files.createDirectories(Paths.get(path+ "\\"+"Materials"+"\\"+"\\"+"Textures"+"\\"));
			//Files.createDirectories(Paths.get(path+ "\\"+"Models"+"\\"));
			
			for(int i = 0; i<texturedata.size(); i++){		
				out = new FileOutputStream(path + "\\" + texturename.get(i).substring(0, texturename.get(i).length()-1) + ".tga").getChannel();
			    out.write(prepareToExport(i));
				out.close();
			}
		
		
		BufferedWriter obj;
		int counter = toInteger(object.get(1).data, 0);
			for(int i = 0; i<counter; i++){
				String mHash =  materialid.get(i);
				String mName = null;
				String textPath = null;
				int transparent = toInteger(object.get(2+i).data,0);
				if(!materialname.get(i).equals("")){
					String name = materialname.get(i).substring(0, materialname.get(i).length()-1);
					mName =  name + "_" +mHash;
					if(transparent == 0){
						textPath = ("map_Kd " + path + "\\" +name+ ".tga");
					}else{
						textPath = ("Tr 0.000000" + '\n' +"map_Kd " + path + "\\" +name+ ".tga" + '\n'+"map_d " + path + "\\" +name+ ".tga");
					}
				}else{
					mName = mHash;
					textPath = "";
				}
				
				float x = (toInteger(object.get(i+2).data, 12)/255);
				float y = (toInteger(object.get(i+2).data, 16)/255);
				float z = (toInteger(object.get(i+2).data, 20)/255);
				
				
				
				obj = new BufferedWriter(new FileWriter(path + "\\" + mName + ".mtl"));
				obj.write("newmtl " + mName  + '\n' 
				+ "Ka "+
				x + " " + 
				y + " " + 
				z+ '\n' 
				+ "Kd "+
				x + " " + 
				y + " " + 
				z + '\n' 
				+ "Ks "+
				(float)(toInteger(object.get(i+2).data, 28)/255)+
				(float)(toInteger(object.get(i+2).data, 32)/255)+
				(float)(toInteger(object.get(i+2).data, 36)/255) + '\n' 
				+ "d 1.0000" + '\n'
				+"Ns 10.0000"+ '\n' 
				+ "illum 2"+ '\n' 
				+textPath);
				obj.close();
			}
		}catch(Exception e){e.printStackTrace();}
	  
	}
	
	
	public void ModelExport(FloatBuffer vectors, FloatBuffer normals, FloatBuffer textcoords, IntBuffer indices, String material_id, int chunk){
		StringBuilder sbVertex = new StringBuilder();
		StringBuilder sbNormals = new StringBuilder();
		StringBuilder sbCoords = new StringBuilder();
		StringBuilder sbIndices = new StringBuilder();
		for(int i = 0; i<vectors.limit(); i+=3){
			sbVertex.append("v  "+vectors.get(i));
			sbVertex.append(" "+vectors.get(i+1));
			sbVertex.append(" "+vectors.get(i+2) + '\n');
			
			sbNormals.append("vn  "+normals.get(i));
			sbNormals.append(" "+normals.get(i+1));
			sbNormals.append(" "+normals.get(i+2) + '\n');
		}
		for(int i = 0; i<textcoords.limit(); i+=2){
			sbCoords.append("vt  "+textcoords.get(i));
			sbCoords.append(" "+textcoords.get(i+1) + '\n');
		}
		if(textcoords.limit() != 0){
			for(int i = 0; i<indices.limit(); i+=3){
				sbIndices.append("f "+(indices.get(i)+1)+"/"+(indices.get(i)+1)+"/"+(indices.get(i)+1));
				sbIndices.append(" "+(indices.get(i+1)+1)+"/"+(indices.get(i+1)+1)+"/"+(indices.get(i+1)+1));
				sbIndices.append(" "+(indices.get(i+2)+1)+"/"+(indices.get(i+2)+1)+"/"+(indices.get(i+2)+1) + '\n');
			}
		}else{
			for(int i = 0; i<indices.limit(); i+=3){
				sbIndices.append("f "+(indices.get(i)+1)+"//"+(indices.get(i)+1));
				sbIndices.append(" "+(indices.get(i+1)+1)+"//"+(indices.get(i+1)+1));
				sbIndices.append(" "+(indices.get(i+2)+1)+"//"+(indices.get(i+2)+1)+ '\n');
			}
		}
		int index = materialid.indexOf(material_id);
		String mHash =  materialid.get(index);
		String mName =  materialname.get(index);
		
		String path = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExportModel");
		
		if(!mName.equals("")){
			mName = materialname.get(index).substring(0, materialname.get(index).length()-1) + "_";
		}
		
		//export
		try {
				BufferedWriter obj = new BufferedWriter(new FileWriter(path + "\\" + "SHAPE" + chunk + ".obj"));
			    obj.write("mtllib " +mName + mHash+ ".mtl"+ '\n'
				+sbVertex.toString()+ '\n'
				+sbCoords.toString()+ '\n'
			    +sbNormals.toString()+ '\n'
			    +"g Model "+ "SHAPE" + chunk + '\n'
			    +"usemtl "+mName + mHash+ '\n' 
			    +sbIndices.toString());
			    obj.close();
		}catch (IOException e){e.printStackTrace();}
	}
	
	
	private void renderBone(float x, float y, float z) {
	     GL11.glPushMatrix();
	     GL11.glTranslatef(x, y, z);
	     Sphere s = new Sphere();
	     s.draw(0.01f, 16, 16);
	     GL11.glPopMatrix();
	}
	
	private void code_1009_Matrix(){
		if(currentLevel == nextLevel || currentLevel > nextLevel && nextLevel != -1){
				ArrayList<float[]> currentMatrix2 = new ArrayList<float[]>(currentMatrix.subList(0, nextLevel));
				currentMatrix.clear();
				currentMatrix.addAll(currentMatrix2);
				currentMatrix2.clear();
		}
	}
	
	private void exportAll(){
		try{
			JFileChooser exportModelDir =  new JFileChooser(Advapi32Util.registryGetStringValue(
			WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExportModel"));
			exportModelDir.setDialogTitle("Select Folder");
			exportModelDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			exportModelDir.setAcceptAllFileFilterUsed(false);
		    if (exportModelDir.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		    	String path = exportModelDir.getSelectedFile().toString();
	    		Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExportModel", path);
	    		exportMaterials(path);
	    		int index = 0,index2 = 0;
        		Pattern p = Pattern.compile("\\{(.*?)\\}");
        		Matcher m = p.matcher(tree.getModel().getChild(tree.getModel().getRoot(), 2).toString());
        		if(m.find()){
        			index =  Integer.parseInt(m.group(1));
        		}
        		m = p.matcher(tree.getModel().getChild(tree.getModel().getRoot(), tree.getModel().getChildCount(tree.getModel().getRoot())-1).toString());
        		if(m.find()){
        			index2 =  Integer.parseInt(m.group(1));
        		}
				currentMatrix = new ArrayList<float[]>();
				System.out.println(index2);
				treeWalk(index);// skip materials and textures. // world
				treeWalk(index2);// skip materials and textures. //entities
				JOptionPane.showMessageDialog(null,"File was exported successfully!");
		    }
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void treeWalk(int chunk){
		if(object.get(chunk).type == 20001){
		currentMatrix.clear();
			//entity
		}else if(object.get(chunk).type == 1001){ //bone 
			currentLevel = -1;
			nextLevel = -1;
			// FloatBuffer fb = BufferUtils.createFloatBuffer(16);
			 int extra = 0;
			 currentMatrix.add(new float[]{
					 toFloat(object.get(chunk).data, 0+extra),
					 toFloat(object.get(chunk).data, 4+extra),
					 toFloat(object.get(chunk).data, 8+extra),
					 0f,
					 toFloat(object.get(chunk).data, 12+extra),
					 toFloat(object.get(chunk).data, 16+extra),
					 toFloat(object.get(chunk).data, 20+extra),
					 0f,
					 toFloat(object.get(chunk).data, 24+extra),
					 toFloat(object.get(chunk).data, 28+extra),
					 toFloat(object.get(chunk).data, 32+extra),
					 0f,
					 toFloat(object.get(chunk).data, 36+extra),
					 toFloat(object.get(chunk).data, 40+extra),
					 toFloat(object.get(chunk).data, 44+extra),
					 1f
			 });
		}else if(object.get(chunk).type == 1002){
				ModelLoad(chunk , true);
		}else if(object.get(chunk).type == 1009){
			//currentJoint = null;
			int tlevel = toInteger(object.get(chunk).data , 0);
			if(currentLevel == -1){
				currentLevel = tlevel;
			}else{
				nextLevel = tlevel;
			}
			if(chunk+1 == object.size()) {
				code_1009_Matrix();
			}else if(object.get(chunk+1).type != 1009){
				code_1009_Matrix();
			}
		}
		
		//get new child
		if(object.get(chunk).childs.size()!= 0){
			for(int i = 0; i<object.get(chunk).childs.size(); i++){
				treeWalk(object.get(chunk).childs.get(i));
			}
		}
	}
	
	private void printModel(int chunk){
		//calculate values
		if(object.get(chunk).type == 20001){ //entity
			 /*FloatBuffer fb = BufferUtils.createFloatBuffer(16);
			 fb.put(toFloat(object.get(chunk).data, 4));
			 fb.put(toFloat(object.get(chunk).data, 8));
			 fb.put(toFloat(object.get(chunk).data, 12));
			 fb.put(0);
			 fb.put(toFloat(object.get(chunk).data, 16));
			 fb.put(toFloat(object.get(chunk).data, 20));
			 fb.put(toFloat(object.get(chunk).data, 24));
			 fb.put(0);
			 fb.put(toFloat(object.get(chunk).data, 28));
			 fb.put(toFloat(object.get(chunk).data, 32));
			 fb.put(toFloat(object.get(chunk).data, 36));
			 fb.put(0);
			 fb.put(toFloat(object.get(chunk).data, 40));
			 fb.put(toFloat(object.get(chunk).data, 44));
			 fb.put(toFloat(object.get(chunk).data, 48));
			 fb.put(1);
			 fb.flip();
			 GL13.glMultTransposeMatrix(fb);*/
		}else if(object.get(chunk).type == 1005){ //skin
			
			int skin_count = toInteger(object.get(chunk).data, 20);
				if(skin_count!= 0){
					for(int i_1005 = 24;i_1005<24+skin_count*4;i_1005+=4){
						skinHash.add(toInteger(object.get(chunk).data,i_1005));
					}
					for(int mat_index = 0; mat_index<skin_count; mat_index++){
						int off = 24+skin_count*4 + 56*mat_index + 12; //12 normalzie
						skinMatrix.add(new float[]{
							toFloat(object.get(chunk).data, off),
							toFloat(object.get(chunk).data, off+4),
							toFloat(object.get(chunk).data, off+8),
							0f,
							toFloat(object.get(chunk).data, off+12),
							toFloat(object.get(chunk).data, off+16),
							toFloat(object.get(chunk).data, off+20),
							0f,
							toFloat(object.get(chunk).data, off+24),
							toFloat(object.get(chunk).data, off+28),
							toFloat(object.get(chunk).data, off+32),
							0f,
							0f,
							0f,
							0f,
							1f
						});
					}
				}
		}else if(object.get(chunk).type == 1001){ //bone 
			currentLevel = -1;
			nextLevel = -1;
			// FloatBuffer fb = BufferUtils.createFloatBuffer(16);
			 int extra = 0;
			 currentMatrix.add(new float[]{
					 toFloat(object.get(chunk).data, 0+extra),
					 toFloat(object.get(chunk).data, 4+extra),
					 toFloat(object.get(chunk).data, 8+extra),
					 0f,
					 toFloat(object.get(chunk).data, 12+extra),
					 toFloat(object.get(chunk).data, 16+extra),
					 toFloat(object.get(chunk).data, 20+extra),
					 0f,
					 toFloat(object.get(chunk).data, 24+extra),
					 toFloat(object.get(chunk).data, 28+extra),
					 toFloat(object.get(chunk).data, 32+extra),
					 0f,
					 toFloat(object.get(chunk).data, 36+extra),
					 toFloat(object.get(chunk).data, 40+extra),
					 toFloat(object.get(chunk).data, 44+extra),
					 1f
			 });
				if(BonesOn.isSelected()){
				int temp =  parent.get(chunk);
					if(toInteger(object.get(chunk).data, 112) != -1){ // if is bone
						GL11.glPushMatrix();
						{
							float childx = toFloat(object.get(chunk).data,92);
							float childy = toFloat(object.get(chunk).data,96);
							float childz = toFloat(object.get(chunk).data,100);
							//if(selectBone){
								//if(chunk == indexBoneS)
									renderBone(childx,childy,childz);
							//}
							label1:
							while(object.get(temp).type != 1005){
								if(toInteger(object.get(temp).data, 112) != -1){ //if found second bone
									float parentx = toFloat(object.get(temp).data,92);
									float parenty = toFloat(object.get(temp).data,96);
									float parentz = toFloat(object.get(temp).data,100);
									GL11.glBegin(GL11.GL_LINES);
									{
										GL11.glVertex3f(parentx, parenty, parentz);
										GL11.glVertex3f(childx, childy, childz);
									}
									GL11.glEnd();
									break label1;
								}
								temp = parent.get(temp);
							}
						}
						GL11.glPopMatrix();
						}
				}
			// fb.put(1);
			 //fb.flip();
			 //GL13.glMultTransposeMatrix(fb);
		}else if(object.get(chunk).type == 1026){
			float[] temp = new float[]{
					toFloat(object.get(chunk).data, 20),
					toFloat(object.get(chunk).data, 36),
					toFloat(object.get(chunk).data, 52),
					0f,
					toFloat(object.get(chunk).data, 24),
					toFloat(object.get(chunk).data, 40),
					toFloat(object.get(chunk).data, 56),
					0f,
					toFloat(object.get(chunk).data, 28),
					toFloat(object.get(chunk).data, 44),
					toFloat(object.get(chunk).data, 60),
					0f,
					toFloat(object.get(chunk).data, 32),
					toFloat(object.get(chunk).data, 48),
					toFloat(object.get(chunk).data, 64),
					1f
			};
			//currentJoint = temp;
			int hash = toInteger(object.get(chunk).data, 68);
			if(hash != 0){
				//int index = skinHash.indexOf(hash);
				//if(index != -1){
					//currentJoint = matrix_mult_float(temp, skinMatrix.get(index));
				//}
			}
			//currentJoint.add(bb.asFloatBuffer().array());
		}else if(object.get(chunk).type == 1000){
			
		}else if(object.get(chunk).type == 1002){
			if(showModel.isSelected()){
				ModelLoad(chunk , false);
				int color = meshes.get(meshcounter).color;
				GL11.glPushMatrix();{
				if(!BonesOn.isSelected()){
					GL11.glColor4ub((byte) ((color>> 24) & 0xFF), (byte) ((color>> 16) & 0xFF), (byte) ((color>> 8) & 0xFF), (byte) (color & 0xFF));
				}else{
					GL11.glColor4ub((byte) ((color>> 24) & 0xFF), (byte) ((color>> 16) & 0xFF), (byte) ((color>> 8) & 0xFF), (byte) 127);
				}
				
				if(meshes.get(meshcounter).length != 0){
					if(texturesOn.isSelected()){
						GL11.glEnable(GL11.GL_TEXTURE_2D);	
						textures.get(meshcounter).bind();
					}
					meshes.get(meshcounter).render();
				}else{
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					meshes.get(meshcounter).render3();
				}
				meshes.get(meshcounter).cleanBuffers();
				}GL11.glPopMatrix();
				meshcounter++;
			}
		}else if(object.get(chunk).type == 1009){
			//currentJoint = null;
			int tlevel = toInteger(object.get(chunk).data , 0);
			if(currentLevel == -1){
				currentLevel = tlevel;
			}else{
				nextLevel = tlevel;
			}
			if(chunk+1 == object.size()) {
				code_1009_Matrix();
			}else if(object.get(chunk+1).type != 1009){
				code_1009_Matrix();
			}
		}
		
		//get new child
		if(object.get(chunk).childs.size()!= 0){
			for(int i = 0; i<object.get(chunk).childs.size(); i++){
				printModel(object.get(chunk).childs.get(i));
			}
		}
		//

	}
	
	
	public void draw(int index) throws IOException{
		try{
			Display.setDisplayMode(new DisplayMode(640,480));
			Display.setTitle(file.getName());
			Display.create();
		}catch(Exception e){
			e.printStackTrace();
		}
		Camera camera = new Camera(70,(float)Display.getWidth()/(float)Display.getHeight(),0.3f,1000);
		
		meshes = new ArrayList<Model>();
		textures = new ArrayList<Texture>();
		
		
		while (!Display.isCloseRequested()){
			
			if(Mouse.hasWheel()){
				int wheel = Mouse.getDWheel()/120;
				camera.move(0.2f * wheel,1);
			}
			
			if(Keyboard.isKeyDown(Keyboard.KEY_W)){
				camera.PosY(-0.05f);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_S)){
				camera.PosY(0.05f);
			}
			
			if(Keyboard.isKeyDown(Keyboard.KEY_A)){
				camera.move(0.1f,0);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_D)){
				camera.move(-0.1f,0);
			}
			
			if(Mouse.isButtonDown(1)){
				float mouseDX = Mouse.getDX()*0.5f;
				camera.RotateY(mouseDX);
			}
			
			if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
				camera.RotateY(-0.2f);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
				camera.RotateY(0.2f);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
				camera.RotateX(-0.05f);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
				camera.RotateX(0.05f);
			}
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glLoadIdentity();
			if(wireframe.isSelected()){
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			}
			
		    GL11.glEnable(GL11.GL_BLEND);
		    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_LIGHT0);
			GL11.glEnable(GL11.GL_CULL_FACE);
			
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			camera.view();
			
			meshcounter = 0;
			currentLevel = -1;
			nextLevel = -1;
			skinHash = new ArrayList<Integer>();
			skinMatrix = new ArrayList<float[]>();
			transMatrix = new ArrayList<float[]>();
			currentMatrix = new ArrayList<float[]>();
			//boneMatrix = new ArrayList<Vector3f>();
			meshes.clear();
			for(int i = 0; i<textures.size(); i++){
				if(textures.get(i) != null){
					textures.get(i).cleanTexture();
				}
			}
			textures.clear();
			int chunk = getTreeIndex();
			printModel(chunk);
			
			/*for(int i = 0; i<meshes.size(); i++){
				int color = meshes.get(i).color;
				GL11.glPushMatrix();{
				if(!BonesOn.isSelected()){
					GL11.glColor4ub((byte) ((color>> 24) & 0xFF), (byte) ((color>> 16) & 0xFF), (byte) ((color>> 8) & 0xFF), (byte) (color & 0xFF));
				}else{
					GL11.glColor4ub((byte) ((color>> 24) & 0xFF), (byte) ((color>> 16) & 0xFF), (byte) ((color>> 8) & 0xFF), (byte) 127);
				}
				
				if(meshes.get(i).length != 0){
					if(texturesOn.isSelected()){
						GL11.glEnable(GL11.GL_TEXTURE_2D);	
						textures.get(i).bind();
					}
					meshes.get(i).render();
				}else{
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					meshes.get(i).render3();
				}
				}GL11.glPopMatrix();
			}*/
			Display.update();
			Display.sync(60);
		}
		Display.destroy();
	}
	
	public void loadTree(){
		//clean
		for(Type type : object){
			type.childs.clear();
		}
		previous = new String("");
		//
    	if(!modified){
    		frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]");
    	}else{
    		frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");	
    	}
		
		tree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode(file.getName()) {
				{
				}
			}
		));
	
		
	//Make Tree
		model = (DefaultTreeModel) tree.getModel();
		root = (DefaultMutableTreeNode) model.getRoot();
		
		materialname = new ArrayList<String>();
		animname = new ArrayList<String>();
		materialid = new ArrayList<String>();
		parent = new ArrayList<Integer>();
		ArrayList<DefaultMutableTreeNode> chunk = new ArrayList<DefaultMutableTreeNode>();
		bonehash = new ArrayList<String>();
		bonename = new ArrayList<String>();
		tlevel = new ArrayList<Integer>();
		bones = new ArrayList<Bone>();
		
		boolean first = true;
		boolean firstBone = true;
		int checktype;
		int entitycount = 0;
		int shapecount = 0;
		int animcounter = 0;
		int materialcounter = 0;
		int sectorOcreeIndex = 0;
		int skinTotalBones =0;
		int entityIndex = 0;
		int animIndex = 0;
		int modelgroupIndex = 0;
		int value_1009 = -1;
		for(int c = 0; c<object.size(); c++){
		checktype = object.get(c).type;		
			if(checktype ==  20002){
				parent.add(-1);
				chunk.add(new DefaultMutableTreeNode("Textures" + " {"+c+"}"));
				if(texturedata != null){
					for (int k = 0;k<texturedata.size();k++){
						int textcounter = toInteger(texturedata.get(k),0);
						int width = toInteger(texturedata.get(k),4+textcounter*4+4);
						int height = toInteger(texturedata.get(k),4+textcounter*4+8);
						chunk.get(c).add(new DefaultMutableTreeNode("[" + k + "] " +"'"+texturename.get(k)+ "' " + "["+
								width+"x"+height+" 32 bit]"));
					}
				}
			}
			else if(checktype == 1010){
				parent.add(-1);
				chunk.add(new DefaultMutableTreeNode("Materials"+ " {"+c+"}"));
				materialname = new ArrayList<String>();
				materialid = new ArrayList<String>();
				first = true;
			
			}else if(checktype == 5){		
				MaterialLoad(object.get(c).data);
				if(first){
					object.get(chunk.size()-1).childs.add(c);
					parent.add(chunk.size()-1);
					first = false;
				}
				else{
					object.get(parent.get(parent.size()-1)).childs.add(c);
					parent.add(parent.get(parent.size()-1));
				}
			 chunk.add(new DefaultMutableTreeNode("[" +
			 materialcounter+ "] " +"'" + materialname.get(materialcounter) + "'"+ " {"+c+"}"));
			 materialcounter++;
			}
			else if(checktype ==  1012){
				parent.add(-1);
				chunk.add(new DefaultMutableTreeNode("World"+ " {"+c+"}"));
			}
			else if(checktype ==  1000){
				if(object.get(c-1).type == 1012){
					object.get(chunk.size()-1).childs.add(c);
					parent.add(chunk.size()-1);
				}else{
					object.get(parent.get(parent.size()-1)).childs.add(c);
					parent.add(parent.get(parent.size()-1));
				}
		    chunk.add(new DefaultMutableTreeNode("Model Group"+ " {"+c+"}"));
			modelgroupIndex = c;
			}
			else if(checktype == 1002){
				object.get(modelgroupIndex).childs.add(c);
				parent.add(modelgroupIndex);
				chunk.add(new DefaultMutableTreeNode("SHAPE ["+shapecount+"]"+ " {"+c+"}"));
				shapecount++;
			}
			else if(checktype == 1011){
				object.get(parent.get(parent.size()-1)).childs.add(c);
				parent.add(parent.get(parent.size()-1));
				chunk.add(new DefaultMutableTreeNode("SECTOROCTREE"+ " {"+c+"}"));
				sectorOcreeIndex = c;
			}
			else if(checktype == 20000){
				parent.add(-1);
				chunk.add(new DefaultMutableTreeNode("Entities"+ " {"+c+"}"));
				entityIndex = c;
			}
			else if(checktype == 20001){
				object.get(entityIndex).childs.add(c);
				parent.add(entityIndex);
				tlevel = new ArrayList<Integer>();
				framecounter = 0;
				int namecounter = toInteger(object.get(c).data , 64);
				StringBuilder builder = new StringBuilder();
				if(namecounter != 0){
					for(int i = 68; i<68 + namecounter; i++){
						builder.append((char)object.get(c).data[i]);
    				}
					chunk.add(new DefaultMutableTreeNode("Entity '"+ builder.toString() + "' ["+entitycount+"]"+ " {"+c+"}"));
				}else{
					chunk.add(new DefaultMutableTreeNode("Entity ["+entitycount+"]"+ " {"+c+"}"));
				}
				entitycount++;
			}
			else if(checktype == 1005){
				object.get(chunk.size()-1).childs.add(c);
				parent.add(chunk.size()-1);
				chunk.add(new DefaultMutableTreeNode("Skin"+ " {"+c+"}"));
				int testB = toInteger(object.get(c).data, 20);
				if(testB > skinTotalBones){
					skinTotalBones = testB;
					boneMatrix = new ArrayList<float[]>(skinTotalBones);
					for(int i =0; i< skinTotalBones; i++){
						boneMatrix.add(new float[0]);
					}
				}
				first = true;
			}
			else if(checktype == 1001){
				current = -1;
				next = -1;
				if (first){
					object.get(chunk.size()-1).childs.add(c);
					parent.add(chunk.size()-1);
				}else{
					object.get(tlevel.get(tlevel.size()-1)).childs.add(c);
					parent.add(tlevel.get(tlevel.size()-1));
				}
				bonehash.add(
				""+String.format("%02x", object.get(c).data[120])
				+""+String.format("%02x", object.get(c).data[121])
				+""+String.format("%02x", object.get(c).data[122])
				+""+String.format("%02x", object.get(c).data[123]));
				bonename.add(GetName(object.get(c).data,checktype));
				//test bone
				
				int bIndex = toInteger(object.get(c).data, 112);
				int extra = 56;
				if(bIndex != -1){
					boneMatrix.set(bIndex, (new float[]{
							 toFloat(object.get(c).data, 0+extra),
							 toFloat(object.get(c).data, 4+extra),
							 toFloat(object.get(c).data, 8+extra),
							 0f,
							 toFloat(object.get(c).data, 12+extra),
							 toFloat(object.get(c).data, 16+extra),
							 toFloat(object.get(c).data, 20+extra),
							 0f,
							 toFloat(object.get(c).data, 24+extra),
							 toFloat(object.get(c).data, 28+extra),
							 toFloat(object.get(c).data, 32+extra),
							 0f,
							 toFloat(object.get(c).data, 36+extra),
							 toFloat(object.get(c).data, 40+extra),
							 toFloat(object.get(c).data, 44+extra),
							 1f
					 }));
				}
				
				
				chunk.add(new DefaultMutableTreeNode("Bone [" + framecounter +"] " + "'" + bonename.get(bonename.size()-1) + "'"+ " {"+c+"}"));
				first = false;
				tlevel.add(c);
			}
			else if(checktype == 1004){
				object.get(tlevel.get(tlevel.size()-1)).childs.add(c);
				parent.add(tlevel.get(tlevel.size()-1));
				chunk.add(new DefaultMutableTreeNode("Atomic Mesh"+ " {"+c+"}"));
			}
			else if(checktype == 1017){
				parent.add(-1);
				chunk.add(new DefaultMutableTreeNode("Anim library"+ " {"+c+"}"));
				animIndex = c;
			}
			else if(checktype == 1027){
				AnimLoad(object.get(c).data);
				object.get(animIndex).childs.add(c);
				parent.add(animIndex);
				chunk.add(new DefaultMutableTreeNode("CLIP: " + animname.get(animcounter)+ " {"+c+"}"));
				animcounter++;
				firstBone = true;
			}else if(checktype == 1015){
			if(firstBone){
				object.get(chunk.size()-1).childs.add(c);
				parent.add(chunk.size()-1);
				firstBone = false;
			}else{
				object.get(parent.get(parent.size()-1)).childs.add(c);
				parent.add(parent.get(parent.size()-1));	
			}
			//String replace = makeString(object.get(c).data,4,4);
			//int boneIndex = bonehash.indexOf(replace);
			//if(boneIndex == -1){
				chunk.add(new DefaultMutableTreeNode("BONE: " + makeString(object.get(c).data,4,4)+ " {"+c+"}"));
			//}else{
				//chunk.add(new DefaultMutableTreeNode("BONE: " + bonename.get(boneIndex)+ " {"+c+"}"));
			//}
		}else if(checktype == 1026){
			object.get(tlevel.get(tlevel.size()-1)).childs.add(c);
			parent.add(tlevel.get(tlevel.size()-1));
		    if((toInteger(object.get(c).data,68) == 0)){
		    	chunk.add(new DefaultMutableTreeNode("Transform" + " {"+c+"}"));
		    }else{
		    	chunk.add(new DefaultMutableTreeNode("JointTransform"+ " {"+c+"}"));
		    }
		}else if(checktype == 1009){
			object.get(tlevel.get(tlevel.size()-1)).childs.add(c);
			parent.add(tlevel.get(tlevel.size()-1));
			if(current == -1){
				current = toInteger(object.get(c).data,0);
			}else{
				next = toInteger(object.get(c).data,0);
			}
			chunk.add(new DefaultMutableTreeNode("Tree level - "+toInteger(object.get(c).data,0) +" {"+c+"}"));
				if(c+1 == object.size()) {
					code_1009();
				}else if(object.get(c+1).type != 1009){
					code_1009();
				}
			}
		else if(checktype == 1020){
			object.get(parent.get(parent.size()-1)).childs.add(c);
			parent.add(parent.get(parent.size()-1));
			chunk.add(new DefaultMutableTreeNode("Group"+ " {"+c+"}"));
		}
		else if(checktype == 1007){
			object.get(tlevel.get(tlevel.size()-1)).childs.add(c);
			parent.add(tlevel.get(tlevel.size()-1));
			chunk.add(new DefaultMutableTreeNode("Light"+ " {"+c+"}"));
		}
		else if(checktype == 1019){
			if(value_1009 == -1){
				object.get(parent.get(parent.size()-1)).childs.add(c);
				parent.add(parent.get(parent.size()-1));
				value_1009 = parent.get(parent.size()-1);
			}else{
				object.get(value_1009).childs.add(c);
				parent.add(value_1009);	
			}
			chunk.add(new DefaultMutableTreeNode("Script Object"+ " {"+c+"}"));
		}
		else if(checktype == 1018){
			object.get(chunk.size()-1).childs.add(c);
			parent.add(chunk.size()-1);
			chunk.add(new DefaultMutableTreeNode(checktype+ " {"+c+"}"));
		}
		else if(checktype == 1024){
			object.get(chunk.size()-1).childs.add(c);
			parent.add(chunk.size()-1);
			chunk.add(new DefaultMutableTreeNode(checktype+ " {"+c+"}"));
		}
		else{
			object.get(parent.get(parent.size()-1)).childs.add(c);
			parent.add(parent.get(parent.size()-1));
			chunk.add(new DefaultMutableTreeNode(checktype+ " {"+c+"}"));
		}
	}
		//make tree
		for(int j = 0; j<chunk.size(); j++){
			if(parent.get(j) == -1){
				root.add(chunk.get(j));
			}else{
				chunk.get(parent.get(j)).add(chunk.get(j));
			}
		}
	}
	
	public void loadChunks(){
		difference = 0;
		JFileChooser openFile =  new JFileChooser(Advapi32Util.registryGetStringValue(
		WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathFile"));
		
		tree = new JTree();
        JMenuItem data = new JMenuItem("Show Data");
        JMenuItem smodel = new JMenuItem("Show Model");
        JMenuItem exportmodel = new JMenuItem("Export Model");
        JMenuItem importmodel = new JMenuItem("Import Model");
        JMenuItem exportnode = new JMenuItem("Export Node");
        JMenuItem importnode = new JMenuItem("Import Node");
        JMenuItem deletenode = new JMenuItem("Delete Node");
        JMenuItem imp = new JMenuItem("Import Texture");
        JMenuItem exp = new JMenuItem("Export Texture");
	    tree.addMouseListener ( new MouseAdapter (){       
	    	public void mousePressed ( MouseEvent e ){
            if ( SwingUtilities.isRightMouseButton ( e ) ){
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                TreePath path = tree.getPathForLocation ( e.getX (), e.getY () );
                Rectangle pathBounds = tree.getUI ().getPathBounds ( tree, path );
                if ( pathBounds != null && pathBounds.contains ( e.getX (), e.getY () ) )
                {
                    JPopupMenu menu = new JPopupMenu ();
                    if((String.valueOf(node)).startsWith("Entity")){
                    	menu.add (data);
        		        menu.add (smodel);
                    	menu.add(exportnode);
                    	menu.add(importnode);
        		        menu.add (deletenode);
        		    }
                    else if((String.valueOf(node)).startsWith("Model Group")){
                    	menu.add (data);
        		        menu.add (smodel);
                    	menu.add(importmodel);                    	
                    	menu.add(exportnode);
                    	menu.add(importnode);
                    	menu.add (deletenode);
                    }
                    else if((String.valueOf(node)).startsWith("SHAPE")){
                    	menu.add (data);
                    	menu.add(exportmodel);                    	
                    	menu.add(exportnode);
                    	menu.add(importnode);
                    	menu.add (deletenode);
                    }
                    else if (!String.valueOf(node.getParent()).equals("Textures" + " {"+0+"}")){	
                    	menu.add (data);
                    	menu.add(exportnode);
                    	menu.add(importnode);
                    	menu.add (deletenode);
    		        }
    		        else{
    		        	menu.add(imp);
    		        	menu.add(exp);
    		        }
                    menu.show ( tree, pathBounds.x+pathBounds.width, pathBounds.y + pathBounds.height );
                    
                }
            }
        }
    });
	    importmodel.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		int first = getTreeIndex();
        		int replaceIndex = addMaterial();
        		if(replaceIndex == -1){
        			first +=1;
        		}
        		
        		Type model = importObjModel();
        		
        		short modelCounter = toShort(object.get(first).data , 4);
        		modelCounter++;
        		object.get(first).data[4] = (byte)modelCounter;
        		object.get(first).data[5] = (byte)(modelCounter >> 8);
        		//add +1 to model group

				//replace modelhash
        		if(replaceIndex == -1){
				    int len = hashname_new.get(hashname_new.size()-1).length();
				    byte[] names = new byte[len / 2];
				    for (int i = 0; i < len; i += 2) {
				        names[i / 2] = (byte) ((Character.digit(hashname_new.get(hashname_new.size()-1).charAt(i), 16) << 4)
				                             + Character.digit(hashname_new.get(hashname_new.size()-1).charAt(i+1), 16));
				    }
				    model.data[42] = names[0];
				    model.data[43] = names[1];
				    model.data[44] = names[2];
				    model.data[45] = names[3];
				     
	    			hashname_old.clear();
	    			hashname_new.clear();
	    			
        		}else{
				    model.data[42] = object.get(replaceIndex).data[84];
				    model.data[43] = object.get(replaceIndex).data[85];
				    model.data[44] = object.get(replaceIndex).data[86];
				    model.data[45] = object.get(replaceIndex).data[87];
        		}
        		ArrayList<Type> test = new ArrayList<Type>(object.subList(0, first+1));
				ArrayList<Type> test2 = new ArrayList<Type>(object.subList(first+1, object.size()));
				test.add(model);
				test.addAll(test2);
				difference += model.size + 12;
				object = new ArrayList<Type>(test);
				modified = true;
				frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");
				loadTree();
        	}
	    });
	    
	    exportnode.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		int firstIndex = getTreeIndex();
        		
        		exportNode(firstIndex);
        		JFileChooser fcExportNode =  new JFileChooser(Advapi32Util.registryGetStringValue(
 				WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExportModel"));
        		
        		FileNameExtensionFilter fnefExportNode = new FileNameExtensionFilter("Bsp Export Format", "bspdata");	
        		fcExportNode.setDialogTitle("Export Node");
        		fcExportNode.setSelectedFile(new File(((DefaultMutableTreeNode)tree.getLastSelectedPathComponent()).toString() + ".bspdata"));
        		fcExportNode.setFileFilter(fnefExportNode);
        		fcExportNode.setApproveButtonText("Save");
        		fcExportNode.showOpenDialog(null);
        		File expFileNode = fcExportNode.getSelectedFile();	
        		fcExportNode.setCurrentDirectory(expFileNode);
        		Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExport", expFileNode.getPath());
        		
        		int sizeOf = 0;
				ArrayList<Type> exportNode = new ArrayList<Type>(object.subList(firstIndex, secondIndex+1));
				for(int i =0; i<exportNode.size(); i++){
					 sizeOf += exportNode.get(i).size + 12;
					 exportNode.set(i, exportNode.get(i).clone());
				}
				try {
					FileChannel out;

					out = new FileOutputStream(expFileNode.getParent()+"\\"+expFileNode.getName()).getChannel();
						
						
					ByteBuffer buffer = ByteBuffer.allocate(sizeOf);
					for(int c = 0; c<exportNode.size(); c++){
						buffer.putInt(Integer.reverseBytes(exportNode.get(c).type));
						buffer.putInt(Integer.reverseBytes(exportNode.get(c).size));
						buffer.putInt(Integer.reverseBytes(exportNode.get(c).tof));
						buffer.put(exportNode.get(c).data);
					}
					buffer.flip();
					out.write(buffer);
					out.close();
				}catch (Exception e1) {
					e1.printStackTrace();
				}
        	}
        });
	    
	    importnode.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		JFileChooser jfimportNode =  new JFileChooser(Advapi32Util.registryGetStringValue(
        		WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathImportModel"));
        		
            	FileNameExtensionFilter fnefImportNode = new FileNameExtensionFilter("Bsp Export Format", "bspdata");	
            	jfimportNode.setDialogTitle("Open Bsp Node");
            	jfimportNode.setFileFilter(fnefImportNode);
            	jfimportNode.showOpenDialog(null);
            	File fileImportNode = jfimportNode.getSelectedFile();	
				byte[] importdata = null;
				try {
					importdata = Files.readAllBytes(fileImportNode.toPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
        		
        		int first = getTreeIndex();
        		changeReferences(first,1);
        		ArrayList<Type> temp = new ArrayList<Type>();
				ArrayList<Type> test = new ArrayList<Type>(object.subList(0, first));

				int i = 0;
				int bytecounter = 0;

				while(bytecounter !=importdata.length){
					temp.add(new Type());
					temp.get(i).type = toInteger(importdata , bytecounter);
					bytecounter +=4;
					temp.get(i).size = toInteger(importdata , bytecounter);
					bytecounter +=4;
					temp.get(i).tof = toInteger(importdata , bytecounter);
					bytecounter +=4;
					temp.get(i).data =  Arrays.copyOfRange(importdata, bytecounter, bytecounter+temp.get(i).size);
					bytecounter += temp.get(i).size;
					difference += temp.get(i).size + 12;
	                i++;
	            }
				ArrayList<Type> test3 = new ArrayList<Type>(object.subList(first, object.size()));
				test.addAll(temp);
				test.addAll(test3);
				object = new ArrayList<Type>(test);
        		modified = true;
				frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");
				loadTree();
        	}
        });
	    
	    
	    deletenode.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		int chunk = getTreeIndex();
        		changeReferences(chunk,-1);
        		removeNode(chunk);
        		if(searchNode != null){
        			searchNode.clear();
        		}
        		modified = true;
				frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");
        		loadTree();
        	}
        });
	    
	    
        data.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        		Pattern p = Pattern.compile("\\{(.*?)\\}");
        		Matcher m = p.matcher(String.valueOf(node.toString()));
        		while(m.find())
        		{
					Float values = new Float(0.0f);
					Float step = new Float(0.1f);
        			
        			int chunk = Integer.parseInt(m.group(1));
    				JFrame d = new JFrame();
    				d.setTitle(node.toString());
    				d.setResizable(false);
    				d.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
    				d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    				d.getContentPane().setLayout(null);
    				d.setVisible(true);
    				
        			if (object.get(chunk).type == 1005){
        				d.setSize(232,280);
						ArrayList<JSpinner> vector = new ArrayList<JSpinner>();
        				ArrayList<JSpinner> value = new ArrayList<JSpinner>();
        				JTextField shash = new JTextField();
        				JTextField thash = new JTextField();
        				JSpinner lightvalue = new JSpinner();
        				JTextField firsthash = new JTextField();
        				
        				ArrayList<JSpinner> secondvalue = new ArrayList<JSpinner>();
        				ArrayList<JSpinner> thirdvalue = new ArrayList<JSpinner>();
        				
        				JPanel vectorPanel = new JPanel(new GridLayout(1, 3));
						JPanel mainPanel = new JPanel(new GridLayout(3, 3));
						JPanel intPanel = new JPanel(new GridLayout(1, 2));
        				JPanel lightPanel = new JPanel(new GridLayout(1, 2));
        				JPanel secondPanel = new JPanel(new GridLayout(1, 3));
        				JPanel thirdPanel = new JPanel(new GridLayout(1, 4));
        				
    					JComboBox<String> cb = new JComboBox<String>();
        				
    					lightPanel.setBounds(0, 0, 225, 20);
						secondPanel.setBounds(0, 30, 225, 20);
						thirdPanel.setBounds(0, 60, 225, 20);
						
						vectorPanel.setBounds(0, 90, 225, 20);
						mainPanel.setBounds(0, 120, 225, 69);
						intPanel.setBounds(0, 199, 225, 20);
						
    					cb.setBounds(0, 229, 225, 20);
						
        				int temp = toInteger(object.get(chunk).data,20);
						if(temp != 0){
							for(int i_1005 = 24;i_1005<24+temp*4;i_1005+=4){
								cb.addItem(bonename.get(bonehash.indexOf(makeString(object.get(chunk).data,i_1005,4))));
							}
						}
						for(int i = 0; i<3; i++){
						vector.add(new JSpinner());
						vectorPanel.add(vector.get(i));
						}
						
						for(int i = 0; i<9; i++){
						value.add(new JSpinner());
						mainPanel.add(value.get(i));
						}
						intPanel.add(shash);
						intPanel.add(thash);
							
						lightPanel.add(lightvalue);
						lightPanel.add(firsthash);
						
						
						for(int i = 0; i<3; i++){
						secondvalue.add(new JSpinner());
						secondPanel.add(secondvalue.get(i));
						}
						for(int i = 0; i<4; i++){
						thirdvalue.add(new JSpinner());
						thirdPanel.add(thirdvalue.get(i));
						}
						d.getContentPane().add(cb);
        				d.getContentPane().add(vectorPanel);
        				d.getContentPane().add(mainPanel);
        				d.getContentPane().add(intPanel);
        				d.getContentPane().add(lightPanel);
        				d.getContentPane().add(secondPanel);
        				d.getContentPane().add(thirdPanel);
        				
        				int off = 0;
							lightvalue.setValue(toInteger(object.get(chunk).data,off));
							off+=4;
							
							if(bonehash.indexOf(makeString(object.get(chunk).data,off,4)) != -1){
							firsthash.setText(bonename.get(bonehash.indexOf(makeString(object.get(chunk).data,off,4))));
							}
							else{
								firsthash.setText(makeString(object.get(chunk).data,off,4));
							}
							firsthash.setCaretPosition(0);
							off+=4;
							
						for(int i = 0; i<3; i++){
							secondvalue.get(i).setValue(object.get(chunk).data[off] & 0xFF);
							off++;
						}
						for(int i = 0; i<4; i++){
							if(i == 1){
								thirdvalue.get(i).setValue(object.get(chunk).data[off] & 0xFF);
								off++;
							}else{
							thirdvalue.get(i).setValue(toInteger(object.get(chunk).data,off));
							off+=4;
							}
						}
						if(cb.getItemCount() > 0){
				        	int index = cb.getSelectedIndex();
							off = 24+temp*4 + 56*index;
							
							for(int i = 0; i<3; i++){
								vector.get(i).setModel(new SpinnerNumberModel(step, null, null, values));
								JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(vector.get(i),"0.0000000");
								vector.get(i).setEditor(numberEditor);
								vector.get(i).setValue(toFloat(object.get(chunk).data,off));
								off+=4;
							}
							
							
							for(int i = 0;i<9;i++){
        					    value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.0000000"));
								value.get(i).setValue(toFloat(object.get(chunk).data,off));
								off+=4;
							}
								
							shash.setText(makeString(object.get(chunk).data,off,4));
							shash.setCaretPosition(0);
							off+=4;
								
							thash.setText(makeString(object.get(chunk).data,off,4));
							thash.setCaretPosition(0);
								
				        	
						}
        				cb.addItemListener(new ItemListener() {
    						public void itemStateChanged(ItemEvent arg0) {
        				        	int index = cb.getSelectedIndex();
									int off = 24+temp*4 + 56*index;
									
									for(int i = 0; i<3; i++){
										vector.get(i).setModel(new SpinnerNumberModel(step, null, null, values));
										JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(vector.get(i),"0.0000000");
										vector.get(i).setEditor(numberEditor);
										vector.get(i).setValue(toFloat(object.get(chunk).data,off));
										off+=4;
									}
									
									
									for(int i = 0;i<9;i++){
		        					    value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
		    							value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.0000000"));
										value.get(i).setValue(toFloat(object.get(chunk).data,off));
										off+=4;
									}
										
									shash.setText(makeString(object.get(chunk).data,off,4));
									shash.setCaretPosition(0);
									off+=4;
										
									thash.setText(makeString(object.get(chunk).data,off,4));
									thash.setCaretPosition(0);
										
        				        	
        				    }
        				});
        			}else if(object.get(chunk).type == 1017){
        				d.setBounds(100, 100, 400, 400);
        				
        				JLabel lblAnimation = new JLabel("Animation");
        				lblAnimation.setFont(new Font("Tahoma", Font.PLAIN, 13));
        				lblAnimation.setBounds(139, 22, 64, 14);
        				d.getContentPane().add(lblAnimation);
        				
        				JComboBox animComboBox = new JComboBox();
        				animComboBox.setSelectedIndex(-1);
        				animComboBox.setBounds(213, 20, 146, 20);
        				d.getContentPane().add(animComboBox);
        				
        				JLabel lblEvent = new JLabel("Event");
        				lblEvent.setHorizontalAlignment(SwingConstants.CENTER);
        				lblEvent.setFont(new Font("Tahoma", Font.PLAIN, 13));
        				lblEvent.setBounds(139, 47, 64, 14);
        				d.getContentPane().add(lblEvent);
        				
        				JLabel lblTime = new JLabel("Time");
        				lblTime.setHorizontalAlignment(SwingConstants.LEFT);
        				lblTime.setFont(new Font("Tahoma", Font.PLAIN, 13));
        				lblTime.setBounds(23, 47, 64, 14);
        				d.getContentPane().add(lblTime);
        				
        				Button button = new Button("Add Event To List");
        				button.addActionListener(new ActionListener() {
        					public void actionPerformed(ActionEvent e) {
        					}
        				});
        				button.setBounds(23, 301, 146, 22);
        				d.getContentPane().add(button);
        				
        				Button button_1 = new Button("Remove Event From List");
        				button_1.setBounds(213, 301, 146, 22);
        				d.getContentPane().add(button_1);
        				
        				Button button_2 = new Button("Save");
        				button_2.setBounds(289, 329, 70, 22);
        				d.getContentPane().add(button_2);
        				
        				Button editEventButton = new Button("Edit Event");
        				editEventButton.setBounds(213, 273, 146, 22);
        				d.getContentPane().add(editEventButton);
        				
        				Button button_4 = new Button("Test");
        				button_4.setBounds(23, 329, 70, 22);
        				d.getContentPane().add(button_4);
        				
        				JComboBox boneComboBox = new JComboBox();
        				boneComboBox.setBounds(23, 273, 146, 20);
        				d.getContentPane().add(boneComboBox);
        				
        				JSlider slider = new JSlider();
        				slider.setValue(0);
        				slider.setBounds(54, 248, 216, 14);
        				d.getContentPane().add(slider);
        				
        				JLabel label = new JLabel("Time");
        				label.setHorizontalAlignment(SwingConstants.LEFT);
        				label.setFont(new Font("Tahoma", Font.PLAIN, 13));
        				label.setBounds(23, 248, 36, 14);
        				d.getContentPane().add(label);
        				
        				textField = new JTextField();
        				textField.setBounds(295, 247, 64, 20);
        				d.getContentPane().add(textField);
        				textField.setColumns(10);
        				
        				JSplitPane splitPane = new JSplitPane();
        				splitPane.setBounds(24, 67, 336, 161);
        				d.getContentPane().add(splitPane);
        				
        				List list = new List();
        				splitPane.setRightComponent(list);
        				
        				
        				List list_1 = new List();
        				splitPane.setLeftComponent(list_1);
        				
        				//calculation
        				int eventCounter = toInteger(object.get(chunk).data, 0);
        				
        				int libCounter = toInteger(object.get(chunk).data, 4+eventCounter*20);			
        				
        				int actualAnim = chunk+1;
        				
        				ArrayList<Integer> animIndexes = new ArrayList<Integer>();
        				boneIndexes = null;
        				
        				for(int i = 0; i<libCounter; i++){
        					while(object.get(actualAnim).type != 1027){
        						actualAnim++;
        					}
        					animIndexes.add(actualAnim);
        					animComboBox.addItem(animname.get(i));
        					actualAnim++;
        				}
        				
        				done = false;
        				animComboBox.addItemListener(new ItemListener() {
    						public void itemStateChanged(ItemEvent e) {
    							if (e.getStateChange() == ItemEvent.SELECTED && done) {
    								done = false;
    								list.removeAll();
    								list_1.removeAll();
    								boneComboBox.removeAllItems(); // clean
    								int animIndex = animComboBox.getSelectedIndex();
    								int boneIndex = (animIndexes.get(animIndex)) + 1; //first bone
    								minTime = toFloat(object.get(animIndexes.get(animIndex)).data, 4);
    								maxTime = toFloat(object.get(animIndexes.get(animIndex)).data, 8);
    								slider.setMinimum((int)minTime * 1000);
    								slider.setMaximum((int)maxTime * 1000);
    								int animsCounter = toInteger(object.get(animIndexes.get(animIndex)).data, 12);
    								
    								int boneCounter = toInteger(object.get(animIndexes.get(animIndex)).data,16 + animsCounter*8);
    								
    								IntBuffer temp = IntBuffer.allocate(boneCounter);
    								for(int j = boneIndex; j<animIndexes.get(animIndex)+boneCounter+1; j++){
    									temp.put(j);
    									String hashname = makeString(object.get(j).data, 4, 4);
    									boneComboBox.addItem(hashname);
    								}
    								temp.flip();
    								boneIndexes =(temp.array());
    							}
    							done = true;
    							if(boneComboBox.getItemCount() != 0){
	    	        				boneComboBox.setSelectedIndex(-1);
	    	        				boneComboBox.setSelectedIndex(0);
    							}
    						}	
    					});
        				
           				boneComboBox.addItemListener(new ItemListener() {
        						public void itemStateChanged(ItemEvent e) {
        							if (e.getStateChange() == 1 && done) {
        								list.removeAll(); // clean list
        								list_1.removeAll(); // clean list
        								int boneIndex = boneComboBox.getSelectedIndex();
        									//bones to event;
        									int times = toInteger(object.get(boneIndexes[boneIndex]).data, 12);
        									FloatBuffer test = FloatBuffer.allocate(times);
        									for(int j = 0; j<times; j++){ // times
        										test.put(toFloat(object.get(boneIndexes[boneIndex]).data,30 + j*4));
        									}
        									int dataIndex = 30+times*4;
        									int index = 0;
        									int type = toInteger(object.get(boneIndexes[boneIndex]).data, 0);
        									if(type == 0){
        										for(int j = dataIndex; j <dataIndex+times*16; j+=16, index++){
        											int unk = (byte)object.get(boneIndexes[boneIndex]).data[j] & 0xFF;
        											int rotatex = (byte)object.get(boneIndexes[boneIndex]).data[j+1] & 0xFF;
        											int parabol1 = (byte)object.get(boneIndexes[boneIndex]).data[j+2] & 0xFF;
        											int parabol2 = (byte)object.get(boneIndexes[boneIndex]).data[j+3] & 0xFF;
        										
        											int unk2 = (byte)object.get(boneIndexes[boneIndex]).data[j+4] & 0xFF;
        											int rotatey = (byte)object.get(boneIndexes[boneIndex]).data[j+5] & 0xFF;
        											int parabol3 = (byte)object.get(boneIndexes[boneIndex]).data[j+6] & 0xFF;
        											int parabol4 = (byte)object.get(boneIndexes[boneIndex]).data[j+7] & 0xFF;
        										
        											int unk3 = (byte)object.get(boneIndexes[boneIndex]).data[j+8] & 0xFF;
        											int rotatez = (byte)object.get(boneIndexes[boneIndex]).data[j+9] & 0xFF;
        											int parabol5 = (byte)object.get(boneIndexes[boneIndex]).data[j+10] & 0xFF;
        											int parabol6 = (byte)object.get(boneIndexes[boneIndex]).data[j+11] & 0xFF;
        										
        											int unk4 = (byte)object.get(boneIndexes[boneIndex]).data[j+12] & 0xFF;
        											int value = (byte)object.get(boneIndexes[boneIndex]).data[j+13] & 0xFF;
        											int parabol7 = (byte)object.get(boneIndexes[boneIndex]).data[j+14] & 0xFF;
        											int parabol8 = (byte)object.get(boneIndexes[boneIndex]).data[j+15] & 0xFF;
        											
        											list_1.add(""+test.get(index));
        											
        											list.add
        											(
        													unk + " " +rotatex+ " " + parabol1 + " " + parabol2 + " " +
        													unk2 + " " +rotatey+ " " + parabol3 + " " + parabol4 + " " +
        													unk3 + " " +rotatez+ " " + parabol5 + " " + parabol6 + " "+
        													unk4 + " " +value+ " " + parabol7 + " " + parabol8
        													);
        										}
        									}else if(type == 1){
        										for(int j = dataIndex; j <dataIndex+times*12; j+=12, index++){
        											float posx = toFloat(object.get(boneIndexes[boneIndex]).data, j);
        											float posy = toFloat(object.get(boneIndexes[boneIndex]).data, j+4);
        											float posz = toFloat(object.get(boneIndexes[boneIndex]).data, j+8);
        											list_1.add(""+test.get(index));
        											list.add
        											(posx + " " +posy+ " " + posz);
        										}
        										
        									}
        							}
        						}	
        					});
           				
        				animComboBox.setSelectedIndex(-1);
        				animComboBox.setSelectedIndex(0);
           				
           				
           				list_1.addItemListener(new ItemListener(){
    						public void itemStateChanged(ItemEvent e) {
    							int index = list_1.getSelectedIndex();
    							list.select(index);
    							slider.setValue((int)(Float.parseFloat(list_1.getSelectedItem())*1000));
    						}
           				});
           				
           				editEventButton.addActionListener(new ActionListener() {
           					public void actionPerformed(ActionEvent arg0) {
           						if(list.getSelectedIndex() != -1){
           							JFrame editWindow = new JFrame("Event: " + list.getSelectedIndex());
           							editWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
           							editWindow.getContentPane().setLayout(null);
           							editWindow.setResizable(false);
           							editWindow.setVisible(true);
           							String[] split = list.getSelectedItem().split("\\s+");
           							if(split.length > 3){
           								editWindow.setSize(827, 143);
           						
           								JSpinner rx_spinner = new JSpinner();
           								rx_spinner.setBounds(222, 8, 73, 20);
           								editWindow.getContentPane().add(rx_spinner);
           						
           								JLabel lblNewLabel = new JLabel("Rotate X:");
           								lblNewLabel.setBounds(157, 11, 55, 14);
           								editWindow.getContentPane().add(lblNewLabel);
           						
           								JLabel lblRotateY = new JLabel("Rotate Y:");
           								lblRotateY.setBounds(157, 36, 55, 14);
           								editWindow.getContentPane().add(lblRotateY);
           						
           								JLabel lblRotateZ = new JLabel("Rotate Z:");
           								lblRotateZ.setBounds(157, 61, 55, 14);
           								editWindow.getContentPane().add(lblRotateZ);
           						
           								JSpinner ry_spinner = new JSpinner();
           								ry_spinner.setBounds(222, 33, 73, 20);
           								editWindow.getContentPane().add(ry_spinner);
           						
           								JSpinner rz_spinner = new JSpinner();
           								rz_spinner.setBounds(222, 58, 73, 20);
           								editWindow.getContentPane().add(rz_spinner);
           						
           								JCheckBox bradCheckBox = new JCheckBox("brad");
           								bradCheckBox.setBounds(645, 33, 53, 23);
           								bradCheckBox.setSelected(true);
           								editWindow.getContentPane().add(bradCheckBox);
           						
           								JLabel lblScale = new JLabel("Scale:");
           								lblScale.setBounds(157, 86, 55, 14);
           								editWindow.getContentPane().add(lblScale);
           							
           								JSpinner scale_spinner = new JSpinner();
           								scale_spinner.setBounds(222, 83, 73, 20);
           								editWindow.getContentPane().add(scale_spinner);
           						
           								JLabel lblTangentStart = new JLabel("Tangent start:");
           								lblTangentStart.setBounds(305, 11, 79, 14);
           								editWindow.getContentPane().add(lblTangentStart);
           							
           								JSpinner rx_tan_start_spinner = new JSpinner();
           								rx_tan_start_spinner.setBounds(394, 8, 73, 20);
           								editWindow.getContentPane().add(rx_tan_start_spinner);
           						
           								JLabel lblTangentStart_1 = new JLabel("Tangent start:");
           								lblTangentStart_1.setBounds(305, 36, 79, 14);
           								editWindow.getContentPane().add(lblTangentStart_1);
           						
           								JSpinner ry_tan_start_spinner = new JSpinner();
           								ry_tan_start_spinner.setBounds(394, 33, 73, 20);
           								editWindow.getContentPane().add(ry_tan_start_spinner);
           						
           								JLabel lblTangentStart_2 = new JLabel("Tangent start:");
           								lblTangentStart_2.setBounds(305, 61, 79, 14);
           								editWindow.getContentPane().add(lblTangentStart_2);
           						
           								JSpinner rz_tan_start_spinner = new JSpinner();
           								rz_tan_start_spinner.setBounds(394, 58, 73, 20);
           								editWindow.getContentPane().add(rz_tan_start_spinner);
           						
           								JLabel lblTangentStart_3 = new JLabel("Tangent start:");
           								lblTangentStart_3.setBounds(305, 86, 79, 14);
           								editWindow.getContentPane().add(lblTangentStart_3);
           						
           								JSpinner scale_tan_start_spinner = new JSpinner();
           								scale_tan_start_spinner.setBounds(394, 83, 73, 20);
           								editWindow.getContentPane().add(scale_tan_start_spinner);
           						
           								JLabel lblTangentEnd = new JLabel("Tangent end:");
           								lblTangentEnd.setBounds(477, 11, 79, 14);
           								editWindow.getContentPane().add(lblTangentEnd);
           						
           								JSpinner rx_tan_end_spinner = new JSpinner();
           								rx_tan_end_spinner.setBounds(566, 8, 73, 20);
           								editWindow.getContentPane().add(rx_tan_end_spinner);
           						
           								JLabel lblTangentEnd_1 = new JLabel("Tangent end:");
           								lblTangentEnd_1.setBounds(477, 36, 79, 14);
           								editWindow.getContentPane().add(lblTangentEnd_1);
           						
           								JSpinner ry_tan_end_spinner = new JSpinner();
           								ry_tan_end_spinner.setBounds(566, 33, 73, 20);
           								editWindow.getContentPane().add(ry_tan_end_spinner);
           						
           								JLabel lblTangentEnd_2 = new JLabel("Tangent end:");
           								lblTangentEnd_2.setBounds(477, 61, 79, 14);
           								editWindow.getContentPane().add(lblTangentEnd_2);
           						
           								JSpinner rz_tan_end_spinner = new JSpinner();
           								rz_tan_end_spinner.setBounds(566, 58, 73, 20);
           								editWindow.getContentPane().add(rz_tan_end_spinner);
           						
           								JLabel lblTangentEnd_3 = new JLabel("Tangent end:");
           								lblTangentEnd_3.setBounds(477, 86, 79, 14);
           								editWindow.getContentPane().add(lblTangentEnd_3);
           						
           								JSpinner scale_tan_end_spinner = new JSpinner();
           								scale_tan_end_spinner.setBounds(566, 83, 73, 20);
           								editWindow.getContentPane().add(scale_tan_end_spinner);
           								
           								JLabel lblTimer = new JLabel("Time:");
           								lblTimer.setBounds(649, 11, 79, 14);
           								editWindow.getContentPane().add(lblTimer);
           						
           								JSpinner timer_spinner = new JSpinner();
           								timer_spinner.setBounds(738, 8, 73, 20);
           								editWindow.getContentPane().add(timer_spinner);
           								
           								JButton btnNewButton = new JButton("Save");
           								btnNewButton.setBounds(738, 33, 73, 20);
           								editWindow.getContentPane().add(btnNewButton);
           							
           								JLabel lblUnkX = new JLabel("Unk X:");
           								lblUnkX.setBounds(9, 11, 55, 14);
           								editWindow.getContentPane().add(lblUnkX);
           							
           								JSpinner unk_x_spinner = new JSpinner();
           								unk_x_spinner.setBounds(74, 8, 73, 20);
           								editWindow.getContentPane().add(unk_x_spinner);
           							
           								JLabel lblUnkY = new JLabel("Unk Y:");
           								lblUnkY.setBounds(9, 36, 55, 14);
           								editWindow.getContentPane().add(lblUnkY);
           							
           								JSpinner unk_y_spinner = new JSpinner();
           								unk_y_spinner.setBounds(74, 33, 73, 20);
           								editWindow.getContentPane().add(unk_y_spinner);
           							
           								JLabel lblUnkZ = new JLabel("Unk Z:");
           								lblUnkZ.setBounds(9, 61, 55, 14);
           								editWindow.getContentPane().add(lblUnkZ);
           							
           								JSpinner unk_z_spinner = new JSpinner();
           								unk_z_spinner.setBounds(74, 58, 73, 20);
           								editWindow.getContentPane().add(unk_z_spinner);
           							
           								JLabel lblUnkScale = new JLabel("Unk S:");
           								lblUnkScale.setBounds(9, 86, 55, 14);
           								editWindow.getContentPane().add(lblUnkScale);
           							
           								JSpinner unk_scale_spinner = new JSpinner();
           								unk_scale_spinner.setBounds(74, 83, 73, 20);
           								editWindow.getContentPane().add(unk_scale_spinner);
           							
           								//calculations	
           								Double values = new Double(0.0);
           								Double step = new Double(0.1);
           							
           								bradCheckBox.addActionListener(new ActionListener() {
           									public void actionPerformed(ActionEvent arg0) {
           										//x
           										unk_x_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										unk_x_spinner.setEditor(new JSpinner.NumberEditor(unk_x_spinner,"0.00"));
           										unk_x_spinner.setValue(bradToDegrees(Integer.parseInt(split[0]),bradCheckBox.isSelected()));
                   							
           										rx_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										rx_spinner.setEditor(new JSpinner.NumberEditor(rx_spinner,"0.00"));
           										rx_spinner.setValue(bradToDegrees(Integer.parseInt(split[1]),bradCheckBox.isSelected()));
                   							
           										rx_tan_start_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										rx_tan_start_spinner.setEditor(new JSpinner.NumberEditor(rx_tan_start_spinner,"0.00"));
           										rx_tan_start_spinner.setValue(bradToDegrees(Integer.parseInt(split[2]),bradCheckBox.isSelected()));
           										
           										rx_tan_end_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										rx_tan_end_spinner.setEditor(new JSpinner.NumberEditor(rx_tan_end_spinner,"0.00"));
           										rx_tan_end_spinner.setValue(bradToDegrees(Integer.parseInt(split[3]),bradCheckBox.isSelected()));
           										
           										//y
           										unk_y_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										unk_y_spinner.setEditor(new JSpinner.NumberEditor(unk_y_spinner,"0.00"));
           										unk_y_spinner.setValue(bradToDegrees(Integer.parseInt(split[4]),bradCheckBox.isSelected()));
           										
           										ry_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										ry_spinner.setEditor(new JSpinner.NumberEditor(ry_spinner,"0.00"));
           										ry_spinner.setValue(bradToDegrees(Integer.parseInt(split[5]),bradCheckBox.isSelected()));
           										
           										ry_tan_start_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										ry_tan_start_spinner.setEditor(new JSpinner.NumberEditor(ry_tan_start_spinner,"0.00"));
           										ry_tan_start_spinner.setValue(bradToDegrees(Integer.parseInt(split[6]),bradCheckBox.isSelected()));
           										
           										ry_tan_end_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										ry_tan_end_spinner.setEditor(new JSpinner.NumberEditor(ry_tan_end_spinner,"0.00"));
           										ry_tan_end_spinner.setValue(bradToDegrees(Integer.parseInt(split[7]),bradCheckBox.isSelected()));
                   							
           										//z
           										unk_z_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           										unk_z_spinner.setEditor(new JSpinner.NumberEditor(unk_z_spinner,"0.00"));
           										unk_z_spinner.setValue(bradToDegrees(Integer.parseInt(split[8]),bradCheckBox.isSelected()));
           										
           										rz_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								rz_spinner.setEditor(new JSpinner.NumberEditor(rz_spinner,"0.00"));
                   								rz_spinner.setValue(bradToDegrees(Integer.parseInt(split[9]),bradCheckBox.isSelected()));
                   								
                   								rz_tan_start_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								rz_tan_start_spinner.setEditor(new JSpinner.NumberEditor(rz_tan_start_spinner,"0.00"));
                   								rz_tan_start_spinner.setValue(bradToDegrees(Integer.parseInt(split[10]),bradCheckBox.isSelected()));
                   								
                   								rz_tan_end_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								rz_tan_end_spinner.setEditor(new JSpinner.NumberEditor(rz_tan_end_spinner,"0.00"));
                   								rz_tan_end_spinner.setValue(bradToDegrees(Integer.parseInt(split[11]),bradCheckBox.isSelected()));
                   							
                   								//scale
                   								unk_scale_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								unk_scale_spinner.setEditor(new JSpinner.NumberEditor(unk_scale_spinner,"0.00"));
                   								unk_scale_spinner.setValue(bradToDegrees(Integer.parseInt(split[12]),bradCheckBox.isSelected()));
                   							
                   								scale_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								scale_spinner.setEditor(new JSpinner.NumberEditor(scale_spinner,"0.00"));
                   								scale_spinner.setValue(bradToDegrees(Integer.parseInt(split[13]),bradCheckBox.isSelected()));
                   								
                   								scale_tan_start_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								scale_tan_start_spinner.setEditor(new JSpinner.NumberEditor(scale_tan_start_spinner,"0.00"));
                   								scale_tan_start_spinner.setValue(bradToDegrees(Integer.parseInt(split[14]),bradCheckBox.isSelected()));
                   								
                   								scale_tan_end_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								scale_tan_end_spinner.setEditor(new JSpinner.NumberEditor(scale_tan_end_spinner,"0.00"));
                   								scale_tan_end_spinner.setValue(bradToDegrees(Integer.parseInt(split[15]),bradCheckBox.isSelected()));
                   								
                   								timer_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
                   								timer_spinner.setEditor(new JSpinner.NumberEditor(timer_spinner,"0.000000000"));
                   								timer_spinner.setValue(Double.parseDouble(list_1.getSelectedItem()));
           									}
           								});
           								
           								bradCheckBox.doClick();
           							}else{
           								editWindow.setSize(663, 64);
           								
           								JSpinner posy_spinner = new JSpinner();
           								posy_spinner.setBounds(222, 8, 73, 20);
           								editWindow.getContentPane().add(posy_spinner);
           								
           								JLabel lblNewLabel = new JLabel("Position Y:");
           								lblNewLabel.setBounds(157, 11, 62, 14);
           								editWindow.getContentPane().add(lblNewLabel);
           								
           								JLabel lblTangentStart = new JLabel("Position Z:");
           								lblTangentStart.setBounds(305, 11, 62, 14);
           								editWindow.getContentPane().add(lblTangentStart);
           								
           								JSpinner posz_spinner = new JSpinner();
           								posz_spinner.setBounds(370, 8, 73, 20);
           								editWindow.getContentPane().add(posz_spinner);
           								
           								JLabel lblTangentEnd = new JLabel("Time:");
           								lblTangentEnd.setBounds(453, 11, 36, 14);
           								editWindow.getContentPane().add(lblTangentEnd);
           								
           								JSpinner timer_spinner = new JSpinner();
           								timer_spinner.setBounds(499, 8, 73, 20);
           								editWindow.getContentPane().add(timer_spinner);
           								
           								JButton btnNewButton = new JButton("Save");
           								btnNewButton.setBounds(578, 8, 73, 20);
           								editWindow.getContentPane().add(btnNewButton);
           								
           								JLabel lblUnkX = new JLabel("Position X:");
           								lblUnkX.setBounds(9, 11, 62, 14);
           								editWindow.getContentPane().add(lblUnkX);
           								
           								JSpinner posx_spinner = new JSpinner();
           								posx_spinner.setBounds(74, 8, 73, 20);
           								editWindow.getContentPane().add(posx_spinner);
           								
           								//calculations
           								
           								
           								posx_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           								posx_spinner.setEditor(new JSpinner.NumberEditor(posx_spinner,"0.00000"));
           								posx_spinner.setValue(Double.parseDouble(split[0]));
           								
           								posy_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           								posy_spinner.setEditor(new JSpinner.NumberEditor(posy_spinner,"0.00000"));
           								posy_spinner.setValue(Double.parseDouble(split[1]));
           								
           								posz_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           								posz_spinner.setEditor(new JSpinner.NumberEditor(posz_spinner,"0.00000"));
           								posz_spinner.setValue(Double.parseDouble(split[2]));
           								
           								timer_spinner.setModel(new SpinnerNumberModel(values, null, null, step));
           								timer_spinner.setEditor(new JSpinner.NumberEditor(timer_spinner,"0.000000000"));
           								timer_spinner.setValue(Double.parseDouble(list_1.getSelectedItem()));
           							}
           						}

           					}
           				});
        			}else if(object.get(chunk).type == 5){
        		        	d.setSize(310,341);
        		        	ArrayList<byte[]> data = new ArrayList<byte[]>();
        					
        					JComboBox<String> cb = new JComboBox<String>();
        					
        		        	JSpinner id = new JSpinner();
        		        	id.setModel(new SpinnerNumberModel(0, null, null, 1));
        		        	JTextField fhash = new JTextField();
        		        	JSpinner id2 = new JSpinner();
        		        	id2.setModel(new SpinnerNumberModel(0, null, null, 1));
        		        	ArrayList<JSpinner> color = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> color2 = new ArrayList<JSpinner>();
        		        	JTextField shash = new JTextField();
        		        	ArrayList<JSpinner> value = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> value1 = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> value2 = new ArrayList<JSpinner>();
        		        	JTextField thash = new JTextField();
        		        	ArrayList<JSpinner> value3 = new ArrayList<JSpinner>();
        		        	
        		        	JPanel idPanel = new JPanel(new GridLayout(1, 3));
        		        	JPanel colorPanel = new JPanel(new GridLayout(1, 4));
        		        	JPanel color2Panel = new JPanel(new GridLayout(1, 4));
        		        	JPanel valuePanel = new JPanel(new GridLayout(4, 4));
        		        	
        		        	cb.setBounds(110, 290, 100, 20);
        		        	idPanel.setBounds(0, 0, 300, 20);
        					colorPanel.setBounds(0, 30, 200, 20);
        					color2Panel.setBounds(0, 60, 200, 20);
        					shash.setBounds(0, 90, 100, 20);
        					valuePanel.setBounds(0, 120, 300, 80);
        					
        					idPanel.add(id);
        					idPanel.add(fhash);
        					idPanel.add(id2);
        					
        					for(int i = 0; i<4; i++){
        						color.add(new JSpinner());
        						color.get(i).setModel(new SpinnerNumberModel(0, null, null, 1));
        						colorPanel.add(color.get(i));
        						
        						color2.add(new JSpinner());
        						color2.get(i).setModel(new SpinnerNumberModel(0, null, null, 1));
        						color2Panel.add(color2.get(i));
        					}
        					for(int i = 0; i<3; i++){
        						value.add(new JSpinner());
        						value.get(i).setModel(new SpinnerNumberModel(0, null, null, 1));
        						valuePanel.add(value.get(i));
        					}
        					for(int j = 0; j<3; j++){
        						value1.add(new JSpinner());
        						value1.get(j).setModel(new SpinnerNumberModel(0, null, null, 1));
        						valuePanel.add(value1.get(j));
        					}
        					value2.add(new JSpinner());
        					value2.get(0).setModel(new SpinnerNumberModel(0, null, null, 1));
        					value2.add(new JSpinner());
        					value2.get(1).setModel(new SpinnerNumberModel(0, null, null, 1));
        					value2.add(new JSpinner());
        					value2.get(2).setModel(new SpinnerNumberModel(0, null, null, 1));
        					
        					valuePanel.add(value2.get(0));
        					valuePanel.add(value2.get(1));
        					valuePanel.add(value2.get(2));
        					
        					valuePanel.add(thash);
        					value3.add(new JSpinner());
        					value3.get(0).setModel(new SpinnerNumberModel(0, null, null, 1));
        					value3.add(new JSpinner());
        					value3.get(1).setModel(new SpinnerNumberModel(0, null, null, 1));
        					
        					valuePanel.add(value3.get(0));
        					valuePanel.add(value3.get(1));
        					
        					int namec;
        					ArrayList<Integer> offsets = new ArrayList<Integer>();
        					int off = 96;
        							StringBuilder builder = new StringBuilder();
        							int count = 0;
        							int ex = 0;
        								while(object.get(chunk).data[off] == count && object.get(chunk).data[off+4] != 0 || count == 1  && object.get(chunk).data[off+8] == 1){
        									if(count == 1 && object.get(chunk).data[off+8] == 1){
        										ex = 8;
        									}
                							namec = toInteger(object.get(chunk).data,off+4+ex);
                							
                							data.add(Arrays.copyOfRange(object.get(chunk).data, off, off+ex+8+namec*4+36));
                							
                							builder = new StringBuilder();
                					        for (int j = off+ex+8; j<off+ex+8+namec*4;j=j+4){
                					        	builder.append((char)object.get(chunk).data[j]);
                					        }
                							cb.addItem(builder.toString());
                							off = off+ex+8+namec*4+36;
                							offsets.add(namec);
                							builder.setLength(0);
                							count++;
        								}
        					
        					JSpinner matid = new JSpinner();
        					matid.setModel(new SpinnerNumberModel(0, null, null, 1));
        					JSpinner matc = new JSpinner();
        					matc.setModel(new SpinnerNumberModel(0, null, null, 1));
        					JTextField matname = new JTextField();
        					
        		        	JPanel multi2Panel = new JPanel(new GridLayout(2, 4));
        		        	JTextField texthash = new JTextField();
        					JPanel multiPanel = new JPanel(new GridLayout(1, 3));
        					multiPanel.add(matid);
        					multiPanel.add(matc);
        					multiPanel.add(matname);
        					
        		        	ArrayList<JSpinner> multi = new ArrayList<JSpinner>();
        		        	for(int i = 0; i<8; i++){
        		        		multi.add(new JSpinner());
        		        		multi.get(i).setModel(new SpinnerNumberModel(0, null, null, 1));
        		        		multi2Panel.add(multi.get(i));
        		        	}
        		        	multiPanel.setBounds(0, 210, 300, 20);
        		        	multi2Panel.setBounds(0, 240, 300, 40);
        		        	texthash.setBounds(0, 290, 100, 20);
        		        	
        					JButton calculate = new JButton("");
        					JButton calculate2 = new JButton("");
        					JButton save = new JButton("Save");
        					
        		        	save.setBounds(220, 290, 80, 20);
        					calculate.setBounds(220, 30, 80, 20);
        					calculate2.setBounds(220, 60, 80, 20);
        		        	
        					
        					d.getContentPane().add(calculate);
        					d.getContentPane().add(calculate2);
        					d.getContentPane().add(save);
        					d.getContentPane().add(cb);
        					d.getContentPane().add(idPanel);
        					d.getContentPane().add(colorPanel);
        					d.getContentPane().add(color2Panel);
        					d.getContentPane().add(shash);
        					d.getContentPane().add(valuePanel);
        					d.getContentPane().add(multiPanel);
        					d.getContentPane().add(multi2Panel);
        					d.getContentPane().add(texthash);

        		        	off = 0;
        					id.setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					fhash.setText(makeString(object.get(chunk).data,off,4));
        					fhash.setCaretPosition(0);
        					off+=4;
        					id2.setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					
        					for(int i = 0; i<4; i++){
        						color.get(i).setValue(toInteger(object.get(chunk).data,off));
        						off+=4;
        					}
        					
        					for(int i = 0; i<4; i++){
        						color2.get(i).setValue(toInteger(object.get(chunk).data,off));
        						off+=4;	
        					}
        					
        					shash.setText(makeString(object.get(chunk).data,off,4));
        					shash.setCaretPosition(0);
        					off+=4;
        					
        					for(int i = 0; i<3; i++){
        						value.get(i).setValue(toInteger(object.get(chunk).data,off));
        						off+=4;	
        					}
        					for(int i = 0; i<3; i++){
        						value1.get(i).setValue(toInteger(object.get(chunk).data,off));
        						off+=4;	
        					}
        					for(int i = 0; i<3; i++){
        						value2.get(i).setValue(toInteger(object.get(chunk).data,off));
        						off+=4;
        					}
        					thash.setText(makeString(object.get(chunk).data,off,4));
        					thash.setCaretPosition(0);
        					off+=4;
        					
        					value3.get(0).setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					value3.get(1).setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					colors = new Color((int)color.get(0).getValue(),(int)color.get(1).getValue(),(int)color.get(2).getValue(),(int)color.get(3).getValue());
        					calculate.setBackground(colors);
        					
        					colors2 = new Color((int)color2.get(0).getValue(),(int)color2.get(1).getValue(),(int)color2.get(2).getValue(),(int)color2.get(3).getValue());
        					calculate2.setBackground(colors2);
        					
        					if(cb.getItemCount() > 0){
        						int index = cb.getSelectedIndex();
        						off = 0;
        						matid.setValue(toInteger(data.get(index),off));
        						off+=4;
        						matc.setValue(toInteger(data.get(index),off));
        						off+=4;
        						matname.setText(cb.getName());
        						matname.setCaretPosition(0);
        						off+=(int)matc.getValue()*4;
        						for(int i = 0; i<8; i++){
        							multi.get(i).setValue(toInteger(data.get(index),off));
        							off+=4;
        						}
        						texthash.setText(makeString(data.get(index),off,4));
        						texthash.setCaretPosition(0);
        						off+=4;
        					}
        					
            				cb.addItemListener(new ItemListener() {
        						public void itemStateChanged(ItemEvent arg0) {
        					        	int index = cb.getSelectedIndex();
        					        	int off = 0;
        								matid.setValue(toInteger(data.get(index),off));
        								off+=4;
        								matc.setValue(toInteger(data.get(index),off));
        								off+=4;
        								matname.setText(cb.getName());
        								matname.setCaretPosition(0);
        								off+=(int)matc.getValue()*4;
        								for(int i = 0; i<8; i++){
        									multi.get(i).setValue(toInteger(data.get(index),off));
        									off+=4;
        								}
        								texthash.setText(makeString(data.get(index),off,4));
        								texthash.setCaretPosition(0);
        								off+=4;
        					  }
        					});
        					
        					calculate.addActionListener(new ActionListener() {
        						public void actionPerformed(ActionEvent arg0) {
        							try{
        								 colors = JColorChooser.showDialog(null,"Pick your color", colors);
        								 color.get(0).setValue(colors.getRed());
        								 color.get(1).setValue(colors.getGreen());
        								 color.get(2).setValue(colors.getBlue());
        								 color.get(3).setValue(colors.getAlpha());
        								 calculate.setBackground(colors);
        							}catch(Exception e){
        							}
        								
        						}
        					});
        					calculate2.addActionListener(new ActionListener() {
        						public void actionPerformed(ActionEvent arg0) {
        							try{
        								 colors2 = JColorChooser.showDialog(null,"Pick your color", colors);
        								 color2.get(0).setValue(colors2.getRed());
        								 color2.get(1).setValue(colors2.getGreen());
        								 color2.get(2).setValue(colors2.getBlue());
        								 color2.get(3).setValue(colors2.getAlpha());
        								 calculate2.setBackground(colors2);
        							}catch(Exception e){
        							}
        								
        						}
        					});
        					save.addActionListener(new ActionListener() {
        						public void actionPerformed(ActionEvent arg0) {
        							try{
        								
         								ByteBuffer replace = ByteBuffer.allocate(4);
        								if(offsets.size() == 0){
        									if((object.get(chunk).data[96] & 0xFF)== -1 ||
        											(object.get(chunk).data[96] & 0xFF)== 0){
        										if((object.get(chunk).data[10] & 0xFF)== 0){
        											replace.putInt(Integer.reverseBytes(0));
        										}
        									}
        								}else{
        									if(object.get(chunk).data[104+offsets.get(0)*4+44] == 1){
        										replace.put(object.get(chunk).data[104+offsets.get(0)*4+44+8+offsets.get(1)*4+32]);
        										replace.put(object.get(chunk).data[104+offsets.get(0)*4+44+8+offsets.get(1)*4+33]);
        										replace.put(object.get(chunk).data[104+offsets.get(0)*4+44+8+offsets.get(1)*4+34]);
        										replace.put(object.get(chunk).data[104+offsets.get(0)*4+44+8+offsets.get(1)*4+35]);
        									}else{
        										replace.put(object.get(chunk).data[(104+offsets.get(0)*4+32)]);
        										replace.put(object.get(chunk).data[(104+offsets.get(0)*4+32+1)]);
        										replace.put(object.get(chunk).data[(104+offsets.get(0)*4+32+2)]);
        										replace.put(object.get(chunk).data[(104+offsets.get(0)*4+32+3)]);
        									}
        								}
        								
        								ByteBuffer bb = ByteBuffer.allocate(96);
        								
        								bb.putInt(Integer.reverseBytes((int)id.getValue()));
        								byte[] hashnametest = hexStringToByteArray(fhash.getText());
        								for(int b = hashnametest.length-1; b>=0 ; b--){
        									bb.put(hashnametest[b]);
        								}
        								bb.putInt(Integer.reverseBytes((int)id2.getValue()));
        								for(int i = 0; i<4; i++){
        									bb.putInt(Integer.reverseBytes((int)color.get(i).getValue()));
        								}
        								for(int i = 0; i<4; i++){
        									bb.putInt(Integer.reverseBytes((int)color2.get(i).getValue()));
        								}
        								hashnametest = hexStringToByteArray(shash.getText());
        								for(int b = 0; b<hashnametest.length; b++){
        									bb.put(hashnametest[b]);
        								}
        								for(int i = 0; i<3 ; i++){
        									bb.putInt(Integer.reverseBytes((int)value.get(i).getValue()));
        								}
        								for(int i = 0; i<3 ; i++){
        									bb.putInt(Integer.reverseBytes((int)value1.get(i).getValue()));
        								}
        								for(int i = 0; i<3 ; i++){
        									bb.putInt(Integer.reverseBytes((int)value2.get(i).getValue()));
        								}
        								hashnametest = hexStringToByteArray(thash.getText());
        								for(int b = 0; b<hashnametest.length; b++){
        									bb.put(hashnametest[b]);
        								}
        								
        								bb.putInt(Integer.reverseBytes((int)value3.get(0).getValue()));
        								bb.putInt(Integer.reverseBytes((int)value3.get(1).getValue()));
        								
        								ByteArrayOutputStream temp = new ByteArrayOutputStream();
        								
        								temp.write(bb.array());
        								
        								/*for(int j = 0; j<data.size();j++){
        									temp.write(data.get(j));
        								}*/
        								temp.write(Arrays.copyOfRange(object.get(chunk).data, 96, object.get(chunk).data.length));
        								
        								byte[] tempArray = temp.toByteArray();
        								
        								calculateDiff(tempArray, chunk);
        								
        								if(offsets.size() == 0){
        								materialhash(object.get(chunk).data,replace.array(),0, false);
        								}else{
        									materialhash(object.get(chunk).data,replace.array(),offsets.get(0), false);	
        								}
        								
        								
        								byte[] names = null;
        		        				int hashindex;
        		        				for(int c = 0; c<object.size(); c++){
        		        					if(object.get(c).type == 1002 ){
        		        						hashindex = hashname_old.indexOf(
        		        						""+String.format("%02x", object.get(c).data[42])
        		        						+""+String.format("%02x", object.get(c).data[43])
        		        						+""+String.format("%02x", object.get(c).data[44])
        		        						+""+String.format("%02x", object.get(c).data[45]));
        		        						if(hashindex != -1){								
        		        						    int len = hashname_new.get(hashindex).length();
        		        						    names = new byte[len / 2];
        		        						    for (int i = 0; i < len; i += 2) {
        		        						        names[i / 2] = (byte) ((Character.digit(hashname_new.get(hashindex).charAt(i), 16) << 4)
        		        						                             + Character.digit(hashname_new.get(hashindex).charAt(i+1), 16));
        		        						    }
        		        							object.get(c).data[42] = names[0];
        		        							object.get(c).data[43] = names[1];
        		        							object.get(c).data[44] = names[2];
        		        							object.get(c).data[45] = names[3];
        		        						}
        		        					}
        		        				}
        		        				if(names == null){
		        						    int len = hashname_new.get(hashname_new.size()-1).length();
		        						    names = new byte[len / 2];
		        						    for (int i = 0; i < len; i += 2) {
		        						        names[i / 2] = (byte) ((Character.digit(hashname_new.get(hashname_new.size()-1).charAt(i), 16) << 4)
		        						                             + Character.digit(hashname_new.get(hashname_new.size()-1).charAt(i+1), 16));
		        						    }
        		        				}
        		        					object.get(chunk).data[84] = names[0];
        		        					object.get(chunk).data[85] = names[1];
        		        					object.get(chunk).data[86] = names[2];
        		        					object.get(chunk).data[87] = names[3];

        		        				
        		        				StringBuilder sb = new StringBuilder(names.length * 2);
        		        					      sb.append(String.format("%02x", names[0] & 0xff));
        		        					      sb.append(String.format("%02x", names[1] & 0xff));
        		        					      sb.append(String.format("%02x", names[2] & 0xff));
        		        					      sb.append(String.format("%02x", names[3] & 0xff));
        		        				
        		        				thash.setText(sb.toString());
        		        				sb.setLength(0);
        		        				hashname_old.clear();
        		        				hashname_new.clear();
        		        				
        		        				savefile();		
        							}catch(Exception e){
        								e.printStackTrace();
        							}
        						}
        					});
        		       }else if(object.get(chunk).type == 20001){
        		        	d.setSize(420,315);
        		        	
        		        	ArrayList<JSpinner> translate = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> value = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> integer = new ArrayList<JSpinner>();
        		        	JSpinner idk = new JSpinner();
        		        	JTextField hash = new JTextField();
        		        	JComboBox<String> name = new JComboBox<String>();
        		        	name.setModel(new DefaultComboBoxModel<String>(new String[]{"1FlGlDr4","1doorunit","1drawerunit","2FlSecDDrFrm","2FlSecDr3","2doorunit","3FlDDrFrm","3Seater_Sofa_1","3mfir_tree","4FlDr5","4Post_Lift","6mfir_tree","7mDouglasfir_tree","7m_tree_bare","AirCondSys_Small","Alarum","Alecrate","AlligatorSuitcase","ArcadeMachine","ArclightsSkeleton","Armchair","Armour_Ring","Army_Chair","Army_Double_Door","Army_Single_Door","Army_Sink","Army_Sink1","Army_Slide_Door","Army_Wardrobe","ArtDrawers","AshTray","Atomic_Generator","BBQ","BabePosters","Bar_Seat","Bar_Stool_1","Bar_Table","Bar_tablelamp_50","Bar_tablelamp_80","BareBulb","Barrier_1","Base_Lift","BaseballBat","BaseballPosters","BasementLight","BasementLight_1","BasketBall_outside","Bath","Bath_Water","Bathroom_Slide_Door","Bathroom_Towel_Rail","Beachball","BedRoom_Sink","BedSideTable_2","Bed_CurtRail_1","Bed_CurtRail_2","Bed_Table_1","Bedside_Metal_Table","Bedside_Metal_Table9","BeerCan2","BenchDesk","Bicycle","BigDeskDrawers","BigMirror","BigRug","BigWallShelf","Big_Coffee_Table_1","Big_Desk","Big_Sink","Big_TableLamp_50","Bigcupboard","Bilge_Pump","Billiard","Bin","Blackbox","Blue_Alarum","Blue_Rug","BoardingPoster","BoatTrailer","Boiler","BoilerTank","BonsaiTree","BookShelves","Book_2","BoomBox","Bottle_1","Bottle_2","Bottle_3","Bottle_4","Bottle_5","Bottle_6","BrainInJar","BreakingWall","BreakingWall_1","Bridge_Chair","BrokenAerial","BrokenBicycle","Broom","BsDrFrm7","Bucket","Bunk_Bed_1","Busy_Table_1","CV_Armchair","CV_BigFireplace","CV_BunkBeds","CV_Crate","CV_DoubleBed","CV_OldSgBed","CV_Oven","CV_Plate","CV_Rug","CV_Shower","CV_SingleBed","CV_SmallFireplace","CV_SmallTable","CV_Sofa","CV_SpotLights","CV_Stool","CV_SunChair","CV_TableLamp_50","CV_TableLamp_80","CV_WineRack","CV_WoodenChair","CV_doublebed","CabinetSink_1","CabinetSink_2","Can","Candleabra_80","Candlestick_80","Car_Lift","CardBoardBox","CardBoardBox_2","Cardboard_Box_1","CashRegister","CeilingLight","CeilingLightRose","Cement_Bag","ChatteringTeeth","Cheese_Box","Chest_Freezer","Chevette1","ChocoBreakBox","Chocolate_box","ChoppingBoard","Class_Chair","Cloths_Hanger","Cobweb","CoffeeMaker","Coke_Machine_1","Com_Cord","Combi_Oven","ComedyHammer","Computer_Script","Cookpot","CordlessPhoneBase","CordlessPhone_receiver_1","CordlessPhone_reciever","CornerBenches","CornerCabinet","CosmeticBottle1","CosmeticBottle2","CosmeticBottle3","Crate","Crate_1","Cucumber_Box","Curved_Sinks","CuttingBoard","CyclingPoster","DI_GlazedDoor","DND_Light","DeskLamp_80","DeskPictureFrame_50","DeskPictureFrame_80","DeskSet","DeskTopPC","DeskTopPC1","DeskTopPC2","DeskTopPC3","DeskTopPC4","DeskTopPC5","DeskTopPC6","DeskTopPC7","Desk_1","Desk_2","Deskspot_80","DiningTable","Dining_Chair_1","Dining_Door1","Dining_Plate","Dining_Room_Table","Dining_Serving_Table_1","Dining_Train_Table","Dome_Light","DomesticDryer","Door","DoorBell","Door_2","Door_DarkWood","DoorwayBoards","DoubleBed_Shack","DoubleSinks","Drawers","DressStool","DressingTable","DrinkCup","DrinkCupStack","Drip","DrugAbusePoster","DullBook","Dumbell","Dustbin","ESD","Easel_no_pic","ElectricChair","ElectricalBox","ElectricalBox1","ElectricalBox2","EngineBlock","Engine_Door_1","Engine_Levers_1","Engine_Seat","Engine_Seat2","Engineroom_Door","EntranceDoorSet1","EquipmentCase","Ether_Bomb","Exam_Bed_1","ExaminationLight","ExaminationTable","Examination_table_1","ExtGlazedDoor","FH_BathroomMirror","FH_BathroomMirror1","FH_BedSideTable","FH_Box","FH_Drawers","FH_FirePlace","FH_IntDoor","FH_KitchenUnits","FH_Locker","FH_PizzaBox","FH_SingleBed","FH_SingleBed1","FH_SingleBed4","FH_Sofa","FH_WallShelf","Fan_Window","FilingCabinet","FilingCabinet_metal","FilthyBath","FireExtinguisher","FirePoker","FireplaceLogs","FireplaceLogs_1stFStorage","FireplaceLogs_2ndF_Bed4","FireplaceLogs_Grnd_Family","FireplaceLogs_Grnd_Lounge1","FlakesBox","Flash_Push_Point","Flee","FloorLamp","Flour_Bag","FlowerBox","FlowerQuads","FlowersVase","FluorecentLight","FoldedStepLadder","Food_cupboard_conts","Foot_Locker","Fork","Formaldehyde","Fountain","FreeWeights","Fridge_white","FrontDoor","Frying_Pan","GB_ward_generator","Galley_Hob","GamesConsole","GarageDoor","GarageDoor_2","GarbageBin","GardenBench","Gas_Trolley","Gas_Trolley_9","GbraA","GbraB","GbraC","GdesB","GdesD","GdesE","GdesF","GdesG","Gener_Panel","Gener_Panel_1","Gener_Panel_2","Generator","Generator_Switch","Ghost_Cage","Ghost_Making_Machine_1","Glass","Glass_Jug","GlazedDoor","Glazed_Door","Goth_painting","Gothic_giant_Boiler","Gp_Chair_1","Gp_Desk_1","GrFlDDr","GrFlDDrFrm7","GrFlDr3","GrFlLiftDoor3","Grain_Sack","Green_Rug","Half_Dining_Table","Half_Tone_Pic","HandDryer","HandDryer1","HandDryer2","Hangar_Lift","HatStand","Head_Rug","Head_Rug_Small","Headphones","Health_n_Safety","Heart_Bed_1","HeavySurfBoard","Heli_Lift","Helm_Controls","HiFi","Hidden_Flap","Hidden_lift","Hosp_Bath","Hosp_toilet_1","HumanBodyPoster","IndustrialDryer","Inner_Door_1","IntDoor1_111","IntGlazedDoor","Iris_door","Ironpress","JailDoor","JailDoor1","JailDoor2","JailDoor3","JailDoor4","Jeep","JerryCan1","JeweleryBox","Keypad","KitchenBin","Knife","LCD_Monitor_1","LR_Computer","LR_Sink","LR_WoodenChair1","LR_WoodenChair2","LR_WoodenChair3","LR_WoodenChair4","LR_WoodenChair5","LR_WoodenChair6","LR_WoodenChair7","LampPost_victorian","Laptop","LargeWallShelves","Laundry_Basket","LeatherSofa1","Letter","Lettice_Box_1","Life_Ring","Lift_Motor_1","Lift_Turn","LightSwitch","Light_Guard","Log_single","Lounge_Doors","LoveRug","MDL-BigMirror1","MDL-BigMirror2","MDL-Blackbox","MDL-CV_OldSgBed","MDL-FireplaceLogs","MDL-FireplaceLogs1","MDL-MaxineSkeleton","MDL-Particle1","MDL-Particle2","MDL-Particle3","MDL-bear_trap","MDL-staghead","MSH-BackYard","MSH-Backdoor","MSH-Basement_Boiler_room","MSH-Basement_Corridor","MSH-Basement_Generator_Room","MSH-Basement_Stairs","MSH-Blend1","MSH-BriefingRoom","MSH-Canteen_Table_Room","MSH-Canteen_pool_Room","MSH-Cell1","MSH-Cell2","MSH-Cell3","MSH-Cell4","MSH-CellCorridor1","MSH-CellCorridor1b","MSH-CellCorridor2","MSH-Corridor1","MSH-EmergencyExit","MSH-EvidenceCounter","MSH-EvidenceRoom","MSH-Floor1_9","MSH-Floor1_Left_Garden","MSH-Floor1_Left_Stairs","MSH-Floor1_Left_Stairs_Big","MSH-Floor1_Left_Stairs_from_base","MSH-Floor1_Right_Garden","MSH-Floor1_Right_Stairs","MSH-Floor2_4","MSH-Floor2_Left_Stairs_Big","MSH-Flr2_Bathroom_1","MSH-Flr2_Bedroom_1","MSH-Flr2_Bedroom_2","MSH-Flr2_Corridor_long","MSH-Flr2_Small_Corridor_1","MSH-Flr2_Small_Corridor_2","MSH-Flr2_storeroom_1","MSH-Flr2_storeroom_2","MSH-Flr3_All","MSH-Foyer","MSH-Ground3","MSH-Ground4","MSH-InterviewRoom","MSH-LieutenantsOffice","MSH-LineupRoom","MSH-MainOffice","MSH-Morgue","MSH-MorgueLockers","MSH-MorgueStorage","MSH-ObservationRoom","MSH-ProcessingRoom","MSH-RadioRoom","MSH-RearEntrance","MSH-Reception","MSH-RoofTop","MSH-Seance_Room","MSH-Stairs1A","MSH-Stairs1B","MSH-Storage1","MSH-Street1","MSH-Street2","MSH-Street3","MSH-Street4","MSH-Toilet1","MSH-Toilet2","MSH-WitnessRoom","MSH-ignore_stairs_hall","MSH-poly10","MSH-poly12","MSH-poly13","MSH-poly14","MSH-poly15","MSH-poly16","MSH-poly17","MSH-poly18","MSH-poly3_4","MSH-poly4","MSH-poly5_1","MSH-poly6_4","MSH-poly7","MSH-poly8","MSH-poly9","MadLabStuff_1","Mad_Tooltrl_1","Mad_Tooltrl_2","Mad_Tooltrl_3","Mail_Cage_Door","Mail_Clock","Mail_Letter","Mail_Outer_Door","Mail_Stool","MakeUpCase","Map","Map_2","Master_Bath_Sink","Master_Bedroom_Door","MaxineSkeleton","Medical_Monitor_1","Medical_Monitor_2","MetalBox","Metal_Door_1","Metal_Fridge_2","Metal_Table_1","Metal_Wood_Door_1","Mic_Stand","Microwave","Micstand2","Mirror","Mirror_2","Mop","MorgeLight_1","MorgeMachine_1","MorgeMachine_2","MorgeMachine_3","MorgeSpotLght_1","Morge_Bench","Morge_Bench_2","MorgueCoolerSlab","MorgueCounter","MoviewnNameFrame","NoticeBoard","O2Cylinder","Oar","ObjectInJar","OfficeChair","OfficeChair1","OfficeChair2","Oil_Drum","OldIntDoor","OldShelves","OldSideTable","Old_Chair_1","Old_Chair_2","Old_aga","OrangeJuice","Organ","Outer_Door","Outer_Door_1","OvalMirror","PC_Rack","PCcase","PCkeyboard","PCmonitor","PS_BookShelves","PS_Door","PS_Drawers","PS_LampPost","PS_Lectern","PS_MetalDoor","PS_RadioMast","PS_ReceptionDesk","PS_Table","Painting","PaintingBigFrm","PaintingBigFrm4","PickAxe","PinBallMachine","PinUpPoster","PineTree","Pirates_chest","Plasam_Shower","PlasticChair","PlasticChair3","PlasticChair4","PlasticChair5","PlasticChair6","Platter","PlugSockets","PoliceCar","Port_Winch","PortableGenerator","PortableGenerator_Yellow","PotPlant","PotPlantBig","Power_Box","PrisonBench","PrisonToilet","PunchBag","REEL2REEL","Radiator","RadiatorSmall","Radio_80_High","Recept_Table","Red_Alarum","ResortStove","RoadSignPoster","RoundRug","Round_Chair_1","RoundcoffeeTable","Rusty_Trolley","Rusty_shelves_1","SH_ArmChair","SH_BedSideTable","SH_CeilingLight","SH_CoatHooks","SH_Cosmetics1","SH_Cosmetics2","SH_Drawers","SH_DressTable","SH_FloorLight","SH_LaundryTable","SH_Magazine","SH_PosterFaces","SH_PosterHeart","SH_SingleBed","SH_SmallBench","SH_Sofa","SH_SofaArmChair","SH_Wardrobe","Sail_Bag","Saucepan","Saw","Sec_Chair_1","Sec_Locker_1","Sec_Table","Security_Mon_1","Shack_Shelf","Shack_bunk_bed","Ship_Boiler","Ship_Pic_1","Ship_Pic_2","Ship_Pic_3","Ship_Pic_4","Ship_Pistons","Ship_door_1","Shower","Shower_Bits","Sickbed_3","Sickbed_4","SideLamp_50","SideLamp_80","Sideboard","Single_Bed_2","Sink_1","Sink_Unit_1","Sink_n_WorkBench","Sink_unit","SitOnLawnMower","SmallFlags","SmallTable","SmallTable_1","Small_Coffee_Table_1","Small_Round_Table","Sofa","Sofa_2","Sofa_3","Soup_Can","SpacePosters","Sportsbag","SquareVase","StaffSofa","StaffTable","Staghead","Standard_Train_Table","StarRug","Star_Winch","StaticsBones","StayKold","Storage_Box","Suitcase","Suitcase_force_field","Sun_Machine_1","Switchbox","Swivel_Chair_1","TV_Stand","TVbig","TVsmall","TableLampNoShade_80","TanoySpeaker","TeaTray","TelegraphPole","Thick_book","Tissues3","Toilet","ToiletDoor","ToiletDoor_2","ToiletMat","Toilet_Roll","Tom_Box","ToolBox","Tool_Box_1","Topsey_Table","Topsy_Light","Torch","TowelRail","TrafficCone","Trailer1","Trailer_Bluecover","Train_Bathroom_Cabinet_1","Train_Bathroom_Cabinet_2","Train_Bed","Train_Coffee_Machine","Train_Shower","Train_Toilet_1","Train_Towel_Rail_1","TrashCan","Tray_1","Two_Tone_Tan_Door","Urinal","Urinal_single","VCR","VacuumCleaner","Van_door_left","Van_door_right","Vase","VendingMachine","Ventfan","VictorianArmchair","WallBench","WallLamp","Wall_OilLamp","War_Picture_1","War_Picture_2","War_Picture_3","Ward_small_table","Wardrobe","WarningPoster","WaterDispenser","WeightBench","WelcomeMat","Wheel_Chair","WineBottle","WineRack","WoodenChair1","WoodenDoor","Wooden_Crate_1","Wooden_Crate_2","Wooden_Crate_3","Wooden_Door_1","WorkTable","Work_Bench_1","World_Chair","World_Glass_Table2","World_Glass_Table_Small","World_Map","World_Screen","XXharp","YellowWard","Zebra_pic","abstractpainting","aircon_switch","alarm_clock","alter","anglepoise_80","anthmask","ash","asylum_cell_door","axe","axe_pickup","balloon","barndoors","barndoorsmall","barrel","bath_rug","bath_script","bear_trap","big_gun","bilge_water","blair_islandtree","bloodyBin","bodybag","boiler","boiler_script","bookcase","bouquet","brain_teleporter","brazier","brick","bunny","campfire","canopy_bed","cardboard_box_opens","cash","cbradio","cd","cell_switch","cellar_soil","chair_script","chatteringteeth","chimney_smoke1","chimney_smoke2","chimney_smoke3","chimney_smoke4","chimney_smoke5","chips","chopping_block","coffeevendor","coffin","cue","curtain","curtain_script","dirty_toilet","dollar_bill","domestic_dryer","door_switch","door_switch1","doorbell","dreamcatcher","dresser","dressing_table","drying_rack","engine_generator","exploding_shack","fallingtree_trunk","farmdoor","floorboards2_only","free_weights","fridge_script","fruit_bowl","fruitbowl","gambling_table","garden_bench","gb_vandoor_l","gb_vandoor_r","generic_plant_script","ghostbreakers_van","giant_candlestick","giant_hourglass","goldfish_bowl","goth_painting2","goth_uprightchair","gothicCloset","gothic_bath","gothic_dining_chair","gothic_dressingtable","gothic_heater","gothic_long_rug","gothic_mirror","gothic_rug","gothic_rug1","gothic_sink","gothic_wardrobe","gothickitchen_table","gran_picture","grave","grave_hump","hammock","hand_in_jar","handycam","hanging_light","harp","harppiano_seat","hearthFire","high_tech_rod","hive","hospital_symbol","ice_layer","industrial_freezer","kennel","key","lab_apparatus","light_switch","log_pile","meat_cleaver","medi_skeleton","medical_monitor","metalShelves","metaldoor","microwave_script","milldoor","miners_lamp","mirror_script","monument_grave","moose_bones","movingtree","necronomican","nurse_chart","openStepLadder","operation_lamp","package","padlock","painting_landscape","painting_script","pallet","piano","pillow","pinballtable","pitchfork","plant_pot","plate","police_car_lights","portrait","pot_of_earth","puddle2","puddle_script","pumpkinhead","quilt_l","quilt_r","rattrap","rattrap1","reading_stand","red_ball","red_cushion","redbox","rifle","river","rock","rocker","roulette_wheel","round_occasionaltable","rug_large","rustic_oillamp_50","rustic_oillamp_80","secret_library_door","security_cam","security_monitor","servants_bed","sh_bridge","shack_chair","shack_table","sickle","sidetable_1","single_winerack","sinkunit1","small_gas_cylinder","spooky_tree","spotlight","staghead","staticbones","staticbones_middle","staticbones_top","station","strange_fragile_object","stump","swat_rifle","tall_table","telephone_script","top_cupboard","toploader","totem","towel_rail","tpoles","traffic_lights","trapdoor_left","trapdoor_right","tv_script","twigs","tyre","urn","vending_machine","victorian_light","victorian_sink","victorian_toilet","volumelighting_rig","waiting_chair_1","waiting_chair_2","walkman","wallmounted_oillamp","water_cooler","waterpump","weathervane","weathervane_script","white_ball","whitebox","window_pane","winerack_steps","woodernunit","woodwork_bench","work_bench","yell_cushion"}));
        					GridLayout gltranslate = new GridLayout(3,3);
        					gltranslate.setVgap(10);
        		        	
        		        	JPanel translatePanel = new JPanel(gltranslate);
        		        	JPanel mainPanel = new JPanel(new GridLayout(4, 4));
        		        	JPanel integerPanel = new JPanel(new GridLayout(1, 4));
        		        	
        					JButton calculate = new JButton("Calculate");
        					JButton save = new JButton("Save");
        					
        		        	JLabel idLabel = new JLabel("Id :");
        		        	JLabel translateLabel = new JLabel("Translate XYZ:");
        		        	JLabel rotationLabel = new JLabel("Rotation XYZ :");
        		        	JLabel scaleLabel = new JLabel("Scale XYZ :");
        		        	JLabel matrixLabel = new JLabel("Matrix4f T :");
        					
        					
        		        	
        		        	idLabel.setBounds(0, 0, 100, 20);
        		        	translateLabel.setBounds(0, 30, 100, 20);
        		        	rotationLabel.setBounds(0, 62, 100, 20);
        		        	scaleLabel.setBounds(0, 95, 100, 20);
        		        	matrixLabel.setBounds(0, 128, 100, 20);
        		        	
        					idk.setBounds(110, 0, 50, 20);
        					translatePanel.setBounds(110, 30, 300, 90);
        					mainPanel.setBounds(110, 130, 300, 92);
        					integerPanel.setBounds(110, 232, 300, 20);
        					name.setBounds(110, 262, 300, 20);
        					calculate.setBounds(195, 0, 100, 20);
        					save.setBounds(330, 0, 80, 20);
        		        	
        					for(int i = 0; i<9; i++){
        						translate.add(new JSpinner());
    							translate.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							translate.get(i).setEditor(new JSpinner.NumberEditor(translate.get(i),"0.00000"));
        						translatePanel.add(translate.get(i));
        					}
        					
        					for(int i = 0; i<16; i++){
        						value.add(new JSpinner());
        					    value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.0000000"));
        						mainPanel.add(value.get(i));
        					}
        					integer.add(new JSpinner());
        					integerPanel.add(integer.get(0));
        					integerPanel.add(hash);
        					integer.add(new JSpinner());
        					integerPanel.add(integer.get(1));
        					integer.add(new JSpinner());
        					integerPanel.add(integer.get(2));
        		        	
        					
        					
        					d.getContentPane().add(idLabel);
        					d.getContentPane().add(translateLabel);
        					d.getContentPane().add(rotationLabel);
        					d.getContentPane().add(scaleLabel);
        					d.getContentPane().add(matrixLabel);
        					d.getContentPane().add(mainPanel);
        					d.getContentPane().add(integerPanel);
        					d.getContentPane().add(calculate);
        					d.getContentPane().add(save);
        					d.getContentPane().add(idk);
        					d.getContentPane().add(translatePanel);
        					d.getContentPane().add(name);
        					
        		        	int off = 0;
        		        	
        					idk.setModel(new SpinnerNumberModel(0, null, null, 1));
        					idk.setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        		        	
        		        	for (int i_2 = 0; i_2<12;i_2+=4){
        						for(int i = 0;i<3;i++){
        								value.get(i_2+i).setValue(toFloat(object.get(chunk).data,off));
        								off+=4;
        						}
        		        	}
        					value.get(12).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					value.get(13).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					value.get(14).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					
        					value.get(3).setValue(0.0);
        					value.get(7).setValue(0.0);
        					value.get(11).setValue(0.0);
        					value.get(15).setValue(1.0);
        					
        					integer.get(0).setModel(new SpinnerNumberModel(0, null, null, 1));	
        					integer.get(0).setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        						
        					hash.setText(makeString(object.get(chunk).data,off,4));
        					hash.setCaretPosition(0);
        					off+=4;
        						
        					integer.get(1).setModel(new SpinnerNumberModel(0, null, null, 1));
        					integer.get(1).setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					
        					int namecounter = toInteger(object.get(chunk).data,off);
        					
        					integer.get(2).setModel(new SpinnerNumberModel(0, null, null, 1));
        					integer.get(2).setValue(namecounter);
        					off+=4;
        					
        					StringBuilder builder = new StringBuilder();
        					if(namecounter != 0){
        						for(int i = off; i<off+namecounter; i++){
            			        	builder.append((char)object.get(chunk).data[i]);
        						}
        						name.setSelectedItem(builder.toString());
        					}
        						
        					//translate
            				translate.get(0).setValue(new Float((Float)value.get(12).getValue()));
            				translate.get(1).setValue(new Float((Float)value.get(13).getValue()));
            				translate.get(2).setValue(new Float((Float)value.get(14).getValue()));
            					
            				translate.get(3).setValue((float) (Math.atan(-(Float)value.get(9).getValue()/(Float)value.get(10).getValue()) * (180/Math.PI)));
            				translate.get(4).setValue((float) (Math.asin((Float)value.get(8).getValue()) * (180/Math.PI)));
            				translate.get(5).setValue((float) (Math.atan(-(Float)value.get(4).getValue()/(Float)value.get(0).getValue()) * (180/Math.PI)));
            					
            				translate.get(6).setValue((float)Math.sqrt(Math.pow((Float)value.get(0).getValue(), 2)+ Math.pow((Float)value.get(1).getValue(), 2) + Math.pow((Float)value.get(2).getValue(), 2)));
            				translate.get(7).setValue((float)Math.sqrt(Math.pow((Float)value.get(4).getValue(), 2)+ Math.pow((Float)value.get(5).getValue(), 2) + Math.pow((Float)value.get(6).getValue(), 2)));
            				translate.get(8).setValue((float)Math.sqrt(Math.pow((Float)value.get(8).getValue(), 2)+ Math.pow((Float)value.get(9).getValue(), 2) + Math.pow((Float)value.get(10).getValue(), 2)));		

        					calculate.addActionListener(new ActionListener() {
        						public void actionPerformed(ActionEvent arg0) {
        							try{
        								matrix_mult(translate,value);
        							}catch(Exception e){
        								e.printStackTrace();
        								//JOptionPane.showMessageDialog(null,"Ups... Something goes wrong!");
        							}
        						}
        					});				
        					save.addActionListener(new ActionListener() {
        						public void actionPerformed(ActionEvent arg0) {
        							try{
        								int integ;
        								integer.get(2).setValue(name.getSelectedItem().toString().length());
        								ByteBuffer bb = ByteBuffer.allocate(68 + (int)integer.get(2).getValue());
        									
        								bb.putInt(Integer.reverseBytes((int)idk.getValue()));
        									
        									
        						        for (int i_2 = 0; i_2<12;i_2+=4){
        									for(int i = 0;i<3;i++){
        										integ =  Float.floatToIntBits((float)value.get(i_2+i).getValue()); 
        											bb.putInt(Integer.reverseBytes(integ));
        										}
        						        	}
        									integ =  Float.floatToIntBits((float)value.get(12).getValue()); 
        									bb.putInt(Integer.reverseBytes(integ));
        									integ =  Float.floatToIntBits((float)value.get(13).getValue()); 
        									bb.putInt(Integer.reverseBytes(integ));
        									integ =  Float.floatToIntBits((float)value.get(14).getValue()); 
        									bb.putInt(Integer.reverseBytes(integ));

        									bb.putInt(Integer.reverseBytes((int)integer.get(0).getValue()));
        									byte[] hashnametest = hexStringToByteArray(hash.getText());
            								for(int b = 0; b<hashnametest.length; b++){
        										bb.put(hashnametest[b]);
        									}
        									bb.putInt(Integer.reverseBytes((int)integer.get(1).getValue()));
        									bb.putInt(Integer.reverseBytes(name.getSelectedItem().toString().length()));
        									
        									if((int)integer.get(2).getValue() != 0){
        	        							byte[] names = name.getSelectedItem().toString().getBytes(); 
        	        							for(int n = 0;n<names.length; n++){
        	        								bb.put(names[n]);
        	        							}
        									}
        									calculateDiff(bb.array(),chunk);
        									modified = true;
        	        						frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");
        	        						loadTree();
        	        						savefile();
        								}catch(Exception e){
        									System.err.println(e);
        								}
        							}
        						});
        				
        				
        				}else if(object.get(chunk).type == 1026){	       		        	
        		        	d.setSize(430,313);
        		        	JSpinner id = new JSpinner();
        		        	JTextField fhash = new JTextField();
        		        	ArrayList<JSpinner> test = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> translate = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> value = new ArrayList<JSpinner>();
        		        	JTextField shash = new JTextField(); 
        		        	JSpinner id2 = new JSpinner();
        		        	
        		        	
        					GridLayout gltranslate = new GridLayout(3,3);
        					gltranslate.setVgap(10);
        		        	
        		        	JLabel id_name = new JLabel("? :");
        		        	
        		        	JLabel Bounds = new JLabel("Bounds :");
        		        	JLabel translateLabel = new JLabel("Translate XYZ:");
        		        	JLabel rotationLabel = new JLabel("Rotation XYZ :");
        		        	JLabel scaleLabel = new JLabel("Scale XYZ :");
        		        	JLabel matrixLabel = new JLabel("Matrix4f :");
        		        	JLabel idk = new JLabel("Joint Type :");
        		        	
        		        	JPanel idPanel = new JPanel(new GridLayout(1, 2));
        		        	JPanel idPanel2 = new JPanel(new GridLayout(1, 2));
        		        	JPanel testPanel = new JPanel(new GridLayout(1,3));
        		        	JPanel translatePanel = new JPanel(gltranslate);
        		        	JPanel valuePanel = new JPanel(new GridLayout(4,4));
        		        	
        		        	
        		        	id_name.setBounds(0, 0, 100, 20);
        		        	Bounds.setBounds(0, 30, 100, 20);
        		        	translateLabel.setBounds(0, 60, 100, 20);
        		        	rotationLabel.setBounds(0, 90, 100, 20);
        		        	scaleLabel.setBounds(0, 120, 100, 20);
        		        	matrixLabel.setBounds(0, 150, 100, 20);
        		        	idk.setBounds(0, 262, 100, 20);
        		        	
        		        	idPanel.setBounds(120, 0, 300, 20);
        		        	testPanel.setBounds(120, 30, 300, 20);
        		        	translatePanel.setBounds(120, 60, 300, 90);
        					valuePanel.setBounds(120, 160, 300, 92);
        		        	idPanel2.setBounds(120, 262, 300, 20);
        		        	
        		        	
        		        	id.setModel(new SpinnerNumberModel(0, null, null, 1));
        					idPanel.add(id);
        					idPanel.add(fhash);
        		 
        					for(int i= 0; i<3; i++){
        					    test.add(new JSpinner());
        					    test.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
        						testPanel.add(test.get(i));
        					 }	
        						
        			       for(int i= 0; i<9; i++){
        			        	translate.add(new JSpinner());
    							translate.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							translate.get(i).setEditor(new JSpinner.NumberEditor(translate.get(i),"0.00000"));
        						translatePanel.add(translate.get(i));
        			        }
        		        	
        		        	for(int i= 0; i<16; i++){
        		        		value.add(new JSpinner());
        					    value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.0000000"));
        						valuePanel.add(value.get(i));
        		        	}
        		        	
        		        	id2.setModel(new SpinnerNumberModel(0, null, null, 1));
        		        	idPanel2.add(shash);
        		        	idPanel2.add(id2);
        		        	
        		        	d.getContentPane().add(idPanel);
        		        	d.getContentPane().add(testPanel);
        		        	d.getContentPane().add(translatePanel);
        		        	d.getContentPane().add(valuePanel);
        		        	d.getContentPane().add(idPanel2);
        		        	
        		        	d.getContentPane().add(id_name);
        		        	d.getContentPane().add(Bounds);
        		        	d.getContentPane().add(translateLabel);
        		        	d.getContentPane().add(rotationLabel);
        		        	d.getContentPane().add(scaleLabel);
        		        	d.getContentPane().add(matrixLabel);
        		        	d.getContentPane().add(idk);
        		        	
        		        	
        		        	int off = 0;
        		        	
        					id.setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					fhash.setText(makeString(object.get(chunk).data,off,4));
        					fhash.setCaretPosition(0);
        					off+=4;
        					for(int i = 0;i<3;i++){
        						test.get(i).setValue(toFloat(object.get(chunk).data,off));
        						off+=4;
        				}
        					
        					int help = 0;
        						for(int i = 0;i<12;i++){
        								if(i == 3){
        									help = 9;
        								}else if(i == 7){
        									help = 6;
        								}else if(i == 11){
        									help = 3;
        								}
        								value.get(i+help).setValue(toFloat(object.get(chunk).data,off));
        								off+=4;
        								help = 0;
        						}
        						value.get(3).setValue(0.0);
        						value.get(7).setValue(0.0);
        						value.get(11).setValue(0.0);
        						value.get(15).setValue(1.0);
        						
        						String temp = makeString(object.get(chunk).data,off,4);
        						if(bonehash.indexOf(temp) != -1){
        							shash.setText(bonename.get(bonehash.indexOf(temp)));
        						}else{
        							shash.setText(temp);
        						}

        						shash.setCaretPosition(0);
        						off+=4;
        						
        						
        					id2.setValue(toInteger(object.get(chunk).data,off));
        					
        					translate.get(0).setValue(new Float((Float)value.get(12).getValue()));
        					translate.get(1).setValue(new Float((Float)value.get(13).getValue()));
        					translate.get(2).setValue(new Float((Float)value.get(14).getValue()));
        					
        					translate.get(3).setValue((float) (Math.atan(-(Float)value.get(9).getValue()/(Float)value.get(10).getValue()) * (180/Math.PI)));
        					translate.get(4).setValue((float) (Math.asin((Float)value.get(8).getValue()) * (180/Math.PI)));
        					translate.get(5).setValue((float) (Math.atan(-(Float)value.get(4).getValue()/(Float)value.get(0).getValue()) * (180/Math.PI)));
        					
        					translate.get(6).setValue((float)Math.sqrt(Math.pow((Float)value.get(0).getValue(), 2)+ Math.pow((Float)value.get(1).getValue(), 2) + Math.pow((Float)value.get(2).getValue(), 2)));
        					translate.get(7).setValue((float)Math.sqrt(Math.pow((Float)value.get(4).getValue(), 2)+ Math.pow((Float)value.get(5).getValue(), 2) + Math.pow((Float)value.get(6).getValue(), 2)));
        					translate.get(8).setValue((float)Math.sqrt(Math.pow((Float)value.get(8).getValue(), 2)+ Math.pow((Float)value.get(9).getValue(), 2) + Math.pow((Float)value.get(10).getValue(), 2)));					
        				
        			}else if(object.get(chunk).type == 1001){	
        		        	d.setSize(847,290);
        		        	ArrayList<JSpinner> translate = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> value = new ArrayList<JSpinner>();
        		        	ArrayList<JTextField> first = new ArrayList<JTextField>();
        		        	ArrayList<JSpinner> translate2 = new ArrayList<JSpinner>();
        		        	ArrayList<JSpinner> value2 = new ArrayList<JSpinner>();
        		        	ArrayList<JTextField> second = new ArrayList<JTextField>();
        		        	JSpinner third_v = new JSpinner();
        		        	ArrayList<JTextField> third = new ArrayList<JTextField>();
        		        	JSpinner fourth = new JSpinner();
        		        	JTextField name = new JTextField();
        		        	
        		        	
        					GridLayout gltranslate = new GridLayout(3,3);
        					gltranslate.setVgap(10);
        		        	
        		        	JPanel translatePanel = new JPanel(gltranslate);
        		        	JPanel valuePanel = new JPanel(new GridLayout(4,4));
        		        	JPanel firstPanel = new JPanel(new GridLayout(1, 2));
        		        	JPanel translate2Panel = new JPanel(gltranslate);
        		        	JPanel value2Panel = new JPanel(new GridLayout(4,4));
        		        	JPanel secondPanel = new JPanel(new GridLayout(1, 2));
        		        	JPanel thirdPanel = new JPanel(new GridLayout(1, 3));
        		        	JPanel fourthPanel = new JPanel(new GridLayout(1, 2));
        					JLabel translateText = new JLabel("Translate XYZ:");
        					JLabel rotationText = new JLabel("Rotation XYZ:");
        					JLabel scaleText = new JLabel("Scale XYZ:");
        					JLabel matrixText = new JLabel("Matrix4f T:");
        					JLabel translate2Text = new JLabel("Translate XYZ:");
        					JLabel rotation2Text = new JLabel("Rotation XYZ:");
        					JLabel scale2Text = new JLabel("Scale XYZ:");
        					JLabel matrix2Text = new JLabel("Matrix4f T:");
        					
        					translateText.setBounds(0, 0, 100, 20);
        					rotationText.setBounds(0, 30, 100, 20);
        					scaleText.setBounds(0, 60, 100, 20);
        					matrixText.setBounds(0, 100, 100, 20);
        					
        					translate2Text.setBounds(420, 0, 100, 20);
        					rotation2Text.setBounds(420, 30, 100, 20);
        					scale2Text.setBounds(420, 60, 100, 20);
        					matrix2Text.setBounds(420, 100, 100, 20);
        		        	
        		        	translatePanel.setBounds(110, 0, 300, 90);
        					
        		        	valuePanel.setBounds(110, 100, 300, 92);
        					firstPanel.setBounds(110, 202, 300, 20);
        					
        		        	translate2Panel.setBounds(530, 0, 300, 90);
        					value2Panel.setBounds(530, 100, 300, 92);
        					secondPanel.setBounds(530, 202, 300, 20);
        					thirdPanel.setBounds(110, 232, 300, 20);
        					fourthPanel.setBounds(530, 232, 300, 20);
        					
        					for(int i = 0; i<9; i++){
        					    translate.add(new JSpinner());
    							translate.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							translate.get(i).setEditor(new JSpinner.NumberEditor(translate.get(i),"0.00000"));
        						translatePanel.add(translate.get(i));
        					}
        					for(int i = 0; i<16; i++){
        					    value.add(new JSpinner());
        					    value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.0000000"));
        						valuePanel.add(value.get(i));
        					}
        					for(int i = 0; i<2; i++){
        					    first.add(new JTextField());
        						firstPanel.add(first.get(i));
        					}
        					for(int i = 0; i<9; i++){
        					    translate2.add(new JSpinner());
    							translate2.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							translate2.get(i).setEditor(new JSpinner.NumberEditor(translate2.get(i),"0.00000"));
        						translate2Panel.add(translate2.get(i));
        					}
        					for(int i = 0; i<16; i++){
        					    value2.add(new JSpinner());
        					    value2.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value2.get(i).setEditor(new JSpinner.NumberEditor(value2.get(i),"0.0000000"));
        						value2Panel.add(value2.get(i));
        					}
        					for(int i = 0; i<2; i++){
        					    second.add(new JTextField());
        						secondPanel.add(second.get(i));
        					}
        					thirdPanel.add(third_v);
        					for(int i = 0; i<2; i++){
        					    third.add(new JTextField());
        						thirdPanel.add(third.get(i));
        					}
        					    fourth.setModel(new SpinnerNumberModel(values, null, null, step));
        					    fourthPanel.add(fourth);
        					    fourthPanel.add(name);
        					
        					d.getContentPane().add(translateText);
        					d.getContentPane().add(rotationText);
        				    d.getContentPane().add(scaleText);
        					d.getContentPane().add(matrixText);
        					d.getContentPane().add(translate2Text);
        					d.getContentPane().add(rotation2Text);
        				    d.getContentPane().add(scale2Text);
        					d.getContentPane().add(matrix2Text);
        					
        					d.getContentPane().add(translatePanel);
        					d.getContentPane().add(valuePanel);
        					d.getContentPane().add(firstPanel);
        					d.getContentPane().add(translate2Panel);
        					d.getContentPane().add(value2Panel);
        					d.getContentPane().add(secondPanel);
        					d.getContentPane().add(thirdPanel);
        					d.getContentPane().add(fourthPanel);
        					
        					
        					int off = 0;
        					
        		        	for (int i_2 = 0; i_2<12;i_2+=4){
        						for(int i = 0;i<3;i++){
        								value.get(i_2+i).setValue(toFloat(object.get(chunk).data,off));
        								off+=4;
        						}
        		        	}
        					value.get(12).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					value.get(13).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					value.get(14).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					
        					value.get(3).setValue(0.0);
        					value.get(7).setValue(0.0);
        					value.get(11).setValue(0.0);
        					value.get(15).setValue(1.0);
        					
        					for(int i = 0; i<2; i++){
        					 first.get(i).setText(makeString(object.get(chunk).data,off,4));
        					 first.get(i).setCaretPosition(0);
        					 off+=4;
        					}
        					
        		        	for (int i_2 = 0; i_2<12;i_2+=4){
        						for(int i = 0;i<3;i++){
        								value2.get(i_2+i).setValue(toFloat(object.get(chunk).data,off));
        								off+=4;
        						}
        		        	}
        					value2.get(12).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					value2.get(13).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					value2.get(14).setValue(toFloat(object.get(chunk).data,off));
        					off+=4;
        					
        					value2.get(3).setValue(0.0);
        					value2.get(7).setValue(0.0);
        					value2.get(11).setValue(0.0);
        					value2.get(15).setValue(1.0);
        					
        					for(int i = 0; i<2; i++){
        						 second.get(i).setText(makeString(object.get(chunk).data,off,4));
        						 second.get(i).setCaretPosition(0);
        						 off+=4;
        						}
        					third_v.setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					for(int i = 0; i<2; i++){
        						third.get(i).setText(makeString(object.get(chunk).data,off,4));
        						third.get(i).setCaretPosition(0);
        						off+=4;
        						}
        					fourth.setValue(toInteger(object.get(chunk).data,off));
        					off+=4;
        					StringBuilder builder = new StringBuilder();
        			        for (int j = off; j<off+(int)fourth.getValue();j++){
        			        	builder.append((char)object.get(chunk).data[j]);
        			        }
        					name.setText(builder.toString());
        					name.setCaretPosition(0);
        				
        					translate.get(0).setValue(new Float((Float)value.get(12).getValue()));
        					translate.get(1).setValue(new Float((Float)value.get(13).getValue()));
        					translate.get(2).setValue(new Float((Float)value.get(14).getValue()));
        					
        					translate.get(3).setValue((float) (Math.atan(-(Float)value.get(9).getValue()/(Float)value.get(10).getValue()) * (180/Math.PI)));
        					translate.get(4).setValue((float) (Math.asin((Float)value.get(8).getValue()) * (180/Math.PI)));
        					translate.get(5).setValue((float) (Math.atan(-(Float)value.get(4).getValue()/(Float)value.get(0).getValue()) * (180/Math.PI)));
        					
        					translate.get(6).setValue((float)Math.sqrt(Math.pow((Float)value.get(0).getValue(), 2)+ Math.pow((Float)value.get(1).getValue(), 2) + Math.pow((Float)value.get(2).getValue(), 2)));
        					translate.get(7).setValue((float)Math.sqrt(Math.pow((Float)value.get(4).getValue(), 2)+ Math.pow((Float)value.get(5).getValue(), 2) + Math.pow((Float)value.get(6).getValue(), 2)));
        					translate.get(8).setValue((float)Math.sqrt(Math.pow((Float)value.get(8).getValue(), 2)+ Math.pow((Float)value.get(9).getValue(), 2) + Math.pow((Float)value.get(10).getValue(), 2)));
        					
        					translate2.get(0).setValue(new Float((Float)value2.get(12).getValue()));
        					translate2.get(1).setValue(new Float((Float)value2.get(13).getValue()));
        					translate2.get(2).setValue(new Float((Float)value2.get(14).getValue()));
        					
        					translate2.get(3).setValue((float) (Math.atan(-(Float)value2.get(9).getValue()/(Float)value2.get(10).getValue()) * (180/Math.PI)));
        					translate2.get(4).setValue((float) (Math.asin((Float)value2.get(8).getValue()) * (180/Math.PI)));
        					translate2.get(5).setValue((float) (Math.atan(-(Float)value2.get(4).getValue()/(Float)value2.get(0).getValue()) * (180/Math.PI)));
        					
        					translate2.get(6).setValue((float)Math.sqrt(Math.pow((Float)value2.get(0).getValue(), 2)+ Math.pow((Float)value2.get(1).getValue(), 2) + Math.pow((Float)value2.get(2).getValue(), 2)));
        					translate2.get(7).setValue((float)Math.sqrt(Math.pow((Float)value2.get(4).getValue(), 2)+ Math.pow((Float)value2.get(5).getValue(), 2) + Math.pow((Float)value2.get(6).getValue(), 2)));
        					translate2.get(8).setValue((float)Math.sqrt(Math.pow((Float)value2.get(8).getValue(), 2)+ Math.pow((Float)value2.get(9).getValue(), 2) + Math.pow((Float)value2.get(10).getValue(), 2)));
        			
        			}	else if(object.get(chunk).type == 1018){
							d.setSize(617,111);
							ArrayList<byte[]> data = new ArrayList<byte[]>();
							ArrayList<byte[]> data2 = new ArrayList<byte[]>();
    						JButton save = new JButton("Save");
    						ArrayList<JSpinner> value = new ArrayList<JSpinner>();
    						ArrayList<JSpinner> value2 = new ArrayList<JSpinner>();
    						JSpinner extra = new JSpinner();
    						ArrayList<JSpinner> value3 = new ArrayList<JSpinner>();
    						ArrayList<JSpinner> integers = new ArrayList<JSpinner>();
    						JComboBox<String> cb = new JComboBox<String>();
    						JComboBox<String> cb2 = new JComboBox<String>();
    						//first
    						JPanel valuePanel = new JPanel(new GridLayout(1,3));
    						JPanel value2Panel = new JPanel(new GridLayout(1,4));
    						//second
    						JPanel value3Panel = new JPanel(new GridLayout(1,4));
    						JPanel integersPanel = new JPanel(new GridLayout(1, 3));
    					
    						valuePanel.setBounds(0, 0, 300, 20);
    						value2Panel.setBounds(0, 30, 300, 20);
    						cb.setBounds(0, 60, 300, 20);
    						
    						value3Panel.setBounds(310, 0, 300, 20);
    						integersPanel.setBounds(310, 30, 300, 20);
    						cb2.setBounds(310, 60, 200, 20);
    						save.setBounds(530, 60, 80, 20);
    					
    						int temp = toInteger(object.get(chunk).data,0);
    						int temp2 = toInteger(object.get(chunk).data,4);
    						int off;
    						if(temp != 0){
    							for(int i = 0; i<temp; i++){
    								cb.addItem("index: "+i);
    							}
    							for(int j = 0; j<temp2; j++){
    								cb2.addItem("index: "+j);
    							}
    							
    							for(int k = 8; k<8+temp*28; k+=28){
    								data.add(Arrays.copyOfRange(object.get(chunk).data, k, k+28));
    							}
    							off = 8+temp*28;
    							for(int l = off; l<off+temp2*28; l+=28){
    								data2.add(Arrays.copyOfRange(object.get(chunk).data, l, l+28));
    							}
    						}
    						for(int i = 0; i<3; i++){
    							value.add(new JSpinner());
    							value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.00000"));
    							valuePanel.add(value.get(i));
    						}
    					
    						for(int i = 0; i<4; i++){
    							value2.add(new JSpinner());
    							value2.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value2.get(i).setEditor(new JSpinner.NumberEditor(value2.get(i),"0.00000"));
    							value2Panel.add(value2.get(i));
    						}
    						
    						for(int i = 0; i<4; i++){
    							value3.add(new JSpinner());
    							value3.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value3.get(i).setEditor(new JSpinner.NumberEditor(value3.get(i),"0.00000"));
    							value3Panel.add(value3.get(i));
    						}
    					
    						for( int i = 0; i<3; i++){
    							integers.add(new JSpinner());
    							integers.get(i).setModel(new SpinnerNumberModel(0, null, null, 1));
    							integersPanel.add(integers.get(i));
    						}
						
    						d.getContentPane().add(save);
    						d.getContentPane().add(valuePanel);
    						d.getContentPane().add(value2Panel);
    						d.getContentPane().add(extra);
    						d.getContentPane().add(value3Panel);
    						d.getContentPane().add(integersPanel);
    						d.getContentPane().add(cb);
    						d.getContentPane().add(cb2);
        				
    						if(cb.getItemCount() > 0){
    							int index = cb.getSelectedIndex();
    							off = 0;
				        	
    							for(int i = 0; i<3; i++){
    								value.get(i).setValue(toFloat(data.get(index), off));
    								off+=4;
    							}
				        	
    							for(int i = 0; i<4; i++){
    								value2.get(i).setValue(toFloat(data.get(index), off));
    								off+=4;
    							}
    						}
    						if(cb2.getItemCount() > 0){
    							int index = cb2.getSelectedIndex();
    							off = 0;
				        	
    							for(int i = 0; i<4; i++){
    								value3.get(i).setValue(toFloat(data2.get(index), off));
    								off+=4;
    							}
				        	
    							for(int i = 0; i<3; i++){
    								integers.get(i).setValue(toInteger(data2.get(index), off));
    								off+=4;
    							}
    						}
        				
    						cb.addItemListener(new ItemListener() {
    							public void itemStateChanged(ItemEvent arg0) {
        							int index = cb.getSelectedIndex();
        							 int off = 0;
    				        	
        							for(int i = 0; i<3; i++){
        								value.get(i).setValue(toFloat(data.get(index), off));
        								off+=4;
        							}
    				        	
        							for(int i = 0; i<4; i++){
        								value2.get(i).setValue(toFloat(data.get(index), off));
        								off+=4;
        							}
    							}
    						});
    						cb2.addItemListener(new ItemListener() {
    							public void itemStateChanged(ItemEvent arg0) {
        							int index = cb2.getSelectedIndex();
        							 int off = 0;
    				        	
        							for(int i = 0; i<4; i++){
        								value3.get(i).setValue(toFloat(data2.get(index), off));
        								off+=4;
        							}
    				        	
        							for(int i = 0; i<3; i++){
        								integers.get(i).setValue(toInteger(data2.get(index), off));
        								off+=4;
        							}
    							}
    						});
            				save.addActionListener(new ActionListener() {
            					public void actionPerformed(ActionEvent arg0) {
            						try{
            							ByteBuffer bb = ByteBuffer.allocate(28);
            							
            							for(int i = 0 ; i< 3 ; i++){
            								bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value.get(i).getValue())));
            							}
            							
            							for(int i = 0 ; i< 4 ; i++){
            								bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value2.get(i).getValue())));
            							}
            							
            							int index = cb.getSelectedIndex();
            							data.set(index, bb.array());
            							
            							ByteBuffer bb2 = ByteBuffer.allocate(28);
            							
            							for(int i = 0 ; i< 4 ; i++){
            								bb2.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value3.get(i).getValue())));
            							}
            							
            							for(int i = 0 ; i< 3 ; i++){
            								bb2.putInt(Integer.reverseBytes((int)integers.get(i).getValue()));
            							}
            							
            							int index2 = cb2.getSelectedIndex();
            							
            							data2.set(index2, bb2.array());
            							
            							ByteBuffer bb3 = ByteBuffer.allocate(8+temp*28+temp2*28);
            							
            							bb3.putInt(Integer.reverseBytes(temp));
            							bb3.putInt(Integer.reverseBytes(temp2));
            							
            							for(int i = 0; i<data.size(); i++){
            								bb3.put(data.get(i));
            							}
            							
            							for(int i = 0; i<data2.size(); i++){
            								bb3.put(data2.get(i));
            							}
            							object.get(chunk).data = bb3.array();
            							
            							modified = true;
            							frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");
            							savefile();
            						}catch(Exception e){
            							e.printStackTrace();
            						}
            					}
            				});
        			}	else if(object.get(chunk).type == 1023){
							d.setSize(308,170);
							ArrayList<byte[]> data = new ArrayList<byte[]>();
    						JButton save = new JButton("Save");
    						ArrayList<JSpinner> value = new ArrayList<JSpinner>();
    						ArrayList<JSpinner> value2 = new ArrayList<JSpinner>();
    						JTextField hash = new JTextField();
    						ArrayList<JSpinner> integers = new ArrayList<JSpinner>();
    						JComboBox<String> cb = new JComboBox<String>();
    					
    						JPanel valuePanel = new JPanel(new GridLayout(1, 3));
    						JPanel value2Panel = new JPanel(new GridLayout(1,3));
    						JPanel integersPanel = new JPanel(new GridLayout(1, 5));
    					
    						valuePanel.setBounds(0, 0, 300, 20);
    						value2Panel.setBounds(0, 30, 300, 20);
    						hash.setBounds(0, 60, 100, 20);
    						integersPanel.setBounds(0, 90, 300, 20);
    						cb.setBounds(0, 120, 200, 20);
    						save.setBounds(220, 120, 80, 20);
    					
    						int temp;
    						temp = toInteger(object.get(chunk).data,0);
    						int off;
    						if(temp != 0){
    							for(off = 4; off<4+temp*4; off+=4){
    								cb.addItem(""+toInteger(object.get(chunk).data, off));
    							}

    							for(int j = off; j<off+temp*48; j+=48){
    								data.add(Arrays.copyOfRange(object.get(chunk).data, j, j+48));
    							}
    						}
						
    						for(int i = 0; i<3; i++){
    							value.add(new JSpinner());
    							value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.00000"));
    							valuePanel.add(value.get(i));
    						}
    					
    						for(int i = 0; i<3; i++){
    							value2.add(new JSpinner());
    							value2.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
    							value2.get(i).setEditor(new JSpinner.NumberEditor(value2.get(i),"0.00000"));
    							value2Panel.add(value2.get(i));
    						}
    					
    						for( int i = 0; i<5; i++){
    							integers.add(new JSpinner());
    							integers.get(i).setModel(new SpinnerNumberModel(0, null, null, 1));
    							integersPanel.add(integers.get(i));
    						}
						
    						d.getContentPane().add(save);
    						d.getContentPane().add(valuePanel);
    						d.getContentPane().add(value2Panel);
    						d.getContentPane().add(hash);
    						d.getContentPane().add(integersPanel);
    						d.getContentPane().add(cb);
        				
    						if(cb.getItemCount() > 0){
    							int index = cb.getSelectedIndex();
    							off = 0;
				        	
    							for(int i = 0; i<3; i++){
    								value.get(i).setValue(toFloat(data.get(index), off));
    								off+=4;
    							}
				        	
    							for(int i = 0; i<3; i++){
    								value2.get(i).setValue(toFloat(data.get(index), off));
    								off+=4;
    							}
    							hash.setText(makeString(data.get(index),off,4));
    							hash.setCaretPosition(0);
    							off+=4;
				        	
    							for(int i = 0; i<5; i++){
    								integers.get(i).setValue(toInteger(data.get(index), off));
    								off+=4;
    							}
    						}
        				
    						cb.addItemListener(new ItemListener() {
    							public void itemStateChanged(ItemEvent arg0) {
    								int index = cb.getSelectedIndex();
    								int off = 0;
    				        	
    								for(int i = 0; i<3; i++){
    									value.get(i).setValue(toFloat(data.get(index), off));
    									off+=4;
    								}
    				        	
    								for(int i = 0; i<3; i++){
    									value2.get(i).setValue(toFloat(data.get(index), off));
    									off+=4;
    								}
    								hash.setText(makeString(data.get(index),off,4));
    								hash.setCaretPosition(0);
    								off+=4;
    				        	
    								for(int i = 0; i<5; i++){
    									integers.get(i).setValue(toInteger(data.get(index), off));
    									off+=4;
    								}
    							}
    						});
            				save.addActionListener(new ActionListener() {
            					public void actionPerformed(ActionEvent arg0) {
            						try{
            							
            							ByteBuffer bb = ByteBuffer.allocate(48);
            							
            							for(int i = 0 ; i< 3 ; i++){
            								bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value.get(i).getValue())));
            							}
            							
            							for(int i = 0 ; i< 3 ; i++){
            								bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value2.get(i).getValue())));
            							}
            							
            							byte[] hashnametest = hexStringToByteArray(hash.getText());
        								for(int b = 0; b<hashnametest.length; b++){
        									bb.put(hashnametest[b]);
        								}
        								
            							for(int i = 0 ; i< 5 ; i++){
            								bb.putInt(Integer.reverseBytes((int)integers.get(i).getValue()));
            							}
            							
            							int index = cb.getSelectedIndex();
            							data.set(index, bb.array());
            							
            							
            							int temp = toInteger(object.get(chunk).data,0);
            							ByteBuffer bb2 = ByteBuffer.allocate(4+temp*52);
            							bb2.putInt(Integer.reverseBytes(temp));
            							
            							for(int i = 0; i<cb.getItemCount(); i++){
            								bb2.putInt(Integer.reverseBytes(Integer.parseInt(cb.getItemAt(i))));
            							}
            							
            							for(int i = 0; i<data.size(); i++){
            								bb2.put(data.get(i));
            							}
            							
            							object.get(chunk).data = bb2.array();
            							
            							modified = true;
            							frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");
            							savefile();
            						}catch(Exception e){
            							e.printStackTrace();
            						}
            					}
            				});
        			}	else if(object.get(chunk).type == 1019){
        				
        				int type = object.get(chunk).type;
						
        				d.setBounds(100, 100, 540, 134);
        				
        				ArrayList<byte[]> data = new ArrayList<byte[]>();
        				
        				JLabel lblUnk = new JLabel("Unk:");
        				lblUnk.setBounds(10, 11, 35, 20);
        				d.getContentPane().add(lblUnk);
        				
        				JSpinner spinner = new JSpinner();
        				spinner.setBounds(46, 11, 62, 20);
        				d.getContentPane().add(spinner);
        				
        				JLabel lblUnk_1 = new JLabel("Unk2:");
        				lblUnk_1.setBounds(10, 41, 35, 20);
        				d.getContentPane().add(lblUnk_1);
        				
        				JTextField spinner_1 = new JTextField();
        				spinner_1.setBounds(46, 41, 62, 20);
        				d.getContentPane().add(spinner_1);
        				
        				JLabel lblUnk_2 = new JLabel("Unk3:");
        				lblUnk_2.setBounds(10, 71, 35, 20);
        				d.getContentPane().add(lblUnk_2);
        				
        				JTextField spinner_2 = new JTextField();
        				spinner_2.setBounds(46, 71, 62, 20);
        				d.getContentPane().add(spinner_2);
        				
        				JComboBox cb = new JComboBox();
        				cb.setBounds(424, 71, 96, 20);
        				d.getContentPane().add(cb);
        				
        				JSpinner spinner_3 = new JSpinner();
        				spinner_3.setBounds(213, 11, 62, 20);
        				d.getContentPane().add(spinner_3);
        				
        				JLabel label = new JLabel("Unk:");
        				label.setBounds(146, 11, 57, 20);
        				d.getContentPane().add(label);
        				
        				JLabel label_1 = new JLabel("Unk2:");
        				label_1.setBounds(285, 11, 57, 20);
        				d.getContentPane().add(label_1);
        				
        				JSpinner spinner_4 = new JSpinner();
        				spinner_4.setBounds(352, 11, 62, 20);
        				d.getContentPane().add(spinner_4);
        				
        				JLabel label_2 = new JLabel("Unk3:");
        				label_2.setBounds(146, 41, 57, 20);
        				d.getContentPane().add(label_2);
        				
        				JTextField spinner_5 = new JTextField();
        				spinner_5.setBounds(213, 41, 62, 20);
        				d.getContentPane().add(spinner_5);
        				
        				JSpinner spinner_8 = new JSpinner();
        				spinner_8.setBounds(352, 71, 62, 20);
        				d.getContentPane().add(spinner_8);
        				
        				JSpinner spinner_7 = new JSpinner();
        				spinner_7.setBounds(213, 71, 62, 20);
        				d.getContentPane().add(spinner_7);
        				
        				JTextField spinner_6 = new JTextField();
        				spinner_6.setBounds(352, 41, 62, 20);
        				d.getContentPane().add(spinner_6);
        				
        				JLabel label_5 = new JLabel("Unk3:");
        				label_5.setBounds(285, 71, 57, 20);
        				d.getContentPane().add(label_5);
        				
        				JLabel label_4 = new JLabel("Unk2:");
        				label_4.setBounds(146, 71, 57, 20);
        				d.getContentPane().add(label_4);
        				
        				JLabel label_3 = new JLabel("Unk:");
        				label_3.setBounds(285, 41, 57, 20);
        				d.getContentPane().add(label_3);
        				
        				//
        				int off = 0;
        				
        				spinner.setValue(toInteger(object.get(chunk).data, off));
        				off+=4;
        				int c1019 = toInteger(object.get(chunk).data ,4);
						for(int k = 16,i = 0; k<16+c1019*24; k+=24,i++){
							data.add(Arrays.copyOfRange(object.get(chunk).data, k, k+24));
							cb.addItem(i);
						}
						off +=4;
						
        				spinner_1.setText(makeString(object.get(chunk).data, off,4));
        				off+=4;
        				spinner_2.setText(makeString(object.get(chunk).data, off,4));
        				
        				
   						cb.addItemListener(new ItemListener() {
							public void itemStateChanged(ItemEvent arg0) {
    							int index = cb.getSelectedIndex();
    							 int off = 0;
				        	
    								spinner_3.setValue(toFloat(data.get(index), off));
    								off+=4;
    								spinner_4.setValue(toFloat(data.get(index), off));
    								off+=4;
    								
    								spinner_5.setText(makeString(data.get(index), off,4));
    								off+=4;
    								spinner_6.setText(makeString(data.get(index), off,4));
    								off+=4;
    								
    								spinner_7.setValue(toFloat(data.get(index), off));
    								off+=4;
    								spinner_8.setValue(toFloat(data.get(index), off));
    								off+=4;	        	
							}
						});
    						
        			}	else if(object.get(chunk).type == 1020){
        					d.setSize(638,253);
        					ArrayList<byte[]> data = new ArrayList<byte[]>();
        				
        					ArrayList<JSpinner> value = new ArrayList<JSpinner>();
        					ArrayList<JSpinner> jbytes = new ArrayList<JSpinner>();
        					ArrayList<JSpinner> floats = new ArrayList<JSpinner>();
        					ArrayList<JSpinner> integer = new ArrayList<JSpinner>();
        					ArrayList<JSpinner> translate = new ArrayList<JSpinner>();
        				
        	        	
        					JSpinner namecount = new JSpinner();
        					JTextField name = new JTextField();
        				
        					JTextField hash = new JTextField();
        					
        					JComboBox<String> cb = new JComboBox<String>();
        					
        					GridLayout gltranslate = new GridLayout(3,3);
        					gltranslate.setVgap(10);
        					GridLayout glbyte = new GridLayout(2,4);
        					glbyte.setVgap(10);
        					GridLayout glfloat = new GridLayout(2,3);
        					glfloat.setVgap(10);

        					JPanel translatePanel = new JPanel(gltranslate);
        					JPanel mainPanel = new JPanel(new GridLayout(4, 4));
        					JPanel bytePanel = new JPanel(glbyte);
        					JPanel floatPanel = new JPanel(glfloat);
        					JPanel integerPanel = new JPanel(new GridLayout(1, 4));
        					JPanel namePanel = new JPanel(new GridLayout(1, 2));
        					JButton calculate = new JButton("Calculate");
        					JButton save = new JButton("Save");
        					JLabel translateText = new JLabel("Translate XYZ:");
        					JLabel rotationText = new JLabel("Rotation XYZ:");
        					JLabel scaleText = new JLabel("Scale XYZ:");
        					JLabel matrixText = new JLabel("Matrix4f T:");
        					
        					translateText.setBounds(0, 0, 100, 20);
        					rotationText.setBounds(0, 30, 100, 20);
        					scaleText.setBounds(0, 60, 100, 20);
        					matrixText.setBounds(0, 100, 100, 20);
        					translatePanel.setBounds(110, 0, 300, 90);
        					
        					mainPanel.setBounds(110, 100, 300, 92);
        					bytePanel.setBounds(420, 0, 200, 57);
        					
        					
        					floatPanel.setBounds(420, 67, 200, 57);
        					integerPanel.setBounds(420, 134, 200, 20);
        					namePanel.setBounds(420, 164, 200, 20);
        					
        					cb.setBounds(420, 194, 200, 20);
        					calculate.setBounds(110, 194, 100, 20);
        					save.setBounds(310, 194, 100, 20);

        					int temp,namec;
        					temp = toInteger(object.get(chunk).data,0);
        					int off = 4;
        					if(temp != 0){
        						for(int i_1005 = 0;i_1005<temp;i_1005++){
        							namec = toInteger(object.get(chunk).data,off+96);
        							data.add(Arrays.copyOfRange(object.get(chunk).data, off, off+100+namec)); //= object.get(chunk).data.subList(off, off+100+namec);
        							off+=100;
        							StringBuilder builder = new StringBuilder();
        							for (int j = off; j<off+namec;j++){
        								builder.append((char)object.get(chunk).data[j]);
        							}
        							cb.addItem(builder.toString());
        						off = off+namec;
        					}
        					for(int i = 0; i<9; i++){
        					translate.add(new JSpinner());
							translate.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
							translate.get(i).setEditor(new JSpinner.NumberEditor(translate.get(i),"0.00000"));
        					translatePanel.add(translate.get(i));
        					}
        					for(int i = 0; i<16; i++){
        					value.add(new JSpinner());
        					value.get(i).setModel(new SpinnerNumberModel(values, null, null, step));
        					value.get(i).setEditor(new JSpinner.NumberEditor(value.get(i),"0.0000000"));
        					mainPanel.add(value.get(i));
        					}
        					for( int k = 0; k<8; k++){
        						jbytes.add(new JSpinner());
        						bytePanel.add(jbytes.get(k));
        					}
        					for( int j = 0; j<6; j++){
        						floats.add(new JSpinner());
        						floatPanel.add(floats.get(j));
        					}
        					integerPanel.add(hash);
        					for( int l = 0; l<3; l++){
        						integer.add(new JSpinner());
        						integerPanel.add(integer.get(l));
        					}
        					namePanel.add(namecount);
        					namePanel.add(name);
        					
        				}
        				d.getContentPane().add(translateText);
        				d.getContentPane().add(rotationText);
        				d.getContentPane().add(scaleText);
        				d.getContentPane().add(matrixText);
        				d.getContentPane().add(cb);
        				d.getContentPane().add(translatePanel);
        				d.getContentPane().add(mainPanel);
        				d.getContentPane().add(bytePanel);
        				d.getContentPane().add(floatPanel);
        				d.getContentPane().add(integerPanel);
        				d.getContentPane().add(namePanel);
        				d.getContentPane().add(calculate);
        				d.getContentPane().add(save);
        				
        				if(cb.getItemCount() > 0){
				        	int index = cb.getSelectedIndex();
				        	off = 0;
				        	for (int i_2 = 0; i_2<12;i_2+=4){
								for(int i = 0;i<3;i++){
										value.get(i_2+i).setValue(toFloat(data.get(index),off));
										off+=4;
								}
				        	}
							value.get(12).setValue(toFloat(data.get(index),off));
							off+=4;
							value.get(13).setValue(toFloat(data.get(index),off));
							off+=4;
							value.get(14).setValue(toFloat(data.get(index),off));
							off+=4;
							value.get(3).setValue(0.0);
							value.get(7).setValue(0.0);
							value.get(11).setValue(0.0);
							value.get(15).setValue(1.0);
							for( int k = 0; k<8; k++){
								jbytes.get(k).setModel(new SpinnerNumberModel((byte)0, null, null, (byte)1));
								jbytes.get(k).setValue(data.get(index)[off+k]);
							}
							off+=8;
							for( int j = 0; j<6; j++){
								floats.get(j).setModel(new SpinnerNumberModel(0.0, null, null, 0.1));
								floats.get(j).setValue(toFloat(data.get(index),off));
								off+=4;
							}
							hash.setText(makeString(data.get(index),off,4));
							hash.setCaretPosition(0);
							off+=4;
							for(int l = 0; l<3;l++){
								integer.get(l).setModel(new SpinnerNumberModel(0, null, null, 1));
								integer.get(l).setValue(toInteger(data.get(index),off));
								off+=4;
							}
							namecount.setValue(toInteger(data.get(index),off));
							off+=4;
							name.setText(cb.getSelectedItem().toString());
							name.setCaretPosition(0);
							off+=4;
									
							//translate
							translate.get(0).setValue(new Float((Float)value.get(12).getValue()));
							translate.get(1).setValue(new Float((Float)value.get(13).getValue()));
							translate.get(2).setValue(new Float((Float)value.get(14).getValue()));
							//rotate
							translate.get(3).setValue((float) (Math.atan(-(Float)value.get(9).getValue()/(Float)value.get(10).getValue()) * (180/Math.PI)));
							translate.get(4).setValue((float) (Math.asin((Float)value.get(8).getValue()) * (180/Math.PI)));
							translate.get(5).setValue((float) (Math.atan(-(Float)value.get(4).getValue()/(Float)value.get(0).getValue()) * (180/Math.PI)));
							//size	
							translate.get(6).setValue((float)Math.sqrt(Math.pow((Float)value.get(0).getValue(), 2)+ Math.pow((Float)value.get(1).getValue(), 2) + Math.pow((Float)value.get(2).getValue(), 2)));
							translate.get(7).setValue((float)Math.sqrt(Math.pow((Float)value.get(4).getValue(), 2)+ Math.pow((Float)value.get(5).getValue(), 2) + Math.pow((Float)value.get(6).getValue(), 2)));
							translate.get(8).setValue((float)Math.sqrt(Math.pow((Float)value.get(8).getValue(), 2)+ Math.pow((Float)value.get(9).getValue(), 2) + Math.pow((Float)value.get(10).getValue(), 2)));
        				}
        				
        				cb.addItemListener(new ItemListener() {
        						public void itemStateChanged(ItemEvent arg0) {
        				        	int index = cb.getSelectedIndex();
        				        	int off = 0;
        				        	for (int i_2 = 0; i_2<12;i_2+=4){
        								for(int i = 0;i<3;i++){
        										value.get(i_2+i).setValue(toFloat(data.get(index),off));
        										off+=4;
        								}
        				        	}
        							value.get(12).setValue(toFloat(data.get(index),off));
        							off+=4;
        							value.get(13).setValue(toFloat(data.get(index),off));
        							off+=4;
        							value.get(14).setValue(toFloat(data.get(index),off));
        							off+=4;
        							value.get(3).setValue(0.0);
        							value.get(7).setValue(0.0);
        							value.get(11).setValue(0.0);
        							value.get(15).setValue(1.0);
        							for( int k = 0; k<8; k++){
        								jbytes.get(k).setModel(new SpinnerNumberModel((byte)0, null, null, (byte)1));
        								jbytes.get(k).setValue(data.get(index)[off+k]);
        							}
        							off+=8;
        							for( int j = 0; j<6; j++){
        								floats.get(j).setModel(new SpinnerNumberModel(0.0, null, null, 0.1));
        								floats.get(j).setValue(toFloat(data.get(index),off));
        								off+=4;
        							}
        							hash.setText(makeString(data.get(index),off,4));
        							hash.setCaretPosition(0);
        							off+=4;
        							for(int l = 0; l<3;l++){
        								integer.get(l).setModel(new SpinnerNumberModel(0, null, null, 1));
        								integer.get(l).setValue(toInteger(data.get(index),off));
        								off+=4;
        							}
        							namecount.setValue(toInteger(data.get(index),off));
        							off+=4;
        							name.setText(cb.getSelectedItem().toString());
        							name.setCaretPosition(0);
        							off+=4;
        									
        							//translate
        							translate.get(0).setValue(new Float((Float)value.get(12).getValue()));
        							translate.get(1).setValue(new Float((Float)value.get(13).getValue()));
        							translate.get(2).setValue(new Float((Float)value.get(14).getValue()));
        							//rotate
        							translate.get(3).setValue((float) (Math.atan(-(Float)value.get(9).getValue()/(Float)value.get(10).getValue()) * (180/Math.PI)));
        							translate.get(4).setValue((float) (Math.asin((Float)value.get(8).getValue()) * (180/Math.PI)));
        							translate.get(5).setValue((float) (Math.atan(-(Float)value.get(4).getValue()/(Float)value.get(0).getValue()) * (180/Math.PI)));
        							//size	
        							translate.get(6).setValue((float)Math.sqrt(Math.pow((Float)value.get(0).getValue(), 2)+ Math.pow((Float)value.get(1).getValue(), 2) + Math.pow((Float)value.get(2).getValue(), 2)));
        							translate.get(7).setValue((float)Math.sqrt(Math.pow((Float)value.get(4).getValue(), 2)+ Math.pow((Float)value.get(5).getValue(), 2) + Math.pow((Float)value.get(6).getValue(), 2)));
        							translate.get(8).setValue((float)Math.sqrt(Math.pow((Float)value.get(8).getValue(), 2)+ Math.pow((Float)value.get(9).getValue(), 2) + Math.pow((Float)value.get(10).getValue(), 2)));
        				    }
        				});
        				calculate.addActionListener(new ActionListener() {
        					public void actionPerformed(ActionEvent arg0) {
        						try{
        							matrix_mult(translate,value);
        							floats.get(0).setValue(value.get(12).getValue());
        							floats.get(1).setValue(value.get(13).getValue());
        							floats.get(2).setValue(value.get(14).getValue());
        							floats.get(3).setValue(value.get(12).getValue());
        							floats.get(4).setValue(value.get(13).getValue());
        							floats.get(5).setValue(value.get(14).getValue());
        						}catch(Exception e){
        							e.printStackTrace();
        							//System.err.println(e);
        							//JOptionPane.showMessageDialog(null,"Ups... Something goes wrong!");
        						}
        					}
        				});
        				save.addActionListener(new ActionListener() {
        					public void actionPerformed(ActionEvent arg0) {
        						try{
        							namecount.setValue(name.getText().length());
        							
        							ByteBuffer bb = ByteBuffer.allocate(100+(int)namecount.getValue());
        				        	for (int i_2 = 0; i_2<12;i_2+=4){
        								for(int i = 0;i<3;i++){
        									bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value.get(i_2+i).getValue())));
        								}
        				        	}
        							bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value.get(12).getValue())));
        							bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value.get(13).getValue())));
        							bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)value.get(14).getValue())));
        							for( int k = 0; k<8; k++){
        								bb.put((byte)jbytes.get(k).getValue());
        							}
        							floats.get(0).setValue(value.get(12).getValue());
        							floats.get(1).setValue(value.get(13).getValue());
        							floats.get(2).setValue(value.get(14).getValue());
        							floats.get(3).setValue(value.get(12).getValue());
        							floats.get(4).setValue(value.get(13).getValue());
        							floats.get(5).setValue(value.get(14).getValue());
        							for( int j = 0; j<6; j++){
        								bb.putInt(Integer.reverseBytes(Float.floatToIntBits((float)floats.get(j).getValue())));
        							}
        							byte[] hashnametest = hexStringToByteArray(hash.getText());
    									for(int b = 0; b<hashnametest.length; b++){
        									bb.put(hashnametest[b]);
        								}
        							for(int l = 0; l<3;l++){
        									bb.putInt(Integer.reverseBytes((int)integer.get(l).getValue()));
        							}
        							bb.putInt(Integer.reverseBytes((int)namecount.getValue()));
        							
        							byte[] names =name.getText().getBytes(); 
        							for(int n = 0;n<names.length; n++){
        								bb.put(names[n]);
        							}
        							
        							int index = cb.getSelectedIndex();
        							
        							
        							byte[] temp = bb.array();
        							
        							data.set(index, temp);
        							
        							calculateChildDiff(temp, data.get(index), index);
        							
        							ByteArrayOutputStream test = new ByteArrayOutputStream();
        							int temp_int = data.size();
        							test.write(new byte[]{(byte)temp_int, (byte)(temp_int >> 8), (byte)(temp_int >> 16), (byte)(temp_int >> 24)});
        							for(int i = 0; i<data.size(); i++){
        								test.write(data.get(i));
        							}
        							object.get(chunk).data = test.toByteArray();
        							modified = true;
        							frmBspviewer.setTitle("Bsp_Viewer by Woitek1993"+" -["+file.getName()+"]*");
        							savefile();
        						}catch(Exception e){
        							e.printStackTrace();
        						}
        					}
        				});
        			}else{}
    				
        		}
        	}
        });
	    
	    
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(300, 8, 284, 512);
		frmBspviewer.getContentPane().add(tabbedPane);
	    
		tree.setModel(new DefaultTreeModel(
				new DefaultMutableTreeNode() {
					{
					}
				}
			));

		 hashname_old = new ArrayList<String>();
		 hashname_new = new ArrayList<String>();	
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		    	try{
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		        if (String.valueOf(node.getParent()).equals("Textures" + " {"+0+"}")){
		           tabbedPane.setSelectedIndex(1);
		           int index = node.getParent().getIndex(node);
		           int namecounter = toInteger(texturedata.get(index),0);
		    	   lblNewLabel.setIcon(TexturePrint(index));
		    	   lbl_Name.setText(texturename.get(index));
		    	   int index2 = 4+namecounter*4;
		    	   int width = toInteger(texturedata.get(index),index2+4);
		    	   int height = toInteger(texturedata.get(index),index2+8);
		    	   label.setText(""+width);
		    	   label_1.setText(""+height);
		    	   label_2.setText(""+toInteger(texturedata.get(index),index2+12));
		    	   label_3.setText(""+width*height);
		    	   label_4.setText(""+toInteger(texturedata.get(index),index2+16));
		    	   label_5.setText(""+toShort(texturedata.get(index),index2+21));
		       }else{
		    	   tabbedPane.setSelectedIndex(0);
		       }
		    	}catch(Exception e5){e5 = null;}
		       
		    }
		});
		exp.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		try{
        		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        		
 				JFileChooser exportImage =  new JFileChooser(Advapi32Util.registryGetStringValue(
 				WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExport"));
        		FileNameExtensionFilter imgfilt = new FileNameExtensionFilter("Targa Format", "tga");	
        		exportImage.setDialogTitle("Save Texture");
        		exportImage.setSelectedFile(new File(texturename.get(node.getParent().getIndex(node)).substring(0, texturename.get(node.getParent().getIndex(node)).length()-1)+".tga"));
        		exportImage.setFileFilter(imgfilt);
        		exportImage.setApproveButtonText("Save");
        		exportImage.showOpenDialog(null);
        		expFile = exportImage.getSelectedFile();	
        		exportImage.setCurrentDirectory(expFile);
        		Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExport", expFile.getPath());
        			    
				FileChannel out = new FileOutputStream(expFile.getParent()+"\\"+expFile.getName()).getChannel();
				
			    out.write(prepareToExport(node.getParent().getIndex(node)));
				out.close();
        		}catch(Exception export){
        			export.printStackTrace();
        		}
        	}
		});
		
		smodel.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
                try{
                	draw(getTreeIndex());
                }catch(Exception exception){
                	exception.printStackTrace();
                }
        	}
		});
		
		imp.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
                replaceMaterial(importTga(null, null));
                replaceMeshHash();
    			hashname_old.clear();
    			hashname_new.clear();
    			modified = true;
    			loadTree();
        	}
        });
		

		JScrollPane jsp_2= new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);	
		jsp_2.setSize(280, 486);
		jsp_2.setLocation(10, 33);
		
		frmBspviewer.getContentPane().add(jsp_2);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Info View", null, panel_1, null);
		panel_1.setLayout(null);
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Image View", null, panel_2, null);
		panel_2.setLayout(null);
		
		lblNewLabel = new JLabel();
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setLayout(null);
		JScrollPane jsp_3= new JScrollPane(lblNewLabel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp_3.setBounds(10, 10, 260, 260);
		panel_2.add(jsp_3);
		
		JLabel lbl1 = new JLabel("Name:");
		lbl1.setBounds(10, 280, 46, 14);
		panel_2.add(lbl1);
		
		JLabel lblNewLabel_1 = new JLabel("Width :");
		lblNewLabel_1.setBounds(10, 300, 46, 14);
		panel_2.add(lblNewLabel_1);
		
		JLabel lblHeigth = new JLabel("Height :");
		lblHeigth.setBounds(100, 300, 46, 14);
		panel_2.add(lblHeigth);
		
		JLabel lblFlags = new JLabel("Flags :");
		lblFlags.setBounds(10, 320, 46, 14);
		panel_2.add(lblFlags);
		
		JLabel lblMipmaps = new JLabel("Mipmap levels:");
		lblMipmaps.setBounds(100, 320, 91, 14);
		panel_2.add(lblMipmaps);
		
		JLabel lblFormat = new JLabel("Format :");
		lblFormat.setBounds(10, 340, 46, 14);
		panel_2.add(lblFormat);
		
		JLabel lblSize = new JLabel("Size :");
		lblSize.setBounds(100, 340, 46, 14);
		panel_2.add(lblSize);
		
		lbl_Name = new JLabel("");
		lbl_Name.setBounds(60, 281, 210, 14);
		panel_2.add(lbl_Name);
		
		label = new JLabel("");
		label.setBounds(60, 300, 36, 14);
		panel_2.add(label);
		
		label_1 = new JLabel("");
		label_1.setBounds(190, 300, 46, 14);
		panel_2.add(label_1);
		
		label_2 = new JLabel("");
		label_2.setBounds(190, 320, 46, 14);
		panel_2.add(label_2);
		
		label_3 = new JLabel("");
		label_3.setBounds(190, 340, 46, 14);
		panel_2.add(label_3);
		
		label_4 = new JLabel("");
		label_4.setBounds(60, 320, 36, 14);
		panel_2.add(label_4);
		
		label_5 = new JLabel("");
		label_5.setBounds(60, 340, 36, 14);
		panel_2.add(label_5);
		
		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Hex View", null, panel_3, null);
		panel_3.setLayout(null);
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(true);
		JScrollPane jsp = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setSize(259, 87);
		jsp.setLocation(10, 11);
		panel_3.add(jsp);
		
		saveChanges = new JButton("Replace Chunk");
		saveChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
        			try{
        			String replace = new String(textArea.getText().replaceAll("\\s",""));
        			byte [] temp = hexStringToByteArray(replace);
        			int index = getTreeIndex();
        			
                	calculateDiff(temp,index);
        			modified = true;
        			loadTree();
        			}catch(Exception e){}
			}
		});
		saveChanges.setBounds(75, 109, 125, 23);
		panel_3.add(saveChanges);
		
		JTextArea debugTextArea = new JTextArea();
		debugTextArea.setLineWrap(true);
		debugTextArea.setEditable(true);
		
		
		JButton debugButton = new JButton("Debug");
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(debugTextArea.getText().startsWith("showall")){
					String[] tokens = debugTextArea.getText().split(",");
					if(tokens.length == 2){
						try{
							StringBuilder text = new StringBuilder();
							int index = Integer.parseInt(tokens[1]);
								for(int i = 0; i< object.size(); i++){
									if(object.get(i).type == index){
										text.append(i + ": " + index + '\n');
									}else if(index == -1){
										text.append(i + ": " + object.get(i).type + '\n');
									}
								}		
								debugTextArea.setText(text.toString());
						}catch(Exception exs){
							debugTextArea.setText("Wrong argument: " + tokens[1]);
						}
					}
				}else if (debugTextArea.getText().startsWith("convert")){
					String[] tokens = debugTextArea.getText().split(",");
					if(tokens.length == 4){
							if(tokens[1].equals("float")){
								try{
									ByteBuffer bb = ByteBuffer.allocate(4);
									int value = Float.floatToIntBits(Float.parseFloat(tokens[2]));
										if(tokens[3].equals("r")){
											bb.putInt(Integer.reverseBytes(value));
											debugTextArea.setText(dataToString(bb.array()));
										}else{
											debugTextArea.setText("Wrong third argument: " + tokens[3]);
										}
								}catch(Exception exs){debugTextArea.setText("Wrong third argument: " + tokens[2]);}
							}else if (tokens[1].equals("int")){
								try{
									ByteBuffer bb = ByteBuffer.allocate(4);
									int value = Integer.parseInt(tokens[2]);
										if(tokens[3].equals("r")){
											bb.putInt(Integer.reverseBytes(value));
											debugTextArea.setText(dataToString(bb.array()));
										}else{
											debugTextArea.setText("Wrong fourth argument: " + tokens[3]);
										}
								}catch(Exception exs){debugTextArea.setText("Wrong third argument: " + tokens[2]);}
						}else{
							debugTextArea.setText("Wrong second argument: " + tokens[1]);
					
					}

				}else if (tokens.length == 3){
					if (tokens[1].equals("byte")){
						try{
							int temp = Integer.parseInt(tokens[2]);
							if(temp >=0 && temp <=255){
								debugTextArea.setText(String.format("%02X",(byte)(temp & 0xFF)));
							}else{
								debugTextArea.setText("Wrong third argument: " + tokens[2]);
							}
						}catch(Exception exs){}
					}else if(tokens[1].equals("float")){
						try{
							ByteBuffer bb = ByteBuffer.allocate(4);
							int value = Float.floatToIntBits(Float.parseFloat(tokens[2]));
							bb.putInt(value);
							debugTextArea.setText(dataToString(bb.array()));
						}catch(Exception exs){debugTextArea.setText("Wrong third argument: " + tokens[2]);}
					}else if(tokens[1].equals("int")){
						try{
							ByteBuffer bb = ByteBuffer.allocate(4);
							int value = Integer.parseInt(tokens[2]);
							bb.putInt(value);
							debugTextArea.setText(dataToString(bb.array()));
						}catch(Exception exs){debugTextArea.setText("Wrong third argument: " + tokens[2]);}
					}else{
						debugTextArea.setText("Wrong second argument: " + tokens[1]);
					}
				}else{
					debugTextArea.setText("Wrong number of arguments: " + tokens.length);
				}
			 }else if(debugTextArea.getText().startsWith("hashname")){
				 String[] tokens = debugTextArea.getText().split(",");
				 if(tokens.length == 2){
					 try{
						 int[]hashvalues = {0,79764919,159529838,222504665,319059676,398814059,445009330,507990021,638119352,583659535,797628118,726387553,890018660,835552979,1015980042,944750013,1276238704,1221641927,1167319070,1095957929,1595256236,1540665371,1452775106,1381403509,1780037320,1859660671,1671105958,1733955601,2031960084,2111593891,1889500026,1952343757,-1742489888,-1662866601,-1851683442,-1788833735,-1960329156,-1880695413,-2103051438,-2040207643,-1104454824,-1159051537,-1213636554,-1284997759,-1389417084,-1444007885,-1532160278,-1603531939,-734892656,-789352409,-575645954,-646886583,-952755380,-1007220997,-827056094,-898286187,-231047128,-151282273,-71779514,-8804623,-515967244,-436212925,-390279782,-327299027,881225847,809987520,1023691545,969234094,662832811,591600412,771767749,717299826,311336399,374308984,453813921,533576470,25881363,88864420,134795389,214552010,2023205639,2086057648,1897238633,1976864222,1804852699,1867694188,1645340341,1724971778,1587496639,1516133128,1461550545,1406951526,1302016099,1230646740,1142491917,1087903418,-1398421865,-1469785312,-1524105735,-1578704818,-1079922613,-1151291908,-1239184603,-1293773166,-1968362705,-1905510760,-2094067647,-2014441994,-1716953613,-1654112188,-1876203875,-1796572374,-525066777,-462094256,-382327159,-302564546,-206542021,-143559028,-97365931,-17609246,-960696225,-1031934488,-817968335,-872425850,-709327229,-780559564,-600130067,-654598054,1762451694,1842216281,1619975040,1682949687,2047383090,2127137669,1938468188,2001449195,1325665622,1271206113,1183200824,1111960463,1543535498,1489069629,1434599652,1363369299,622672798,568075817,748617968,677256519,907627842,853037301,1067152940,995781531,51762726,131386257,177728840,240578815,269590778,349224269,429104020,491947555,-248556018,-168932423,-122852000,-60002089,-500490030,-420856475,-341238852,-278395381,-685261898,-739858943,-559578920,-630940305,-1004286614,-1058877219,-845023740,-916395085,-1119974018,-1174433591,-1262701040,-1333941337,-1371866206,-1426332139,-1481064244,-1552294533,-1690935098,-1611170447,-1833673816,-1770699233,-2009983462,-1930228819,-2119160460,-2056179517,1569362073,1498123566,1409854455,1355396672,1317987909,1246755826,1192025387,1137557660,2072149281,2135122070,1912620623,1992383480,1753615357,1816598090,1627664531,1707420964,295390185,358241886,404320391,483945776,43990325,106832002,186451547,266083308,932423249,861060070,1041341759,986742920,613929101,542559546,756411363,701822548,-978770311,-1050133554,-869589737,-924188512,-693284699,-764654318,-550540341,-605129092,-475935807,-413084042,-366743377,-287118056,-257573603,-194731862,-114850189,-35218492,-1984365303,-1921392450,-2143631769,-2063868976,-1698919467,-1635936670,-1824608069,-1744851700,-1347415887,-1418654458,-1506661409,-1561119128,-1129027987,-1200260134,-1254728445,-1309196108};
						 int hashsum = 0;
						// String printValues = new String(debugTextArea.getText().replaceAll("\\s",""));
						 byte[] hasharray = tokens[1].getBytes();//hexStringToByteArray(printValues);
		    			 for (int u = 0; u<hasharray.length;u++){
		    				 hashsum = hashvalues[(int)((hashsum >> 24) ^ (hasharray[u])) & 0xFF] ^ (hashsum << 8);
		    			 }
						 int hashsumfinal = (hasharray.length ^ hashsum);
						 ByteBuffer bb = ByteBuffer.allocate(4);
						 bb.putInt(hashsumfinal);
						 
						 debugTextArea.setText(""+dataToString(bb.array()));
					 }catch(Exception ex){
						 debugTextArea.setText("Wrong arguments");
					 }
				 }
			 }else if(debugTextArea.getText().startsWith("clone")){
				 String[] tokens = debugTextArea.getText().split(",");
				 if(tokens.length == 3){
					 try{
						 int first = Integer.parseInt(tokens[1]);
						 int second = Integer.parseInt(tokens[2]);
						 ArrayList<Type> test = new ArrayList<Type>(object.subList(0, second));
						 ArrayList<Type> test2 = new ArrayList<Type>(object.subList(first, second));
						 for(int i =0; i<test2.size(); i++){
							 difference += test2.get(i).size + 12;
							 test2.set(i, test2.get(i).clone());
						 }
						 ArrayList<Type> test3 = new ArrayList<Type>(object.subList(second, object.size()));
						 test.addAll(test2);
						 test.addAll(test3);
						 object = new ArrayList<Type>(test);
						 loadTree();
					 }catch(Exception ex){
						 debugTextArea.setText("Wrong arguments"); 
					 }
				 }
			 }
			}
		});
		debugButton.setBounds(75, 239, 125, 23);
		panel_3.add(debugButton);
		
		JScrollPane jsp2= new JScrollPane(debugTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);	
		jsp2.setBounds(10, 143, 259, 87);
		panel_3.add(jsp2);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			/*	for(int i = 0; i<object.size(); i++){
					if(object.get(i).type == 1002){
						byte[] model = object.get(i).data;
						int index = model[24] & 0xFF;
							/*if(index != 0 && index != 2 && index !=3){
								System.out.println(i + "index is: " index);
							}
							int index2 = model[25] & 0xFF;
							if(index2 != 53 && index2!= 13){
								System.out.println(i + "index2 is: " + index2);
							}
						}
				}*/

				
				int index = 78;
				byte[] model = object.get(53).data;
				int count = toInteger(model, 32);
				for(int i = 0; i<count; i++){
					System.out.print(dataToString(Arrays.copyOfRange(model, index+40*i, index+40*i+24)));
					//System.out.print(dataToString(Arrays.copyOfRange(model, 78+40*i+32, 78+40*i+40)));
				}
			}
		});
		btnNewButton.setBounds(150, 350, 89, 23);
		panel_3.add(btnNewButton);
		

		
		
		textField = new JTextField();
		textField.setBounds(108, 5, 182, 20);
		frmBspviewer.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultTreeModel m_model = (DefaultTreeModel) tree.getModel();
				if(!previous.equalsIgnoreCase(textField.getText())){
					searchNode(textField.getText());
					searchCounter = 0;
			          if(searchNode.size() != 0){
			                //make the node visible by scroll to it
			                TreeNode[] nodes = m_model.getPathToRoot(searchNode.get(0));
			                tree.setExpandsSelectedPaths(true);              
			                tree.setSelectionPath(new TreePath(nodes));
			                tree.scrollPathToVisible(new TreePath(nodes));
			                previous = new String(textField.getText());
			                searchCounter++;
			                
			            }
				}else{
					if(searchCounter >= searchNode.size()){
						searchCounter = 0;
					}
					TreeNode[] nodes = m_model.getPathToRoot(searchNode.get(searchCounter));
                	tree.setExpandsSelectedPaths(true);              
	                tree.setSelectionPath(new TreePath(nodes));
	                tree.scrollPathToVisible(new TreePath(nodes));
                	searchCounter++;
				}

			}
		});
		
		
		searchButton.setBounds(10, 5, 89, 20);
		frmBspviewer.getContentPane().add(searchButton);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					int tabbedIndex =  tabbedPane.getSelectedIndex();
					if(tabbedIndex == 2){
						textArea.setText("" + dataToString(object.get(getTreeIndex()).data));
					}else{
						textArea.setText(null);
					}
				}catch(Exception ex){
				}
			}
		});
		
		textArea.addMouseListener(new MouseListener() {
			@Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()==3) {
                	try{
                	JPopupMenu menu = new JPopupMenu();
                	String printValues = new String(textArea.getSelectedText().replaceAll("\\s",""));
                	if(printValues.length() == 4){
                		menu.add(new JMenuItem("Short " + toShort(hexStringToByteArray(printValues), 0)));
                	}else if (printValues.length() == 8){
                		menu.add(new JMenuItem("Integer " + toInteger(hexStringToByteArray(printValues), 0)));
                		menu.add(new JMenuItem("Float " + toFloat(hexStringToByteArray(printValues), 0)));
                	}else if(printValues.length() == 2){
                		byte bajt = (byte)((Character.digit(printValues.charAt(0), 16) << 4)+ Character.digit(printValues.charAt(1), 16));
                		menu.add(new JMenuItem("Byte " + (bajt & 0xFF)));
                	}
                	menu.show(e.getComponent(), e.getX(), e.getY());
                	}catch(NullPointerException noSelect){}
                }
                
            }

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		 });
		
		JMenuBar menuBar = new JMenuBar();
		frmBspviewer.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					counter = 0;
					offset = 0;
					
			    	filter = new FileNameExtensionFilter("Bsp Format", "bsp");
			    	openFile.setDialogTitle("Open Bsp file");
                    openFile.setFileFilter(filter);
                    openFile.showOpenDialog(null);
			    	file = openFile.getSelectedFile();	
					if(file != null){
						bytes = Files.readAllBytes(file.toPath());
	            		if(Arrays.equals(Arrays.copyOfRange(bytes, 0, 2), new byte[]{(byte)31,(byte)139})){
							unzip_load();
						}
						openFile.setCurrentDirectory(file);
						Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathFile", file.getPath());
					
						object = new ArrayList<Type>();
						int i = 0;
						while(counter !=bytes.length){
	    					object.add(new Type());
	                		object.get(i).type = toInteger(bytes , counter);
	                		counter+=4;
	                		object.get(i).size = toInteger(bytes , counter);
		                	counter+=4;
	                		object.get(i).tof = toInteger(bytes , counter);
	                		counter+=4;
	                		data(object.get(i));
	                		i++;
	                	}
						TextureLoad();
						loadTree();
					}
				}catch (Exception e1) {
					e1.printStackTrace();
				}

				
			}
		});


		
		JMenu mnNewMenu_3 = new JMenu("Import");
		mnNewMenu_3.setEnabled(false);
		mnFile.add(mnNewMenu_3);
		
		JMenuItem mntmAnimation = new JMenuItem("Animation");
		mnNewMenu_3.add(mntmAnimation);
		
		JMenuItem mntmImportModel = new JMenuItem("Model");
		mnNewMenu_3.add(mntmImportModel);
		
		JMenu mnNewMenu = new JMenu("Export");
		mnFile.add(mnNewMenu);
	
		
		JMenuItem mntmAnimation_1 = new JMenuItem("Animation");
		mntmAnimation_1.setEnabled(false);
		mnNewMenu.add(mntmAnimation_1);
		
		JMenuItem mntmExportAll = new JMenuItem("All");
		mnNewMenu.add(mntmExportAll);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		
		mntmExportAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportAll();
			}
		});
		
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					savefile();
			}
		});
		
		JMenu mnOptions = new JMenu("View");
		menuBar.add(mnOptions);
		
		JMenu mnTexture = new JMenu("Model");
		mnOptions.add(mnTexture);
		
		texturesOn = new JCheckBoxMenuItem("Show Textures");
		texturesOn.setSelected(true);
		mnTexture.add(texturesOn);
		
		wireframe = new JCheckBoxMenuItem("Show Wireframe");
		mnTexture.add(wireframe);
		
		BonesOn = new JCheckBoxMenuItem("Show Bones");
		BonesOn.setSelected(true);
		mnTexture.add(BonesOn);
		
		showModel = new JCheckBoxMenuItem("Show Models");
		showModel.setSelected(true);
		mnTexture.add(showModel);
		
		JMenu mnDebug = new JMenu("Debug");
		mnOptions.add(mnDebug);
		
		debugMode = new JCheckBoxMenuItem("Debug Mode");
		mnDebug.add(debugMode);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Tutorials");
		mnHelp.add(mntmNewMenuItem);
		
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					Desktop.getDesktop().browse(new URL("http://bspviewer.wiki-site.com/index.php/Main_Page").toURI());
				}catch(Exception e){
					JOptionPane.showMessageDialog(null,"Your browser not support links: " + '\n' + "http://bspviewer.wiki-site.com/index.php/Main_Page");
				}
			}
		});
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null,"If you encounter any bugs, please let me know via:"
			    +'\n'+
			    "http://ghostmaster.proboards.com/thread/2983/bsp-viewer-bug-reports"
			    +'\n', "Bsp Viewer v2.0.5", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBspviewer = new JFrame();
		frmBspviewer.setResizable(false);
		frmBspviewer.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
		frmBspviewer.setTitle("Bsp_Viewer by Woitek1993");
		frmBspviewer.setBounds(100, 100, 600, 576);
		frmBspviewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmBspviewer.getContentPane().setLayout(null);
		
		
		if(!Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer")){
	        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer");
		}
		if(!Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathFile")){
	        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathFile", System.getProperty("user.home"));
		}
		if(!Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathImport")){
	        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathImport", System.getProperty("user.home"));
		}
		if(!Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExport")){
	        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExport", System.getProperty("user.home"));
		}
		if(!Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExportModel")){
	        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathExportModel", System.getProperty("user.home"));
		}
		if(!Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathImportModel")){
	        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\BspViewer", "pathImportModel", System.getProperty("user.home"));
		}
		loadChunks();		
	}
}
