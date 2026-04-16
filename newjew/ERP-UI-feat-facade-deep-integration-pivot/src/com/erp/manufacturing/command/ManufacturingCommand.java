package com.erp.manufacturing.command;

public interface ManufacturingCommand<R> {
    R execute();
}
