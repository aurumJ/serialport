/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lksoft.jin.serialporttool;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口工具 用于获取串口输入流及输出流
 * @author Jin
 * @version 1.0
 */
public class SerialPort {

    private static final String TAG = "SerialPort";

    static {
        System.loadLibrary("SerialPort-v1.0");
    }

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    /**
     * 构造方法,开启串口
     * @param device 串口文件
     * @param baudrate 波特率
     * @param dataBits 数据位
     * @param stopBits 停止位
     * @param parity 奇偶校验
     * @throws SecurityException 串口安全异常(权限异常)
     * @throws IOException 串口连接异常
     */
    public SerialPort(File device, int baudrate, int dataBits, int stopBits, char parity) throws SecurityException, IOException {
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        openSerialPort(device, baudrate, dataBits, stopBits, parity);
    }

    /**
     * 获得串口文件描述
     * @return 文件描述
     */
    public FileDescriptor getmFd() {
        return mFd;
    }


    /**
     * 开启串口
     * @param device 串口文件
     * @param baudrate 波特率
     * @param dataBits 数据位
     * @param stopBits 停止位
     * @param parity 奇偶校验
     * @throws IOException 串口连接异常
     */
    private void openSerialPort(File device, int baudrate, int dataBits, int stopBits, char parity) throws IOException {
        mFd = open(device.getAbsolutePath(), baudrate, dataBits, stopBits, parity);
        if (mFd == null) {
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    /**
     * 构造方法 用于开启串口
     * @param device 串口文件
     * @param baudrate 波特率
     * @param flags 标识
     * @throws SecurityException 串口安全异常(权限异常)
     * @throws IOException 串口连接异常
     */
    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        openSerialPort(device, baudrate, 8, 1, 'N');
    }

    /**
     * 获得输入流
     * @return 输入流
     */
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    /**
     * 获得输出流
     * @return 输出流
     */
    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /**
     * JNI 方法 打开串口
     *
     * @param path     串口路径
     * @param baudrate 波特率
     * @param flags 标识
     * @return 串口文件描述
     */
    private native static FileDescriptor open(String path, int baudrate, int flags);
    /**
     * JNI 方法 打开串口
     *
     * @param path     串口路径
     * @param baudrate 波特率
     * @param dataBits 数据位
     * @param stopBits 停止位
     * @param parity 奇偶校验
     * @return 串口文件描述
     */
    private native static FileDescriptor open(String path, int baudrate, int dataBits, int stopBits, char parity);

    /**
     * 断开串口连接
     */
    public native void close();


}
