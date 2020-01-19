package com.github.ralmnsk.analyser.readerCsv;

import com.github.ralmnsk.analyser.Transaction.Transaction;

import java.util.List;

public interface IReaderCsv {
    List<Transaction> read(String fileName,String fromDate,String toDate,String merchant);
}
