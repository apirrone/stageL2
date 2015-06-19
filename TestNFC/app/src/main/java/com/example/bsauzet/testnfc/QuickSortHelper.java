package com.example.bsauzet.testnfc;

import java.util.List;

/**
 * Created by bsauzet on 19/06/15.
 */
public class QuickSortHelper {

    public static void sortMessagesByDate(List<Message> m){
        if(m.size()>0)
            quickSort(0, m.size() -1, m);
    }

    public static void quickSort(int lowerIndex, int higherIndex, List<Message> m){

        int i = lowerIndex;
        int j = higherIndex;

        double pivot = m.get(lowerIndex+(higherIndex-lowerIndex)/2).getDate();

        while (i <= j) {
            while (m.get(i).getDate() < pivot) {
                i++;
            }
            while (m.get(j).getDate() > pivot) {
                j--;
            }
            if (i <= j) {
                exchangeMessages(i, j, m);
                i++;
                j--;
            }
        }
        if (lowerIndex < j)
            quickSort(lowerIndex, j, m);
        if (i < higherIndex)
            quickSort(i, higherIndex, m);
    }
    private static void exchangeMessages(int i, int j, List<Message> m) {
        Message temp = m.get(i);
        m.set(i,m.get(j));
        m.set(j,temp);
    }



}
