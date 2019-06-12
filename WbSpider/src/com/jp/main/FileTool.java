package com.jp.main;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/*  本类主要是 下载那些已经访问过的文件*/
public class FileTool {

    private static String dirPath;

    /*
    *  生成目录
    * */
    private static void mkdir() {
        if (dirPath == null) {
            dirPath = Class.class.getClass().getResource("/").getPath() + "temp\\";
        }
        File fileDir = new File(dirPath);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
    }

    /**
     * 保存网页字节数组到本地文件，filePath 为要保存的文件的相对地址
     */

    public static void saveToLocal(String fileName,String content) {
        mkdir();
        String filePath = dirPath + fileName ;
        byte[] data = content.getBytes();
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(filePath)));
            for (int i = 0; i < data.length; i++) {
                out.write(data[i]);
            }
            out.flush();
            out.close();
            System.out.println("文件："+ fileName + "已经被存储在"+ filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
