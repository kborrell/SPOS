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

    public void OpenSession(String address, int port, String username) throws Exception {
        OpenSession(address, port, username, null);
    }

    public void OpenSession(String address, int port, String username, Hashtable<Object, Object> properties) throws Exception {
        if (session != null)
            throw new IllegalStateException("A session is still active. You must close it before open another one");

        if (jSch == null)
            throw new NullPointerException("SSHUtils has not been initialized.");

        session = jSch.getSession(username, address, port);
        try {
            Properties config = new Properties();
            if (properties != null)
                config.putAll(properties);
            session.setConfig(config);
            session.connect();
            System.out.println("Session opened in " + address + " as " + username);
        } catch (Exception e){
            session = null;
            throw new Exception(e);
        }
    }

    public void CleanPath(String destPath) throws Exception {
        try {
            System.out.println("Cleaning dest path...");
            ExecuteCommand("rm -rf " + destPath.replace('\\', '/'));
            System.out.println("Cleaning done!");
        } catch (Exception e) {
            CloseSession();
            throw new Exception(e);
        }
    }

    public void SendFile(String sourcePath, String destPath) throws Exception {
        try {
            ChannelSftp channelSftp = (ChannelSftp) getChannel("sftp");
            channelSftp.connect();

            File sourceFile = new File(sourcePath);
            File destFile = new File(destPath);
            if (!sourceFile.exists())
                throw new FileNotFoundException("Invalid source path.");

            ExecuteCommand("mkdir -p " + destFile.getParent().replace('\\', '/'));
            System.out.println("Moving to dest path...");
            channelSftp.cd(destFile.getParent().replace('\\', '/'));
            System.out.println("Sending file...");
            channelSftp.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);
            channelSftp.disconnect();
            System.out.println("Done!");
        } catch (Exception e) {
            CloseSession();
            throw new Exception(e);
        }
    }

    public String ReadFile(String filePath) throws Exception {
        try {
            ChannelSftp channelSftp = (ChannelSftp) getChannel("sftp");
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

    public File ReceiveFile(String filePath, String destPath) throws Exception {
        try {
            ChannelSftp channelSftp = (ChannelSftp) getChannel("sftp");
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
            CloseSession();
            throw new Exception(e);
        }
    }

    public String ExecuteCommand(String command) throws Exception {
        try {
            ChannelExec channelExec = (ChannelExec) getChannel("exec");
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
            CloseSession();
            throw new Exception(e);
        }
    }

    private Channel getChannel(String channelType) throws JSchException, Exception {
        if (session == null)
            throw new NullPointerException("You must open a new session before open any channel");
        Channel channel = session.openChannel(channelType);
        if (channel == null)
            throw new Exception("Channel type does not exist");
        return channel;
    }

    public void CloseSession(){
        if (null != session){
            session.disconnect();
            session = null;
        }
    }

    public void collectChartData(int startTime, int finishTime) throws Exception {
        System.out.println("Collecting data...");
        ExecuteCommand("source collectData " + startTime + " " + finishTime);
    }

    public String getCPUData() throws Exception {
        try {
            return ReadFile("/var/lib/munin/168.101.113/cpuData.txt");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    public String getMemData() throws Exception {
        try {
            return ReadFile("/var/lib/munin/168.101.113/memData.txt");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
