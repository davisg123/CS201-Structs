import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class SimpleHuffProcessor implements IHuffProcessor {
	
	
    private HuffViewer myViewer;
    private TreeNode myHuffmanTreeRoot; 
    private HashMap<Integer, String> myMap; 
    
    private int[] vals = new int[256]; 
    //for compress
    public int bitsIn;
    private int bitsOut; 
    
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {

    }

    public int preprocessCompress(InputStream in) throws IOException {
    	//Begin with a forest of trees. All trees are one node, with the weight of 
    	//the tree equal to the weight of the character in the node. Characters that occur most frequently
    	//have the highest weights. Characters that occur least frequently have the smallest weights.
    	HashMap<Integer, TreeNode>/* weight, node*/ trees = new HashMap<Integer, TreeNode>(); 
    	BitInputStream bits = new BitInputStream(in);
    	bitsIn = 0; 
        int iter = bits.read(); 
        
        while(iter > 0){  	
        	//Any wording in this write-up that uses the word character means an 8-bit chunk
        	bitsIn += 8; 
        	if(trees.containsKey(iter)){
        		TreeNode node = trees.get(iter);
        		node.myWeight++;
        		trees.put(iter, node);
        	}
        	else{
        		TreeNode node = new TreeNode(iter, 1); 
        		trees.put(iter, node);
        	}
        	iter = bits.read(); 
        }
        bits.close(); 
        
        // create list of weights
        for(TreeNode t: trees.values()){
        	int value = t.myValue;
        	int weight = t.myWeight;
        	if(value>0){
        		vals[value] = weight; 
        	}
        }
       
        //Continue the process illustrated below in "queueProcess" until only one node is
        //left in the priority queue. This is the root of the Huffman tree.
        PriorityQueue<TreeNode> q = new PriorityQueue<TreeNode>(trees.values()); 
        TreeNode pseudoEofnod = new TreeNode(PSEUDO_EOF, 1); 
        q.add(pseudoEofnod); 
        myHuffmanTreeRoot = queueProcess(q); 
        
        //Create a table or map of 8-bit chunks (represented as an int value) 
        //to Huffman-codings.  The map has the 8-bit
        //int chunks as keys and the corresponding Huffman/chunk-coding
        //String as the value associated with the key.
        myMap = new HashMap<Integer, String>();  
        traversePath(myHuffmanTreeRoot, ""); 
        System.out.println(bitsIn);
        return 0; 
    }
    
    public TreeNode queueProcess(PriorityQueue<TreeNode> q){
    	TreeNode need; 
    	if(q.size() == 1){
    		need = q.poll(); 
    	}
    	else{
    		TreeNode firstIn = q.poll();
    		TreeNode newfirstIn = q.poll();
    		TreeNode intermNode = new TreeNode(firstIn.myValue*1000, 
    				newfirstIn.myWeight+firstIn.myWeight, firstIn, newfirstIn);
    		q.add(intermNode); 
    		need = queueProcess(q); 
    	}
    	return need; 
    }
    
    public void traversePath(TreeNode traverse, String path){
    	if(traverse.isLeaf()){
    		myMap.put(traverse.myValue, path); 
    		return; 
    	}
    	else{
    		traversePath(traverse.myLeft, path + "0");
    		traversePath(traverse.myRight, path + "1");
    	}
    }

    public void setViewer(HuffViewer viewer) {
        myViewer = viewer;
    }

    public int uncompress(InputStream in, OutputStream out) throws IOException {
        myViewer.showError("uncompress is not implemented");
        return 0;
    }
    
    private void showMessage(ArrayList<String> list){
        myViewer.update(list);
    }

    public int transform(InputStream in, OutputStream out) throws IOException{
    	BitInputStream bis = new BitInputStream(in);
        BitOutputStream bos = new BitOutputStream(out);
        int bitCount = 0;
        
        BurrowsWheeler bw = new BurrowsWheeler();
        while (true){
            char[] chunk = bw.transform(bis);
            if (chunk.length == 0) break;
            chunk = bw.mtf(chunk);
            byte[] array = new byte[chunk.length];
            for(int k=0; k < array.length; k++){
                array[k] = (byte) chunk[k];
            }
            ByteArrayInputStream bas = new ByteArrayInputStream(array);
            preprocessCompress(bas);
           
            int first = bw.getFirst();
            // write header information as appriopriate, e.g.,
            // magic-number and first
    }
        
        public int untransform(BitInputStream bis, OutputStream out) throws IOException{
            BurrowsWheeler bw = new BurrowsWheeler();
            int chunkCount = 1;
            while (true){
               int first = bis.readBits(BITS_PER_INT);  // read first index
               if (first == -1){
                   myViewer.showError("problem getting first index");
                   break;
               }

               ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
               BitOutputStream temp = new BitOutputStream(byteOut);
               doOriginalHuffUncompressAfterMagic(bis,temp);
               byte[] array = byteOut.toByteArray();
               char[] chunks = new char[array.length];
               for(int k=0; k < chunks.length; k++){
                   byte b = array[k];
                   char bp = (char) (b & 0xff);
                   chunks[k] = bp;
               }


               // TODO: write code here:
               // now that you have a char[] first call unmove-to-front
               // then call decode to untransform (you'll need first to do this)
               // then write out each char to the OutputStream out that's a parameter

               int header = bis.readBits(BITS_PER_INT);
               if (header == WHEELER_MAGIC){
                   chunkCount++;
               }
               else {
                   break;
               }
            }
            out.flush();
            return chunkCount;
        }

}
