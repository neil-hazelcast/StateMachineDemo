package net.stickels.demo.fsm;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Used to convert JSON to a TmpOrder object
 * @author nstickels
 *
 */
public class OrderDeserializer implements Deserializer<TmpOrder> {


	@Override
	public TmpOrder deserialize(String topic, byte[] data) {
		if(data == null)
			return null;
		ObjectMapper objectMapper = new ObjectMapper();
		try
		{
			JsonNode json = objectMapper.readTree(data);
			String orderId = json.get("orderId").asText();
			int status = json.get("newStatus").asInt();
			TmpOrder order = new TmpOrder(orderId, status);
			return order;
		} catch (JsonProcessingException e)
		{
			e.printStackTrace();
			return null;
		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static TmpOrder deserialize(String str) {
		
		if(str == null)
			return null;
		byte[] data = str.getBytes();
		ObjectMapper objectMapper = new ObjectMapper();
		try
		{
			JsonNode json = objectMapper.readTree(data);
			String orderId = json.get("orderId").asText();
			int status = json.get("newStatus").asInt();
			TmpOrder order = new TmpOrder(orderId, status);
			return order;
		} catch (JsonProcessingException e)
		{
			e.printStackTrace();
			return null;
		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
	}	

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	
	

}
