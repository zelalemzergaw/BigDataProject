package averagecomputation;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class AvarageComputation extends Configured implements Tool
{


	public static class AvarageComputationMapper extends Mapper<LongWritable, Text, Text, IntWritable>
	{
		private   IntWritable quantity;
		private Text word;

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
	
			 word = new Text();

				String record = value.toString();
				String[] fields = record.split(" ");
				word.set(fields[0]);
				int i = 0;
				try {
					i = Integer.parseInt(fields[fields.length - 1]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				quantity = new IntWritable(i);
				context.write(word, quantity);
			
           	}
	}
	

	public static class AvarageComputationReducer extends Reducer<Text, IntWritable, Text, DoubleWritable>
	{
		private DoubleWritable result = new DoubleWritable();

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
		{
			int sum = 0; int counter = 0;
			for (IntWritable val : values)
			{
				sum += val.get();
				counter++;
			}
			result.set(sum/counter);
			context.write(key, result);
		}
	}
	

	public static void main(String[] args) throws Exception
	{
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		if(fs.exists(new Path(args[1]))){
		   fs.delete(new Path(args[1]),true);
		}
		int res = ToolRunner.run(conf, new AvarageComputation(), args);

		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception
	{


		Job job = new Job(getConf(), "AvarageComputation");
		job.setJarByClass(AvarageComputation.class);
		
		//job.setNumReduceTasks(2);

		job.setMapperClass(AvarageComputationMapper.class);
		job.setReducerClass(AvarageComputationReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		return job.waitForCompletion(true) ? 0 : 1;
	}
}
