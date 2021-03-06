package com.hg.hyy.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

public class Filestrem {

    public Filestrem() {
    }

    public void bis_bos() throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream("logs\\1.txt"));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("logs\\2.txt"));

        // int ch;
        // while ((ch = bis.read()) != -1) {
        // System.out.print((char)ch);
        // }

        byte[] b = new byte[1024];
        int len;
        while ((len = bis.read(b)) != -1) {
            System.out.print(new String(b, 0, len));
            bos.write(b, 0, len);
            bos.flush();
        }

        bis.close();
        bos.close();
    }

    public void br_bw() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("logs\\1.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("logs\\2.txt"));

        InputStreamReader isr = new InputStreamReader(new FileInputStream("logs\\3.txt"), "GBK");
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("logs\\4.txt"), "utf-8");

        char[] b = new char[1024];
        int len;
        while ((len = isr.read(b)) != -1) {
            System.out.print(new String(b, 0, len));
            osw.write(b, 0, len);
            osw.flush();
        }

        isr.close();
        osw.close();

        String s;
        while ((s = br.readLine()) != null) {

            try {
              bw.write(s);  
            } catch (NullPointerException e) {
                
            }
            
            bw.newLine();
            bw.flush();

        }

        br.close();
        bw.close();
    }

    public void userrecord(ArrayList<String> al) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter("logs\\2.txt"));
        for (String s : al) {

            try {
                bw.write(s);  
              } catch (NullPointerException e) {
                  
              }
            bw.newLine();
            bw.flush();
        }
        bw.close();
    }

    public void record(Object O) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("logs\\1.txt", true));

        oos.writeObject(O);
        oos.close();
    }
}
