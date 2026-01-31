package com.example.pattern.event.impl;

import com.example.pattern.event.PatternEvent;

import java.math.BigDecimal;

@PatternEvent("printConsole")
public class PrintConsoleEvent {

    public void print() {
        System.out.println("Print");
    }

    public void printWithStringParameter(String text) {
        System.out.println("Print text: " + text);
    }

    public void printWithNumberParameter(BigDecimal number) {
        System.out.println("Print number: " + number);
    }

    public void printWithParameters(String text, BigDecimal number, boolean printText) {
        System.out.println("Print text: " + text + ", number: " + number + " bool: " + printText);
    }
}
