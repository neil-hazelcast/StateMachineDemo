package net.stickels.demo.fsm;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import net.stickels.demo.fsm.Order.OrderStatus;

/**
 * OrderService is used to compare the incoming order and existing order and
 * the states of each to determine how to handle the update.
 * @author nstickels
 *
 */
public class OrderService {
	private static final ILogger log = Logger.getLogger(OrderService.class);
	
	/**
	 * This method does the work to compare the state of the incoming order and
	 * the state of the existing order and determine if a transition is needed
	 * @param incomingTmp - The incoming order with a new state
	 * @param existing - Existing order from the Order map
	 * @return the latest version of the order with an updated state if needed
	 */
	public Order updateOrder(TmpOrder incomingTmp, Order existing)
	{
		// convert the TmpOrder to an Order so that we are comparing apples to apples
		Order incoming = new Order(incomingTmp.getOrderIdString(),incomingTmp.getStep());
		if(existing == null)
		{
			// this means it couldn't find an existing order in the IMap, in that
			// case, we will just add this new incoming order to the Order IMap
			log.info("No existing order for "+incoming.getOrderIdString()
				+" adding it to OrderMap");
			return incoming;
		}
		// get the status of the existing (just used for logging purposes here)
		OrderStatus existingInitial = existing.getStatus();
		// do the transition
		boolean result = existing.transitionStatus(incoming.getStatus());
		// order was transitioned to a new state
		if(result)
			log.info("Updated "+incoming.getOrderIdString()+" from "
					+existingInitial+" to "+incoming.getStatus());
		// order didn't need to transition
		else
			log.info("No need to update "+incoming.getOrderIdString()+" from "
						+existingInitial+" to "+incoming.getStatus());
		return existing;
	}

}
