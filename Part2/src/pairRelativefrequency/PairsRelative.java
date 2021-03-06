package pairRelativefrequency;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
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
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PairsRelative extends Configured implements Tool {
	
	
	public static class PairsMapper extends Mapper<LongWritable, Text, Pair, IntWritable> {

	    @Override
	    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	        List<String> record = Arrays.asList(value.toString().split("\\s+"));
	        int i = 1;
	        for(String term : record) {
	            for(String v : record.subList(i,record.size())) {
	                if(term.equals(v)) {
	                    break;
	                }

	                context.write(new Pair(new Text(term), new Text(v)), new IntWritable(1));
	                context.write(new Pair(new Text(term), new Text("*")), new IntWritable(1));
	            }
	            i++;
	        }
	    }
	}

	public static class PairsReducer extends Reducer<Pair, IntWritable, Pair, Text> {

	    private int sum;

	    @Override
	    protected void setup(Context context) throws IOException, InterruptedException {
	        sum = 0;
	    }

	    @Override
	    protected void reduce(Pair key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
	        int s = 0;
	        for(IntWritable value : values) {
	            s += value.get();
	        }
	        if(key.getValue().toString().equals("*")) {
	            sum = s;
	        }else {
	            context.write(key, new Text(s + "/" + sum));
	        }
	    }

	}

	
	public static void main(String[] args) throws Exception
	{
		Configuration conf = new Configuration();
		/* Creating Filesystem object with the configuration */
		FileSystem fs = FileSystem.get(conf);
		/* Check if output path (args[1])exist or not */
		if (fs.exists(new Path(args[1]))) {
			/* If exist delete the output path */
			fs.delete(new Path(args[1]), true);
		}

		int res = ToolRunner.run(conf, new PairsRelative(), args);

		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception
	{


		Job job = new Job(getConf(), "PairsRelative");
		job.setJarByClass(PairsRelative.class);
		
		//job.setNumReduceTasks(2);

		job.setMapperClass(PairsMapper.class);
		job.setReducerClass(PairsReducer.class);
		//  job.setMapOutputKeyClass(Pair.class);
		job.setOutputKeyClass(Pair.class);
		job.setOutputValueClass(IntWritable.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		return job.waitForCompletion(true) ? 0 : 1;
		
	}

}
