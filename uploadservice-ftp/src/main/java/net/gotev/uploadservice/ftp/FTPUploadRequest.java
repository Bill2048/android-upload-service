package net.gotev.uploadservice.ftp;

import android.content.Context;
import android.content.Intent;

import net.gotev.uploadservice.UploadFile;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadRequest;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;
import net.gotev.uploadservice.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

/**
 * Creates a new FTP Upload Request.
 * @author Aleksandar Gotev
 */
public class FTPUploadRequest extends UploadRequest {

    protected final FTPUploadTaskParameters ftpParams = new FTPUploadTaskParameters();

    @Override
    protected Class<? extends UploadTask> getTaskClass() {
        return FTPUploadTask.class;
    }

    /**
     * Creates a new FTP upload request.
     *
     * @param context application context
     * @param uploadId unique ID to assign to this upload request.<br>
     *                 It can be whatever string you want, as long as it's unique.
     *                 If you set it to null or an empty string, an UUID will be automatically
     *                 generated.<br> It's advised to keep a reference to it in your code,
     *                 so when you receive status updates in {@link UploadServiceBroadcastReceiver},
     *                 you know to which upload they refer to.
     * @param serverUrl server IP address or hostname
     * @param port FTP port
     */
    public FTPUploadRequest(Context context, String uploadId, String serverUrl, int port) {
        super(context, uploadId, serverUrl);
        ftpParams.setPort(port);
    }

    /**
     * Creates a new FTP upload request and automatically generates an upload id that will
     * be returned when you call {@link UploadRequest#startUpload()}.
     *
     * @param context application context
     * @param serverUrl server IP address or hostname
     * @param port FTP port
     */
    public FTPUploadRequest(final Context context, final String serverUrl, int port) {
        this(context, null, serverUrl, port);
    }

    @Override
    protected void initializeIntent(Intent intent) {
        super.initializeIntent(intent);
        intent.putExtra(FTPUploadTaskParameters.PARAM_FTP_TASK_PARAMETERS, ftpParams);
    }

    @Override
    protected void validate() throws IllegalArgumentException, MalformedURLException {
        super.validate();

        if (ftpParams.getUsername() == null || "".equals(ftpParams.getUsername())) {
            throw new IllegalArgumentException("Specify FTP account username!");
        }

        if (ftpParams.getPassword() == null || "".equals(ftpParams.getPassword())) {
            throw new IllegalArgumentException("Specify FTP account password!");
        }

        if (ftpParams.getPort() <= 0) {
            throw new IllegalArgumentException("Specify FTP port!");
        }

        if (ftpParams.getSocketTimeout() < 2000) {
            throw new IllegalArgumentException("Set at least 2000ms socket timeout!");
        }

        if (ftpParams.getConnectTimeout() < 2000) {
            throw new IllegalArgumentException("Set at least 2000ms connect timeout!");
        }
    }

    /**
     * Set the credentials used to login on the FTP Server.
     * @param username account username
     * @param password account password
     * @return {@link FTPUploadRequest}
     */
    public FTPUploadRequest setUsernameAndPassword(String username, String password) {
        ftpParams.setUsername(username);
        ftpParams.setPassword(password);
        return this;
    }

    /**
     * Add a file to be uploaded.
     * @param filePath path to the local file on the device
     * @param remotePath absolute path (or relative path to the default remote working directory)
     *                   of the file on the FTP server. Valid paths are for example:
     *                   {@code /path/to/myfile.txt}, {@code relative/path/} or {@code myfile.zip}.
     *                   If any of the directories of the specified remote path does not exist,
     *                   they will be automatically created. You can also set with which permissions
     *                   to create them by using
     *                   {@link FTPUploadRequest#setCreatedDirectoriesPermissions(UnixPermissions)}
     *                   method.
     *                   <br><br>
     *                   Remember that if the remote path ends with {@code /}, the remote file name
     *                   will be the same as the local file, so for example if I'm uploading
     *                   {@code /home/alex/photos.zip} into {@code images/} remote path, I will have
     *                   {@code photos.zip} into the remote {@code images/} directory.
     *                   <br><br>
     *                   If the remote path does not end with {@code /}, the last path segment
     *                   will be used as the remote file name, so for example if I'm uploading
     *                   {@code /home/alex/photos.zip} into {@code images/vacations.zip}, I will
     *                   have {@code vacations.zip} into the remote {@code images/} directory.
     * @return {@link FTPUploadRequest}
     * @throws FileNotFoundException if the local file does not exist
     */
    public FTPUploadRequest addFileToUpload(String filePath, String remotePath) throws FileNotFoundException {
        return addFileToUpload(filePath, remotePath, null);
    }

    /**
     * Add a file to be uploaded.
     * @param filePath path to the local file on the device
     * @param remotePath absolute path (or relative path to the default remote working directory)
     *                   of the file on the FTP server. Valid paths are for example:
     *                   {@code /path/to/myfile.txt}, {@code relative/path/} or {@code myfile.zip}.
     *                   If any of the directories of the specified remote path does not exist,
     *                   they will be automatically created. You can also set with which permissions
     *                   to create them by using
     *                   {@link FTPUploadRequest#setCreatedDirectoriesPermissions(UnixPermissions)}
     *                   method.
     *                   <br><br>
     *                   Remember that if the remote path ends with {@code /}, the remote file name
     *                   will be the same as the local file, so for example if I'm uploading
     *                   {@code /home/alex/photos.zip} into {@code images/} remote path, I will have
     *                   {@code photos.zip} into the remote {@code images/} directory.
     *                   <br><br>
     *                   If the remote path does not end with {@code /}, the last path segment
     *                   will be used as the remote file name, so for example if I'm uploading
     *                   {@code /home/alex/photos.zip} into {@code images/vacations.zip}, I will
     *                   have {@code vacations.zip} into the remote {@code images/} directory.
     * @param permissions UNIX permissions for the uploaded file
     * @return {@link FTPUploadRequest}
     * @throws FileNotFoundException if the local file does not exist
     */
    public FTPUploadRequest addFileToUpload(String filePath, String remotePath, UnixPermissions permissions)
            throws FileNotFoundException {
        UploadFile file = new UploadFile(filePath);

        if (remotePath == null || remotePath.isEmpty()) {
            throw new IllegalArgumentException("You have to specify a remote path");
        }

        file.setProperty(FTPUploadTask.PARAM_REMOTE_PATH, remotePath);

        if (permissions != null) {
            file.setProperty(FTPUploadTask.PARAM_PERMISSIONS, permissions.toString());
        }

        params.addFile(file);
        return this;
    }

    /**
     * Add a file to be uploaded in the default working directory of the account used to login
     * into the FTP server. The uploaded file name will be the same as the local file name, so
     * if you are uploading {@code /path/to/myfile.txt}, you will have {@code myfile.txt}
     * inside the default remote working directory.
     * If any of the directories of the specified remote path does not exist,
     * they will be automatically created. You can also set with which permissions to create them
     * by using {@link FTPUploadRequest#setCreatedDirectoriesPermissions(UnixPermissions)}
     * method.
     * @param filePath path to the local file on the device
     * @return {@link FTPUploadRequest}
     * @throws FileNotFoundException if the local file does not exist
     */
    public FTPUploadRequest addFileToUpload(String filePath) throws FileNotFoundException {
        UploadFile file = new UploadFile(filePath);

        file.setProperty(FTPUploadTask.PARAM_REMOTE_PATH, new File(filePath).getName());

        params.addFile(file);
        return this;
    }

    /**
     * Sets the FTP connection timeout.
     * The default value is defined in {@link FTPUploadTaskParameters#DEFAULT_CONNECT_TIMEOUT}.
     * @param milliseconds timeout in milliseconds
     * @return {@link FTPUploadRequest}
     */
    public FTPUploadRequest setConnectTimeout(int milliseconds) {
        ftpParams.setConnectTimeout(milliseconds);
        return this;
    }

    /**
     * Sets FTP socket timeout. This affects login, logout and change working directory timeout.
     * The default value is defined in {@link FTPUploadTaskParameters#DEFAULT_SOCKET_TIMEOUT}.
     * @param milliseconds timeout in milliseconds
     * @return {@link FTPUploadRequest}
     */
    public FTPUploadRequest setSocketTimeout(int milliseconds) {
        ftpParams.setSocketTimeout(milliseconds);
        return this;
    }

    /**
     * Sets if the compressed file transfer mode should be used. If your server supports it, this
     * will allow you to use less bandwidth to transfer files, however some additional processing
     * has to be made on your device. By default compressed file transfer mode is disabled and if
     * enabled it works only if it's both supported and enabled on your FTP server.
     * @param value true to enable compressed file transfer mode, false to disable it and use the
     *              default streaming mode
     * @return {@link FTPUploadRequest}
     */
    public FTPUploadRequest useCompressedFileTransferMode(boolean value) {
        ftpParams.setCompressedFileTransfer(value);
        return this;
    }

    /**
     * Sets the UNIX permissions to set to newly created directories (if any). This may happen if
     * you upload files to directories which does not exist on your FTP server. They will be
     * automatically created. If <b>null</b> is set here or you never call this method,
     * the default permissions for new folders set on your FTP server will be applied.
     * @param permissions UNIX permissions to set to newly created directories
     * @return {@link FTPUploadRequest}
     */
    public FTPUploadRequest setCreatedDirectoriesPermissions(UnixPermissions permissions) {
        if (permissions == null)
            return this;

        ftpParams.setCreatedDirectoriesPermissions(permissions.toString());
        return this;
    }

    @Override
    public FTPUploadRequest setNotificationConfig(UploadNotificationConfig config) {
        super.setNotificationConfig(config);
        return this;
    }

    @Override
    public FTPUploadRequest setAutoDeleteFilesAfterSuccessfulUpload(boolean autoDeleteFiles) {
        super.setAutoDeleteFilesAfterSuccessfulUpload(autoDeleteFiles);
        return this;
    }

    @Override
    public FTPUploadRequest setMaxRetries(int maxRetries) {
        super.setMaxRetries(maxRetries);
        return this;
    }
}
