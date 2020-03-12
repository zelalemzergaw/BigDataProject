package pairInmapperandstripeinreducer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class PairStripeRelative  extends Configured implements Tool{
	
	
	
	public static class PairMapper extends Mapper<LongWritable,Text,Pair,IntWritable> {

	    private HashMap<Pair, Integer> hashMap;

	    @Override
	    protected void setup(Context context) throws IOException, InterruptedException {
	        hashMap = new HashMap<>();
	    }

	    @Override
	    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	        List<String> record = Arrays.asList(value.toString().split("\\s+"));
	        int i = 1;
	        for(String term : record) {
	            for(String v : record.subList(i,record.size())) {
	                if(term.equals(v)) {
	                    break;
	                }
	                Pair pair = new Pair(new Text(term), new Text(v));
	                hashMap.put(pair, hashMap.containsKey(pair) ? hashMap.get(pair) + 1 : 1);
	            }
	            i++;
	        }
	    }

	    @Override
	    protected void cleanup(Context context) throws IOException, InterruptedException {
	        for(Map.Entry entry : hashMap.entrySet()) {
	            context.write((Pair) entry.getKey(), new IntWritable((Integer) entry.getValue()));
	        }
	    }
	}
	
	
	    
	    public static class StripeReducer extends Reducer<Pair,IntWritable,Text, CustomMapWritable> {

	        private String prevTerm;
	        private HashMap<String,Integer> hashMap;

	        @Override
	        protected void setup(Context context) throws IOException, InterruptedException {
	            prevTerm = null;
	            hashMap = new HashMap<>();
	        }

	        @Override
	        protected void reduce(Pair key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
	            if(!key.getKey().toString().equals(prevTerm) && prevTerm != null) {
	                emit(context);
	                hashMap.clear();
	            }
	            hashMap.put(key.getValue().toString(), sum(values));
	            prevTerm = key.getKey().toString();
	        }

	        @Override
	        protected void cleanup(Context context) throws IOException, InterruptedException {
	            emit(context);
	        }

	        private int sum(Iterable<IntWritable> values) {
	            int sum = 0;
	            for(IntWritable value : values) {
	                sum += value.get();
	            }
	            return sum;
	        }

	        private int total() {
	            int sum = 0;
	            for(Map.Entry entry : hashMap.entrySet()) {
	                sum += (Integer) entry.getValue();
	            }
	            return sum;
	        }

	        private void emit(Context context) throws IOException, InterruptedException {
	            int total = total();
	            CustomMapWritable result = new CustomMapWritable();
	            for(Map.Entry entry : hashMap.entrySet()) {
	                result.put(new Text((String) entry.getKey()), new Text(entry.getValue() + "/" + total));
	            }
	            context.write(new Text(prevTerm), result);
	        }
	    }

	
	
	
	   public static void main(String[] args) throws Exception {
	        Configuration conf = new Configuration();
	        FileSystem.get(conf).delete(new Path(args[1]));
	        int res = ToolRunner.run(conf, new PairStripeRelative(), args);
	        System.exit(res);
	    }

	    @Override
	    public int run(String[] args) throws Exception {
	        Job job = new Job(getConf(), "PairStripeRelative");
	        job.setJarByClass(PairStripeRelative.class);

	        job.setMapperClass(PairMapper.class);
	        job.setReducerClass(StripeReducer.class);

//			job.setNumReduceTasks(2);

	        job.setMapOutputKeyClass(Pair.class);
	        job.setMapOutputValueClass(IntWritable.class);

	        job.setOutputKeyClass(Text.class);
	        job.setOutputValueClass(CustomMapWritable.class);

	        job.setInputFormatClass(TextInputFormat.class);
	        job.setOutputFormatClass(TextOutputFormat.class);

	        FileInputFormat.addInputPath(job, new Path(args[0]));
	        FileOutputFormat.setOutputPath(job, new Path(args[1]));

	        return job.waitForCompletion(true) ? 0 : 1;
	    }

}
