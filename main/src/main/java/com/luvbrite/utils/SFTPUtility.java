package com.luvbrite.utils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
@PropertySource("classpath:/env.properties")
public class SFTPUtility {

	@Autowired
	private Environment env;

	/*
	 * @Autowired
	 * 
	 * @Qualifier("getSession") Session sftpsession;
	 */
	private static Logger logger = Logger.getLogger(SFTPUtility.class);

	private static Session sftpSessionObj;

	// @Bean
	public Session getSession() {

		Session session = null;
		JSch jsch = new JSch();
		String user = env.getProperty("user");
		String host = env.getProperty("host");
		int port = Integer.parseInt(env.getProperty("sftpPort").trim());
		String privateKey = env.getProperty("privateKeyPath");

		//System.out.println("$$$Private Key" + privateKey);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");

		try {
			jsch.addIdentity(System.getProperty("user.home") + File.separator + privateKey);
			logger.info("$$Identity added for sftp");
			session = jsch.getSession(user, host, port);
			logger.info("$$ sftp session created.");
			session.setConfig(config);
		} catch (JSchException e) {
			logger.error("$$ sftp session creation failed");
			e.printStackTrace();
			return null;
		}

		return session;
	}

	public Session getSFTPSessionFromBean() {
		return getSession();
	}

	public void uploadFileToSFTP(File file) {

		// String imageUploadPath = env.getProperty("imagePath");
		String imageUploadPath = "/opt/bitnami/apache-tomcat/webapps/ROOT/static";
		Session session = getSession();
		try {
			session.connect();
			if (session.isConnected()) {
				Channel channel = session.openChannel("sftp");
				channel.setInputStream(System.in);
				channel.setOutputStream(System.out);
				channel.connect();
				if (channel.isConnected()) {
					System.out.println("shell channel connected....");
					ChannelSftp sftpChannel = (ChannelSftp) channel;
					sftpChannel.cd(imageUploadPath);
					sftpChannel.put(new FileInputStream(file), file.getName(), ChannelSftp.OVERWRITE);
					sftpChannel.exit();
					if (sftpChannel.isClosed()) {
						System.out.println("SftpChannel  is closed now....");
						System.out.println("Present Working Directotry.." + sftpChannel.pwd());
					}
					System.out.println("done....");
				} else {
					System.out.println("Could not connect the channel");
				}
			} else {
				System.out.println("Unable to connect to the session");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occured while ");

		} finally {
			try {
				if (session != null) {
					session.disconnect();
				    session = null;
				}
			} catch (Exception e) {

			}
		}

	}

	public void getFileUsingSFTP(File file) throws JSchException {
		JSch jsch = new JSch();

		String user = env.getProperty("user");
		String host = env.getProperty("host");
		int port = Integer.parseInt(env.getProperty("sftpPort").trim());

		String imageUploadPath = env.getProperty("imagePath");
		String privateKey = env.getProperty("privateKeyPath");

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");

		jsch.addIdentity(privateKey);
		System.out.println("identity added ");

		Session session = jsch.getSession(user, host, port);
		System.out.println("session created.");

		session.setConfig(config);
		try {

			session.connect();
			System.out.println("session connected.....");
			System.out.println(session.isConnected());

			Channel channel = session.openChannel("sftp");
			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);
			channel.connect();
			System.out.println("shell channel connected....");

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			// c.mkdir("/home/logicoy/test1");
			sftpChannel.cd(imageUploadPath);

			System.out.println(sftpChannel.pwd());
			// File f1 = new File("C:\\Users\\dell\\Documents\\pkt.JPG");
			sftpChannel.put(new FileInputStream(file), file.getName(), ChannelSftp.OVERWRITE);

			sftpChannel.exit();
			System.out.println("done");

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e);
		} finally {
			session.disconnect();

		}
	}

}
