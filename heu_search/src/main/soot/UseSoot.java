package soot;

import p_heu.entity.ReadWriteNode;
import soot.structure.entity.CommonCaller;

import java.util.Set;

public class UseSoot {

    //����ģʽ
    private static UseSoot useSoot = new UseSoot();

    private UseSoot() {
    }

    public static UseSoot getInstance() {
        return useSoot;
    }

    //��������
    private int minLine = Integer.MAX_VALUE;
    private int maxLine = 0;

    //�������ļ�
    private String syncJava = "";

    public int getMinLine() {
        return minLine;
    }

    public int getMaxLine() {
        return maxLine;
    }

    public String getSyncJava() {
        return syncJava;
    }

    public static void main(String[] args) {
        /*ReadWriteNode rw1 = new ReadWriteNode(1, "datarace.CustomerInfo@16f", "accounts", "WRITE", "main", "datarace/Account.java:12");
        ReadWriteNode rw2 = new ReadWriteNode(2, "datarace.CustomerInfo@16f", "accounts", "READ", "Thread-1", "datarace/Account.java:8");*/
        ReadWriteNode rw1 = new ReadWriteNode(1, "manager.Manager", "flag", "WRITE", "main", "manager/Trelease.java:19");
        ReadWriteNode rw2 = new ReadWriteNode(2, "manager.Manager", "flag", "READ", "Thread-1", "manager/Manager.java:11");
        UseSoot useSoot = UseSoot.getInstance();
        useSoot.getCallGraph(rw1, rw2);
        System.out.println(useSoot.getMinLine());
        System.out.println(useSoot.getMaxLine());
        System.out.println(useSoot.getSyncJava());
    }

    public  void getCallGraph(ReadWriteNode rw1, ReadWriteNode rw2) {
        //��������
        String positionOne = rw1.getPosition();
        String classNameOne = positionOne.split("\\.")[0].replaceAll("/", ".");
        int classLineOne = Integer.parseInt(positionOne.split(":")[1]);
        String positionTwo = rw2.getPosition();
        String classNameTwo = positionTwo.split("\\.")[0].replaceAll("/", ".");
        int classLineTwo = Integer.parseInt(positionTwo.split(":")[1]);

        //����soot�õ�����ͼ
        Set<CommonCaller> callGraphInfo = Main.getCallGraphInfo(classNameOne, classLineOne, classNameTwo, classLineTwo);

        System.out.println(callGraphInfo +  "================>");
        System.exit(-1);
        for (CommonCaller caller : callGraphInfo) {
            syncJava = caller.getMethod().getClassName();
            int tempMin = Math.min(caller.getLeftRow(),caller.getRightRow());
            int tempMax = Math.max(caller.getLeftRow(),caller.getRightRow());
            if(minLine > tempMin) {
                minLine = tempMin;
            }
            if(maxLine < tempMax) {
                maxLine = tempMax;
            }
        }

        syncJava =syncJava.replaceAll("\\.","/") + ".java";

    }
}
