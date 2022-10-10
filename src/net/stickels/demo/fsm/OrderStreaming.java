package net.stickels.demo.fsm;

import java.util.Map;
import java.util.Properties;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Util;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.IMap;

/**
 * This class builds the streaming pipeline for Hazelcast.  In this code,
 * I am using a bootstrapped instance, meaning Hazelcast just running in my
 * IDE.  But I also included a method to show how to attach to a running
 * instance.  In that scenario, you would need to submit this job to the
 * Hazelcast server manually from a jar file built from this source.
 * @author nstickels
 *
 */

public class OrderStreaming {
	
	private static final String TOPIC = "OrderTopic";
	private static final String MAP_NAME = "OrderMap";
	private static final ILogger log = Logger.getLogger(OrderStreaming.class);
	
	/**
	 * Every Hazelcast streaming process needs to have a main method which
	 * is used to build the streaming job.
	 * @param args
	 */
	public static void main(String[] args)
	{
		HazelcastInstance hz = Hazelcast.bootstrappedInstance();
		//HazelcastInstance hz = getHazelcastInstance();
		Pipeline p = buildPipeline(hz);
		hz.getJet().newJob(p);
	}

	private static HazelcastInstance getHazelcastInstance() 
	{
		Config config = new Config();
		config.setClusterName("fsm");
		config.getJetConfig().setEnabled(true);
		config.setLicenseKey("PLATFORM#9999Nodes#1KyPbMqinYQBW9kEAN0lUfdgwDCjTuGHOJZ86XSm5238911191991091010210111010019040419111231000");
		HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
		return hz;
	}


	private static Pipeline buildPipeline(HazelcastInstance hz) 
	{
		
		Pipeline p = Pipeline.create();
		// define kafka parameters
		Properties properties = new Properties();
		properties.put("bootstrap.servers", "localhost:9092");
		properties.put("key.deserializer", 
				"org.apache.kafka.common.serialization.StringDeserializer");
		properties.put("value.deserializer", 
				"org.apache.kafka.common.serialization.StringDeserializer");
		
		// IMap in Hazelcast for storing existing orders
		IMap<String, Order> orderMap = hz.getMap(MAP_NAME);
		
		// setup to stream events from Kafka
		StreamSource<Map.Entry<String, String>> source = 
				KafkaSources.kafka(properties,TOPIC);
		
		// get the orders from kafka
		StreamStage<TmpOrder> events = p.readFrom(source)
				.withoutTimestamps()
				.map(entry->OrderDeserializer.deserialize(entry.getValue()))
				.setName("Receive Order Event");
		
		
		// get the existing order from the Order Map
		StreamStage<Tuple2<TmpOrder,Order>> incomingAndExistingOrders = 
				events.mapUsingIMap(
				orderMap, // existing order map
				TmpOrder::getOrderIdString, 
				(tmpOrder,order) -> Tuple2.tuple2(tmpOrder, order))
				.setName("Get Existing Order");
		
		//create a service for updating the order
		ServiceFactory<?, OrderService> orderService = 
				ServiceFactories.sharedService(ctx->new OrderService())
				.toNonCooperative();
		
		// update the order if needed
		StreamStage<Order> updatedOrders = 
				incomingAndExistingOrders.mapUsingService(orderService,
						(service, tuple) -> 
				{
					TmpOrder incOrder = tuple.f0();
					Order extOrder = tuple.f1();
					return service.updateOrder(incOrder,extOrder);
				}).setName("Update Order if Needed");
		
		// setup updated order to write it out
		StreamStage<Map.Entry<String, Order>> ordersToWrite = updatedOrders.map(
				entry->(Util.entry(entry.getOrderIdString(), entry)))
				.setName("Setup Order to Write to Map");
		
		// write the order (with changes or not based on previous step) back
		// to the order map
		SinkStage sinkStage = ordersToWrite.writeTo(Sinks.map(orderMap))
				.setName("Write to Order Map");
		
		
		return p;
		
	}	
	
	
	

	


}
