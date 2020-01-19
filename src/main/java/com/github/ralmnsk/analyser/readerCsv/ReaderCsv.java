package com.github.ralmnsk.analyser.readerCsv;

import com.github.ralmnsk.analyser.Transaction.Transaction;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class ReaderCsv implements IReaderCsv{
    private File file;


    public List<Transaction> read(String fileName,String fromStr,String toStr,String merchant){
        file=new File(fileName); //"INPUT.TXT"
        List<Transaction> list=new LinkedList<>();
        LocalDateTime timeFrom=timeConvert(fromStr);
        LocalDateTime timeTo=timeConvert(toStr);
        String b=new String();
        String str=new String();
        CurrencyUnit usd = Monetary.getCurrency("USD");

        try(BufferedReader fis=new BufferedReader(new FileReader(file));){
            while((b=fis.readLine())!=null){
                str=str+b;
            }

            String[] strings=str.trim().split(",");
            int counter=0;
            String id=new String();

            while(counter<=strings.length-1){
                    Transaction tx=new Transaction();
                    if(strings[counter].length()>9){
                        id=strings[counter].trim().substring(8,16);
                    }
                    if(strings[counter].length()<9){
                        id=strings[counter].trim();
                    }
                        tx.setId(id);
                    counter++;
                    if(counter>strings.length-1){
                        break;
                    }
                LocalDateTime time = timeConvert(strings[counter].trim());
                tx.setTime(time);
                counter++;

                float amount=Float.parseFloat(strings[counter].trim());
                Money money = Money.of(amount, usd);
                tx.setMoney(money);

                counter++;
                String merchantInFile=strings[counter].trim();
                tx.setMerchant(merchantInFile);
                counter++;
                String typeTransaction=strings[counter].trim();
                tx.setTypeTransaction(typeTransaction);
                counter++;
                if(typeTransaction.equals("REVERSAL")&&(counter<=strings.length-1)){
                    String relatedTransaction=strings[counter].trim().substring(0,8);
                    tx.setRelatedTransaction(relatedTransaction);
                }
                System.out.println(id+" "+time+" "+money+" "+merchantInFile+" "+typeTransaction+" ");
                if(isMatchByMerchantAndTime(timeFrom,tx,merchant)){
                    list.add(tx);
                }
            }

            List<Transaction> reversals=list
                    .stream()
                    .filter(t->t.getRelatedTransaction()!=null).collect(Collectors.toList());

            Iterator<Transaction> iteratorList = list.iterator();
            for (Transaction reversal : reversals) {
                while(iteratorList.hasNext()) {
                    Transaction t=iteratorList.next();
                    if (t.equals(reversal)) {
                        iteratorList.remove();
                    }
                }
            }

            Iterator<Transaction> iterator = list.iterator();
            for (Transaction r : reversals) {
                while (iterator.hasNext()) {
                    Transaction t=iterator.next();
                    if (r.getRelatedTransaction().equals(t.getId())) {
                        iterator.remove();
                    }
                }
            }

            list=list.stream().filter(t->isMatchLessEndTime(timeTo,t)).collect(Collectors.toList());
            Money average=average(list,usd);

            System.out.println("Number of transactions = "+list.size());
            NumberFormat formatter = new DecimalFormat( "#.##" );
            System.out.println("Average Transaction Value = "+formatter.format(average.getNumber().doubleValue()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private Money average(List<Transaction> list, CurrencyUnit currencyUnit) {
        Money sum = Money.of(0, currencyUnit);
        for (Transaction t : list) {
            sum=sum.add(t.getMoney());
        }
        sum=sum.divide(list.size());

        return sum;
    }

    private boolean isMatchByMerchantAndTime(LocalDateTime start,Transaction tx,String merchant){
        LocalDateTime time=tx.getTime();
        ZonedDateTime zdt = ZonedDateTime.of(time, ZoneId.systemDefault());
        long timeLong = zdt.toInstant().toEpochMilli();
        long zStart=ZonedDateTime.of(start, ZoneId.systemDefault()).toInstant().toEpochMilli();
        if((timeLong>=zStart)&&(tx.getMerchant().equals(merchant))){
//            System.out.println("time is in the range>=start:"+tx.getId());
            return true;
        }
        return false;
    }

    private boolean isMatchLessEndTime(LocalDateTime end,Transaction tx){
        LocalDateTime time=tx.getTime();
        ZonedDateTime zdt = ZonedDateTime.of(time, ZoneId.systemDefault());
        long timeLong = zdt.toInstant().toEpochMilli();
        long zEnd=ZonedDateTime.of(end, ZoneId.systemDefault()).toInstant().toEpochMilli();
        if((timeLong<=zEnd)){
//            System.out.println("time is in the range<=end:"+tx.getId());
            return true;
        }
        return false;
    }

    private LocalDateTime timeConvert(String trim) {
        String str[]=trim.split(" ");
        String dateStr[]=str[0].split("/");
        String timeStr[]=str[1].split(":");
        LocalDateTime time=LocalDateTime.of(Integer.parseInt(dateStr[2]),
                                            Integer.parseInt(dateStr[1]),
                                            Integer.parseInt(dateStr[0]),
                                            Integer.parseInt(timeStr[0]),
                                            Integer.parseInt(timeStr[1]),
                                            Integer.parseInt(timeStr[2]));
        return time;
    }



}
