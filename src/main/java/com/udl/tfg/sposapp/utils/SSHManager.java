package com.udl.tfg.sposapp.utils;

import com.jcraft.jsch.*;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Hashtable;
import java.util.Properties;

@Service
public class SSHManager {

    private static final Logger logger = LoggerFactory.getLogger(SSHManager.class);

    @Value("${sshIdentityFile}") private String sshIdentityFile;
    @Value("${sshIdentityPass}") private String sshIdentityPass;
    @Value("${sshKnownHostsFile}") private String sshKnownHostsFile;

    private JSch jSch = null;
    private Session session = null;

    public void Initialize() throws JSchException {
        if (jSch != null)
            throw new IllegalStateException("SSHUtils has been already initialized");

        jSch = new JSch();
        jSch.addIdentity(sshIdentityFile, sshIdentityPass);
        jSch.setKnownHosts(sshKnownHostsFile);
        System.out.println("JSCH Initialized");
    }

    public void OpenSession(String address, int port, String username, String password) throws JSchException {
        OpenSession(address, port, username, password, null);
    }

    public void OpenSession(String address, int port, String username, String password, Hashtable<Object, Object> properties) throws JSchException {
        if (session != null)
            throw new IllegalStateException("A session is still active. You must close it before open another one");

        if (jSch == null)
            throw new NullPointerException("SSHUtils has not been initialized.");

        session = jSch.getSession(username, address, port);
        session.setPassword(password);
        Properties config = new Properties();
        if (properties != null)
            config.putAll(properties);
        session.setConfig(config);
        session.connect();
        System.out.println("Session opened in " + address + " as " + username);
    }

    public void SendFile(String sourcePath, String destPath) throws Exception {
        try{
            ChannelSftp channelSftp = (ChannelSftp) getChannel("sftp");
            channelSftp.connect();

            File sourceFile = new File(sourcePath);
            File destFile = new File(destPath);
            if (!sourceFile.exists())
                throw new FileNotFoundException("Invalid source path.");

            ExecuteCommand("mkdir -p " +  destFile.getParent().replace('\\', '/'));
            System.out.println("Moving to dest path...");
            channelSftp.cd(destFile.getParent().replace('\\', '/'));
            System.out.println("Sending file...");
            channelSftp.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);
            channelSftp.disconnect();
            System.out.println("Done!");
        } catch (Exception e){
            throw new Exception(e);
        } finally {
            CloseSession();
        }
    }

    private void ExecuteCommand(String command) throws Exception {
        try {
            ChannelExec channelExec = (ChannelExec) getChannel("exec");
            System.out.println("Running command: " + command);
            BufferedReader in = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            channelExec.setCommand(command);
            channelExec.setErrStream(System.err);
            channelExec.connect();

            String msg = null;
            while((msg = in.readLine()) != null){
                System.out.println(msg);
            }

            System.out.println("Finished running command. Return code " + channelExec.getExitStatus());
            channelExec.disconnect();
        } catch (Exception e){
            throw new Exception(e);
        } finally {
            CloseSession();
        }
    }

    private Channel getChannel(String channelType) throws JSchException, InvalidArgumentException {
        if (session == null)
            throw new NullPointerException("You must open a new session before open any channel");
        Channel channel = session.openChannel(channelType);
        if (channel == null)
            throw new InvalidArgumentException(new String[]{"Channel type does not exist"});
        return channel;
    }

    public void CloseSession(){
        session.disconnect();
        session = null;
    }
}
