package striperelativefrequency;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
public class StripesRelative extends Configured implements Tool{
	
	
	
	
public static class StripeMapper extends Mapper<LongWritable, Text, Text, CustomMapWritable>{

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        List<String> record = Arrays.asList(value.toString().split("\\s+"));
        int i=1;
        for(String term : record) {
            CustomMapWritable mapWritable = new CustomMapWritable();
            for(String v : record.subList(i,record.size())) {
                if(term.equals(v)) {
                    break;
                }
                Text mapKey = new Text(v);
                mapWritable.put(mapKey, mapWritable.containsKey(mapKey) ?
                        new IntWritable(((IntWritable) mapWritable.get(mapKey)).get() + 1) :
                        new IntWritable(1));
            }
//            System.out.println(term + " ==> " + mapWritable);
//            System.out.println("=======================");
            context.write(new Text(term), mapWritable);
            i++;
        }
    }
}



public static class StripeReducer extends Reducer<Text, CustomMapWritable,Text, CustomMapWritable> {

    @Override
    protected void reduce(Text key, Iterable<CustomMapWritable> values, Context context) throws IOException, InterruptedException {
        CustomMapWritable customMapWritable = new CustomMapWritable();
        for(MapWritable entry : values) {
            for(Map.Entry<Writable,Writable> data : entry.entrySet()) {
                if(customMapWritable.containsKey(data.getKey())) {
                    int v = ((IntWritable) customMapWritable.get(data.getKey())).get();
                    int sum = v + ((IntWritable) data.getValue()).get();
                    customMapWritable.put(data.getKey(), new IntWritable(sum));
                }else {
                    customMapWritable.put(data.getKey(),data.getValue());
                }
            }
        }
        int sum = 0;
        for(Map.Entry entry : customMapWritable.entrySet()) {
            sum += ((IntWritable) entry.getValue()).get();
        }
        CustomMapWritable result = new CustomMapWritable();
        for(Map.Entry entry : customMapWritable.entrySet()) {
            result.put((Text)entry.getKey(),
                    new Text(((IntWritable)entry.getValue()).get() + "/" + sum));
        }
        context.write(key, result);
    }

}

public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    FileSystem.get(conf).delete(new Path(args[1]));
    int res = ToolRunner.run(conf, new StripesRelative(), args);
    System.exit(res);
}


@Override
public int run(String[] args) throws Exception {
    Job job = new Job(getConf(), "Stripe");
    job.setJarByClass(StripesRelative.class);

    job.setMapperClass(StripeMapper.class);
    job.setReducerClass(StripeReducer.class);

//	job.setNumReduceTasks(2);

    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(CustomMapWritable.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(CustomMapWritable.class);

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    return job.waitForCompletion(true) ? 0 : 1;
}
}