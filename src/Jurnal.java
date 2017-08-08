
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import util.DbConn;
import util.Sutil;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author calvin
 */
public class Jurnal extends javax.swing.JFrame {

    /**
     * Creates new form Jurnal
     */
    private Connection conn;

    public Jurnal() {
        databaseConnection();
        initComponents();
        setLocationRelativeTo(null);
        setJournalID();
        setIDInv();
        loadInvData();
        setJournalNew();
        setInvNew();
        tableSelectionListenerInventory();
        tableSelectionListenerJournal();
        try {
            loadListGL();
        } catch (SQLException ex) {
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void databaseConnection() {
        try {
            conn = DriverManager.getConnection(DbConn.JDBC_URL,
                    DbConn.JDBC_USERNAME,
                    DbConn.JDBC_PASSWORD);
            if (conn != null) {

            }
        } catch (SQLException ex) {
            System.out.println("Error:\n" + ex.getLocalizedMessage());
        }
    }

    public void setJournalID() {
        try {
            int jid = 0;
            String sql = "select max(journal_id) from journal_master";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty

                while (rs.next()) {
                    jid = rs.getInt("max(journal_id)");
                }
                jid += 1;
                txtJID.setText(String.valueOf(jid));
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void recordToTable() throws SQLException {
        if (txtJID.getText().isEmpty() == true) {
            Sutil.mse(this, "Journal ID is empty!");
        } else if (txtChartNo.getText().isEmpty() == true) {
            Sutil.mse(this, "Chart No is empty!");
        } else if (txtChartName.getText().isEmpty() == true) {
            Sutil.mse(this, "Chart Name is empty!");
        } else if (txaDescription.getText().isEmpty() == true) {
            Sutil.mse(this, "Description is empty!");
        } else if (txtDebit.getText().trim().matches("[a-zA-Z_]+") && txtCredit.getText().trim().matches("[a-zA-Z_]+")) {
            Sutil.mse(this, "Invalid input, only allow numbers");
            txtDebit.setText("");
            txtCredit.setText("");
        } else if (txtDebit.getText().trim().matches("[a-zA-Z_]+")) {
            Sutil.mse(this, "Invalid input, only allow numbers");
            txtDebit.setText("");
        } else if (txtCredit.getText().trim().matches("[a-zA-Z_]+")) {
            Sutil.mse(this, "Invalid input, only allow numbers");
            txtCredit.setText("");
        } else if (dtcDate.getDate() == null) {
            Sutil.mse(this, "Date is empty!");
        } else {
            if (txtDebit.getText().trim().isEmpty() == true || txtDebit.getText().equals("0")) {
                txtDebit.setText("0.0");
                Object data[] = {dtcDate.getDate(), txtJID.getText(),
                    Integer.parseInt(txtChartNo.getText()),
                    txtChartName.getText(),
                    Double.parseDouble(txtDebit.getText()), Double.parseDouble(txtCredit.getText()), txaDescription.getText(),};
                DefaultTableModel tableModel = (DefaultTableModel) tblJournal.getModel();
                tableModel.addRow(data);

                recordJournalMaster();
                recordJournalDetail();
                setJournalNew();
            } else if (txtCredit.getText().trim().isEmpty() == true || txtCredit.getText().equals("0")) {
                txtCredit.setText("0.0");
                Object data[] = {dtcDate.getDate(), txtJID.getText(),
                    Integer.parseInt(txtChartNo.getText()),
                    txtChartName.getText(),
                    Double.parseDouble(txtDebit.getText()), Double.parseDouble(txtCredit.getText()), txaDescription.getText(),};
                DefaultTableModel tableModel = (DefaultTableModel) tblJournal.getModel();
                tableModel.addRow(data);
                recordJournalMaster();
                recordJournalDetail();
                setJournalNew();
            } else if (txtDebit.getText().trim().isEmpty() == false && txtCredit.getText().trim().isEmpty() == false) {
                Sutil.msg(this, "You can't fill both debit and credit");
            }

        }
    }

    public void recordJournalMaster() throws SQLException {
        String insertSql = "insert into journal_master values (?,?,?)"; // ganti/buang
        PreparedStatement pstatement = conn.prepareStatement(insertSql);
        pstatement.setInt(1, Integer.parseInt(txtJID.getText()));
        pstatement.setObject(2, dtcDate.getDate());
        pstatement.setString(3, txaDescription.getText());
        pstatement.executeUpdate();

        pstatement.close();
    }

    public void recordJournalDetail() throws SQLException {
        String insertSql = "insert into journal_detail values (null,?,?,?,?,?)"; // ganti/buang
        PreparedStatement pstatement = conn.prepareStatement(insertSql);
        pstatement.setInt(1, Integer.parseInt(txtJID.getText()));
        pstatement.setInt(2, Integer.parseInt(txtChartNo.getText()));
        pstatement.setString(3, txtChartName.getText());

        pstatement.setDouble(4, Double.parseDouble(txtDebit.getText()));
        pstatement.setDouble(5, Double.parseDouble(txtCredit.getText()));
        pstatement.executeUpdate();

        pstatement.close();
    }

    public void deleteTblJournalRow() {
        DefaultTableModel tableModel = (DefaultTableModel) tblJournal.getModel();
        tableModel.setRowCount(0);

        //add Prepared Statement
    }

    public void removeTblGL() {
        DefaultTableModel tableModel = (DefaultTableModel) tblGL.getModel();
        tableModel.setRowCount(0);
        txtTotalDebit.setText("");
        txtTotalCredit.setText("");
        txtTotalBalance.setText("");
    }

    public void refreshGL() {
        try {
            removeTblGL();
            String sql = "select jm.journal_date, jm.journal_id ,jd.Debit,jd.Credit,jm.description "
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) "
                    + "where jd.Chart_name = '" + cbxListGL.getSelectedItem() + "' and month(jm.journal_date) =" + cbxMonthGL.getSelectedItem() + " and "
                    + "year(jm.journal_date)=" + cbxYearGL.getSelectedItem() + " order by jm.journal_date;"; //sql injection vulnerable?
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                DefaultTableModel tableModel = (DefaultTableModel) tblGL.getModel();
                while (rs.next()) {
                    Object jdate = rs.getDate("jm.journal_date");
                    int jid = rs.getInt("jm.journal_id");
                    double debit = rs.getDouble("jd.Debit");
                    double credit = rs.getDouble("jd.Credit");
                    String desc = rs.getString("jm.description");

                    Object data[][] = {
                        {jdate, jid, desc, debit, credit,}
                    };
                    for (Object o[] : data) {
                        tableModel.addRow(o);
                    }
                }
                totalGL();
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeTblTrial() {
        DefaultTableModel tableModel = (DefaultTableModel) tblTrialBalance.getModel();
        tableModel.setRowCount(0);
    }

    public void totalGL() {

        try {

            String sql = "select sum(jd.Debit),sum(jd.Credit),sum(jd.Debit)-sum(jd.Credit) "
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) "
                    + "where jd.Chart_name = '" + cbxListGL.getSelectedItem() + "' and month(jm.journal_date) = " + cbxMonthGL.getSelectedItem()
                    + " and year(jm.journal_date)=" + cbxYearGL.getSelectedItem() + "; "; //sql injection vulnerable?
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty

                while (rs.next()) {
                    double totdebit = rs.getDouble("sum(jd.Debit)");
                    double totcredit = rs.getDouble("sum(jd.Credit)");
                    double totbalance = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    txtTotalDebit.setText(String.format("%,.2f", totdebit));
                    txtTotalCredit.setText(String.format("%,.2f", totcredit));
                    txtTotalBalance.setText(String.format("%,.2f", totbalance));
                }

            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void refreshTrialBalance() {
        try {
            removeTblTrial();
            double opening = 0;
            double debit = 0;
            double credit = 0;
            double ending = 0;
            String sql = "select jd.Chart_no, jd.Chart_name,sum(jd.Debit),sum(jd.Credit),sum(jd.Debit)-sum(jd.Credit) "
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) "
                    + "where month(jm.journal_date) = " + cbxMonthTrial.getSelectedItem() + " and year(jm.journal_date)=" + cbxYearTrial.getSelectedItem() + " group by jd.Chart_name order by jd.Chart_no"; //sql injection vulnerable?
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                DefaultTableModel tableModel = (DefaultTableModel) tblTrialBalance.getModel();
                while (rs.next()) {
                    int cid = rs.getInt("jd.Chart_no");
                    String cname = rs.getString("jd.Chart_name");
                    double sumdebit = rs.getDouble("sum(jd.Debit)");
                    double sumcredit = rs.getDouble("sum(jd.Credit)");
                    double sumending = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    debit += sumdebit;
                    credit += sumcredit;
                    ending += sumending;
                    Object data[][] = {
                        {cid, cname, opening, sumdebit, sumcredit, sumending,}
                    };
                    for (Object o[] : data) {
                        tableModel.addRow(o);
                    }
                }
                txtOpeningTrial.setText(String.format("%,.2f", opening));
                txtDebitTrial.setText(String.format("%,.2f", debit));
                txtCreditTrial.setText(String.format("%,.2f", credit));
                txtEndingTrial.setText(String.format("%,.2f", ending));                
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void executeBrowseChart() {
        DlgChartOfAcc chartacc = new DlgChartOfAcc(this, true);
        chartacc.setVisible(true);
        try {
            txtChartName.setText(getChartNamebyNo(txtChartNo.getText()));
        } catch (SQLException ex) {
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deleteJournalDetail() throws SQLException {
        String updateSql = "DELETE FROM journal_detail WHERE journal_id = ? ";
        PreparedStatement pstatement = conn.prepareStatement(updateSql);
        pstatement.setInt(1, Integer.valueOf(txtJID.getText().trim()));
        pstatement.executeUpdate();
        pstatement.close();

    }

    public void deleteJournalMaster() throws SQLException {
        String updateSql = "DELETE FROM journal_master WHERE journal_id = ? ";
        PreparedStatement pstatement = conn.prepareStatement(updateSql);
        pstatement.setInt(1, Integer.valueOf(txtJID.getText().trim()));
        pstatement.executeUpdate();
        pstatement.close();

    }

    public void deleteJournalRow() throws SQLException {
        deleteTableSelectionListenerJournal();
        deleteJournalDetail();
        deleteJournalMaster();        
        btnRecord.setEnabled(true);
        setJournalNew();
        setJournalID();
    }

    public void executeUpdateDetail() throws SQLException {
        String updateSql = "update journal_detail set Chart_no = ?, Chart_name = ?, Debit = ?, Credit = ? where journal_id = ?;";
        PreparedStatement pstatement = conn.prepareStatement(updateSql);
        pstatement.setInt(1, Integer.parseInt(txtChartNo.getText()));
        pstatement.setString(2, txtChartName.getText());
        pstatement.setDouble(3, Double.parseDouble(txtDebit.getText()));
        pstatement.setDouble(4, Double.parseDouble(txtCredit.getText()));
        pstatement.setInt(5, Integer.parseInt(txtJID.getText()));
        pstatement.executeUpdate();

        pstatement.close();
    }

    public void executeUpdateMaster() throws SQLException {
        String updateSql = "update journal_master set journal_date = ? , description = ? where journal_id = ?;";
        PreparedStatement pstatement = conn.prepareStatement(updateSql);
        pstatement.setObject(1, dtcDate.getDate());
        pstatement.setString(2, txaDescription.getText());
        pstatement.setInt(3, Integer.parseInt(txtJID.getText()));
        pstatement.executeUpdate();

        pstatement.close();
    }

    public void executeUpdate() throws SQLException {
        if (txtJID.getText().isEmpty() == true) {
            Sutil.mse(this, "Journal ID is empty!");
        } else if (txtChartNo.getText().isEmpty() == true) {
            Sutil.mse(this, "Chart No is empty!");
        } else if (txtChartName.getText().isEmpty() == true) {
            Sutil.mse(this, "Chart Name is empty!");
        } else if (txaDescription.getText().isEmpty() == true) {
            Sutil.mse(this, "Description is empty!");
        } else if (txtDebit.getText().trim().isEmpty() == true) {
            Sutil.mse(this, "Debit is empty!");
        } else if (txtCredit.getText().trim().isEmpty() == true) {
            Sutil.mse(this, "Credit is empty!");
        } else {
            executeUpdateMaster();
            executeUpdateDetail();
            btnRecord.setEnabled(true);
            setJournalNew();
        }
    }

    public void setJournalNew() {
        setJournalID();
        txtChartNo.setText("");
        txtChartName.setText("");
        txaDescription.setText("");
        txtDebit.setText("");
        txtCredit.setText("");
    }

    public void setIDInv() {
        try {
            int invid = 0;
            String sql = "select max(inv_id) from inventory";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty

                while (rs.next()) {
                    invid = rs.getInt("max(inv_id)");
                }
                invid += 1;
                lblIDInv.setText(String.valueOf(invid));
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deleteTableInv() {
        DefaultTableModel tableModel = (DefaultTableModel) tblInventory.getModel();
        tableModel.setRowCount(0);
    }
    double invTotalValue = 0;

    public void loadInvData() {
        try {
            deleteTableInv();
            String sql = "select * from inventory";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                DefaultTableModel tableModel = (DefaultTableModel) tblInventory.getModel();
                while (rs.next()) {
                    Object invdate = rs.getDate("inv_date");
                    int invid = rs.getInt("inv_id");
                    String desc = rs.getString("description");
                    int opening = rs.getInt("opening");
                    double price = rs.getDouble("price");
                    int in = rs.getInt("inv_in");
                    int out = rs.getInt("inv_out");
                    int ending = rs.getInt("ending");
                    double value = rs.getDouble("inv_value");

                    Object data[][] = {
                        {invdate, invid, desc, opening, price, in, out, ending, value}
                    };
                    for (Object o[] : data) {
                        tableModel.addRow(o);
                        invTotalValue += value;
                    }
                }
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setInvNew() {
        Date date = new Date();
        setIDInv();
        txtOpeningInv.setText("");
        txaDescInv.setText("");
        txtPriceInv.setText("");
        txtInInv.setText("");
        txtOutInv.setText("");
        dtcDateInv.setDate(date);
    }

    public void executeAddInv() throws SQLException {
        if (txaDescInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Description is empty!");
        } else if (txtOpeningInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Opening is empty!");
        } else if (txtPriceInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Price is empty!");
        } else if (txtInInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "In is empty!");
        } else if (txtOutInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Out is empty!");
        } else {
            int opening = Integer.parseInt(txtOpeningInv.getText());
            int in = Integer.parseInt(txtInInv.getText());
            int out = Integer.parseInt(txtOutInv.getText());
            int ending = opening + in - out;
            double value = ending * Double.parseDouble(txtPriceInv.getText());
            String insertSql = "insert into inventory values (?,?,?,?,?,?,?,?,?)"; // ganti/buang
            PreparedStatement pstatement = conn.prepareStatement(insertSql);
            pstatement.setObject(1, dtcDateInv.getDate());
            pstatement.setInt(2, Integer.parseInt(lblIDInv.getText()));
            pstatement.setString(3, txaDescInv.getText());
            pstatement.setInt(4, Integer.parseInt(txtOpeningInv.getText()));
            pstatement.setDouble(5, Double.parseDouble(txtPriceInv.getText()));
            pstatement.setInt(6, Integer.parseInt(txtInInv.getText()));
            pstatement.setInt(7, Integer.parseInt(txtOutInv.getText()));
            pstatement.setInt(8, ending);
            pstatement.setDouble(9, Double.parseDouble(txtPriceInv.getText()) * ending);
            pstatement.executeUpdate();

            pstatement.close();
            loadInvData();
            setInvNew();
        }

    }

    private void tableSelectionListenerJournal() {

        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = tblJournal.getSelectedRow();
                if (row >= 0) {
                    dtcDate.setDate((java.util.Date) tblJournal.getValueAt(row, 0));
                    txtJID.setText(tblJournal.getValueAt(row, 1).toString());
                    txtChartNo.setText(tblJournal.getValueAt(row, 2).toString());
                    txtChartName.setText(tblJournal.getValueAt(row, 3).toString());
                    txtDebit.setText(tblJournal.getValueAt(row, 4).toString());
                    txtCredit.setText(tblJournal.getValueAt(row, 5).toString());
                    txaDescription.setText(tblJournal.getValueAt(row, 6).toString());
                }
            }
        };
        tblJournal.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblJournal.getSelectionModel().addListSelectionListener(listener);
    }

    private void deleteTableSelectionListenerJournal() {
        DefaultTableModel tablemodel = (DefaultTableModel) tblJournal.getModel();
        int selRow = tblJournal.getSelectedRow();
        if (selRow != -1) {
            tablemodel.removeRow(selRow);
        }
    }

    private void tableSelectionListenerInventory() {

        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = tblInventory.getSelectedRow();
                if (row >= 0) {
                    dtcDateInv.setDate((java.util.Date) tblInventory.getValueAt(row, 0));
                    lblIDInv.setText(tblInventory.getValueAt(row, 1).toString());
                    txaDescInv.setText(tblInventory.getValueAt(row, 2).toString());
                    txtOpeningInv.setText(tblInventory.getValueAt(row, 3).toString());
                    txtPriceInv.setText(tblInventory.getValueAt(row, 4).toString());
                    txtInInv.setText(tblInventory.getValueAt(row, 5).toString());
                    txtOutInv.setText(tblInventory.getValueAt(row, 6).toString());
                }
                btnAddInv.setEnabled(false);
            }
        };
        tblInventory.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblInventory.getSelectionModel().addListSelectionListener(listener);

    }

    public void executeUpdateInv() throws SQLException {
        if (txaDescInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Description is empty!");
        } else if (txtOpeningInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Opening is empty!");
        } else if (txtPriceInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Price is empty!");
        } else if (txtInInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "In is empty!");
        } else if (txtOutInv.getText().trim().isEmpty() == true) {
            Sutil.msg(this, "Out is empty!");
        } else {
            int opening = Integer.parseInt(txtOpeningInv.getText());
            int in = Integer.parseInt(txtInInv.getText());
            int out = Integer.parseInt(txtOutInv.getText());
            int ending = opening + in - out;
            String updateSql = "update inventory set inv_date = ?, description = ?, opening = ?, price = ?, inv_in = ?, inv_out = ?, ending = ?, inv_value = ?"
                    + "where inv_id = ?";
            PreparedStatement pstatement = conn.prepareStatement(updateSql);
            pstatement.setObject(1, (java.util.Date) dtcDateInv.getDate());
            pstatement.setString(2, txaDescInv.getText());
            pstatement.setInt(3, Integer.parseInt(txtOpeningInv.getText()));
            pstatement.setDouble(4, Double.parseDouble(txtPriceInv.getText()));
            pstatement.setInt(5, Integer.parseInt(txtInInv.getText()));
            pstatement.setInt(6, Integer.parseInt(txtOutInv.getText()));
            pstatement.setInt(7, ending);
            pstatement.setDouble(8, Double.parseDouble(txtPriceInv.getText()) * ending);
            pstatement.setInt(9, Integer.parseInt(lblIDInv.getText()));
            pstatement.executeUpdate();
            pstatement.close();
            btnAddInv.setEnabled(true);
            setInvNew();
            loadInvData();
        }
    }

    public void executeDeleteInv() throws SQLException {
        String updateSql = "DELETE FROM inventory WHERE inv_id = ? ";
        PreparedStatement pstatement = conn.prepareStatement(updateSql);
        pstatement.setInt(1, Integer.valueOf(lblIDInv.getText().trim()));
        pstatement.executeUpdate();
        pstatement.close();
        setInvNew();
        setIDInv();
        loadInvData();
        btnAddInv.setEnabled(true);
    }

    public void loadListGL() throws SQLException {
        String sql = "SELECT Chart_name FROM chart";

        PreparedStatement pstatement = conn.prepareStatement(sql);
        ResultSet rs = pstatement.executeQuery();

        while (rs.next()) {
            String name = rs.getString("Chart_name");
            cbxListGL.addItem(name);
        }

        rs.close();
        pstatement.close();
    }
    double salestotal = 0;

    public void reportSales() {
        try {
            String sql = "select jd.Chart_name, sum(jd.Debit)-sum(jd.Credit)  \n"
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) \n"
                    + "where month(jm.journal_date) =" + cbxMonthTrial.getSelectedItem() + " and year(jm.journal_date) = " + cbxYearTrial.getSelectedItem() + " and jd.Chart_no like '4%' group by jd.Chart_name";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                txaSalesRevenue.append("Sales Revenue\n=========================\n");
                salestotal = 0;
                while (rs.next()) {
                    String cname = rs.getString("jd.Chart_name");
                    double sumending = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    txaSalesRevenue.append(cname + " : " + String.format("%,.2f", -sumending)  + "\n");
                    salestotal += (long) -sumending;
                }
                txaSalesRevenue.append("=========================\n");
                txaSalesRevenue.append("Total Sales Revenue: " +  String.format("%,.2f", salestotal));
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    double cogstotal = 0;

    public void reportCogs() {
        try {
            String sql = "select jd.Chart_name, sum(jd.Debit)-sum(jd.Credit)  \n"
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) \n"
                    + "where month(jm.journal_date) =" + cbxMonthTrial.getSelectedItem() + " and year(jm.journal_date) = " + cbxYearTrial.getSelectedItem() + " and jd.Chart_no like '5%' group by jd.Chart_name";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                txaCogs.append("Cost Of Good Sold\n====================\n");
                cogstotal = 0;
                txaCogs.append("Beginning Inventory : " + 0 + "\n");
                while (rs.next()) {
                    String cname = rs.getString("jd.Chart_name");
                    double sumending = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    txaCogs.append(cname + " : " + String.format("%,.2f", sumending)  + "\n");
                    cogstotal += (long) sumending;
                }
                txaCogs.append("Ending Inventory : " + String.format("%,.2f",invTotalValue));
                txaCogs.append("\n=========================\n");
                txaCogs.append("Total Cost Of Good Sold: " + String.format("%,.2f", (cogstotal - invTotalValue)));

            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    double operationaltotal = 0;

    public void reportExpenditure() {
        try {
            String sql = "select jd.Chart_name, sum(jd.Debit)-sum(jd.Credit)  \n"
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) \n"
                    + "where month(jm.journal_date) =" + cbxMonthTrial.getSelectedItem() + " and year(jm.journal_date) = " + cbxYearTrial.getSelectedItem() + " and jd.Chart_no like '6%' group by jd.Chart_name";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                txaOperational.append("Operational Expenditure\n====================\n");
                operationaltotal = 0;
                while (rs.next()) {
                    String cname = rs.getString("jd.Chart_name");
                    double sumending = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    txaOperational.append(cname + " : " + String.format("%,.2f", sumending) + "\n");
                    operationaltotal += (long) sumending;
                }
                txaOperational.append("=========================\n");
                txaOperational.append("Total Operational Expenditure: " + String.format("%,.2f", operationaltotal));
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reportGross() {
        txaGross.setText("Total Gross Profit(Loss) : " + String.format("%,.2f", salestotal - cogstotal + invTotalValue));;
    }

    public void reportNet() {
        txaNet.setText("Total Net Profit(Loss) : " + String.format("%,.2f", (salestotal - cogstotal + invTotalValue - operationaltotal)));;
    }

    public void executeRefreshProfit() {
        txaSalesRevenue.setText("");
        txaOperational.setText("");
        txaCogs.setText("");
        txaGross.setText("");
        txaNet.setText("");
        reportSales();
        reportCogs();
        reportGross();
        reportExpenditure();
        reportNet();
    }
    double assetstotal = 0;

    public void reportAssets() {
        try {
            String sql = "select jd.Chart_name, sum(jd.Debit)-sum(jd.Credit)  \n"
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) \n"
                    + "where month(jm.journal_date) =" + cbxMonthTrial.getSelectedItem() + " and year(jm.journal_date) = " + cbxYearTrial.getSelectedItem() + " and jd.Chart_no like '1%' group by jd.Chart_name";

            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                txaAssets.append("Assets\n=========================\n");
                assetstotal = 0;
                while (rs.next()) {
                    String cname = rs.getString("jd.Chart_name");
                    double sumending = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    txaAssets.append(cname + " : " + String.format("%,.2f", sumending) + "\n");
                    assetstotal += (long) sumending;
                }
                txaAssets.append("Inventory : " + String.format("%,.2f", invTotalValue));
                txaAssets.append("\n=========================\n");
                txaAssets.append("Total Assets: " + String.format("%,.2f", (assetstotal + invTotalValue)));
            } else {
 
            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    double liabilitytotal = 0;

    public void reportLiability() {
        try {
            String sql = "select jd.Chart_name, sum(jd.Debit)-sum(jd.Credit)  \n"
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) \n"
                    + "where month(jm.journal_date) =" + cbxMonthTrial.getSelectedItem() + " and year(jm.journal_date) = " + cbxYearTrial.getSelectedItem() + " and jd.Chart_no like '2%' group by jd.Chart_name";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                txaLiability.append("Liability\n=========================\n");
                liabilitytotal = 0;
                while (rs.next()) {
                    String cname = rs.getString("jd.Chart_name");
                    double sumending = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    txaLiability.append(cname + " : " + String.format("%,.2f", -sumending) + "\n");
                    liabilitytotal += -sumending;
                }
                txaLiability.append("=========================\n");
                txaLiability.append("Total Liability: " + String.format("%,.2f" , liabilitytotal));
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    double capitaltotal = 0;

    public void reportCapital() {
        try {
            String sql = "select jd.Chart_name, sum(jd.Debit)-sum(jd.Credit)  \n"
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) \n"
                    + "where month(jm.journal_date) =" + cbxMonthTrial.getSelectedItem() + " and year(jm.journal_date) = " + cbxYearTrial.getSelectedItem() + " and jd.Chart_no like '3%' group by jd.Chart_name";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                txaCapital.append("Capital\n=========================\n");
                capitaltotal = 0;
                while (rs.next()) {
                    String cname = rs.getString("jd.Chart_name");
                    double sumending = rs.getDouble("sum(jd.Debit)-sum(jd.Credit)");
                    txaCapital.append(cname + " : " + String.format("%,.2f", -sumending) + "\n");
                    capitaltotal += -sumending;
                }
                txaCapital.append("Retained Earning : " + String.format("%,.2f",(salestotal - cogstotal + invTotalValue - operationaltotal)));
                txaCapital.append("\n=========================\n");
                txaCapital.append("Total Capital: " + String.format("%,.2f", (capitaltotal + (salestotal - cogstotal + invTotalValue - operationaltotal))));
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reportSubtotal() {
        txaSubtotal.setText("Total Assets\t\t: " + String.format("%,.2f", (assetstotal + invTotalValue)));
        txaSubtotal.append("\nTotal Liability & Capital\t: " + String.format("%,.2f", liabilitytotal + (long) (capitaltotal + (salestotal - cogstotal + invTotalValue - operationaltotal))
        ));
    }

    public void executeBalance() {
        txaAssets.setText("");
        txaLiability.setText("");
        txaCapital.setText("");
        txaSubtotal.setText("");
        reportAssets();
        reportLiability();
        reportCapital();
        reportSubtotal();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblJournal = new javax.swing.JTable();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        txtChartNo = new javax.swing.JTextField();
        txtChartName = new javax.swing.JTextField();
        jScrollPane8 = new javax.swing.JScrollPane();
        txaDescription = new javax.swing.JTextArea();
        jLabel26 = new javax.swing.JLabel();
        txtDebit = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        txtJID = new javax.swing.JTextField();
        btnRecord = new javax.swing.JButton();
        btnBrowseChart = new javax.swing.JButton();
        btnViewJournal = new javax.swing.JButton();
        txtCredit = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        dtcDate = new com.toedter.calendar.JDateChooser();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblGL = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        cbxListGL = new javax.swing.JComboBox<>();
        btnRefreshGL = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtTotalDebit = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtTotalCredit = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtTotalBalance = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cbxMonthGL = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        cbxYearGL = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblTrialBalance = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        cbxMonthTrial = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        cbxYearTrial = new javax.swing.JComboBox<>();
        btnRefreshTrial = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        txtDebitTrial = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtCreditTrial = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtEndingTrial = new javax.swing.JTextField();
        txtOpeningTrial = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblInventory = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        lblIDInv = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtPriceInv = new javax.swing.JTextField();
        txtInInv = new javax.swing.JTextField();
        txtOutInv = new javax.swing.JTextField();
        dtcDateInv = new com.toedter.calendar.JDateChooser();
        jLabel19 = new javax.swing.JLabel();
        btnAddInv = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        txaDescInv = new javax.swing.JTextArea();
        btnUpdateInv = new javax.swing.JButton();
        btnDeleteInv = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        txtOpeningInv = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txaSalesRevenue = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jscrollpane = new javax.swing.JScrollPane();
        txaCogs = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        txaGross = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        txaOperational = new javax.swing.JTextArea();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        txaNet = new javax.swing.JTextArea();
        btnRefreshProfit = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        txaAssets = new javax.swing.JTextArea();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        txaLiability = new javax.swing.JTextArea();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane13 = new javax.swing.JScrollPane();
        txaCapital = new javax.swing.JTextArea();
        jPanel16 = new javax.swing.JPanel();
        jScrollPane14 = new javax.swing.JScrollPane();
        txaSubtotal = new javax.swing.JTextArea();
        btnRefreshBalance = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mniLogout = new javax.swing.JMenuItem();
        mniExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mniChart = new javax.swing.JMenuItem();
        mniViewJournal = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        mniAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simple Accounting v.1");

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Entry Journal"));

        tblJournal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Journal ID", "Chart No", "Chart Name", "Debit", "Credit", "Description"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(tblJournal);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Journal ID : ");

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Chart.No : ");

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Chart Name : ");

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Description : ");

        txaDescription.setColumns(20);
        txaDescription.setLineWrap(true);
        txaDescription.setRows(5);
        txaDescription.setTabSize(10);
        txaDescription.setWrapStyleWord(true);
        jScrollPane8.setViewportView(txaDescription);

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Debit : ");

        txtDebit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtDebit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDebitActionPerformed(evt);
            }
        });

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel29.setText("Date : ");

        txtJID.setEditable(false);

        btnRecord.setText("Record");
        btnRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordActionPerformed(evt);
            }
        });

        btnBrowseChart.setText("Browse");
        btnBrowseChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseChartActionPerformed(evt);
            }
        });

        btnViewJournal.setText("View Journal");
        btnViewJournal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewJournalActionPerformed(evt);
            }
        });

        txtCredit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("Credit : ");

        dtcDate.setDateFormatString("yyyy-MM-dd");

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane7))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtChartName, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtJID))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                        .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtChartNo, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBrowseChart)))
                        .addGap(36, 36, 36)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtDebit, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                                        .addComponent(dtcDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtCredit)))
                            .addComponent(btnViewJournal, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                        .addComponent(btnRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel22)
                                .addComponent(txtJID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel29))
                            .addComponent(dtcDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(txtChartNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26)
                            .addComponent(txtDebit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBrowseChart))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel27)
                                .addComponent(txtCredit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel24)
                                .addComponent(txtChartName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnRecord)
                        .addComponent(btnUpdate)
                        .addComponent(btnDelete)
                        .addComponent(btnViewJournal)))
                .addGap(9, 9, 9)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Journal", jPanel4);

        tblGL.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Journal ID", "Description", "Debit", "Credit"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblGL);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("List G/L by : ");

        btnRefreshGL.setText("Refresh ");
        btnRefreshGL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshGLActionPerformed(evt);
            }
        });

        jLabel2.setText("Total Debit :");

        txtTotalDebit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotalDebit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalDebitActionPerformed(evt);
            }
        });

        jLabel3.setText("Total Credit : ");

        txtTotalCredit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel4.setText("Total Balance :");

        txtTotalBalance.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel5.setText("Month : ");

        cbxMonthGL.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        jLabel8.setText("Year :");

        cbxYearGL.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030", "2031", "2032", "2033", "2034", "2035", "2036" }));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 978, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnRefreshGL))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxListGL, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(59, 59, 59)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxMonthGL, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(59, 59, 59)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxYearGL, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTotalDebit, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTotalCredit, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTotalBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbxListGL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(cbxMonthGL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(cbxYearGL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addComponent(btnRefreshGL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtTotalDebit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtTotalCredit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtTotalBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );

        jTabbedPane1.addTab("General Ledger", jPanel5);

        tblTrialBalance.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Chart No", "Chart Name", "Opening", "Debit", "Credit", "Ending"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblTrialBalance);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Month : ");

        cbxMonthTrial.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Year : ");

        cbxYearTrial.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030", "2031", "2032", "2033", "2034", "2035", "2036" }));

        btnRefreshTrial.setText("Refresh");
        btnRefreshTrial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshTrialActionPerformed(evt);
            }
        });

        jLabel10.setText("Total Debit : ");

        txtDebitTrial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDebitTrialActionPerformed(evt);
            }
        });

        jLabel11.setText("Total Credit : ");

        jLabel12.setText("Total Ending : ");

        txtOpeningTrial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOpeningTrialActionPerformed(evt);
            }
        });

        jLabel14.setText("Total Opening : ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 555, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxMonthTrial, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxYearTrial, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnRefreshTrial)
                                .addGap(37, 37, 37))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOpeningTrial, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDebitTrial, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCreditTrial, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEndingTrial, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(123, 123, 123))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(cbxMonthTrial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(cbxYearTrial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addComponent(btnRefreshTrial))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14)
                        .addComponent(txtOpeningTrial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(txtDebitTrial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel11)
                        .addComponent(txtCreditTrial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12)
                        .addComponent(txtEndingTrial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24))
        );

        jTabbedPane1.addTab("Trial Balance", jPanel1);

        tblInventory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Inventory ID ", "Description", "Opening", "Price (Avg)", "In", "Out", "Ending", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tblInventory);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Inventory Input"));

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText(" Inventory ID : ");

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Description  : ");

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Average Price : ");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("In : ");

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Out : ");

        txtPriceInv.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtInInv.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtOutInv.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtOutInv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOutInvActionPerformed(evt);
            }
        });

        dtcDateInv.setDateFormatString("yyyy-MM-dd");

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("Date : ");

        btnAddInv.setText("Add to Inventory");
        btnAddInv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddInvActionPerformed(evt);
            }
        });

        txaDescInv.setColumns(20);
        txaDescInv.setRows(5);
        jScrollPane11.setViewportView(txaDescInv);

        btnUpdateInv.setText("Update");
        btnUpdateInv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateInvActionPerformed(evt);
            }
        });

        btnDeleteInv.setText("Delete");
        btnDeleteInv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteInvActionPerformed(evt);
            }
        });

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Opening : ");

        txtOpeningInv.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(btnAddInv)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdateInv, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDeleteInv, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addComponent(lblIDInv, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dtcDateInv, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(237, Short.MAX_VALUE))
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtInInv, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPriceInv, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtOutInv, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtOpeningInv, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(lblIDInv, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(dtcDateInv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOpeningInv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(76, 76, 76))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(txtPriceInv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtInInv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtOutInv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(15, 15, 15)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddInv)
                    .addComponent(btnUpdateInv)
                    .addComponent(btnDeleteInv)))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 978, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        jTabbedPane1.addTab("Inventory", jPanel2);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Sales Revenue"));

        txaSalesRevenue.setColumns(20);
        txaSalesRevenue.setRows(5);
        jScrollPane4.setViewportView(txaSalesRevenue);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Cost of Good Sold"));

        txaCogs.setColumns(20);
        txaCogs.setRows(5);
        jscrollpane.setViewportView(txaCogs);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jscrollpane)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jscrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Gross Profit(Loss)"));

        txaGross.setColumns(20);
        txaGross.setRows(5);
        jScrollPane6.setViewportView(txaGross);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Operational Expenditure"));

        txaOperational.setColumns(20);
        txaOperational.setRows(5);
        jScrollPane9.setViewportView(txaOperational);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Net Profit(Loss)"));

        txaNet.setColumns(20);
        txaNet.setRows(5);
        jScrollPane10.setViewportView(txaNet);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane10)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        btnRefreshProfit.setText("Refresh");
        btnRefreshProfit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshProfitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(31, 31, 31)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(173, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnRefreshProfit, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addComponent(btnRefreshProfit, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Profit-Loss", jPanel3);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Assets"));

        txaAssets.setColumns(20);
        txaAssets.setRows(5);
        jScrollPane5.setViewportView(txaAssets);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jScrollPane5)
                .addContainerGap())
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Liability"));

        txaLiability.setColumns(20);
        txaLiability.setRows(5);
        jScrollPane12.setViewportView(txaLiability);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jScrollPane12)
                .addContainerGap())
        );

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Capital"));

        txaCapital.setColumns(20);
        txaCapital.setRows(5);
        jScrollPane13.setViewportView(txaCapital);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Subtotal"));

        txaSubtotal.setColumns(20);
        txaSubtotal.setRows(5);
        jScrollPane14.setViewportView(txaSubtotal);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnRefreshBalance.setText("Refresh");
        btnRefreshBalance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshBalanceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnRefreshBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnRefreshBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Balance Sheet", jPanel12);

        jMenu1.setText("System");

        mniLogout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        mniLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/logout.png"))); // NOI18N
        mniLogout.setText("Logout");
        mniLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniLogoutActionPerformed(evt);
            }
        });
        jMenu1.add(mniLogout);

        mniExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        mniExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/exit.png"))); // NOI18N
        mniExit.setText("Exit");
        mniExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExitActionPerformed(evt);
            }
        });
        jMenu1.add(mniExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Account");

        mniChart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/column_right.png"))); // NOI18N
        mniChart.setText("Chart Of Accounts");
        mniChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniChartActionPerformed(evt);
            }
        });
        jMenu2.add(mniChart);

        mniViewJournal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/reviewing_pane.png"))); // NOI18N
        mniViewJournal.setText("View Journal");
        mniViewJournal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniViewJournalActionPerformed(evt);
            }
        });
        jMenu2.add(mniViewJournal);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");

        mniAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/about.png"))); // NOI18N
        mniAbout.setText("About");
        mniAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniAboutActionPerformed(evt);
            }
        });
        jMenu3.add(mniAbout);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordActionPerformed
        try {
            recordToTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }

    }//GEN-LAST:event_btnRecordActionPerformed

    private void txtDebitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDebitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDebitActionPerformed

    private void btnBrowseChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseChartActionPerformed
        executeBrowseChart();
    }//GEN-LAST:event_btnBrowseChartActionPerformed

    private void btnViewJournalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewJournalActionPerformed
        DlgViewJournal viewJournal = new DlgViewJournal(this, true);
        viewJournal.setVisible(true);
    }//GEN-LAST:event_btnViewJournalActionPerformed

    private void btnRefreshGLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshGLActionPerformed
        refreshGL();
    }//GEN-LAST:event_btnRefreshGLActionPerformed

    private void txtTotalDebitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalDebitActionPerformed

    }//GEN-LAST:event_txtTotalDebitActionPerformed

    private void btnRefreshTrialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshTrialActionPerformed
        refreshTrialBalance();
    }//GEN-LAST:event_btnRefreshTrialActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        try {
            executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        try {
            deleteJournalRow();
        } catch (SQLException ex) {
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void txtOutInvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOutInvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOutInvActionPerformed

    private void btnAddInvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddInvActionPerformed
        try {
            executeAddInv();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddInvActionPerformed

    private void btnUpdateInvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateInvActionPerformed
        try {
            executeUpdateInv();
            btnAddInv.setEnabled(true);
        } catch (SQLException ex) {
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnUpdateInvActionPerformed

    private void btnDeleteInvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteInvActionPerformed
        try {
            executeDeleteInv();
        } catch (SQLException ex) {
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDeleteInvActionPerformed

    private void mniChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniChartActionPerformed
        try {
            FrmChartOfAcc chart = new FrmChartOfAcc();
            chart.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Jurnal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_mniChartActionPerformed

    private void mniLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLogoutActionPerformed
        this.dispose();
        Login login = new Login();
        login.setVisible(true);
    }//GEN-LAST:event_mniLogoutActionPerformed

    private void mniExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_mniExitActionPerformed

    private void mniViewJournalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniViewJournalActionPerformed
        FrmViewJournal view = new FrmViewJournal();
        view.setVisible(true);

    }//GEN-LAST:event_mniViewJournalActionPerformed

    private void btnRefreshProfitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshProfitActionPerformed
        executeRefreshProfit();
    }//GEN-LAST:event_btnRefreshProfitActionPerformed

    private void txtDebitTrialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDebitTrialActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDebitTrialActionPerformed

    private void btnRefreshBalanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshBalanceActionPerformed
        executeBalance();
    }//GEN-LAST:event_btnRefreshBalanceActionPerformed

    private void mniAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniAboutActionPerformed
        Sutil.msg(this, "Simple Accounting v.1\n By: Calvin , Alvin and Jovita");
    }//GEN-LAST:event_mniAboutActionPerformed

    private void txtOpeningTrialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOpeningTrialActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOpeningTrialActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Jurnal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Jurnal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Jurnal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Jurnal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Jurnal().setVisible(true);
            }
        });
    }

    private String getChartNamebyNo(String id) throws SQLException {

        String chartName = "";
        String sqlChartLookup = "SELECT Chart_name FROM chart WHERE Chart_no = ?";

        PreparedStatement pstChartLookup = conn.prepareStatement(sqlChartLookup);
        pstChartLookup.setString(1, id);

        ResultSet rsChartLookup = pstChartLookup.executeQuery();
        while (rsChartLookup.next()) {
            chartName = rsChartLookup.getString("Chart_name");
        }
        return chartName;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddInv;
    private javax.swing.JButton btnBrowseChart;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeleteInv;
    public static javax.swing.JButton btnRecord;
    private javax.swing.JButton btnRefreshBalance;
    private javax.swing.JButton btnRefreshGL;
    private javax.swing.JButton btnRefreshProfit;
    private javax.swing.JButton btnRefreshTrial;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnUpdateInv;
    private javax.swing.JButton btnViewJournal;
    public static javax.swing.JComboBox<String> cbxListGL;
    private javax.swing.JComboBox<String> cbxMonthGL;
    private javax.swing.JComboBox<String> cbxMonthTrial;
    private javax.swing.JComboBox<String> cbxYearGL;
    private javax.swing.JComboBox<String> cbxYearTrial;
    public static com.toedter.calendar.JDateChooser dtcDate;
    private com.toedter.calendar.JDateChooser dtcDateInv;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane jscrollpane;
    private javax.swing.JLabel lblIDInv;
    private javax.swing.JMenuItem mniAbout;
    private javax.swing.JMenuItem mniChart;
    private javax.swing.JMenuItem mniExit;
    private javax.swing.JMenuItem mniLogout;
    private javax.swing.JMenuItem mniViewJournal;
    private javax.swing.JTable tblGL;
    private javax.swing.JTable tblInventory;
    private javax.swing.JTable tblJournal;
    private javax.swing.JTable tblTrialBalance;
    private javax.swing.JTextArea txaAssets;
    private javax.swing.JTextArea txaCapital;
    private javax.swing.JTextArea txaCogs;
    private javax.swing.JTextArea txaDescInv;
    public static javax.swing.JTextArea txaDescription;
    private javax.swing.JTextArea txaGross;
    private javax.swing.JTextArea txaLiability;
    private javax.swing.JTextArea txaNet;
    private javax.swing.JTextArea txaOperational;
    private javax.swing.JTextArea txaSalesRevenue;
    private javax.swing.JTextArea txaSubtotal;
    public static javax.swing.JTextField txtChartName;
    public static javax.swing.JTextField txtChartNo;
    public static javax.swing.JTextField txtCredit;
    private javax.swing.JTextField txtCreditTrial;
    public static javax.swing.JTextField txtDebit;
    private javax.swing.JTextField txtDebitTrial;
    private javax.swing.JTextField txtEndingTrial;
    private javax.swing.JTextField txtInInv;
    public static javax.swing.JTextField txtJID;
    private javax.swing.JTextField txtOpeningInv;
    private javax.swing.JTextField txtOpeningTrial;
    private javax.swing.JTextField txtOutInv;
    private javax.swing.JTextField txtPriceInv;
    private javax.swing.JTextField txtTotalBalance;
    private javax.swing.JTextField txtTotalCredit;
    private javax.swing.JTextField txtTotalDebit;
    // End of variables declaration//GEN-END:variables
}
