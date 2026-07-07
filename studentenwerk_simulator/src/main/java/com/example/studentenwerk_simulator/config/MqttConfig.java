package com.example.studentenwerk_simulator.config;

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
    public MessageChannel speiseplanOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mensenOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "speiseplanOutboundChannel")
    public MessageHandler speiseplanOutboundHandler() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                clientId + "-pub-speiseplan", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic(speiseplanTopic);
        handler.setDefaultRetained(true);
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "mensenOutboundChannel")
    public MessageHandler mensenOutboundHandler() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                clientId + "-pub-mensen", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic(mensenTopic);
        handler.setDefaultRetained(true);
        return handler;
    }

    @Bean
    public MessageChannel ordersInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel votesInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter ordersInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-sub-orders", mqttClientFactory(), ordersTopic);
        adapter.setOutputChannel(ordersInboundChannel());
        return adapter;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter votesInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-sub-votes", mqttClientFactory(), votesTopic);
        adapter.setOutputChannel(votesInboundChannel());
        return adapter;
    }

    public String getSpeiseplanTopic() { return speiseplanTopic; }
    public String getMensenTopic() { return mensenTopic; }
    public String getOrdersTopic() { return ordersTopic; }
    public String getVotesTopic() { return votesTopic; }
}
