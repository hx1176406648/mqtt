package com.hzdsn.mqtt;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Title: MqttSender.java
 * @description:MQTT发送提供者
 * @author: WQ
 * @date: 2019年9月3日
 */
@Slf4j
public class MqttSender implements MqttCallback {

	/**
	 * MqttClient实例
	 */
	private MqttClient client;
	/**
	 * MqttConfig配置信息
	 */
	private MqttConfig config;

	/**
	 * 是否标记销毁
	 */
	private boolean isDestory = false;

	/**
	 * 重连间隔时间
	 */
	private long retryInterval = 10000L;

	/**
	 * 是否已连接
	 */
	private boolean connected;

	public MqttSender(MqttConfig config) {
		this.config = config;
		conntion();
	}

	/**
	 * 是否连接
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return this.connected;
	}

	/**
	 * 连接
	 */
	public void conntion() {
		// 连接属性配置信息
		MqttConnectOptions options = new MqttConnectOptions();
		// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
		options.setCleanSession(true);
		// 设置连接的用户名
		options.setUserName(config.getUsername());
		// 设置连接的密码
		options.setPassword(config.getPassword().toCharArray());
		// 设置超时时间 单位为秒
		options.setConnectionTimeout(10);
		// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
		options.setKeepAliveInterval(30);
		try {
			/*// 判断是否为ssl，如果为ssl忽略证书连接
			if (config.getUrl().startsWith("ssl://")) {
				options.setSocketFactory(MqttSSLSocketFactory.getSSLSocketFactory());
			}*/

            options.setSocketFactory(MqttSSLSocketFactory.getSocketFactory("D:/ProgramFiles/emqtt/emqttd/etc/certs/mycert/cacert.pem", "D:/ProgramFiles/emqtt/emqttd/etc/certs/mycert/clientcert.pem", "D:/ProgramFiles/emqtt/emqttd/etc/certs/mycert/clientkey.pem", ""));

            // 创建client实例
			client = new MqttClient(config.getUrl(), config.getPrefix() + "_" + UUID.randomUUID().toString());
			client.connect(options);
			// 设置回调
			client.setCallback(this);
			connected = true;
			log.info("连接mqtt[{}]成功", this.config.getServerid());
        } catch (Exception e) {
			log.error("连接mqtt[{}]失败{}", this.config.getServerid(), e.getMessage());
		}
	}

	/**
	 * 销毁对象
	 */
	public void destory() {
		try {
			isDestory = true;
			this.client.disconnect();
			log.warn("关闭mqtt[{}]", this.config.getServerid());
		} catch (Exception e) {
			log.error("关闭mqtt[{}]失败{}", this.config.getServerid(), e.getMessage());
		}
		this.client = null;
	}

	/**
	 * 发送消息
	 * 
	 * @param topic 主题
	 * @param data  发送内容
	 * @return
	 */
	public boolean send(String topic, String data) {
        try {
            client.subscribe("testTopic");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        if (isDestory) {
			log.warn("发送mqtt[{}]消息失败，实例已经销毁", this.config.getServerid());
			return false;
		}
		MqttMessage message = new MqttMessage(data.getBytes());
		try {
			if (client == null && !isDestory) {
				conntion();
			}
			client.publish(topic, message);
			return true;
		} catch (Exception e) {
			log.error("发送mqtt[{}]消息失败{}", e.getMessage());
		}
		return false;
	}

	@Override
	public void connectionLost(Throwable cause) {
		connected = false;
		try {
			log.info("检测mqtt[{}]已断线！正在重新连接...:", this.config.getServerid());
			client.close();
			conntion();
		} catch (Exception e) {
			log.info("重连mqtt[{}]失败！:", this.config.getServerid());
		}
		if (!connected) {
			log.info("重连mqtt[{}]失败！{}s后重新连接...:", this.config.getServerid(), this.retryInterval / 1000);
			try {
				Thread.sleep(retryInterval);
				connectionLost(cause);
			} catch (Exception e) {
				log.info("重连mqtt[{}]线程异常！", this.config.getServerid());
			}
		}

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		log.info("收到来自 " + topic + " 的消息：{}", new String(message.getPayload()));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		log.info("发送mqtt[{}]消息成功", this.config.getServerid());
	}
}
