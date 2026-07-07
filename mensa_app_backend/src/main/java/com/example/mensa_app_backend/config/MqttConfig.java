package com.example.mensa_app_backend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topics.speiseplan}")
    private String speiseplanTopic;

    @Value("${mqtt.topics.mensen}")
    private String mensenTopic;

    @Value("${mqtt.topics.orders}")
    private String ordersTopic;

    @Value("${mqtt.topics.votes}")
    private String votesTopic;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel ordersOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel votesOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "ordersOutboundChannel")
    public MessageHandler ordersOutboundHandler() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                clientId + "-pub-orders", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic(ordersTopic);
        handler.setDefaultRetained(false);
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "votesOutboundChannel")
    public MessageHandler votesOutboundHandler() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                clientId + "-pub-votes", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic(votesTopic);
        handler.setDefaultRetained(false);
        return handler;
    }

    @Bean
    public MessageChannel speiseplanInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mensenInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter speiseplanInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-sub-speiseplan", mqttClientFactory(), speiseplanTopic);
        adapter.setOutputChannel(speiseplanInboundChannel());
        return adapter;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mensenInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-sub-mensen", mqttClientFactory(), mensenTopic);
        adapter.setOutputChannel(mensenInboundChannel());
        return adapter;
    }

    public String getSpeiseplanTopic() { return speiseplanTopic; }
    public String getMensenTopic() { return mensenTopic; }
    public String getOrdersTopic() { return ordersTopic; }
    public String getVotesTopic() { return votesTopic; }
}
