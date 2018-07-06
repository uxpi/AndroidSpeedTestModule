package com.example.nicholasarduini.androidspeedtest;

public class CellData {
    private int eNodeB;
    private int localCellId;
    private int earfcn;
    private int dBm;
    private int sinr;
    private int pci;
    private int mcc;
    private int mnc;
    private int ta;

    public CellData(){

    }

    public CellData(int eNodeB, int localCellId, int earfcn, int dBm, int sinr, int pci, int mcc, int mnc, int ta) {
        this.eNodeB = eNodeB;
        this.localCellId = localCellId;
        this.earfcn = earfcn;
        this.dBm = dBm;
        this.sinr = sinr;
        this.pci = pci;
        this.mcc = mcc;
        this.mnc = mnc;
        this.ta = ta;
    }

    public String toString(){
        return "\neNodeB: " + eNodeB +
                "\nLocal Cell ID: " + localCellId +
                "\nEARFCN: " + earfcn +
                "\nRSRP: " + dBm +
                "\nSINR: " + sinr +
                "\nPCI: " + pci +
                "\nMCC: " + mcc +
                "\nMNC: " + mnc +
                "\nTA: " + ta;
    }

    public int geteNodeB() {
        return eNodeB;
    }

    public void seteNodeB(int eNodeB) {
        this.eNodeB = eNodeB;
    }

    public int getLocalCellId() {
        return localCellId;
    }

    public void setLocalCellId(int localCellId) {
        this.localCellId = localCellId;
    }

    public int getEarfcn() {
        return earfcn;
    }

    public void setEarfcn(int earfcn) {
        this.earfcn = earfcn;
    }

    public int getdBm() {
        return dBm;
    }

    public void setdBm(int dBm) {
        this.dBm = dBm;
    }

    public int getSinr() {
        return sinr;
    }

    public void setSinr(int sinr) {
        this.sinr = sinr;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getTa() {
        return ta;
    }

    public void setTa(int ta) {
        this.ta = ta;
    }
}
