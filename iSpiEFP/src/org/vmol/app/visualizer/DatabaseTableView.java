package org.vmol.app.visualizer;

import org.vmol.app.Main;
import org.vmol.app.database.DatabaseRecord;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

public class DatabaseTableView {
    private TableView tableView;
    
    public DatabaseTableView() {
        //Add table to pane
        TableView table = new TableView();
        TableColumn column1 = new TableColumn("Choice");
        TableColumn column2 = new TableColumn("RMSD");
        TableColumn column3 = new TableColumn("Select");

        //Init column 1 of table (choice)
        TableColumn<DatabaseRecord, String> index = column1;
        index.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("choice"));
        index.setPrefWidth(200.0);

        //Init column 2 of table (rmsd)
        TableColumn<DatabaseRecord, String> rmsd = column2;
        rmsd.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("rmsd"));
        rmsd.setPrefWidth(100);

        //Init column 3 of table (check)
        TableColumn<DatabaseRecord, Boolean> check = column3;
        check.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, Boolean>("check"));
        check.setCellFactory(column -> new CheckBoxTableCell());
        check.setEditable(true);
        check.setPrefWidth(75);

        //add columns to table
        table.getColumns().addAll(column1, column2, column3);
        table.setEditable(true);
        
        this.tableView = table;
    }
    
    public TableView getTable() {
        return this.tableView;
    }
}
