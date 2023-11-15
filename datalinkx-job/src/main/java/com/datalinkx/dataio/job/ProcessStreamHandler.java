package com.datalinkx.dataio.job;

import java.io.IOException;
import java.io.LineNumberReader;

public class ProcessStreamHandler implements Runnable {
    LineNumberReader inputReader;
    StringBuffer content;

    public ProcessStreamHandler(LineNumberReader inputReader, StringBuffer content) {
        this.inputReader = inputReader;
        this.content = content;
    }

    @Override
    public void run() {
        String line = "";
        while (true)
        {
            try {
                if ((line = inputReader.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            content.append(line).append("\n");
        }
    }
}
