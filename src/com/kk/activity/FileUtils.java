package com.kk.activity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class FileUtils {
	private String SDCardRoot;

	public String getSDCardRoot() {
		return SDCardRoot;
	}
	public FileUtils() {
		//�õ���ǰ�ⲿ�洢�豸��Ŀ¼
		SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	}
	/**
	 * ��SD���ϴ����ļ�
	 * 
	 * @throws IOException
	 */
	public File creatFileInSDCard(String fileName, String dir) throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		file.createNewFile();
		return file;
	}
	
	/**
	 * ��SD���ϴ���Ŀ¼
	 * 
	 * @param dirName
	 */
	public File creatSDDir(String dirName) {
		File dir = new File(SDCardRoot + dirName + File.separator);
		dir.mkdirs();
		return dir;
	}

	/**
	 * �ж�SD���ϵ��ļ����Ƿ����
	 */
	public boolean isFileExist(String fileName, String path){
		File file = new File(SDCardRoot + path + File.separator + fileName);
		return file.exists();
	}
	
	/**
	 * ��һ��InputStream���������д�뵽SD����
	 */
	public File write2SDFromInput(String path,String fileName,InputStream input){
		File file = null;
		/*test
		 * System.out.println("path--->"+path);
		System.out.println("name--->"+fileName);*/
		OutputStream output = null;
		try{
			creatSDDir(path);
			file = creatFileInSDCard(fileName, path);
			output = new FileOutputStream(file);
			byte buffer [] = new byte[4 * 1024];
			int temp;
			while((temp = input.read(buffer)) != -1){
				output.write(buffer,0,temp);
			}
			output.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				output.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return file;
	}
	

}