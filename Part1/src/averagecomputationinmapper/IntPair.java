package averagecomputationinmapper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

public class IntPair implements Writable {
	Double quantity;
	Double count;
	
	public Double getQuantity() {
		return quantity;
	}
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	public Double getCount() {
		return count;
	}
	public void setCount(Double count) {
		this.count = count;
	}
	public void readFields(DataInput arg0) throws IOException {
		quantity = arg0.readDouble();
		count = arg0.readDouble();
		
	}
	public void write(DataOutput arg0) throws IOException {
		arg0.writeDouble(quantity);
		arg0.writeDouble(count);
		
	}
	public IntPair(Double quantity, Double count) {
		this.quantity = quantity;
		this.count = count;
	}
	public IntPair() {
	}
	
	

}