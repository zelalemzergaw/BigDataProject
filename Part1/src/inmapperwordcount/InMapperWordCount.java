package inmapperwordcount;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class InMapperWordCount {
	
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
	    private final static IntWritable one = new IntWritable(1);
	    private Text word = new Text();
//	    private HashMap<String,Integer> hashmap;
	   // private Logger logger=Logger.getLogger(Map.class);
	    
//	    public void setUp(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
//	    	String line = value.toString().toLowerCase();
//	    	
//	    		
//	    	}	
//	    	
//	    }
//	    
//	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
//	         String line = value.toString();  // we have to change the in to string and we save it in line
//	    StringTokenizer tokenizer = new StringTokenizer(line);
//	   
//	    HashMap<Text,Integer> hashmap= new HashMap<>();
//	    //int count=0;
//	    int count =0;
//	    while (tokenizer.hasMoreTokens()) {
//	    	
//	        word.set(tokenizer.nextToken());
//	       // hashmap.put(word, one);
////	        if(hashmap.containsValue(word)) {
////	        	hashmap.put(word, one);
////	        }
////	       
////	        if(key.equals(word)) {
////	        	count =count +one.get(); 
////	        	hashmap.put(word, count);
////	        }
////
////	        }
//	        
//	        context.write(word, one);
	    private HashMap<String,Integer> words;

	    @Override
	    protected void setup(Context context) throws IOException, InterruptedException {
	        words = new HashMap<>();
	    }

	    @Override
	    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	        for (String word : value.toString().split(" "))
	        {
	            if(word.matches("^[A-Za-z]+$")) {
	                words.put(word.toLowerCase(),words.containsKey(word.toLowerCase()) ? words.get(word.toLowerCase()) + 1 : 1);
	            }
	        }
	    }

	    @Override
	    protected void cleanup(Context context) throws IOException, InterruptedException {
	        for(String key : words.keySet()) {
	            context.write(new Text(key), new IntWritable(words.get(key)));
	        }
	    }
	    }
	
	     
	 public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
	 
	    public void reduce(Text key, Iterable<IntWritable> values, Context context)
	      throws IOException, InterruptedException {
	        int sum = 0;
	    for (IntWritable val : values) {
	        sum += val.get();
	        
	    }
	    context.write(key, new IntWritable(sum));
	    }
	 }
	     
	 public static void main(String[] args) throws Exception {
	    Configuration conf = new Configuration();
	     
	    Job job = new Job(conf, "wordcount");
	    //Job job = Job.getInstance(conf, "wordcount");
	    job.setJarByClass(InMapperWordCount.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	     
	    job.setMapperClass(Map.class);
	    job.setReducerClass(Reduce.class);
	     
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	     
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	     
	    job.waitForCompletion(true);
	 }

}

