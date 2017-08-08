


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import util.DbConn;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Calvin
 */
public class FrmViewJournal extends javax.swing.JFrame {

    /**
     * Creates new form FrmViewJournal
     */
    private Connection conn;
    
    public FrmViewJournal() {
        initComponents();
        databaseConnection();
        loadAllJournal();
        setLocationRelativeTo(null);
        tableSelectionListenerViewJournal();
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
                Jurnal.txtJID.setText(String.valueOf(jid));
            } else {

            }

            rs.close();
            pstatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DlgViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void loadAllJournal() {

        try {
            
            String sql = "select jm.journal_date, jm.journal_id ,jd.Chart_no,jd.Chart_name,jd.Debit,jd.Credit,jm.description\n" +
"from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id);";
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                DefaultTableModel tableModel = (DefaultTableModel) tblJournal.getModel();
                while (rs.next()) {
                    Object jdate = rs.getDate("jm.journal_date");
                    int jid = rs.getInt("jm.journal_id");
                    int cid = rs.getInt("jd.Chart_no");
                    String cname = rs.getString("jd.Chart_name");
                    double debit = rs.getDouble("jd.Debit");
                    double credit = rs.getDouble("jd.Credit");
                    String desc = rs.getString("jm.description");
                    
                    Object data[][] = {
                        {jdate,jid,cid,cname,debit,credit,desc}
                    };
                    for (Object o[] : data) {
                        tableModel.addRow(o);
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
    public void removeTblViewJournal(){
        DefaultTableModel tableModel = (DefaultTableModel) tblJournal.getModel();
        tableModel.setRowCount(0);      
    }
    public void executeFilter(){
        try {
            removeTblViewJournal();
            String sql = "select jm.journal_date, jm.journal_id ,jd.Chart_no,jd.Chart_name,jd.Debit,jd.Credit,jm.description "
                    + "from (journal_master jm inner join journal_detail jd on jm.journal_id = jd.journal_id) "
                    + "where month(jm.journal_date) =" + cbxMonthView.getSelectedItem() + " and "
                    + "year(jm.journal_date)=" + cbxYearView.getSelectedItem() + " order by jm.journal_id;"; //sql injection vulnerable?
            PreparedStatement pstatement = conn.prepareStatement(sql);

            ResultSet rs = pstatement.executeQuery();
            if (rs.isBeforeFirst()) { // check is resultset not empty
                DefaultTableModel tableModel = (DefaultTableModel) tblJournal.getModel();
                while (rs.next()) {
                    Object jdate = rs.getDate("jm.journal_date");
                    int jid = rs.getInt("jm.journal_id");
                    int cid = rs.getInt("jd.Chart_no");
                    String cname = rs.getString("jd.Chart_name");
                    double debit = rs.getDouble("jd.Debit");
                    double credit = rs.getDouble("jd.Credit");
                    String desc = rs.getString("jm.description");

                    Object data[][] = {
                        {jdate, jid, cid, cname, debit, credit, desc}
                    };
                    for (Object o[] : data) {
                        tableModel.addRow(o);
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
    public void deleteJournalDetail() throws SQLException{
        String updateSql = "DELETE FROM journal_detail WHERE journal_id = ? ";                
        PreparedStatement pstatement = conn.prepareStatement(updateSql);
        pstatement.setInt(1, Integer.valueOf(lblID.getText().trim()));        
        pstatement.executeUpdate();
        pstatement.close();        

    }
    public void deleteJournalMaster() throws SQLException{
        String updateSql = "DELETE FROM journal_master WHERE journal_id = ? ";                
        PreparedStatement pstatement = conn.prepareStatement(updateSql);
        pstatement.setInt(1, Integer.valueOf(lblID.getText().trim()));        
        pstatement.executeUpdate();
        pstatement.close();        
   
    }
    public void deleteJournalRow() throws SQLException{
        DefaultTableModel tablemodel = (DefaultTableModel) tblJournal.getModel();
        tablemodel.setRowCount(0);
        deleteJournalDetail();
        deleteJournalMaster();        
        loadAllJournal();
        setJournalID();
    }
    
    private void tableSelectionListenerViewJournal() {

            ListSelectionListener listener = new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int row = tblJournal.getSelectedRow();
                    if (row >= 0) {                      
                          lblID.setText(tblJournal.getValueAt(row, 1).toString());                       
                    }
                }
            };
            tblJournal.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblJournal.getSelectionModel().addListSelectionListener(listener);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbxMonthView = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbxYearView = new javax.swing.JComboBox<>();
        btnFilterView = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblJournal = new javax.swing.JTable();
        btnDeleteView = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        lblID = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("View Journal");

        cbxMonthView.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        jLabel1.setText("Month : ");

        jLabel2.setText("Year : ");

        cbxYearView.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030", "2031", "2032", "2033", "2034", "2035", "2036" }));

        btnFilterView.setText("Filter");
        btnFilterView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterViewActionPerformed(evt);
            }
        });

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

        btnDeleteView.setText("Delete ");
        btnDeleteView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteViewActionPerformed(evt);
            }
        });

        jLabel3.setText("ID : ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblID, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 191, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxMonthView, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(89, 89, 89)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxYearView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(159, 159, 159)
                        .addComponent(btnFilterView, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane7)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnDeleteView, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(cbxMonthView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbxYearView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnFilterView))
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeleteView)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFilterViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterViewActionPerformed
        executeFilter();
    }//GEN-LAST:event_btnFilterViewActionPerformed

    private void btnDeleteViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteViewActionPerformed
        try {
            deleteJournalRow();
        } catch (SQLException ex) {
            Logger.getLogger(FrmViewJournal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDeleteViewActionPerformed

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
            java.util.logging.Logger.getLogger(FrmViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmViewJournal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDeleteView;
    private javax.swing.JButton btnFilterView;
    private javax.swing.JComboBox<String> cbxMonthView;
    private javax.swing.JComboBox<String> cbxYearView;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel lblID;
    private javax.swing.JTable tblJournal;
    // End of variables declaration//GEN-END:variables
}
