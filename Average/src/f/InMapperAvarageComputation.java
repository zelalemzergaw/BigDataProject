package f;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class InMapperAvarageComputation {

	public static class MapCombiner extends Mapper<LongWritable, Text, Text, IntPair> {
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			Map<String,IntPair> map = new HashMap<String,IntPair>();
			Double sum;
			Double count;
		
			String record = value.toString();
			String[] fields = record.split(" ");
			word.set(fields[0]);
			Double i = 0.0;
			
			try {
				i = Double.parseDouble(fields[fields.length - 1]);
				} catch (Exception e) {
					
					System.err.println(e);
				}
			Double quantity = new Double(i);
			
			if(map.containsKey(word)){
				sum = map.get(word).getQuantity() + quantity;
				count = map.get(word).getCount() + 1;
			}
			else{
				sum = quantity;
				count = 1.0;
			}
		
			
			context.write(word, new IntPair(sum, count));
			System.out.println("Map output ===== word: " + word.toString()
					+ " quantity: " + quantity);
		}
	}

	public static class Reduce extends
			Reducer<Text, IntPair, Text, DoubleWritable> {
		private DoubleWritable result = new DoubleWritable();

		public void reduce(Text key, Iterable<IntPair> values, Context context)
				throws IOException, InterruptedException {

			Double sum = 0.0;
			Double count = 0.0;
			for (IntPair val : values) {
				sum += val.getQuantity();
				count += val.getCount();
			}
			result.set(sum / count);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws IllegalArgumentException,
			IOException, ClassNotFoundException, InterruptedException {
		/* Provides access to configuration parameters */
		Configuration conf = new Configuration();
		/* Creating Filesystem object with the configuration */
		FileSystem fs = FileSystem.get(conf);
		/* Check if output path (args[1])exist or not */
		if (fs.exists(new Path(args[1]))) {
			/* If exist delete the output path */
			fs.delete(new Path(args[1]), true);
		}

		// Configuration conf = new Configuration();
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "avecomp");
		job.setJarByClass(InMapperAvarageComputation.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntPair.class);

		job.setMapperClass(MapCombiner.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}

}