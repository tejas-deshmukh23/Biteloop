package com.tiffin.common.constants;

/**
 * Centralized Kafka topic names.
 * Used by both producers and consumers.
 * Never hardcode topic names in service code.
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String PROVIDER_REGISTERED    = "user.provider.registered";
    public static final String ORDER_PLACED           = "order.placed";
    public static final String ORDER_STATUS_UPDATED   = "order.status.updated";
    public static final String PAYMENT_SUCCESS        = "payment.success";
    public static final String PAYMENT_FAILED         = "payment.failed";
    public static final String PAYMENT_REFUNDED       = "payment.refunded";
    public static final String ORDER_CANCELLED        = "order.cancelled";
}