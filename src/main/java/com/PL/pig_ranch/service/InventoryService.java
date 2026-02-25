package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.InventoryTransaction;
import java.util.List;
import java.util.Optional;

public interface InventoryService {

    List<InventoryItem> getAllItems();

    Optional<InventoryItem> getItemById(Long id);

    InventoryItem saveItem(InventoryItem item);

    void deleteItem(Long id);

    InventoryItem updateStock(Long itemId, int amount, InventoryTransaction.TransactionType type, String notes);

    List<InventoryTransaction> getTransactionHistory(Long itemId);
}
