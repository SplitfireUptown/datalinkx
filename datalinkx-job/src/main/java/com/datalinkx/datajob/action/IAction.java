package com.datalinkx.datajob.action;

public interface IAction<T> {
    void doAction(T actionInfo) throws Exception;
}
