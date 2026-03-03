package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.Order;

public interface InvoiceService {
    void generateInvoice(Order order, String filePath);

    void generateAndOpenInvoice(Order order, String filePath);
}
