package com.udl.tfg.sposapp.utils;

import com.jcraft.jsch.*;
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

    private final String keyString = "AAAAB3NzaC1yc2EAAAADAQABAAABAQDQOZfsakkxrA01sgvEv+ECdr/" +
            "DcSC9M4fnCVotvS/zNK4ntE2aOTXavHJScIn4doNmLcHz66OLmvI4Og2/v0dKUsU75wkjURANqjtlBF/" +
            "Kk1aDD69kenBq7xYbDWs6iHey26gyfkfCUVQaT6LzSFR/05kIvdgfI6um3wrJ5cdYsBHVex4DOOttBqA/" +
            "nTmev1Th3JrmJN7D1b5YJwh0iuKeVVEprzJJaEpEAmaa5wTyfH6tDCcSJOGQpmLjIiJRdLbTgLuxDk0Cnyx" +
            "owk0mz00lv6j2sd0uoleIB53FylIP0EG6/mWdqFgmttWI+6iBq6qCUZnNpVG4kJIDIhaYUQg3";

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
        //if (session != null)
            //throw new IllegalStateException("A session is still active. You must close it before open another one");

        Session session = null;

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
            session = null;
            throw new Exception(e);
        }
    }

    public void CleanPath(Session session, String destPath) throws Exception {
        try {
            System.out.println("Cleaning dest path...");
            ExecuteCommand(session, "rm -rf " + destPath.replace('\\', '/'));
            System.out.println("Cleaning done!");
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
            System.out.println("Moving to dest path...");
            channelSftp.cd(destFile.getParent().replace('\\', '/'));
            System.out.println("Sending file...");
            channelSftp.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);
            channelSftp.disconnect();
            System.out.println("Done!");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public String ReadFile(Session session, String filePath) throws Exception {
        try {
            ChannelSftp channelSftp = (ChannelSftp) getChannel(session, "sftp");
            channelSftp.connect();
            InputStream out = channelSftp.get(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(out));
            String line;
            String content = "";
            while ((line = br.readLine()) != null){
                content += line + "\n";
            }
            br.close();
            channelSftp.disconnect();
            return content;
        }catch (Exception e) {
            throw new Exception(e);
        }
    }

    public File ReceiveFile(Session session, String filePath, String destPath) throws Exception {
        try {
            ChannelSftp channelSftp = (ChannelSftp) getChannel(session, "sftp");
            channelSftp.connect();

            File outputFile = new File(destPath);
            outputFile.mkdirs();

            if (outputFile.exists())
                outputFile.delete();
            else
                outputFile.createNewFile();

            FileOutputStream output = new FileOutputStream(outputFile);
            try {
                channelSftp.get(filePath, output);
            } catch (SftpException e){
            }finally {
                output.close();
                return outputFile;
            }
        }catch (Exception e) {
            throw new Exception(e);
        }
    }

    public File GetFile(long id, String ip, String fileName) throws Exception {
        //CloseSession();
        String srcPath = sshStorageFolder + "/" + String.valueOf(id) + "/" + fileName;
        Session session = OpenSession(ip, 22, "root");
        File f = ReceiveFile(session, srcPath, localStorageFolder + "/" + String.valueOf(id) + "/" + fileName);
        session.disconnect();
        return f;
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
            String msg = null;
            while((msg = in.readLine()) != null){
                System.out.println(msg);
                output += msg;
            }

            System.out.println("Finished running command. Return code " + channelExec.getExitStatus());
            channelExec.disconnect();
            return output;
        } catch (Exception e){
            session.disconnect();
            throw new Exception(e);
        }
    }

    private Channel getChannel(Session session, String channelType) throws JSchException, Exception {
        if (session == null)
            throw new NullPointerException("You must open a new session before open any channel");
        Channel channel = session.openChannel(channelType);
        if (channel == null)
            throw new Exception("Channel type does not exist");
        return channel;
    }
}
