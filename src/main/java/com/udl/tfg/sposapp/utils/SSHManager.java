package com.udl.tfg.sposapp.utils;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Hashtable;
import java.util.Properties;

@Service
public class SSHManager {

    @Value("${sshIdentityFile}")    private String sshIdentityFile;
    @Value("${sshIdentityPass}")    private String sshIdentityPass;
    @Value("${sshKnownHostsFile}")  private String sshKnownHostsFile;
    @Value("${localStorageFolder}") private String localStorageFolder;
    @Value("${sshStorageFolder}")   private String sshStorageFolder;

    private JSch jSch = null;

    public void Initialize() throws JSchException {
        if (jSch != null)
            throw new IllegalStateException("SSHUtils has been already initialized");

        jSch = new JSch();
        jSch.addIdentity(sshIdentityFile, sshIdentityPass);
        jSch.setKnownHosts(sshKnownHostsFile);
        System.out.println("JSCH Initialized");
    }

    public Session OpenSession(String address, int port, String username) throws Exception {
        return OpenSession(address, port, username, null);
    }

    public Session OpenSession(String address, int port, String username, Hashtable<Object, Object> properties) throws Exception {
        Session session;
        if (jSch == null)
            throw new NullPointerException("SSHUtils has not been initialized.");

        session = jSch.getSession(username, address, port);
        try {
            Properties config = new Properties();
            if (properties != null)
                config.putAll(properties);
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setConfig(config);
            session.connect();
            System.out.println("Session opened in " + address + " as " + username);
            return session;
        } catch (Exception e){
            throw new Exception(e);
        }
    }

    public void CleanPath(Session session, String destPath) throws Exception {
        try {
            ExecuteCommand(session, "rm -rf " + destPath.replace('\\', '/'));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void SendFile(Session session, long id, File sourceFile) throws Exception {
        if (sourceFile == null)
            return;

        String destPath = sshStorageFolder + "/" + String.valueOf(id) + "/" + sourceFile.getName();
        SendFile(session, sourceFile.getPath(), destPath);
    }

    public void SendFile(Session session, String sourcePath, String destPath) throws Exception {
        try {
            ChannelSftp channelSftp = (ChannelSftp) getChannel(session, "sftp");
            channelSftp.connect();

            File sourceFile = new File(sourcePath);
            File destFile = new File(destPath);
            if (!sourceFile.exists())
                throw new FileNotFoundException("Invalid source path.");

            ExecuteCommand(session, "mkdir -p " + destFile.getParent().replace('\\', '/'));
            channelSftp.cd(destFile.getParent().replace('\\', '/'));
            channelSftp.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);
            channelSftp.disconnect();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public String ExecuteCommand(Session session, String command) throws Exception {
        try {
            ChannelExec channelExec = (ChannelExec) getChannel(session, "exec");
            System.out.println("Running command: " + command);
            BufferedReader in = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            channelExec.setCommand(". ~/.bashrc && " + command);
            channelExec.setErrStream(System.err);
            channelExec.connect();

            String output = "";
            String msg;
            while((msg = in.readLine()) != null){
                System.out.println(msg);
                output += msg;
            }

            channelExec.disconnect();
            return output;
        } catch (Exception e){
            session.disconnect();
            throw new Exception(e);
        }
    }

    private Channel getChannel(Session session, String channelType) throws Exception {
        if (session == null)
            throw new NullPointerException("You must open a new session before open any channel");
        Channel channel = session.openChannel(channelType);
        if (channel == null)
            throw new Exception("Channel type does not exist");
        return channel;
    }
}
