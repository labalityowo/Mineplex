package com.mineplex.clansqueue.common;

import java.util.concurrent.TimeUnit;

public class QueueConstant
{
	public static final String SERVICE_MESSENGER_IDENTIFIER = "Queue System";
	public static final int BYPASS_QUEUE_WEIGHT = -1;
	public static final int MAX_TRANSFERS_PER_UPDATE = 5;
	public static final int MAXIMUM_WEIGHT_FROM_INCREASE = 6;
	public static final long TIME_TO_INCREASE_WEIGHT = TimeUnit.MINUTES.toMillis(10);
}