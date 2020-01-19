package com.github.ralmnsk.analyser;

import com.github.ralmnsk.analyser.readerCsv.IReaderCsv;
import com.github.ralmnsk.analyser.readerCsv.ReaderCsv;

public class Application {

    public static void main(String[] args) {
        IReaderCsv reader=new ReaderCsv();
        reader.read("INPUT.CSV","20/08/2018 12:00:00","20/08/2018 13:00:00","Kwik-E-Mart");
    }

}