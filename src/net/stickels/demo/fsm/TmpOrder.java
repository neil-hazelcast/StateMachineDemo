package net.stickels.demo.fsm;

import java.io.Serializable;
import java.util.UUID;

public class TmpOrder implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5650726604505248488L;

	public enum OrderStatus
	{
		SUBMITTED {
			@Override
			public boolean isSubmitted()
			{
				return true;
			}
			@Override
			public String toString()
			{
				return "SUBMITTED";
			}
		},
		PROCESSING {
			@Override
			public boolean isProcessing()
			{
				return true;
			}
			@Override
			public String toString()
			{
				return "PROCESSING";
			}
		},
		READY_FOR_DELIVERY {
			@Override
			public boolean isReady()
			{
				return true;
			}
			@Override
			public String toString()
			{
				return "READY_FOR_DELIVERY";
			}
		},
		OUT_FOR_DELIVERY {
			@Override
			public boolean isOutForDelivery()
			{
				return true;
			}
			@Override
			public String toString()
			{
				return "OUT_FOR_DELIVERY";
			}
		},
		DELIVERED {
			@Override
			public boolean isDelivered()
			{
				return true;
			}
			@Override
			public String toString()
			{
				return "DELIVERED";
			}
		};
		
		public boolean isSubmitted() {return false;}
		public boolean isProcessing() {return false;}
		public boolean isReady() {return false;}
		public boolean isOutForDelivery() {return false;}
		public boolean isDelivered() {return false;}
		
		
		
	}
	
	private UUID orderId;
	private OrderStatus status;
	
	public TmpOrder()
	{
		orderId = UUID.randomUUID();
		status = OrderStatus.SUBMITTED;
	}
	
	public TmpOrder(UUID orderId)
	{
		this.orderId = orderId;
		status = OrderStatus.SUBMITTED;
	}
	
	public TmpOrder(UUID orderId, OrderStatus status)
	{
		this.orderId = orderId;
		this.status = status;
	}
	
	public TmpOrder(String orderId, int status)
	{
		this.orderId = UUID.fromString(orderId);
		this.status = getStatusForStep(status);
	}
	
	public int getStep()
	{
		return getStepForStatus(status);
	}
	
	public int getStepForStatus(OrderStatus newStatus)
	{
		switch(newStatus)
		{
			case SUBMITTED: return 0;
			case PROCESSING: return 1;
			case READY_FOR_DELIVERY: return 2;
			case OUT_FOR_DELIVERY: return 3;
			case DELIVERED: return 4;
		}
		return -1;
	}
	
	public static OrderStatus getStatusForStep(int step)
	{
		switch(step)
		{
			case 0: return OrderStatus.SUBMITTED;
			case 1: return OrderStatus.PROCESSING;
			case 2: return OrderStatus.READY_FOR_DELIVERY;
			case 3: return OrderStatus.OUT_FOR_DELIVERY;
			case 4: return OrderStatus.DELIVERED;
		}
		return OrderStatus.SUBMITTED;
	}
	
	public boolean transitionStatus(OrderStatus newStatus)
	{
		if(getStepForStatus(newStatus) <= getStepForStatus(status))
			return false;
		status = newStatus;
		return true;
	}
	
	public OrderStatus getStatus()
	{
		return status;
	}
	
	public UUID getOrderId()
	{
		return orderId;
	}
	
	public String getOrderIdString()
	{
		return orderId.toString();
	}
	
	public void setStatus(OrderStatus status)
	{
		this.status = status;
	}
	
	public void setOrderId(UUID orderId)
	{
		this.orderId = orderId;
	}
	

}
