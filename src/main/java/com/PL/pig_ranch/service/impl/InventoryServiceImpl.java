package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.InventoryTransaction;
import com.PL.pig_ranch.repository.InventoryRepository;
import com.PL.pig_ranch.repository.TransactionRepository;
import com.PL.pig_ranch.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository, TransactionRepository transactionRepository) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<InventoryItem> getAllItems() {
        return inventoryRepository.findAll();
    }

    @Override
    public Optional<InventoryItem> getItemById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Override
    @Transactional
    public InventoryItem saveItem(InventoryItem item) {
        return inventoryRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        inventoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public InventoryItem updateStock(Long itemId, int amount, InventoryTransaction.TransactionType type, String notes) {
        InventoryItem item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Inventory Item not found with id: " + itemId));

        item.setQuantity(item.getQuantity() + amount);
        InventoryItem savedItem = inventoryRepository.save(item);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(savedItem);
        transaction.setChangeAmount(amount);
        transaction.setType(type);
        transaction.setNotes(notes);
        transactionRepository.save(transaction);

        return savedItem;
    }

    @Override
    public List<InventoryTransaction> getTransactionHistory(Long itemId) {
        return transactionRepository.findByItemIdOrderByTimestampDesc(itemId);
    }
}
