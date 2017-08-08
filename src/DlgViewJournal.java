
import java.sql.Connection;
import java.sql.Date;
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
 * @author calvin
 */
public class DlgViewJournal extends javax.swing.JDialog {

    /**
     * Creates new form DlgViewJournal
     */
    
    private Connection conn;
    
    public DlgViewJournal(java.awt.Frame parent, boolean modal) {
        
        super(parent, modal);        
        databaseConnection();
        initComponents();
        loadAllJournal();
        tableSelectionListenerViewJournal();
        setLocationRelativeTo(null);
    }
    
    private void tableSelectionListenerViewJournal() {
        
        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = tblJournal.getSelectedRow();
                if (row >= 0) {
                      Jurnal.dtcDate.setDate((java.util.Date) tblJournal.getValueAt(row, 0));
                      Jurnal.txtJID.setText(tblJournal.getValueAt(row, 1).toString());
                      Jurnal.txtChartNo.setText(tblJournal.getValueAt(row, 2).toString());
                      Jurnal.txtChartName.setText(tblJournal.getValueAt(row, 3).toString());
                      Jurnal.txtDebit.setText(tblJournal.getValueAt(row, 4).toString());
                      Jurnal.txtCredit.setText(tblJournal.getValueAt(row, 5).toString());
                      Jurnal.txaDescription.setText(tblJournal.getValueAt(row, 6).toString());
                      Jurnal.btnRecord.setEnabled(false);
                      dispose();
                      
                }
            }
        };
        tblJournal.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblJournal.getSelectionModel().addListSelectionListener(listener);
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
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane7 = new javax.swing.JScrollPane();
        tblJournal = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbxMonthView = new javax.swing.JComboBox<>();
        cbxYearView = new javax.swing.JComboBox<>();
        btnFilterView = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("View Journal");

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

        jLabel1.setText("Month : ");

        jLabel2.setText("Year : ");

        cbxMonthView.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        cbxYearView.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025" }));

        btnFilterView.setText("Filter");
        btnFilterView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterViewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 832, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxMonthView, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(89, 89, 89)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxYearView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(184, 184, 184)
                        .addComponent(btnFilterView, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(cbxMonthView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxYearView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFilterView))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFilterViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterViewActionPerformed
        executeFilter();
    }//GEN-LAST:event_btnFilterViewActionPerformed

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
            java.util.logging.Logger.getLogger(DlgViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DlgViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DlgViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DlgViewJournal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DlgViewJournal dialog = new DlgViewJournal(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFilterView;
    private javax.swing.JComboBox<String> cbxMonthView;
    private javax.swing.JComboBox<String> cbxYearView;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTable tblJournal;
    // End of variables declaration//GEN-END:variables
}
