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

    private double lat;
    private double lon;

    CellData(){
        this.eNodeB = 0;
        this.localCellId = 0;
        this.earfcn = 0;
        this.dBm = 0;
        this.sinr = 0;
        this.pci = 0;
        this.mcc = 0;
        this.mnc = 0;
        this.ta = 0;
        this.lat = 0;
        this.lon = 0;
    }

    public void clear(){
        this.eNodeB = 0;
        this.localCellId = 0;
        this.earfcn = 0;
        this.dBm = 0;
        this.sinr = 0;
        this.pci = 0;
        this.mcc = 0;
        this.mnc = 0;
        this.ta = 0;
        this.lat = 0;
        this.lon = 0;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
