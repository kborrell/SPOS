package com.udl.tfg.sposapp.utils;

import com.jcraft.jsch.*;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

@Service
public class SSHManager {

    private static final Logger logger = LoggerFactory.getLogger(SSHManager.class);

    @Value("${sshIdentityFile}") private String sshIdentityFile;
    @Value("${sshIdentityPass}") private String sshIdentityPass;
    @Value("${sshKnownHostsFile") private String sshKnownHostsFile;

    private JSch jSch = null;
    private Session session = null;

    public void Initialize() throws JSchException {
        if (jSch != null)
            throw new IllegalStateException("SSHUtils has been already initialized");

        jSch = new JSch();
        jSch.addIdentity(sshIdentityFile);
        jSch.setKnownHosts(sshKnownHostsFile);
        logger.debug("JSCH Initialized");
    }

    public void OpenSession(String address, int port, String username, String password) throws JSchException {
        OpenSession(address, port, username, password, new Hashtable<>());
    }

    public void OpenSession(String address, int port, String username, String password, Hashtable<Object, Object> properties) throws JSchException {
        if (session != null)
            throw new IllegalStateException("A session is still active. You must close it before open another one");

        if (jSch == null)
            throw new NullPointerException("SSHUtils has not been initialized.");

        session = jSch.getSession(username, address, port);
        session.setPassword(password);
        Properties config = new Properties();
        config.putAll(properties);
        session.setConfig(config);
        session.connect();
        logger.debug("Session opened in " + address + " as " + username);
    }

    public void SendFile(String sourcePath, String destPath) throws JSchException, IOException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) getChannel("sftp");
        ChannelExec channelExec = (ChannelExec) getChannel("exec");

        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        if (!sourceFile.exists())
            throw new FileNotFoundException("Invalid source path.");

        CreateDestPath(destFile.getParent(), channelExec);
        logger.debug("Moving to dest path...");
        channelSftp.cd(destFile.getParent());
        logger.debug("Sending file...");
        channelSftp.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);
        channelSftp.disconnect();
        logger.debug("Done!");
    }

    private void CreateDestPath(String destPath, ChannelExec channelExec) throws JSchException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
        logger.debug("Creating path on remote...");
        channelExec.setCommand("mkdir -p " + destPath);
        channelExec.setErrStream(System.err);
        channelExec.connect();

        String msg = null;
        while((msg = in.readLine()) != null){
            logger.debug(msg);
        }

        logger.debug("Finished creating path. Return code " + channelExec.getExitStatus());

        if (channelExec.isClosed())
            channelExec.disconnect();
    }

    private Channel getChannel(String channelType) throws JSchException {
        Channel channel = session.openChannel("channelType");
        channel.connect();
        return channel;
    }

    public void CloseSession(){
        session.disconnect();
        session = null;
    }
}
