package com.synix.uwait_backend.service.emailsend;

public enum EmailTemplate {

    OWNER_WELCOME("sendgrid.template.owner-welcome"),
    ORDER_INFO_MESSAGE("sendgrid.order.info.template-id"),
    PAYMENT_CONFIRMED("sendgrid.template.payment-confirmed"),
    SUPPORT_TICKET("sendgrid.support.ticket.template-id"),
    ATTACHMENT("sendgrid.attachment.template-id"),
    SUBSCRIPTION_ALERT("sendgrid.template.subscription-alert");

    private final String propertyKey;

    EmailTemplate(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}

