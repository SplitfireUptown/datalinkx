package com.datalinkx.dataio.action;

public interface IAction<T> {
    void doAction(T actionInfo) throws Exception;
}
