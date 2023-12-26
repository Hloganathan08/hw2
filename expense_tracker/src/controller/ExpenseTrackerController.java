package controller;

import view.ExpenseTrackerView;

import java.util.Stack;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import model.ExpenseTrackerModel;
import model.Transaction;
import model.Filter.TransactionFilter;

public class ExpenseTrackerController {
  
  private ExpenseTrackerModel model;
  private ExpenseTrackerView view;
  private Stack<Transaction> undoStack = new Stack<>();
  /** 
   * The Controller is applying the Strategy design pattern.
   * This is the has-a relationship with the Strategy class 
   * being used in the applyFilter method.
   */
  private TransactionFilter filter;

  public ExpenseTrackerController(ExpenseTrackerModel model, ExpenseTrackerView view) {
    this.model = model;
    this.view = view;
  }

  public void setFilter(TransactionFilter filter) {
    // Sets the Strategy class being used in the applyFilter method.
    this.filter = filter;
  }

  public void refresh() {
    List<Transaction> transactions = model.getTransactions();
    view.refreshTable(transactions);
  }

  public boolean addTransaction(double amount, String category) {
    if (!InputValidation.isValidAmount(amount)) {
      return false;
    }
    if (!InputValidation.isValidCategory(category)) {
      return false;
    }
    
    Transaction t = new Transaction(amount, category);
    model.addTransaction(t);
    view.getTableModel().addRow(new Object[]{t.getAmount(), t.getCategory(), t.getTimestamp()});
    undoStack.push(t);
    refresh();
    view.setUndoButtonEnabled(true);
    return true;
  }

  public void applyFilter() {
    //null check for filter
    if(filter!=null){
      // Use the Strategy class to perform the desired filtering
      List<Transaction> transactions = model.getTransactions();
      List<Transaction> filteredTransactions = filter.filter(transactions);
      List<Integer> rowIndexes = new ArrayList<>();
      for (Transaction t : filteredTransactions) {
        int rowIndex = transactions.indexOf(t);
        if (rowIndex != -1) {
          rowIndexes.add(rowIndex);
        }
      }
      view.highlightRows(rowIndexes);
    }
    else{
      JOptionPane.showMessageDialog(view, "No filter applied");
      view.toFront();}

  }
  public void removeSelectedTransaction() {
    int selectedRow = view.getTransactionsTable().getSelectedRow();
    if (selectedRow >= 0) {
        // Convert view index to model index in case sorting/filtering is applied
        int modelRow = view.getTransactionsTable().convertRowIndexToModel(selectedRow);
        Transaction toRemove = model.getTransactions().get(modelRow);
        model.removeTransaction(toRemove);
        view.getTableModel().removeRow(modelRow);
        refresh();
        JOptionPane.showMessageDialog(view, "Selected transaction removed.");
    } else {
        JOptionPane.showMessageDialog(view, "No transaction selected to remove.");
    }
    // Update the undo button state based on whether there are transactions left
    view.setUndoButtonEnabled(model.getTransactions().size() > 0);
}

}
